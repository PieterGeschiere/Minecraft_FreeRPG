package mc.carlton.freerpg.worldevents;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/** Starts and stops the world event system, and answers the questions the rest of the plugin has. */
public final class WorldEvents {

  private WorldEvents() {
  }

  public static void initialize(Plugin plugin) {
    WorldEventConfig.load(plugin);
    WorldEventManager.register(new mc.carlton.freerpg.elites.BloodMoonEvent());
    WorldEventManager.register(new HarvestEvent());
    Bukkit.getPluginManager().registerEvents(new BonusDropListener(), plugin);
    Bukkit.getPluginManager().registerEvents(new HarvestFishListener(), plugin);
    WorldEventManager.register(new GoldRushEvent());
    Bukkit.getPluginManager().registerEvents(new GoldRushNoiseListener(), plugin);
    WorldEventManager.register(new EclipseEvent());
    Bukkit.getPluginManager().registerEvents(new EclipseSpawnListener(), plugin);
    WorldEventManager.register(new FishingFestivalEvent());
    Bukkit.getPluginManager().registerEvents(new FestivalListener(), plugin);
    WorldEventManager.begin(plugin);
  }

  /** Re-reads worldevents.yml (a running event keeps running). */
  public static void reload(Plugin plugin) {
    WorldEventConfig.reload(plugin);
  }

  public static void shutdown() {
    WorldEventManager.shutdown();
  }

  public static boolean isActive(WorldEventType type, World world) {
    return WorldEventManager.isActive(type, world);
  }

  /** The exp multiplier of the running event for this player and skill (1.0 when none applies). */
  public static double xpMultiplier(Player p, String skillName) {
    return WorldEventManager.current()
        .filter(event -> event.appliesTo(p.getWorld()))
        .map(event -> WorldEventManager.eventOf(event.type()).xpMultiplier(skillName))
        .orElse(1.0);
  }
}
