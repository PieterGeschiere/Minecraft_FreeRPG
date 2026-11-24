package mc.carlton.freerpg.elites;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

/**
 * The Blood Moon: each night of an allowed world has a small chance to become one. It lasts until
 * the end tick of the night (or until an admin stops it).
 */
public final class BloodMoonManager {

  private static final long DAY = 24000L;

  /** A Blood Moon never outlasts a day of server time, even when the world time is frozen. */
  private static final int MAX_BEATS = 2400; // 10 ticks per beat

  private record Moon(long startFull, long endFull, int startBeat) {
  }

  private static final Map<UUID, Moon> ACTIVE = new HashMap<>();
  private static final Map<UUID, Long> LAST_ROLLED = new HashMap<>();
  private static final Random RANDOM = new Random();
  private static BukkitTask task;
  private static int beat;

  private BloodMoonManager() {
  }

  // ------------------------------------------------------------------ pure rules

  /** Whether tonight's roll is due: inside the night window and not rolled yet today. */
  static boolean due(long dayNumber, long lastRolledDay, long timeOfDay, int startTick,
      int endTick) {
    return dayNumber != lastRolledDay && timeOfDay >= startTick && timeOfDay < endTick;
  }

  static boolean rollStart(double chance, Random random) {
    return chance > 0.0 && random.nextDouble() < chance;
  }

  /** The world time at which a Blood Moon that starts now ends (never right away). */
  static long nextEnd(long fullTime, int endTick) {
    long timeOfDay = fullTime % DAY;
    long delta = (((endTick - timeOfDay) % DAY) + DAY) % DAY;
    if (delta == 0) {
      delta = DAY;
    }
    return fullTime + delta;
  }

  /**
   * Whether a running Blood Moon is over: its end time has come, the world time went back (for
   * example /time set day), or a day of server time has passed (frozen day cycle).
   */
  static boolean shouldEnd(long fullTime, long startFull, long endFull, int beatsElapsed) {
    return fullTime >= endFull || fullTime < startFull || beatsElapsed >= MAX_BEATS;
  }

  static boolean worldAllowed(String worldName, boolean isOverworldType, Set<String> worlds) {
    return isOverworldType && worlds.contains(worldName);
  }

  // ------------------------------------------------------------------ state

  public static boolean isActive(World world) {
    return ACTIVE.containsKey(world.getUID());
  }

  /** Ticks left of the Blood Moon in this world (0 when there is none). */
  static long ticksLeft(World world) {
    Moon moon = ACTIVE.get(world.getUID());
    return moon == null ? 0 : Math.max(0, moon.endFull() - world.getFullTime());
  }

  /** How much of the Blood Moon is left, from 1.0 down to 0.0. */
  static double progress(World world) {
    Moon moon = ACTIVE.get(world.getUID());
    if (moon == null || moon.endFull() <= moon.startFull()) {
      return 0.0;
    }
    return Math.max(0.0, Math.min(1.0, (double) ticksLeft(world) / (moon.endFull() - moon.startFull())));
  }

  /** Starts a Blood Moon in the world. Returns false when one is already going. */
  public static boolean start(World world) {
    if (isActive(world)) {
      return false;
    }
    long now = world.getFullTime();
    ACTIVE.put(world.getUID(), new Moon(now, nextEnd(now, EliteConfig.bloodMoonEndTick()), beat));
    if (EliteConfig.bloodMoonEffect("thunder", false)) {
      world.setStorm(true);
      world.setThundering(true);
    }
    BloodMoonVisuals.refresh();
    return true;
  }

  /** Ends the Blood Moon in the world. Returns false when there was none. */
  public static boolean stop(World world) {
    if (ACTIVE.remove(world.getUID()) == null) {
      return false;
    }
    for (Player player : world.getPlayers()) {
      player.sendMessage(EliteMessages.text(player, "bloodMoonEnded"));
    }
    BloodMoonVisuals.refresh();
    return true;
  }

  /** The once-a-second check of every world: end finished Blood Moons, roll for new ones. */
  private static void tickWorlds() {
    for (World world : Bukkit.getWorlds()) {
      Moon moon = ACTIVE.get(world.getUID());
      if (moon != null) {
        if (shouldEnd(world.getFullTime(), moon.startFull(), moon.endFull(), beat - moon.startBeat())) {
          stop(world);
        }
        continue;
      }
      boolean overworldType = world.getEnvironment() == World.Environment.NORMAL;
      if (!EliteConfig.bloodMoonEnabled()
          || !worldAllowed(world.getName(), overworldType, EliteConfig.bloodMoonWorlds())) {
        continue;
      }
      long day = world.getFullTime() / DAY;
      if (due(day, LAST_ROLLED.getOrDefault(world.getUID(), -1L), world.getTime(),
          EliteConfig.bloodMoonStartTick(), EliteConfig.bloodMoonEndTick())) {
        LAST_ROLLED.put(world.getUID(), day);
        if (rollStart(EliteConfig.bloodMoonChance(), RANDOM)) {
          start(world);
        }
      }
    }
  }

  public static void begin(Plugin plugin) {
    shutdown();
    task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
      beat++;
      if (beat % 2 == 0) {
        tickWorlds();
      }
      BloodMoonVisuals.refresh();
    }, 10L, 10L);
  }

  /** Stops the task and removes every Blood Moon effect from the players. */
  public static void shutdown() {
    if (task != null) {
      task.cancel();
      task = null;
    }
    ACTIVE.clear();
    LAST_ROLLED.clear();
    BloodMoonVisuals.clearAll();
  }
}
