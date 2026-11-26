package mc.carlton.freerpg.elites;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

/** Starts and stops the elite mob system. */
public final class Elites {

  private Elites() {
  }

  public static void initialize(Plugin plugin) {
    EliteConfig.load(plugin);
    AffixRegistry.registerDefaults();
    Bukkit.getPluginManager().registerEvents(new BloodMoonListener(), plugin);
    Bukkit.getPluginManager().registerEvents(new EliteListener(), plugin);
    Bukkit.getPluginManager().registerEvents(new AffixListener(), plugin);
    Bukkit.getPluginManager().registerEvents(new DamageScalingListener(), plugin);
    Bukkit.getPluginManager().registerEvents(new EliteKillListener(), plugin);
    Bukkit.getPluginManager().registerEvents(new BigShooterListener(), plugin);
    EliteTracker.start(plugin);
  }

  public static void reload(Plugin plugin) {
    EliteConfig.reload(plugin);
    mc.carlton.freerpg.worldevents.WorldEvents.reload(plugin);
  }

  public static void shutdown() {
    EliteTracker.stop();
  }
}
