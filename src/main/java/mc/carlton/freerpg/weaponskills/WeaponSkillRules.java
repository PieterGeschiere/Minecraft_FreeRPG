package mc.carlton.freerpg.weaponskills;

import java.util.ArrayList;
import mc.carlton.freerpg.FreeRPG;
import mc.carlton.freerpg.config.ConfigLoad;
import mc.carlton.freerpg.core.info.player.PlayerStats;
import mc.carlton.freerpg.utils.game.AbilitySounds;
import mc.carlton.freerpg.utils.game.ActionBarMessages;
import mc.carlton.freerpg.utils.game.ExpFarmTracker;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

/**
 * The rules the main combat skills follow, shared by the weapon skills: creative mode, PvP and
 * animal settings, permissions, the global ability toggle, the cooldown perk and the "ready"
 * message when a cooldown is over.
 */
public final class WeaponSkillRules {

  private WeaponSkillRules() {
  }

  /** Whether a hit on the target counts for the skill (exp, bonuses and effects). */
  public static boolean canHit(Player p, WeaponSkill skill, LivingEntity target) {
    if (!WeaponSkillConfig.isEnabled()
        || !WeaponSkillConfig.getBoolean(skill.getConfigKey() + ".skillAllowed", true)
        || p.getGameMode() == GameMode.CREATIVE || target.isInvulnerable()) {
      return false;
    }
    ConfigLoad config = new ConfigLoad();
    if (target instanceof Player) {
      return config.isAllowPvP() && target != p;
    }
    if (target instanceof Animals) {
      return config.isAllowHurtAnimals();
    }
    return true;
  }

  /** Whether the player may ready the ability of the skill right now. */
  public static boolean canUseAbility(Player p, WeaponSkill skill, boolean clickDenied) {
    if (!WeaponSkillConfig.isEnabled()
        || !WeaponSkillConfig.getBoolean(skill.getConfigKey() + ".skillAllowed", true)
        || clickDenied || p.isBlocking() || p.getGameMode() == GameMode.CREATIVE
        || !p.hasPermission("freeRPG." + skill.getId() + "Ability")) {
      return false;
    }
    ArrayList<Number> global = new PlayerStats(p).getPlayerData().get("global");
    return global.get(24).intValue() >= 1; // the global "trigger abilities" toggle
  }

  /** The cooldown perk of the global skill tree makes cooldowns a third shorter. */
  public static int adjustCooldown(Player p, int cooldownSeconds) {
    ArrayList<Number> global = new PlayerStats(p).getPlayerData().get("global");
    if (global.get(11).intValue() > 0) {
      return (int) Math.round(cooldownSeconds * 2.0 / 3.0);
    }
    return cooldownSeconds;
  }

  /** Exp is reduced for mob farms and spawners, like for the other combat skills. */
  public static double farmMultiplier(Entity target) {
    return new ExpFarmTracker().getExpFarmAndSpawnerCombinedMultiplier(target, "swordsmanship");
  }

  /** Tells the player (with a chime) when the cooldown of the ability is over. */
  public static void scheduleReady(Player p, WeaponSkill skill, WeaponSkillData data,
      long delayTicks) {
    java.util.UUID uuid = p.getUniqueId();
    Bukkit.getScheduler().runTaskLater(FreeRPG.getPlugin(FreeRPG.class), () -> {
      Player online = Bukkit.getPlayer(uuid);
      if (online != null && System.currentTimeMillis() >= data.getAbilityCooldownUntil() - 500) {
        new ActionBarMessages(online).sendMessage(
            ChatColor.GREEN + ">>>" + skill.getAbilityName() + " ready to use<<<");
        online.playSound(online.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.8f, 1.3f);
      }
    }, delayTicks);
  }

  /** Ends a running ability early (death, logout, dropping the weapon). */
  public static void endAbility(Player p, WeaponSkill skill, boolean announce) {
    WeaponSkillData data = WeaponSkillManager.get(p, skill);
    boolean wasActive = data.isAbilityActive();
    data.setAbilityActiveUntil(0);
    data.setAbilityCharges(0);
    if (wasActive && announce && p.isOnline()) {
      new ActionBarMessages(p).sendMessage(
          ChatColor.RED + ">>>" + skill.getAbilityName() + " ended<<<");
      AbilitySounds.end(p, "digging");
    }
  }
}
