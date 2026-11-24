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

  // ------------------------------------------------------------------ party quests

  /** Whether parties get shared daily quests (everyone's progress adds up). */
  public static boolean partyQuestsEnabled() {
    return enabled() && config.getBoolean("party.enabled", true);
  }

  public static int partyQuestsPerDay() {
    return Math.max(1, config.getInt("party.quests", 2));
  }

  /** The party pool is the daily pool with every amount multiplied by this. */
  public static int partyTargetScale() {
    return Math.max(1, config.getInt("party.targetScale", 4));
  }

  /** The prize every online member gets is this many times bigger than a daily wheel prize. */
  public static int partyRewardMultiplier() {
    return Math.max(1, config.getInt("party.rewardMultiplier", 2));
  }

  public static List<QuestDefinition> partyPool() {
    List<QuestDefinition> own = parsePool(config.getConfigurationSection("party.pool"));
    if (!own.isEmpty()) {
      return own;
    }
    List<QuestDefinition> scaled = new ArrayList<>();
    for (QuestDefinition def : pool) {
      scaled.add(def.scaled(partyTargetScale()));
    }
    return scaled;
  }

  // ------------------------------------------------------------------ weekly and seasonal quests

  /** Whether quests of this period are dealt. Daily follows {@code enabled}; the rest their own. */
  public static boolean periodEnabled(QuestPeriod period) {
    if (period == QuestPeriod.DAILY) {
      return enabled();
    }
    return enabled() && config.getBoolean(period.id() + ".enabled", true);
  }

  /** How many quests are dealt for each new period. */
  public static int questsPer(QuestPeriod period) {
    return switch (period) {
      case DAILY -> questsPerDay();
      case WEEKLY -> Math.max(1, config.getInt("weekly.quests", 4));
      case SEASONAL -> Math.max(1, config.getInt("seasonal.quests", 3));
    };
  }

  /** How much bigger the quest amounts are than the daily ones when no own pool is given. */
  public static int targetScale(QuestPeriod period) {
    return switch (period) {
      case DAILY -> 1;
      case WEEKLY -> Math.max(1, config.getInt("weekly.targetScale", 5));
      case SEASONAL -> Math.max(1, config.getInt("seasonal.targetScale", 20));
    };
  }

  /** The wheel prizes of this period are this many times bigger than the daily ones. */
  public static int rewardMultiplier(QuestPeriod period) {
    return switch (period) {
      case DAILY -> 1;
      case WEEKLY -> Math.max(1, config.getInt("weekly.rewardMultiplier", 3));
      case SEASONAL -> Math.max(1, config.getInt("seasonal.rewardMultiplier", 8));
    };
  }

  /**
   * The quest pool of a period: its own {@code <period>.pool} section from quests.yml when there is
   * one, otherwise the daily pool with every amount multiplied by the period's target scale.
   */
  public static List<QuestDefinition> pool(QuestPeriod period) {
    if (period == QuestPeriod.DAILY) {
      return pool;
    }
    List<QuestDefinition> own = parsePool(config.getConfigurationSection(period.id() + ".pool"));
    if (!own.isEmpty()) {
      return own;
    }
    int scale = targetScale(period);
    List<QuestDefinition> scaled = new ArrayList<>();
    for (QuestDefinition def : pool) {
      scaled.add(def.scaled(scale));
    }
    return scaled;
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
