package mc.carlton.freerpg.utils.game;

import mc.carlton.freerpg.weaponskills.WeaponSkillConfig;

/**
 * The switches for the sounds and particles of the main skill abilities and the perks. They are in
 * the effects: section of weaponSkills.yml and are all on when missing.
 */
public final class EffectSettings {

  public static final String SKILL_ABILITY_SOUNDS = "effects.skillAbilitySounds";
  public static final String SKILL_ABILITY_PARTICLES = "effects.skillAbilityParticles";
  public static final String PERK_SOUNDS = "effects.perkSounds";
  public static final String PERK_PARTICLES = "effects.perkParticles";

  private EffectSettings() {
  }

  public static boolean skillAbilitySounds() {
    return WeaponSkillConfig.getBoolean(SKILL_ABILITY_SOUNDS, true);
  }

  public static boolean skillAbilityParticles() {
    return WeaponSkillConfig.getBoolean(SKILL_ABILITY_PARTICLES, true);
  }

  public static boolean perkSounds() {
    return WeaponSkillConfig.getBoolean(PERK_SOUNDS, true);
  }

  public static boolean perkParticles() {
    return WeaponSkillConfig.getBoolean(PERK_PARTICLES, true);
  }
}
