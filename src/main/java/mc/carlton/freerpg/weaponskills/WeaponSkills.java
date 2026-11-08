package mc.carlton.freerpg.weaponskills;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

/**
 * Entry point of the self-contained weapon skill module (Mace Mastery and later Spear and Trident
 * Mastery). It has its own data, config (weaponSkills.yml) and menu (/frpg weapons) and does not
 * touch the data of the main FreeRPG skills.
 */
public class WeaponSkills implements Listener {

  private static final long SAVE_INTERVAL_TICKS = 20L * 60 * 5;

  /** Loads the config, registers the listeners and starts the periodic saving. */
  public static void initialize(Plugin plugin) {
    WeaponSkillConfig.load(plugin);
    WeaponSkillManager.initialize(plugin);
    Bukkit.getPluginManager().registerEvents(new WeaponSkills(), plugin);
    Bukkit.getPluginManager().registerEvents(new MaceMastery(), plugin);
    Bukkit.getPluginManager().registerEvents(new WeaponSkillsMenu(), plugin);
    Bukkit.getPluginManager().registerEvents(new MaceSkillTreeMenu(), plugin);
    Bukkit.getScheduler().runTaskTimer(plugin, WeaponSkillManager::saveAll, SAVE_INTERVAL_TICKS,
        SAVE_INTERVAL_TICKS);
  }

  /** Saves everything, called when the plugin shuts down. */
  public static void shutdown() {
    WeaponSkillManager.saveAll();
  }

  public static void openMenu(Player p) {
    WeaponSkillsMenu.open(p);
  }

  @EventHandler
  public void onQuit(PlayerQuitEvent e) {
    WeaponSkillManager.unload(e.getPlayer().getUniqueId());
  }
}
