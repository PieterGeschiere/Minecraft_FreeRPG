package mc.carlton.freerpg.utils.game;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

/**
 * Sound effects for the lifecycle of the abilities of the main skills: a skill specific sound when
 * the ability is activated, a soft sound when it ends and a chime when it can be used again.
 */
public final class AbilitySounds {

  private static final class Cue {

    private final Sound sound;
    private final float pitch;

    private Cue(Sound sound, float pitch) {
      this.sound = sound;
      this.pitch = pitch;
    }
  }

  private static final Map<String, Cue> ACTIVATE = new HashMap<>();

  static {
    ACTIVATE.put("digging", new Cue(Sound.BLOCK_GRAVEL_BREAK, 0.7f));
    ACTIVATE.put("woodcutting", new Cue(Sound.ITEM_AXE_STRIP, 0.9f));
    ACTIVATE.put("mining", new Cue(Sound.BLOCK_DEEPSLATE_BREAK, 0.8f));
    ACTIVATE.put("farming", new Cue(Sound.ITEM_BONE_MEAL_USE, 1.0f));
    ACTIVATE.put("fishing", new Cue(Sound.ENTITY_FISHING_BOBBER_SPLASH, 1.0f));
    ACTIVATE.put("archery", new Cue(Sound.ENTITY_ARROW_SHOOT, 1.3f));
    ACTIVATE.put("beastMastery", new Cue(Sound.ENTITY_HORSE_GALLOP, 1.0f));
    ACTIVATE.put("swordsmanship", new Cue(Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f));
    ACTIVATE.put("defense", new Cue(Sound.ITEM_SHIELD_BLOCK, 0.8f));
    ACTIVATE.put("axeMastery", new Cue(Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK, 0.9f));
  }

  private AbilitySounds() {
  }

  /** Plays the activation sound of the skill's ability. */
  public static void activate(Player p, String skillName) {
    Cue cue = ACTIVATE.get(skillName);
    if (cue != null && p.isOnline()) {
      p.playSound(p.getLocation(), cue.sound, 1f, cue.pitch);
      p.playSound(p.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 0.4f, 1.4f);
    }
  }

  /** Plays the sound for an ability that ran out. */
  public static void end(Player p, String skillName) {
    if (ACTIVATE.containsKey(skillName) && p.isOnline()) {
      p.playSound(p.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 0.5f, 1.2f);
    }
  }

  /** Plays the chime for an ability that can be used again. */
  public static void ready(Player p, String skillName) {
    if (ACTIVATE.containsKey(skillName) && p.isOnline()) {
      p.playSound(p.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.8f, 1.3f);
    }
  }
}
