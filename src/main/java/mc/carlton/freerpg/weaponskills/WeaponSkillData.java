package mc.carlton.freerpg.weaponskills;

import java.util.HashMap;
import java.util.Map;

/**
 * The saved progress of one player in one weapon skill (experience, invested passive tokens and
 * skill levels of the skill tree), plus the runtime ability timers.
 *
 * <p>The skill tree has the same shape as the main skills: three passives ("p1", "p2", "p3") and
 * seven skills ("1a", "1b", "2a", "2b", "3a", "3b", "M").
 */
public class WeaponSkillData {

  private long experience;
  private int level;
  private final Map<String, Integer> passiveLevels = new HashMap<>();
  private final Map<String, Integer> skillLevels = new HashMap<>();
  private boolean abilityEnabled = true;
  /** Time (ms) until the ability stays active, 0 if it is not active. Not saved. */
  private long abilityActiveUntil;
  /** Time (ms) until the ability can be used again. Not saved. */
  private long abilityCooldownUntil;
  /** Shockwaves left in the current activation. Not saved. */
  private int abilityCharges;

  public long getExperience() {
    return experience;
  }

  public void setExperience(long experience) {
    this.experience = experience;
  }

  public int getLevel() {
    return level;
  }

  public void setLevel(int level) {
    this.level = level;
  }

  /** Levels invested in a passive (0 if none). */
  public int getPassive(String key) {
    return passiveLevels.getOrDefault(key, 0);
  }

  public void setPassive(String key, int levels) {
    passiveLevels.put(key, levels);
  }

  public Map<String, Integer> getPassives() {
    return passiveLevels;
  }

  public int getTotalPassiveLevels() {
    int total = 0;
    for (int value : passiveLevels.values()) {
      total += value;
    }
    return total;
  }

  /** Level of a skill of the tree (0 if not learned). */
  public int getSkill(String key) {
    return skillLevels.getOrDefault(key, 0);
  }

  public void setSkill(String key, int levels) {
    skillLevels.put(key, levels);
  }

  public Map<String, Integer> getSkills() {
    return skillLevels;
  }

  public int getTotalSkillLevels() {
    int total = 0;
    for (int value : skillLevels.values()) {
      total += value;
    }
    return total;
  }

  /** Removes everything invested in the skill tree (a refund). */
  public void resetTree() {
    passiveLevels.clear();
    skillLevels.clear();
  }

  public boolean isAbilityEnabled() {
    return abilityEnabled;
  }

  public void setAbilityEnabled(boolean abilityEnabled) {
    this.abilityEnabled = abilityEnabled;
  }

  public long getAbilityActiveUntil() {
    return abilityActiveUntil;
  }

  public void setAbilityActiveUntil(long abilityActiveUntil) {
    this.abilityActiveUntil = abilityActiveUntil;
  }

  public long getAbilityCooldownUntil() {
    return abilityCooldownUntil;
  }

  public void setAbilityCooldownUntil(long abilityCooldownUntil) {
    this.abilityCooldownUntil = abilityCooldownUntil;
  }

  public int getAbilityCharges() {
    return abilityCharges;
  }

  public void setAbilityCharges(int abilityCharges) {
    this.abilityCharges = abilityCharges;
  }

  public boolean isAbilityActive() {
    return System.currentTimeMillis() < abilityActiveUntil;
  }
}
