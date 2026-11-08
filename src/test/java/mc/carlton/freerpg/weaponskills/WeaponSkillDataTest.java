package mc.carlton.freerpg.weaponskills;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** The per-player data of one weapon skill. */
class WeaponSkillDataTest {

  @Test
  void aNewPlayerHasNothingInvested() {
    WeaponSkillData data = new WeaponSkillData();
    assertEquals(0, data.getLevel());
    assertEquals(0L, data.getExperience());
    assertEquals(0, data.getPassive("p1"));
    assertEquals(0, data.getSkill("1a"));
    assertEquals(0, data.getTotalPassiveLevels());
    assertEquals(0, data.getTotalSkillLevels());
    assertTrue(data.isAbilityEnabled());
    assertTrue(data.isExpBarEnabled());
    assertFalse(data.isAbilityActive());
  }

  @Test
  void totalsAddUpEveryInvestedLevel() {
    WeaponSkillData data = new WeaponSkillData();
    data.setPassive("p1", 10);
    data.setPassive("p2", 5);
    data.setSkill("1a", 3);
    data.setSkill("M", 1);
    assertEquals(15, data.getTotalPassiveLevels());
    assertEquals(4, data.getTotalSkillLevels());
  }

  @Test
  void settingALevelAgainReplacesIt() {
    WeaponSkillData data = new WeaponSkillData();
    data.setSkill("1a", 2);
    data.setSkill("1a", 4);
    assertEquals(4, data.getSkill("1a"));
    assertEquals(4, data.getTotalSkillLevels());
  }

  @Test
  void resetTreeRefundsPassivesAndSkillsButKeepsLevelAndExp() {
    WeaponSkillData data = new WeaponSkillData();
    data.setLevel(120);
    data.setExperience(99_999L);
    data.setPassive("p1", 10);
    data.setSkill("2b", 5);
    data.resetTree();
    assertEquals(0, data.getTotalPassiveLevels());
    assertEquals(0, data.getTotalSkillLevels());
    assertEquals(120, data.getLevel());
    assertEquals(99_999L, data.getExperience());
  }

  @Test
  void abilityStateIsStoredPerField() {
    WeaponSkillData data = new WeaponSkillData();
    data.setAbilityCharges(3);
    data.setAbilityCooldownSeconds(90);
    data.setAbilityCooldownUntil(123_456L);
    assertEquals(3, data.getAbilityCharges());
    assertEquals(90, data.getAbilityCooldownSeconds());
    assertEquals(123_456L, data.getAbilityCooldownUntil());
  }
}
