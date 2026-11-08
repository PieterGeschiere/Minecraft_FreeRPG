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

  /** Like {@link #text(Player, String, String...)} for a command sender (the console gets the default language). */
  public static String text(org.bukkit.command.CommandSender sender, String id,
      String... replacements) {
    return text(sender instanceof Player ? (Player) sender : null, id, replacements);
  }

  /**
   * A message in the player's language (English if it has no translation yet). Placeholders like
   * {ability} are replaced by the given key/value pairs.
   */
  public static String text(Player p, String id, String... replacements) {
    String message = new mc.carlton.freerpg.utils.game.LanguageSelector(p).getString(id);
    for (int i = 0; i + 1 < replacements.length; i += 2) {
      message = message.replace("{" + replacements[i] + "}", replacements[i + 1]);
    }
    return message;
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
    return scaleCooldown(cooldownSeconds, global.get(11).intValue() > 0);
  }

  /** The cooldown after the cooldown perk (a third shorter, rounded) has been applied. */
  static int scaleCooldown(int cooldownSeconds, boolean hasCooldownPerk) {
    return hasCooldownPerk ? (int) Math.round(cooldownSeconds * 2.0 / 3.0) : cooldownSeconds;
  }

  /** The moment (epoch millis) the cooldown of an ability that ends at {@code now} is over. */
  static long cooldownEnd(long now, int cooldownSeconds) {
    return now + Math.max(0, cooldownSeconds) * 1000L;
  }

  /** Exp is reduced for mob farms and spawners, like for the other combat skills. */
  public static double farmMultiplier(Entity target) {
    return new ExpFarmTracker().getExpFarmAndSpawnerCombinedMultiplier(target, "swordsmanship");
  }

  /**
   * The exp a kill is worth: it reuses the mob values of Swordsmanship, collecting what it would
   * give instead of giving it.
   */
  static double killReward(Player killer, LivingEntity victim) {
    double[] reward = {0};
    mc.carlton.freerpg.core.info.player.ChangeStats collector =
        new mc.carlton.freerpg.core.info.player.ChangeStats(killer) {
          @Override
          public void changeEXP(String skillName, int expChange) {
            reward[0] += expChange;
          }
        };
    new mc.carlton.freerpg.utils.globalVariables.EntityGroups().killEntity(victim, "swordsmanship",
        new ConfigLoad().getExpMapForSkill("swordsmanship"), collector);
    return reward[0];
  }

  /** Tells the player (with a chime) when the cooldown of the ability is over. */
  public static void scheduleReady(Player p, WeaponSkill skill, WeaponSkillData data,
      long delayTicks) {
    java.util.UUID uuid = p.getUniqueId();
    Bukkit.getScheduler().runTaskLater(FreeRPG.getPlugin(FreeRPG.class), () -> {
      Player online = Bukkit.getPlayer(uuid);
      if (online != null && System.currentTimeMillis() >= data.getAbilityCooldownUntil() - 500) {
        new ActionBarMessages(online).sendMessage(
            ChatColor.GREEN + text(online, "weaponReadyToUse", "ability", skill.getAbilityName()));
        online.playSound(online.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.8f, 1.3f);
      }
    }, delayTicks);
  }

  /** Ends a running ability early (death, logout, dropping the weapon). */
  public static void endAbility(Player p, WeaponSkill skill, boolean announce) {
    WeaponSkillData data = WeaponSkillManager.get(p, skill);
    if (data.getAbilityCharges() <= 0 && !data.isAbilityActive()) {
      return; // nothing is running
    }
    finishAbility(p, skill, data, announce);
  }

  /**
   * Ends a running ability, for whatever reason (time up, charges used, item switched, death). The
   * cooldown starts now, so it is counted from the moment the ability ends, and the "ready"
   * message is scheduled for the end of it.
   */
  public static void finishAbility(Player p, WeaponSkill skill, WeaponSkillData data,
      boolean announce) {
    data.setAbilityActiveUntil(0);
    data.setAbilityCharges(0);
    int cooldown = Math.max(0, data.getAbilityCooldownSeconds());
    data.setAbilityCooldownUntil(cooldownEnd(System.currentTimeMillis(), cooldown));
    if (announce && p.isOnline()) {
      new ActionBarMessages(p).sendMessage(
          ChatColor.RED + text(p, "weaponEnded", "ability", skill.getAbilityName()));
      AbilityEffects.ended(p, skill);
    }
    scheduleReady(p, skill, data, cooldown * 20L + 2);
  }
}
