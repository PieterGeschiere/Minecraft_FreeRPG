package mc.carlton.freerpg.weaponskills;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Formulas of Bow Mastery and Crossbow Mastery, with the built-in default values. */
class RangedWeaponMathTest {

  private static final double EPS = 1e-9;

  private static WeaponSkillData data() {
    return new WeaponSkillData();
  }

  @Test
  void rangedSkillsAreFoundByName() {
    assertSame(WeaponSkill.BOW, WeaponSkill.fromInput("bow"));
    assertSame(WeaponSkill.BOW, WeaponSkill.fromInput("bowMastery"));
    assertSame(WeaponSkill.CROSSBOW, WeaponSkill.fromInput("CROSSBOW"));
    assertSame(WeaponSkill.CROSSBOW, WeaponSkill.fromInput("crossbowMastery"));
    assertEquals("bow", WeaponSkill.BOW.getConfigKey());
    assertEquals("crossbow", WeaponSkill.CROSSBOW.getConfigKey());
    assertNull(WeaponSkill.fromInput("arrow"));
  }

  // --- Bow ---

  @Test
  void arrowStormFiresTwoExtraArrowsAtFirst() {
    assertEquals(2, BowMastery.getExtraArrows(data()));
  }

  @Test
  void extraArrowsGrowAndAreCapped() {
    WeaponSkillData data = data();
    data.setSkill(BowMastery.EXTRA_ARROWS, 3);
    assertEquals(5, BowMastery.getExtraArrows(data));
    data.setSkill(BowMastery.HAWKEYES_WRATH, 1);
    assertEquals(7, BowMastery.getExtraArrows(data));
    data.setSkill(BowMastery.EXTRA_ARROWS, 100);
    assertEquals(8, BowMastery.getExtraArrows(data));
  }

  @Test
  void volleyArrowsDealLessUntilVolleyPowerMakesThemFull() {
    WeaponSkillData data = data();
    assertEquals(0.7, BowMastery.getVolleyDamage(data), EPS);
    data.setSkill(BowMastery.VOLLEY_POWER, 3);
    assertEquals(0.88, BowMastery.getVolleyDamage(data), EPS);
    data.setSkill(BowMastery.VOLLEY_POWER, 100);
    assertEquals(1.0, BowMastery.getVolleyDamage(data), EPS);
  }

  @Test
  void sniperBonusGrowsWithDistanceUntilFullDistance() {
    WeaponSkillData data = data();
    data.setPassive(BowMastery.SNIPER, 100);
    assertEquals(0.0, BowMastery.getDamageBonus(data, 0), EPS);
    assertEquals(0.2, BowMastery.getDamageBonus(data, 15), EPS);
    assertEquals(0.4, BowMastery.getDamageBonus(data, 30), EPS);
    assertEquals(0.4, BowMastery.getDamageBonus(data, 90), EPS);
  }

  @Test
  void drawPowerAndHawkeyesWrathAddUp() {
    WeaponSkillData data = data();
    data.setPassive(BowMastery.DRAW_POWER, 100);
    data.setSkill(BowMastery.HAWKEYES_WRATH, 1);
    assertEquals(0.3 + 0.25, BowMastery.getDamageBonus(data, 0), EPS);
  }

  @Test
  void farShotsGiveMoreExpUpToFiftyBlocks() {
    assertEquals(15 + 5 * 4.0, BowMastery.getHitExp(4.0, 0, false), EPS);
    assertEquals(15 + 5 * 4.0 + 2 * 10, BowMastery.getHitExp(4.0, 10, false), EPS);
    assertEquals(BowMastery.getHitExp(4.0, 50, false), BowMastery.getHitExp(4.0, 500, false), EPS);
  }

  @Test
  void extraArrowsGiveLessBaseExp() {
    assertTrue(BowMastery.getHitExp(4.0, 10, true) < BowMastery.getHitExp(4.0, 10, false));
    assertEquals(10.0, BowMastery.getHitExp(4.0, 10, false) - BowMastery.getHitExp(4.0, 10, true),
        EPS);
  }

  @Test
  void stormDurationIsCapped() {
    WeaponSkillData data = data();
    assertEquals(8.0, BowMastery.getAbilityDuration(data), EPS);
    data.setPassive(BowMastery.STORM_DURATION, 1_000_000);
    assertEquals(14.0, BowMastery.getAbilityDuration(data), EPS);
  }

  // --- Crossbow ---

  @Test
  void blastRadiusGrowsAndIsCapped() {
    WeaponSkillData data = data();
    assertEquals(3.0, CrossbowMastery.getBlastRadius(data), EPS);
    data.setSkill(CrossbowMastery.WIDE_BLAST, 2);
    assertEquals(4.0, CrossbowMastery.getBlastRadius(data), EPS);
    data.setSkill(CrossbowMastery.SIEGE_WRATH, 1);
    data.setSkill(CrossbowMastery.WIDE_BLAST, 100);
    assertEquals(7.0, CrossbowMastery.getBlastRadius(data), EPS);
  }

  @Test
  void blastDamageAddsHeavyChargeAndShrapnel() {
    WeaponSkillData data = data();
    assertEquals(4.0, CrossbowMastery.getBlastDamage(data), EPS);
    data.setSkill(CrossbowMastery.HEAVY_CHARGE, 2);
    data.setSkill(CrossbowMastery.SHRAPNEL, 1);
    assertEquals(4.0 + 2.0 + 3.0, CrossbowMastery.getBlastDamage(data), EPS);
  }

  @Test
  void pointBlankOnlyCountsWithinEightBlocks() {
    WeaponSkillData data = data();
    data.setPassive(CrossbowMastery.POINT_BLANK, 100);
    data.setPassive(CrossbowMastery.BOLT_POWER, 100);
    assertEquals(0.3 + 0.5, CrossbowMastery.getDamageBonus(data, 8), EPS);
    assertEquals(0.3, CrossbowMastery.getDamageBonus(data, 8.1), EPS);
  }

  @Test
  void concussionAndRendScaleWithTheirLevel() {
    WeaponSkillData data = data();
    assertEquals(0.0, CrossbowMastery.getConcussionSeconds(data), EPS);
    data.setSkill(CrossbowMastery.CONCUSSION, 4);
    data.setSkill(CrossbowMastery.REND, 3);
    assertEquals(4.0, CrossbowMastery.getConcussionSeconds(data), EPS);
    assertEquals(6.0, CrossbowMastery.getRendSeconds(data), EPS);
  }

  @Test
  void boltHitExpUsesDamageAndDistance() {
    assertEquals(15 + 5 * 3.0 + 2 * 20, CrossbowMastery.getHitExp(3.0, 20), EPS);
  }
}
