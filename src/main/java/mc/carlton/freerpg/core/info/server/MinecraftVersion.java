package mc.carlton.freerpg.core.info.server;

import mc.carlton.freerpg.FreeRPG;
import org.apache.logging.log4j.Level;
import org.bukkit.Bukkit;

/**
 * MinecraftVersion is the class which provides the current Minecraft version of the server the
 * plugin is running on. This helps to turn off certain features of the plugin if they are not
 * supported in this version.
 */
public class MinecraftVersion {

  public static String minecraftVersion;
  public static double minecraftVersion_Double;

  /**
   * Initializes the Minecraft version of the server the plugin is running, so features can be
   * turned off for certain versions.
   */
  public void initializeVersion() {
    minecraftVersion = Bukkit.getVersion();
    if (minecraftVersion.contains("1.8")) {
      minecraftVersion_Double = 1.8;
    } else if (minecraftVersion.contains("1.9")) {
      minecraftVersion_Double = 1.9;
    } else if (minecraftVersion.contains("1.10")) {
      minecraftVersion_Double = 1.10;
    } else if (minecraftVersion.contains("1.11")) {
      minecraftVersion_Double = 1.11;
    } else if (minecraftVersion.contains("1.12")) {
      minecraftVersion_Double = 1.12;
    } else if (minecraftVersion.contains("1.13")) {
      minecraftVersion_Double = 1.13;
    } else if (minecraftVersion.contains("1.14")) {
      minecraftVersion_Double = 1.14;
    } else if (minecraftVersion.contains("1.15")) {
      minecraftVersion_Double = 1.15;
    } else if (minecraftVersion.contains("1.16")) {
      minecraftVersion_Double = 1.16;
    } else if (minecraftVersion.contains("1.17")) {
      minecraftVersion_Double = 1.17;
    } else if (minecraftVersion.contains("1.18")) {
      minecraftVersion_Double = 1.18;
    } else if (parseYearBasedVersion(Bukkit.getBukkitVersion()) > 0) {
      // Year-based versioning (26.1 and later), e.g. 26.3 -> 26.03. Always above any 1.x gate.
      minecraftVersion_Double = parseYearBasedVersion(Bukkit.getBukkitVersion());
    } else {
      minecraftVersion_Double = 1.18;
      FreeRPG.log(Level.WARN,
          "Could not determine minecraft version, the plugin assumes 1.18. "
              + "This might be caused due to the usage of a newer minecraft version the plugin does not support. "
              + "If you notice unexpected behaviour please visit the plugins Github page and open a new issue!");
    }
  }

  /** Returns e.g. 26.03 for "26.3-R0.1-SNAPSHOT", or -1 if not a year-based version. */
  private static double parseYearBasedVersion(String bukkitVersion) {
    java.util.regex.Matcher m = java.util.regex.Pattern.compile("^(\\d{2})\\.(\\d+)")
        .matcher(bukkitVersion);
    if (!m.find()) {
      return -1;
    }
    return Integer.parseInt(m.group(1)) + Integer.parseInt(m.group(2)) / 100.0;
  }

  /**
   * Provides the current Minecraft version of the server as a double. A server running on Minecraft
   * 1.18 returns the double 1.18
   *
   * @return
   */
  public double getMinecraftVersionAsDouble() {
    return minecraftVersion_Double;
  }

  /**
   * Provides the current Minecraft version of the server as a String.
   *
   * @return
   */
  public String getMinecraftVersionAsString() {
    return minecraftVersion;
  }
}
