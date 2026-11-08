package mc.carlton.freerpg.weaponskills;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Cooldown and skill lookup rules shared by the weapon skills. */
class WeaponSkillRulesTest {

  @Test
  void cooldownPerkMakesCooldownAThirdShorter() {
    assertEquals(80, WeaponSkillRules.scaleCooldown(120, true));
    assertEquals(60, WeaponSkillRules.scaleCooldown(90, true));
    assertEquals(120, WeaponSkillRules.scaleCooldown(120, false));
  }

  @Test
  void cooldownPerkRoundsToWholeSeconds() {
    assertEquals(7, WeaponSkillRules.scaleCooldown(10, true)); // 6.67
    assertEquals(0, WeaponSkillRules.scaleCooldown(0, true));
  }

  @Test
  void cooldownStartsWhenTheAbilityEnds() {
    assertEquals(1_000_000L + 90_000L, WeaponSkillRules.cooldownEnd(1_000_000L, 90));
    assertEquals(1_000_000L, WeaponSkillRules.cooldownEnd(1_000_000L, 0));
    assertEquals(1_000_000L, WeaponSkillRules.cooldownEnd(1_000_000L, -5));
  }

  @Test
  void skillsAreFoundByIdOrShortNameIgnoringCase() {
    assertSame(WeaponSkill.MACE, WeaponSkill.fromInput("maceMastery"));
    assertSame(WeaponSkill.MACE, WeaponSkill.fromInput("MACE"));
    assertSame(WeaponSkill.TRIDENT, WeaponSkill.fromInput("trident"));
    assertSame(WeaponSkill.SPEAR, WeaponSkill.fromInput("SpearMastery"));
    assertNull(WeaponSkill.fromInput("sword"));
    assertNull(WeaponSkill.fromInput(null));
  }

  @Test
  void configKeysMatchTheSectionsOfWeaponSkillsYml() {
    assertEquals("mace", WeaponSkill.MACE.getConfigKey());
    assertEquals("trident", WeaponSkill.TRIDENT.getConfigKey());
    assertEquals("spear", WeaponSkill.SPEAR.getConfigKey());
  }

  @Test
  void abilityIsActiveOnlyBeforeItsEndTime() {
    WeaponSkillData data = new WeaponSkillData();
    assertFalse(data.isAbilityActive());
    data.setAbilityActiveUntil(System.currentTimeMillis() + 60_000);
    assertTrue(data.isAbilityActive());
    data.setAbilityActiveUntil(System.currentTimeMillis() - 1);
    assertFalse(data.isAbilityActive());
  }
}
