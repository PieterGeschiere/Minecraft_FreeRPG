package mc.carlton.freerpg.weaponskills;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

/**
 * Entry point of the self-contained weapon skill module (Mace, Trident, Spear, Bow and Crossbow
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
    Bukkit.getPluginManager().registerEvents(new TridentMastery(), plugin);
    Bukkit.getPluginManager().registerEvents(new SpearMastery(), plugin);
    Bukkit.getPluginManager().registerEvents(new BowMastery(), plugin);
    Bukkit.getPluginManager().registerEvents(new CrossbowMastery(), plugin);
    Bukkit.getPluginManager().registerEvents(new WeaponSkillTreeMenu(), plugin);
    Bukkit.getPluginManager().registerEvents(new WeaponSkillConfigMenu(), plugin);
    WeaponSkillTreeMenu.register(new MaceTreeInfo());
    WeaponSkillTreeMenu.register(new TridentTreeInfo());
    WeaponSkillTreeMenu.register(new SpearTreeInfo());
    WeaponSkillTreeMenu.register(new BowTreeInfo());
    WeaponSkillTreeMenu.register(new CrossbowTreeInfo());
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
    for (WeaponSkill skill : WeaponSkill.values()) {
      WeaponSkillRules.endAbility(e.getPlayer(), skill, false);
    }
    WeaponSkillManager.unload(e.getPlayer().getUniqueId());
  }

  /** Dying ends the abilities, but the cooldowns keep running. */
  @EventHandler
  public void onDeath(PlayerDeathEvent e) {
    for (WeaponSkill skill : WeaponSkill.values()) {
      WeaponSkillRules.endAbility(e.getEntity(), skill, false);
    }
  }

  /** Dropping the weapon of a running ability ends it. */
  @EventHandler
  public void onDrop(PlayerDropItemEvent e) {
    for (WeaponSkill skill : WeaponSkill.values()) {
      if (skill.isWeapon(e.getItemDrop().getItemStack().getType())) {
        WeaponSkillRules.endAbility(e.getPlayer(), skill, true);
      }
    }
  }

  /** Switching to another item ends the abilities that need the weapon in hand. */
  @EventHandler
  public void onHeld(PlayerItemHeldEvent e) {
    ItemStack now = e.getPlayer().getInventory().getItem(e.getNewSlot());
    for (WeaponSkill skill : WeaponSkill.values()) {
      if (now == null || !skill.isWeapon(now.getType())) {
        WeaponSkillRules.endAbility(e.getPlayer(), skill, true);
      }
    }
  }

  @EventHandler
  public void onSwapHands(PlayerSwapHandItemsEvent e) {
    for (WeaponSkill skill : WeaponSkill.values()) {
      WeaponSkillRules.endAbility(e.getPlayer(), skill, true);
    }
  }
}
