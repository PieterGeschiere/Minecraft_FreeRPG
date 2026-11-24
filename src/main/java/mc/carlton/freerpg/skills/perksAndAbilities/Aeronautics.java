package mc.carlton.freerpg.skills.perksAndAbilities;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import mc.carlton.freerpg.config.ConfigLoad;
import mc.carlton.freerpg.core.info.player.PlayerStats;
import mc.carlton.freerpg.skills.aeronautics.GlideXpRules;
import org.bukkit.entity.Player;

/** The Aeronautics skill: experience for distance glided with an elytra, plus perk helpers. */
public class Aeronautics extends Skill {

  public static final String SKILL_NAME = "aeronautics";
  public static final String GLIDE_STEP_KEY = "glideStep";
  /** Stat indexes in the player's skill array (same layout as the other passive skills). */
  public static final int PASSIVE_EFFICIENCY = 4;
  public static final int PERK_SOFT_LANDING = 7;
  public static final int PERK_LONG_BURN = 9;
  public static final int PERK_TAILWIND = 13;

  private static GlideXpRules xpRules;

  public Aeronautics(Player p) {
    super(p);
    expMap = new ConfigLoad().getExpMapForSkill(SKILL_NAME);
  }

  public static boolean isEnabled() {
    Boolean allowed = new ConfigLoad().getAllowedSkillsMap().get(SKILL_NAME);
    return allowed == null || allowed;
  }

  /** The shared distance rules, built from the config the first time they are needed. */
  public static synchronized GlideXpRules xpRules() {
    if (xpRules == null) {
      ConfigLoad configLoad = new ConfigLoad();
      xpRules = new GlideXpRules(configLoad.getAeronauticsStepBlocks(),
          configLoad.getAeronauticsMaxStepsPerMinute());
    }
    return xpRules;
  }

  /** Awards experience for glide steps this player just earned. */
  public void onGlideSteps(int steps) {
    if (expMap == null || steps <= 0) {
      return;
    }
    Integer perStep = expMap.get(GLIDE_STEP_KEY);
    if (perStep != null && perStep > 0) {
      increaseStats.changeEXP(SKILL_NAME, perStep * steps);
    }
  }

  /** A perk or passive value from a player's loaded stats; 0 when they are not loaded. */
  public static int statOf(UUID player, int statIndex) {
    try {
      Map<String, ArrayList<Number>> stats = new PlayerStats(player).getData().get(player);
      if (stats == null || !stats.containsKey(SKILL_NAME)) {
        return 0;
      }
      ArrayList<Number> skill = stats.get(SKILL_NAME);
      return statIndex < skill.size() ? skill.get(statIndex).intValue() : 0;
    } catch (RuntimeException e) {
      return 0;
    }
  }
}
