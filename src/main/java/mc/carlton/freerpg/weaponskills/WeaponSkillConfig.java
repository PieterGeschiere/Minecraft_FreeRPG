package mc.carlton.freerpg.weaponskills;

import java.io.File;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

/**
 * Reads weaponSkills.yml. Every value has a built-in default, so an old or incomplete file keeps
 * working.
 */
public final class WeaponSkillConfig {

  private static YamlConfiguration config = new YamlConfiguration();

  private WeaponSkillConfig() {
  }

  public static void load(Plugin plugin) {
    File file = new File(plugin.getDataFolder(), "weaponSkills.yml");
    if (!file.exists()) {
      plugin.saveResource("weaponSkills.yml", false);
    }
    config = YamlConfiguration.loadConfiguration(file);
  }

  public static boolean isEnabled() {
    return config.getBoolean("enabled", true);
  }

  public static boolean getBoolean(String path, boolean defaultValue) {
    return config.getBoolean(path, defaultValue);
  }

  public static double getDouble(String path, double defaultValue) {
    return config.getDouble(path, defaultValue);
  }

  public static int getInt(String path, int defaultValue) {
    return config.getInt(path, defaultValue);
  }
}
