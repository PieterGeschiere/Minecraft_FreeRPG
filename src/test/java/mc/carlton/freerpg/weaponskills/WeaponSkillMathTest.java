package mc.carlton.freerpg.weaponskills;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

/** Exp, damage and range formulas of the weapon skills, with the built-in default values. */
class WeaponSkillMathTest {

  private static final double EPS = 1e-9;

  private static WeaponSkillData data() {
    return new WeaponSkillData();
  }

  // --- Spear exp scaling ---

  @Test
  void fullyChargedHitGivesBaseExpPlusDamageExp() {
    assertEquals(10 + 5 * 6.0, SpearMastery.getHitExp(1.0, 6.0, false, false), EPS);
  }

  @Test
  void weakSwingsGiveLessBaseExp() {
    double full = SpearMastery.getHitExp(1.0, 6.0, false, false);
    double half = SpearMastery.getHitExp(0.5, 6.0, false, false);
    assertEquals(full - 5, half, EPS);
    assertEquals(0.0, SpearMastery.getHitExp(0.0, 0.0, true, true), EPS);
  }

  @Test
  void attackChargeIsClampedBetweenZeroAndOne() {
    assertEquals(SpearMastery.getHitExp(1.0, 4.0, true, true),
        SpearMastery.getHitExp(7.0, 4.0, true, true), EPS);
    assertEquals(SpearMastery.getHitExp(0.0, 4.0, true, true),
        SpearMastery.getHitExp(-3.0, 4.0, true, true), EPS);
  }

  @Test
  void sprintAndMountBonusesAddUpAndScaleWithTheSwing() {
    double plain = SpearMastery.getHitExp(1.0, 0.0, false, false);
    assertEquals(plain + 30, SpearMastery.getHitExp(1.0, 0.0, true, false), EPS);
    assertEquals(plain + 30, SpearMastery.getHitExp(1.0, 0.0, false, true), EPS);
    assertEquals(plain + 60, SpearMastery.getHitExp(1.0, 0.0, true, true), EPS);
    assertEquals(5 + 15 + 15, SpearMastery.getHitExp(0.5, 0.0, true, true), EPS);
  }

  // --- Mounted spear detection ---

  @Test
  void noVehicleIsNotMounted() {
    assertFalse(SpearMastery.isMount(null));
  }

  // --- Spear damage and range ---

  @Test
  void spearBonusNeedsTheMatchingSituation() {
    WeaponSkillData data = data();
    data.setPassive(SpearMastery.MOMENTUM, 100);
    data.setPassive(SpearMastery.MOUNTED_CHARGE, 100);
    assertEquals(0.0, SpearMastery.getDamageBonus(data, false, false), EPS);
    assertEquals(0.3, SpearMastery.getDamageBonus(data, true, false), EPS);
    assertEquals(0.4, SpearMastery.getDamageBonus(data, false, true), EPS);
    assertEquals(0.7, SpearMastery.getDamageBonus(data, true, true), EPS);
  }

  @Test
  void dragoonsWrathAddsABonusEverywhere() {
    WeaponSkillData data = data();
    data.setSkill(SpearMastery.DRAGOONS_WRATH, 1);
    assertEquals(0.25, SpearMastery.getDamageBonus(data, false, false), EPS);
  }

  @Test
  void pierceLengthIsCapped() {
    WeaponSkillData data = data();
    assertEquals(5.0, SpearMastery.getPierceLength(data), EPS);
    data.setSkill(SpearMastery.LONG_REACH, 100);
    assertEquals(10.0, SpearMastery.getPierceLength(data), EPS);
  }

  @Test
  void hamstringSlowGrowsPerLevel() {
    WeaponSkillData data = data();
    assertEquals(0.0, SpearMastery.getHamstringSeconds(data), EPS);
    data.setSkill(SpearMastery.HAMSTRING, 3);
    assertEquals(3.0, SpearMastery.getHamstringSeconds(data), EPS);
  }

  @Test
  void rushDurationIsCapped() {
    WeaponSkillData data = data();
    assertEquals(8.0, SpearMastery.getAbilityDuration(data), EPS);
    data.setPassive(SpearMastery.RUSH_DURATION, 1_000_000);
    assertEquals(14.0, SpearMastery.getAbilityDuration(data), EPS);
  }

  // --- Mace ---

  @Test
  void softLandingNeverRemovesAllFallDamage() {
    WeaponSkillData data = data();
    data.setPassive(MaceMastery.SOFT_LANDING, 1000);
    assertEquals(0.95, MaceMastery.getFallReduction(data), EPS);
  }

  @Test
  void shockwaveRadiusGrowsAndIsCapped() {
    WeaponSkillData data = data();
    assertEquals(3.0, MaceMastery.getShockwaveRadius(data), EPS);
    data.setSkill(MaceMastery.WIDE_SHOCKWAVE, 2);
    assertEquals(4.0, MaceMastery.getShockwaveRadius(data), EPS);
    data.setSkill(MaceMastery.TITANS_WRATH, 1);
    data.setSkill(MaceMastery.WIDE_SHOCKWAVE, 100);
    assertEquals(7.0, MaceMastery.getShockwaveRadius(data), EPS);
  }

  @Test
  void shockwaveDamageAddsAftershockAndEarthshaker() {
    WeaponSkillData data = data();
    assertEquals(0.4, MaceMastery.getShockwaveDamage(data), EPS);
    data.setSkill(MaceMastery.AFTERSHOCK, 2);
    data.setSkill(MaceMastery.EARTHSHAKER, 1);
    assertEquals(0.4 + 0.12 + 0.2, MaceMastery.getShockwaveDamage(data), EPS);
  }

  @Test
  void smashBonusCombinesPassiveAndTitansWrath() {
    WeaponSkillData data = data();
    data.setPassive(MaceMastery.SMASH_POWER, 100);
    assertEquals(0.3, MaceMastery.getSmashBonus(data), EPS);
    data.setSkill(MaceMastery.TITANS_WRATH, 1);
    assertEquals(0.55, MaceMastery.getSmashBonus(data), EPS);
  }

  // --- Trident ---

  @Test
  void tidalMightOnlyCountsInWaterOrRain() {
    WeaponSkillData data = data();
    data.setPassive(TridentMastery.THROWING_POWER, 100);
    data.setPassive(TridentMastery.TIDAL_MIGHT, 100);
    assertEquals(0.3, TridentMastery.getThrownBonus(data, false), EPS);
    assertEquals(0.7, TridentMastery.getThrownBonus(data, true), EPS);
  }

  @Test
  void maelstromRadiusIsCapped() {
    WeaponSkillData data = data();
    assertEquals(4.0, TridentMastery.getMaelstromRadius(data), EPS);
    data.setSkill(TridentMastery.WIDE_MAELSTROM, 100);
    assertEquals(8.0, TridentMastery.getMaelstromRadius(data), EPS);
  }
}
