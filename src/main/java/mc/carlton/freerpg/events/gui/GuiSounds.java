package mc.carlton.freerpg.events.gui;

import mc.carlton.freerpg.weaponskills.WeaponSkillConfig;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

/**
 * The cues of actions in the menus that matter more than a plain click: a token was spent, there
 * was nothing to spend, a refund was confirmed. Follows the same switch as {@link MenuSounds}.
 */
public final class GuiSounds {

  private GuiSounds() {
  }

  private static boolean enabled() {
    return WeaponSkillConfig.getBoolean("effects.sounds", true);
  }

  /** A token was spent on a perk: a rising chime and a few sparks around the player. */
  public static void upgrade(Player p) {
    if (!enabled()) {
      return;
    }
    Location at = p.getLocation();
    p.playSound(at, Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.6f);
    p.playSound(at, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 0.5f, 1.4f);
    p.getWorld().spawnParticle(Particle.ENCHANT, at.clone().add(0, 1.5, 0), 20, 0.4, 0.5, 0.4,
        0.6);
  }

  /** The action was refused: no tokens, maxed out, a requirement is missing. */
  public static void deny(Player p) {
    if (!enabled()) {
      return;
    }
    Location at = p.getLocation();
    p.playSound(at, Sound.BLOCK_NOTE_BLOCK_BASS, 0.8f, 0.6f);
    p.playSound(at, Sound.ENTITY_VILLAGER_NO, 0.4f, 1.0f);
  }

  /** A skill tree was refunded for souls. */
  public static void refund(Player p) {
    if (!enabled()) {
      return;
    }
    Location at = p.getLocation();
    p.playSound(at, Sound.ENTITY_ZOMBIE_VILLAGER_CURE, 0.6f, 1.4f);
    p.playSound(at, Sound.PARTICLE_SOUL_ESCAPE, 1.0f, 0.8f);
    p.getWorld().spawnParticle(Particle.SOUL, at.clone().add(0, 1.0, 0), 20, 0.4, 0.7, 0.4, 0.05);
  }

  /** The player backed out of a confirmation. */
  public static void cancel(Player p) {
    if (!enabled()) {
      return;
    }
    p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 0.7f);
  }
}
