package mc.carlton.freerpg.skills.aeronautics;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Reads the Aeronautics numbers out of perkConfig.yml and advancedConfig.yml.
 *
 * <p>Servers keep their old config files, so every value falls back to the default when the file
 * (or its aeronautics section) is missing. Nothing here needs a running server.
 */
public final class AeronauticsConfig {

  public static final int PERK_LEVELS = 5;
  public static final double[] DEFAULT_SOFT_LANDING = {0.10, 0.20, 0.30, 0.40, 0.50};
  public static final int[] DEFAULT_LONG_BURN = {1, 1, 2, 2, 3};
  public static final int DEFAULT_TAILWIND_POWER = 1;
  public static final double DEFAULT_EFFICIENCY_PER_POINT = 0.005;
  public static final double DEFAULT_EFFICIENCY_CAP = 0.5;
  public static final double DEFAULT_STEP_BLOCKS = 50.0;
  public static final int DEFAULT_MAX_STEPS_PER_MINUTE = 60;
  public static final int DEFAULT_GLIDE_STEP_EXP = 100;

  private AeronauticsConfig() {
  }

  /** Proportion of wall-crash damage removed at each level (1-5) of Soft Landing. */
  public static double[] softLandingReductions(FileConfiguration perkConfig) {
    double[] result = DEFAULT_SOFT_LANDING.clone();
    ConfigurationSection section = section(perkConfig, "skill-1");
    if (section != null) {
      for (int level = 1; level <= PERK_LEVELS; level++) {
        result[level - 1] = section.getDouble("level-" + level + ".damageReduction",
            DEFAULT_SOFT_LANDING[level - 1]);
      }
    }
    return result;
  }

  /** Extra rocket flight power at each level (1-5) of Long Burn. */
  public static int[] longBurnPower(FileConfiguration perkConfig) {
    int[] result = DEFAULT_LONG_BURN.clone();
    ConfigurationSection section = section(perkConfig, "skill-2");
    if (section != null) {
      for (int level = 1; level <= PERK_LEVELS; level++) {
        result[level - 1] = section.getInt("level-" + level + ".extraPower",
            DEFAULT_LONG_BURN[level - 1]);
      }
    }
    return result;
  }

  public static int tailwindPower(FileConfiguration perkConfig) {
    ConfigurationSection section = section(perkConfig, "skill-3");
    return section == null ? DEFAULT_TAILWIND_POWER
        : section.getInt("level-1.extraPower", DEFAULT_TAILWIND_POWER);
  }

  /** Proportion of elytra durability loss removed per point of Glide Efficiency. */
  public static double efficiencyPerPoint(FileConfiguration perkConfig) {
    ConfigurationSection section = section(perkConfig, "passive-1");
    return section == null ? DEFAULT_EFFICIENCY_PER_POINT
        : section.getDouble("changePerLevel", DEFAULT_EFFICIENCY_PER_POINT);
  }

  public static double efficiencyCap(FileConfiguration advancedConfig) {
    return advancedConfig == null ? DEFAULT_EFFICIENCY_CAP
        : advancedConfig.getDouble("aeronautics.glideEfficiencyCap", DEFAULT_EFFICIENCY_CAP);
  }

  public static double stepBlocks(FileConfiguration advancedConfig) {
    double blocks = advancedConfig == null ? DEFAULT_STEP_BLOCKS
        : advancedConfig.getDouble("aeronautics.stepBlocks", DEFAULT_STEP_BLOCKS);
    return blocks > 0 ? blocks : DEFAULT_STEP_BLOCKS;
  }

  public static int maxStepsPerMinute(FileConfiguration advancedConfig) {
    return advancedConfig == null ? DEFAULT_MAX_STEPS_PER_MINUTE
        : advancedConfig.getInt("aeronautics.maxStepsPerMinute", DEFAULT_MAX_STEPS_PER_MINUTE);
  }

  private static ConfigurationSection section(FileConfiguration perkConfig, String name) {
    return perkConfig == null ? null : perkConfig.getConfigurationSection("aeronautics." + name);
  }
}
