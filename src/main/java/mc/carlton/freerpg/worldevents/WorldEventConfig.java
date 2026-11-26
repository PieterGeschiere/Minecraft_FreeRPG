package mc.carlton.freerpg.worldevents;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import mc.carlton.freerpg.elites.EliteConfig;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

/**
 * Reads worldevents.yml. Every value has a built-in default, so an old or incomplete file works.
 * Blood Moon keeps its settings in elites.yml; this class passes those through.
 */
public final class WorldEventConfig {

  private static YamlConfiguration config = new YamlConfiguration();

  private WorldEventConfig() {
  }

  public static void load(Plugin plugin) {
    File file = new File(plugin.getDataFolder(), "worldevents.yml");
    if (!file.exists()) {
      plugin.saveResource("worldevents.yml", false);
    }
    loadFrom(YamlConfiguration.loadConfiguration(file));
  }

  /** Reloads worldevents.yml from disk (admin command). */
  public static void reload(Plugin plugin) {
    load(plugin);
  }

  /** Uses an already parsed configuration (also used by the tests). */
  public static void loadFrom(YamlConfiguration loaded) {
    config = loaded;
  }

  private static String path(WorldEventType type, String key) {
    return "events." + type.configKey() + "." + key;
  }

  private static double clamp01(double value) {
    return Math.max(0.0, Math.min(1.0, value));
  }

  private static int clampTick(int value) {
    return Math.max(0, Math.min(23999, value));
  }

  // ------------------------------------------------------------------ shared

  /** Minecraft days that must pass after an event before the next one can start by itself. */
  public static int minGapDays() {
    return Math.max(0, config.getInt("minGapDays", 2));
  }

  /** The master switch of the new events (Blood Moon has its own in elites.yml). */
  public static boolean enabled() {
    return config.getBoolean("enabled", true);
  }

  // ------------------------------------------------------------------ per event

  /** Whether the event can start by itself (admins can always start it). */
  public static boolean enabled(WorldEventType type) {
    if (type == WorldEventType.BLOOD_MOON) {
      return EliteConfig.bloodMoonEnabled();
    }
    return enabled() && config.getBoolean(path(type, "enabled"), true);
  }

  /** The chance (0 to 1) that the event's daily roll succeeds. */
  public static double chance(WorldEventType type) {
    if (type == WorldEventType.BLOOD_MOON) {
      return EliteConfig.bloodMoonChance();
    }
    return clamp01(config.getDouble(path(type, "chance"), defaultChance(type)));
  }

  public static Set<String> worlds(WorldEventType type) {
    if (type == WorldEventType.BLOOD_MOON) {
      return EliteConfig.bloodMoonWorlds();
    }
    String path = path(type, "worlds");
    List<String> worlds = config.isList(path) ? config.getStringList(path) : List.of("world");
    return new HashSet<>(worlds);
  }

  /** The time of day (ticks) at which the roll happens. */
  public static int startTick(WorldEventType type) {
    if (type == WorldEventType.BLOOD_MOON) {
      return EliteConfig.bloodMoonStartTick();
    }
    return clampTick(config.getInt(path(type, "startTick"), defaultStart(type)));
  }

  /** The time of day (ticks) at which the roll window closes and time-of-day events end. */
  public static int endTick(WorldEventType type) {
    if (type == WorldEventType.BLOOD_MOON) {
      return EliteConfig.bloodMoonEndTick();
    }
    return clampTick(config.getInt(path(type, "endTick"), defaultEnd(type)));
  }

  /** Whether a successful roll starts at a random time inside the window. */
  public static boolean randomStart(WorldEventType type) {
    if (type == WorldEventType.BLOOD_MOON) {
      return false;
    }
    return config.getBoolean(path(type, "randomStart"), defaultRandomStart(type));
  }

  /** How long the event lasts in seconds; 0 means until the end tick. */
  public static int durationSeconds(WorldEventType type) {
    if (type == WorldEventType.BLOOD_MOON) {
      return 0;
    }
    return Math.max(0, config.getInt(path(type, "durationSeconds"), defaultDuration(type)));
  }

  /** An effect switch: title, sound, bossBar (and, for Blood Moon, redEdge, particles, thunder). */
  public static boolean effect(WorldEventType type, String key, boolean def) {
    if (type == WorldEventType.BLOOD_MOON) {
      return EliteConfig.bloodMoonEffect(key, def);
    }
    return config.getBoolean(path(type, "effects." + key), def);
  }

  public static double number(WorldEventType type, String key, double def) {
    return config.getDouble(path(type, key), def);
  }

  public static int integer(WorldEventType type, String key, int def) {
    return config.getInt(path(type, key), def);
  }

  public static List<String> list(WorldEventType type, String key) {
    return config.getStringList(path(type, key));
  }

  public static boolean isSet(WorldEventType type, String key) {
    return config.isSet(path(type, key));
  }

  /** The configuration itself, for the few event-specific sections (rewards, points). */
  public static YamlConfiguration raw() {
    return config;
  }

  // ------------------------------------------------------------------ defaults

  private static double defaultChance(WorldEventType type) {
    return switch (type) {
      case BLOOD_MOON -> 0.0625;
      case BOUNTIFUL_HARVEST, ECLIPSE, LUMBERJACKS_DAY, SCHOLARS_MOON, ADVENTURERS_WIND, TITAN_NIGHT,
          MUTATOR_NIGHT -> 0.05;
      case GOLD_RUSH, FISHING_FESTIVAL, FORGE_FEVER -> 0.04;
    };
  }

  private static int defaultStart(WorldEventType type) {
    return switch (type) {
      case BLOOD_MOON, ECLIPSE, SCHOLARS_MOON, TITAN_NIGHT, MUTATOR_NIGHT -> 13000;
      case BOUNTIFUL_HARVEST, GOLD_RUSH, FISHING_FESTIVAL, LUMBERJACKS_DAY, FORGE_FEVER,
          ADVENTURERS_WIND -> 0;
    };
  }

  private static int defaultEnd(WorldEventType type) {
    return type == WorldEventType.BOUNTIFUL_HARVEST || type == WorldEventType.LUMBERJACKS_DAY
        || type == WorldEventType.ADVENTURERS_WIND ? 12000 : 23000;
  }

  private static boolean defaultRandomStart(WorldEventType type) {
    return type == WorldEventType.GOLD_RUSH || type == WorldEventType.FISHING_FESTIVAL
        || type == WorldEventType.FORGE_FEVER;
  }

  private static int defaultDuration(WorldEventType type) {
    return defaultRandomStart(type) ? 600 : 0;
  }
}
