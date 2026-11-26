package mc.carlton.freerpg.worldevents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import mc.carlton.freerpg.FreeRPG;
import mc.carlton.freerpg.core.info.server.PlacedBlocksManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

/**
 * During the Gold Rush, breaking a natural ore is loud: now and then a few hostile mobs show up
 * close to the miner. A player causes noise at most once per cooldown, so vein miner cannot flood
 * the area.
 */
public final class GoldRushNoiseListener implements Listener {

  private static final Random RANDOM = new Random();
  private static final WorldEventType TYPE = WorldEventType.GOLD_RUSH;
  private static final int SPOT_TRIES = 10;

  private final Map<UUID, Long> lastNoise = new HashMap<>();

  /** Locations of natural ores whose break is being processed (cleared one tick later). */
  private final Set<Location> naturalOre = new HashSet<>();

  static int mobCount(Random random, int min, int max) {
    int low = Math.max(0, min);
    int high = Math.max(low, max);
    return low + random.nextInt(high - low + 1);
  }

  static boolean noiseReady(long nowMillis, long lastMillis, int cooldownSeconds) {
    return nowMillis - lastMillis >= Math.max(0, cooldownSeconds) * 1000L;
  }

  static double chance() {
    return Math.max(0.0, Math.min(1.0, WorldEventConfig.number(TYPE, "noise.chance", 0.15)));
  }

  static int cooldownSeconds() {
    return Math.max(0, WorldEventConfig.integer(TYPE, "noise.cooldownSeconds", 5));
  }

  static List<EntityType> mobTypes() {
    List<EntityType> types = new ArrayList<>();
    for (String name : EventDefaults.list(TYPE, "noise.mobTypes", EventDefaults.NOISE_MOBS)) {
      try {
        EntityType type = EntityType.valueOf(name.toUpperCase(java.util.Locale.ROOT));
        if (type.isSpawnable() && type.isAlive()) {
          types.add(type);
        }
      } catch (IllegalArgumentException ignored) {
        // not a mob of this server version
      }
    }
    return types;
  }

  /**
   * First step, before FreeRPG forgets which blocks players placed: only remember that this break
   * is a natural ore. Nothing happens yet, because a protection plugin may still cancel the break.
   */
  @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
  void onBreakCheck(BlockBreakEvent e) {
    Player p = e.getPlayer();
    if ((p.getGameMode() != GameMode.SURVIVAL && p.getGameMode() != GameMode.ADVENTURE)
        || !WorldEvents.isActive(TYPE, p.getWorld())) {
      return;
    }
    Block block = e.getBlock();
    if (!EventDefaults.dropBlocks(TYPE).contains(block.getType())
        || new PlacedBlocksManager().isBlockTracked(block)) {
      return;
    }
    Location at = block.getLocation();
    naturalOre.add(at);
    Bukkit.getScheduler().runTask(FreeRPG.getPlugin(FreeRPG.class), () -> naturalOre.remove(at));
  }

  /** Second step: the ore really broke, so the noise can happen. */
  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  void onBreakConfirmed(BlockBreakEvent e) {
    if (!naturalOre.remove(e.getBlock().getLocation())) {
      return;
    }
    Player p = e.getPlayer();
    World world = p.getWorld();
    long now = System.currentTimeMillis();
    long last = lastNoise.getOrDefault(p.getUniqueId(), Long.MIN_VALUE / 2);
    if (!noiseReady(now, last, cooldownSeconds()) || !EventRules.rollStart(chance(), RANDOM)) {
      return;
    }
    List<EntityType> types = mobTypes();
    if (types.isEmpty()) {
      return;
    }
    int wanted = mobCount(RANDOM, Math.max(0, WorldEventConfig.integer(TYPE, "noise.minMobs", 1)),
        WorldEventConfig.integer(TYPE, "noise.maxMobs", 3));
    int spawned = 0;
    for (int i = 0; i < wanted; i++) {
      Location spot = findSpot(p);
      if (spot == null) {
        continue;
      }
      world.spawnEntity(spot, types.get(RANDOM.nextInt(types.size())));
      spawned++;
    }
    if (spawned > 0) {
      lastNoise.put(p.getUniqueId(), now);
    }
  }

  /** A free spot 5 to 9 blocks from the player, or null when none was found. */
  private static Location findSpot(Player p) {
    Location base = p.getLocation();
    World world = base.getWorld();
    for (int attempt = 0; attempt < SPOT_TRIES; attempt++) {
      double angle = RANDOM.nextDouble() * Math.PI * 2;
      double distance = 5 + RANDOM.nextDouble() * 4;
      int x = base.getBlockX() + (int) Math.round(Math.cos(angle) * distance);
      int z = base.getBlockZ() + (int) Math.round(Math.sin(angle) * distance);
      int y = base.getBlockY() + RANDOM.nextInt(5) - 2;
      if (y <= world.getMinHeight() || y >= world.getMaxHeight() - 2) {
        continue;
      }
      Block feet = world.getBlockAt(x, y, z);
      Block head = world.getBlockAt(x, y + 1, z);
      Block below = world.getBlockAt(x, y - 1, z);
      if (feet.isPassable() && !feet.isLiquid() && head.isPassable() && !head.isLiquid()
          && !below.isPassable() && !below.isLiquid()) {
        return new Location(world, x + 0.5, y, z + 0.5);
      }
    }
    return null;
  }

  @EventHandler
  void onQuit(org.bukkit.event.player.PlayerQuitEvent e) {
    lastNoise.remove(e.getPlayer().getUniqueId());
  }
}
