package mc.carlton.freerpg.skills.building;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Reads the Building numbers out of perkConfig.yml and advancedConfig.yml.
 *
 * <p>Servers keep their old config files, so every value falls back to the default when the
 * file (or its building section) is missing. Nothing here needs a running server.
 */
public final class BuildingConfig {

  public static final int PERK_LEVELS = 5;
  public static final double[] DEFAULT_WEATHERPROOF = {0.20, 0.40, 0.60, 0.80, 1.00};
  public static final double[] DEFAULT_FORTIFIED = {0.10, 0.20, 0.30, 0.40, 0.50};
  public static final double DEFAULT_DEMOLITION_SPEED_BONUS = 0.25;
  public static final double DEFAULT_DEMOLITION_DURABILITY_MULTIPLIER = 0.5;
  public static final double DEFAULT_REACH_PER_POINT = 0.01;
  public static final double DEFAULT_REACH_CAP = 3.0;
  public static final int DEFAULT_XP_GRACE_SECONDS = 30;
  public static final int DEFAULT_XP_PER_MATERIAL_PER_MINUTE = 40;
  public static final int DEFAULT_TRACKER_MAX_BLOCKS = 2_000_000;
  public static final int DEFAULT_BASIC_BLOCK_EXP = 15;
  public static final int DEFAULT_DECORATIVE_BLOCK_EXP = 40;

  private BuildingConfig() {
  }

  /** The survive chance for each level (1-5) of Weatherproof. */
  public static double[] weatherproofChances(FileConfiguration perkConfig) {
    return chances(perkConfig, "skill-1", DEFAULT_WEATHERPROOF);
  }

  /** The survive chance for each level (1-5) of Fortified Blocks. */
  public static double[] fortifiedChances(FileConfiguration perkConfig) {
    return chances(perkConfig, "skill-2", DEFAULT_FORTIFIED);
  }

  public static double demolitionSpeedBonus(FileConfiguration perkConfig) {
    return perkConfig == null ? DEFAULT_DEMOLITION_SPEED_BONUS
        : perkConfig.getDouble("building.skill-3.level-1.breakSpeedBonus",
            DEFAULT_DEMOLITION_SPEED_BONUS);
  }

  public static double demolitionDurabilityMultiplier(FileConfiguration perkConfig) {
    return perkConfig == null ? DEFAULT_DEMOLITION_DURABILITY_MULTIPLIER
        : perkConfig.getDouble("building.skill-3.level-1.durabilityMultiplier",
            DEFAULT_DEMOLITION_DURABILITY_MULTIPLIER);
  }

  /** Blocks of extra reach per point of Builder's Reach. */
  public static double reachPerPoint(FileConfiguration perkConfig) {
    return perkConfig == null ? DEFAULT_REACH_PER_POINT
        : perkConfig.getDouble("building.passive-1.changePerLevel", DEFAULT_REACH_PER_POINT);
  }

  public static double reachCap(FileConfiguration advancedConfig) {
    return advancedConfig == null ? DEFAULT_REACH_CAP
        : advancedConfig.getDouble("building.reachCap", DEFAULT_REACH_CAP);
  }

  public static long xpGraceMillis(FileConfiguration advancedConfig) {
    int seconds = advancedConfig == null ? DEFAULT_XP_GRACE_SECONDS
        : advancedConfig.getInt("building.xpGraceSeconds", DEFAULT_XP_GRACE_SECONDS);
    return seconds * 1000L;
  }

  public static int xpPerMaterialPerMinute(FileConfiguration advancedConfig) {
    return advancedConfig == null ? DEFAULT_XP_PER_MATERIAL_PER_MINUTE
        : advancedConfig.getInt("building.xpPerMaterialPerMinute",
            DEFAULT_XP_PER_MATERIAL_PER_MINUTE);
  }

  public static int trackerMaxBlocks(FileConfiguration advancedConfig) {
    return advancedConfig == null ? DEFAULT_TRACKER_MAX_BLOCKS
        : advancedConfig.getInt("building.trackerMaxBlocks", DEFAULT_TRACKER_MAX_BLOCKS);
  }

  private static double[] chances(FileConfiguration perkConfig, String skill, double[] defaults) {
    double[] result = defaults.clone();
    if (perkConfig == null) {
      return result;
    }
    ConfigurationSection section = perkConfig.getConfigurationSection("building." + skill);
    if (section == null) {
      return result;
    }
    for (int level = 1; level <= PERK_LEVELS; level++) {
      result[level - 1] = section.getDouble("level-" + level + ".surviveChance",
          defaults[level - 1]);
    }
    return result;
  }
}
