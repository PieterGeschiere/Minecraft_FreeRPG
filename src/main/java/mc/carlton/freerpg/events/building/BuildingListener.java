package mc.carlton.freerpg.events.building;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import mc.carlton.freerpg.FreeRPG;
import mc.carlton.freerpg.config.ConfigLoad;
import mc.carlton.freerpg.core.info.server.WorldGuardChecks;
import mc.carlton.freerpg.skills.building.BuildingBlockTracker;
import mc.carlton.freerpg.skills.building.BuildingChances;
import mc.carlton.freerpg.skills.building.BuildingReach;
import mc.carlton.freerpg.skills.building.DemolitionRules;
import mc.carlton.freerpg.skills.perksAndAbilities.Building;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockDamageAbortEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.plugin.Plugin;

/**
 * Glue for the Building skill. All the maths lives in skills/building; this class only reads
 * events, asks those helpers, and applies the result. Nothing here hands out items or drops.
 */
public class BuildingListener implements Listener {

  private final Random random = new Random();
  /** Players whose current block break is on one of their own placed blocks (cleared next tick). */
  private final Set<UUID> breakingOwnBlock = new HashSet<>();

  private static NamespacedKey key(String name) {
    return new NamespacedKey(FreeRPG.getPlugin(FreeRPG.class), name);
  }

  private static UUID worldOf(Block block) {
    return block.getWorld().getUID();
  }

  private static UUID ownerOf(Block block) {
    return BuildingBlockTracker.instance()
        .ownerOf(worldOf(block), block.getX(), block.getY(), block.getZ());
  }

  private static void untrack(Block block) {
    BuildingBlockTracker.instance().untrack(worldOf(block), block.getX(), block.getY(), block.getZ());
  }

  // ---------------------------------------------------------------- placing and breaking

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onPlace(BlockPlaceEvent e) {
    if (!Building.isEnabled()) {
      return;
    }
    Player p = e.getPlayer();
    Block block = e.getBlockPlaced();
    if (!new WorldGuardChecks().canBuild(p, block.getLocation())) {
      return;
    }
    BuildingBlockTracker tracker = BuildingBlockTracker.instance();
    tracker.track(worldOf(block), block.getX(), block.getY(), block.getZ(), p.getUniqueId());
    if (tracker.consumeFullWarning()) {
      FreeRPG.log(org.apache.logging.log4j.Level.WARN,
          "[FreeRPG] Building block tracker is full; new blocks are no longer protected.");
    }
    if (Building.xpRules().shouldAward(p.getUniqueId(), block.getType().name(), worldOf(block),
        block.getX(), block.getY(), block.getZ(), System.currentTimeMillis())) {
      new Building(p).onBlockPlaced(block);
    }
    refreshReach(p);
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onBreak(BlockBreakEvent e) {
    if (!Building.isEnabled()) {
      return;
    }
    Player p = e.getPlayer();
    Block block = e.getBlock();
    Building.xpRules().recordBreak(p.getUniqueId(), worldOf(block), block.getX(), block.getY(),
        block.getZ(), System.currentTimeMillis());
    UUID owner = ownerOf(block);
    if (owner != null && owner.equals(p.getUniqueId())
        && Building.statOf(owner, Building.PERK_DEMOLITION) >= 1) {
      UUID id = p.getUniqueId();
      breakingOwnBlock.add(id);
      Plugin plugin = FreeRPG.getPlugin(FreeRPG.class);
      Bukkit.getScheduler().runTask(plugin, () -> breakingOwnBlock.remove(id));
    }
    untrack(block);
    clearBreakSpeed(p);
  }

  // ---------------------------------------------------------------- Weatherproof and Fortified

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onBurn(BlockBurnEvent e) {
    if (!Building.isEnabled()) {
      return;
    }
    Block block = e.getBlock();
    UUID owner = ownerOf(block);
    if (owner == null) {
      return;
    }
    double chance = BuildingChances.chanceForLevel(
        new ConfigLoad().getBuildingWeatherproofChances(),
        Building.statOf(owner, Building.PERK_WEATHERPROOF));
    if (BuildingChances.survives(chance, random.nextDouble())) {
      e.setCancelled(true);
    } else {
      untrack(block);
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onEntityExplode(EntityExplodeEvent e) {
    applyFortified(e.blockList());
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onBlockExplode(BlockExplodeEvent e) {
    applyFortified(e.blockList());
  }

  private void applyFortified(List<Block> blocks) {
    if (!Building.isEnabled()) {
      return;
    }
    double[] chances = new ConfigLoad().getBuildingFortifiedChances();
    Iterator<Block> it = blocks.iterator();
    while (it.hasNext()) {
      Block block = it.next();
      UUID owner = ownerOf(block);
      if (owner == null) {
        continue;
      }
      double chance = BuildingChances.chanceForLevel(chances,
          Building.statOf(owner, Building.PERK_FORTIFIED));
      if (BuildingChances.survives(chance, random.nextDouble())) {
        it.remove();
      } else {
        untrack(block);
      }
    }
  }

  // ---------------------------------------------------------------- Demolition Expert

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onStartDigging(BlockDamageEvent e) {
    if (!Building.isEnabled()) {
      return;
    }
    Player p = e.getPlayer();
    UUID owner = ownerOf(e.getBlock());
    if (owner != null && owner.equals(p.getUniqueId())
        && Building.statOf(owner, Building.PERK_DEMOLITION) >= 1) {
      setBreakSpeed(p, new ConfigLoad().getBuildingDemolitionSpeedBonus());
    } else {
      clearBreakSpeed(p);
    }
  }

  @EventHandler
  public void onStopDigging(BlockDamageAbortEvent e) {
    clearBreakSpeed(e.getPlayer());
  }

  @EventHandler
  public void onHeldItemChange(PlayerItemHeldEvent e) {
    clearBreakSpeed(e.getPlayer());
  }

  @EventHandler
  public void onItemDamage(PlayerItemDamageEvent e) {
    if (!breakingOwnBlock.contains(e.getPlayer().getUniqueId())) {
      return;
    }
    e.setDamage(DemolitionRules.reducedDamage(e.getDamage(),
        new ConfigLoad().getBuildingDemolitionDurabilityMultiplier(), random.nextDouble()));
  }

  private void setBreakSpeed(Player p, double bonus) {
    AttributeInstance speed = p.getAttribute(Attribute.BLOCK_BREAK_SPEED);
    if (speed == null) {
      return;
    }
    NamespacedKey id = key("building_demolition");
    clearBreakSpeed(p);
    speed.addModifier(new AttributeModifier(id, bonus, AttributeModifier.Operation.ADD_SCALAR,
        EquipmentSlotGroup.ANY));
  }

  private void clearBreakSpeed(Player p) {
    AttributeInstance speed = p.getAttribute(Attribute.BLOCK_BREAK_SPEED);
    if (speed == null) {
      return;
    }
    NamespacedKey id = key("building_demolition");
    for (AttributeModifier modifier : List.copyOf(speed.getModifiers())) {
      if (modifier.getKey().equals(id)) {
        speed.removeModifier(modifier);
      }
    }
  }

  // ---------------------------------------------------------------- Builder's Reach

  @EventHandler
  public void onJoin(PlayerJoinEvent e) {
    Plugin plugin = FreeRPG.getPlugin(FreeRPG.class);
    // stats load asynchronously around login, so refresh a moment later as well
    Bukkit.getScheduler().runTaskLater(plugin, () -> refreshReach(e.getPlayer()), 40L);
  }

  @EventHandler
  public void onRespawn(PlayerRespawnEvent e) {
    Plugin plugin = FreeRPG.getPlugin(FreeRPG.class);
    Bukkit.getScheduler().runTask(plugin, () -> refreshReach(e.getPlayer()));
  }

  @EventHandler
  public void onQuit(PlayerQuitEvent e) {
    clearBreakSpeed(e.getPlayer());
    breakingOwnBlock.remove(e.getPlayer().getUniqueId());
  }

  /** Sets (never stacks) the extra interaction range for the player's current Builder's Reach. */
  public static void refreshReach(Player p) {
    AttributeInstance range = p.getAttribute(Attribute.BLOCK_INTERACTION_RANGE);
    if (range == null) {
      return;
    }
    NamespacedKey id = key("building_reach");
    for (AttributeModifier modifier : List.copyOf(range.getModifiers())) {
      if (modifier.getKey().equals(id)) {
        range.removeModifier(modifier);
      }
    }
    if (!Building.isEnabled()) {
      return;
    }
    ConfigLoad config = new ConfigLoad();
    double bonus = BuildingReach.bonus(Building.statOf(p.getUniqueId(), Building.PASSIVE_REACH),
        config.getBuildingReachPerPoint(), config.getBuildingReachCap());
    if (bonus > 0) {
      range.addModifier(new AttributeModifier(id, bonus, AttributeModifier.Operation.ADD_NUMBER,
          EquipmentSlotGroup.ANY));
    }
  }
}
