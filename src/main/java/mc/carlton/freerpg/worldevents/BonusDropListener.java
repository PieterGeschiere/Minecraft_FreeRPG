package mc.carlton.freerpg.worldevents;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import mc.carlton.freerpg.FreeRPG;
import mc.carlton.freerpg.core.info.server.PlacedBlocksManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;

/**
 * Extra drops while Bountiful Harvest or the Gold Rush runs. Whether a block counts is decided
 * when it breaks, before FreeRPG forgets which blocks players placed, and the drop handler then
 * only looks that answer up. Player-placed blocks and unripe crops never give extra drops.
 */
public final class BonusDropListener implements Listener {

  private static final Random RANDOM = new Random();

  /** Block location to whether its drops can come twice, for the break being processed. */
  private final Map<Location, Boolean> pending = new HashMap<>();

  static boolean qualifies(boolean natural, boolean isCrop, boolean ripe) {
    return natural && (!isCrop || ripe);
  }

  /** The running event, when it is one that gives extra drops and goes on in this world. */
  private static Optional<WorldEventType> bonusType(World world) {
    return WorldEventManager.current()
        .filter(event -> event.appliesTo(world))
        .map(ActiveEvent::type)
        .filter(type -> type == WorldEventType.BOUNTIFUL_HARVEST
            || type == WorldEventType.GOLD_RUSH);
  }

  @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
  void onBreak(BlockBreakEvent e) {
    Player p = e.getPlayer();
    if (p.getGameMode() != GameMode.SURVIVAL && p.getGameMode() != GameMode.ADVENTURE) {
      return;
    }
    Block block = e.getBlock();
    Optional<WorldEventType> type = bonusType(block.getWorld());
    if (type.isEmpty() || !EventDefaults.dropBlocks(type.get()).contains(block.getType())) {
      return;
    }
    boolean natural = !new PlacedBlocksManager().isBlockTracked(block);
    boolean crop = block.getBlockData() instanceof Ageable;
    boolean ripe = block.getBlockData() instanceof Ageable ageable
        && ageable.getAge() >= ageable.getMaximumAge();
    Location at = block.getLocation();
    pending.put(at, qualifies(natural, crop, ripe));
    Bukkit.getScheduler().runTask(FreeRPG.getPlugin(FreeRPG.class), () -> pending.remove(at));
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  void onDrop(BlockDropItemEvent e) {
    Boolean qualifies = pending.remove(e.getBlock().getLocation());
    if (!Boolean.TRUE.equals(qualifies)) {
      return;
    }
    Optional<WorldEventType> type = bonusType(e.getBlock().getWorld());
    if (type.isEmpty()) {
      return;
    }
    double chance = EventDefaults.dropChance(type.get());
    Location at = e.getBlock().getLocation().add(0.5, 0.5, 0.5);
    for (Item item : e.getItems()) {
      if (EventRules.rollStart(chance, RANDOM)) {
        e.getBlock().getWorld().dropItemNaturally(at, item.getItemStack().clone());
      }
    }
  }
}
