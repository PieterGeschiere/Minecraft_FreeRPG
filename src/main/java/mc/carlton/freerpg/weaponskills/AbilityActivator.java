package mc.carlton.freerpg.weaponskills;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import mc.carlton.freerpg.FreeRPG;
import mc.carlton.freerpg.utils.game.ActionBarMessages;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

/** Readies the ability of a weapon skill: checks, cooldown, charges, message and the end timer. */
final class AbilityActivator {

  private static final Map<UUID, Long> LAST_COOLDOWN_MESSAGE = new HashMap<>();

  private AbilityActivator() {
  }

  /**
   * @param readyKey language key of the "ability ready" message, which gets {ability}, {seconds}
   *                 and {count}
   */
  static void activate(Player p, WeaponSkill skill, WeaponSkillData data, boolean clickDenied,
      double duration, int charges, int baseCooldown, String readyKey, Sound sound) {
    if (!WeaponSkillRules.canUseAbility(p, skill, clickDenied)) {
      return;
    }
    if (!data.isAbilityEnabled() || data.isAbilityActive()) {
      return;
    }
    ActionBarMessages bar = new ActionBarMessages(p);
    long now = System.currentTimeMillis();
    if (now < data.getAbilityCooldownUntil()) {
      Long last = LAST_COOLDOWN_MESSAGE.get(p.getUniqueId());
      if (last == null || now - last >= 3000) {
        LAST_COOLDOWN_MESSAGE.put(p.getUniqueId(), now);
        long seconds = (data.getAbilityCooldownUntil() - now) / 1000 + 1;
        bar.sendMessage(ChatColor.RED + WeaponSkillRules.text(p, "weaponCooldown",
            "ability", skill.getAbilityName(), "seconds", Long.toString(seconds)));
      }
      return;
    }
    int cooldown = WeaponSkillRules.adjustCooldown(p, baseCooldown);
    data.setAbilityCharges(charges);
    data.setAbilityCooldownSeconds(cooldown);
    data.setAbilityActiveUntil(now + (long) (duration * 1000));
    data.setAbilityCooldownUntil(now + (long) ((duration + cooldown) * 1000));
    bar.sendMessage(ChatColor.GREEN + WeaponSkillRules.text(p, readyKey,
        "ability", skill.getAbilityName(), "seconds",
        Double.toString(Math.round(duration * 10) / 10.0), "count", Integer.toString(charges)));
    p.playSound(p.getLocation(), sound, 1f, 1f);
    UUID uuid = p.getUniqueId();
    Bukkit.getScheduler().runTaskLater(FreeRPG.getPlugin(FreeRPG.class), () -> {
      Player online = Bukkit.getPlayer(uuid);
      if (online != null && !data.isAbilityActive() && data.getAbilityCharges() > 0) {
        WeaponSkillRules.finishAbility(online, skill, data, true);
      }
    }, (long) (duration * 20) + 2);
  }
}
