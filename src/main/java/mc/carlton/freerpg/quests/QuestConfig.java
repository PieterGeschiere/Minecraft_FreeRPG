package mc.carlton.freerpg.quests;

import java.io.File;
import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

/** Reads quests.yml. Every value has a built-in default, so an old or incomplete file works. */
public final class QuestConfig {

  private static YamlConfiguration config = new YamlConfiguration();
  private static List<QuestDefinition> pool = List.of();

  private QuestConfig() {
  }

  public static void load(Plugin plugin) {
    File file = new File(plugin.getDataFolder(), "quests.yml");
    if (!file.exists()) {
      plugin.saveResource("quests.yml", false);
    }
    config = YamlConfiguration.loadConfiguration(file);
    pool = parsePool(config.getConfigurationSection("quests"));
  }

  /** Reloads quests.yml from disk (admin command). */
  public static void reload(Plugin plugin) {
    load(plugin);
  }

  public static boolean enabled() {
    return config.getBoolean("enabled", true);
  }

  public static int questsPerDay() {
    return Math.max(1, config.getInt("questsPerDay", 3));
  }

  public static int resetHour() {
    return Math.max(0, Math.min(23, config.getInt("reset.hour", 0)));
  }

  public static ZoneId resetZone() {
    return parseZone(config.getString("reset.timezone", ""));
  }

  public static List<QuestDefinition> pool() {
    return pool;
  }

  public static List<String> disabledWorlds() {
    return config.getStringList("disabledWorlds");
  }

  public static int weightBook() {
    return Math.max(0, config.getInt("wheel.weights.book", 1));
  }

  public static int weightMinecraftExp() {
    return Math.max(0, config.getInt("wheel.weights.minecraftExp", 1));
  }

  public static int weightFrpgExp() {
    return Math.max(0, config.getInt("wheel.weights.frpgExp", 1));
  }

  public static int[] minecraftExpRange() {
    return range("wheel.minecraftExp", 100, 1000);
  }

  public static int[] frpgExpRange() {
    return range("wheel.frpgExp", 200, 2000);
  }

  public static boolean effectsEnabled() {
    return config.getBoolean("effects.enabled", true);
  }

  private static int[] range(String path, int defaultMin, int defaultMax) {
    int min = Math.max(1, config.getInt(path + ".min", defaultMin));
    int max = Math.max(min, config.getInt(path + ".max", defaultMax));
    return new int[] {min, max};
  }

  /** Time zone from the config; blank or invalid values give the server's own zone. */
  static ZoneId parseZone(String name) {
    if (name == null || name.isBlank()) {
      return ZoneId.systemDefault();
    }
    try {
      return ZoneId.of(name.trim());
    } catch (DateTimeException e) {
      return ZoneId.systemDefault();
    }
  }

  /** Reads the quest pool. Entries with an unknown type are skipped. */
  static List<QuestDefinition> parsePool(ConfigurationSection section) {
    List<QuestDefinition> result = new ArrayList<>();
    if (section == null) {
      return result;
    }
    for (String id : section.getKeys(false)) {
      ConfigurationSection entry = section.getConfigurationSection(id);
      if (entry == null) {
        continue;
      }
      QuestType type;
      try {
        type = QuestType.valueOf(entry.getString("type", "").trim().toUpperCase(Locale.ROOT));
      } catch (IllegalArgumentException e) {
        continue;
      }
      int min = Math.max(1, entry.getInt("min", 1));
      int max = Math.max(min, entry.getInt("max", min));
      String filter = entry.getString("filter", "ANY").trim().toUpperCase(Locale.ROOT);
      if (filter.isEmpty()) {
        filter = "ANY";
      }
      result.add(new QuestDefinition(id, type, filter, min, max,
          Math.max(0, entry.getInt("weight", 1)), entry.getString("name", id + " {target}")));
    }
    return result;
  }
}
