package mc.carlton.freerpg.weaponskills;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bukkit.Particle;
import org.junit.jupiter.api.Test;

/** The parts of the sounds and visuals that need no running server. */
class EffectsTest {

  private static boolean has(List<ArrowTrail.Layer> layers, Particle particle) {
    for (ArrowTrail.Layer layer : layers) {
      if (layer.particle == particle) {
        return true;
      }
    }
    return false;
  }

  // --- Arrow trails

  @Test
  void aBowWithoutPerksHasABasicTrail() {
    List<ArrowTrail.Layer> layers = ArrowTrail.layers(WeaponSkill.BOW, new WeaponSkillData(),
        false);
    assertEquals(2, layers.size());
    assertTrue(has(layers, Particle.DUST));
    assertTrue(has(layers, Particle.CRIT));
  }

  @Test
  void flamesOnlyFollowTheExtraArrowsOfFlamingVolley() {
    WeaponSkillData data = new WeaponSkillData();
    data.setSkill(BowMastery.FLAMING_VOLLEY, 1);
    assertTrue(has(ArrowTrail.layers(WeaponSkill.BOW, data, true), Particle.FLAME));
    assertFalse(has(ArrowTrail.layers(WeaponSkill.BOW, data, false), Particle.FLAME));
  }

  private static void assertFalse(boolean value) {
    org.junit.jupiter.api.Assertions.assertFalse(value);
  }

  @Test
  void markedPreyAddsAGlowAndPinningShotAddsIce() {
    WeaponSkillData data = new WeaponSkillData();
    data.setSkill(BowMastery.MARKED_PREY, 1);
    assertTrue(has(ArrowTrail.layers(WeaponSkill.BOW, data, false), Particle.END_ROD));
    data.setSkill(BowMastery.PINNING_SHOT, 2);
    List<ArrowTrail.Layer> layers = ArrowTrail.layers(WeaponSkill.BOW, data, false);
    assertTrue(has(layers, Particle.SNOWFLAKE));
    assertEquals(5, layers.size()); // base 2 + glow 1 + ice dust and snowflake 2
  }

  @Test
  void everyBowPerkAddsALayer() {
    WeaponSkillData data = new WeaponSkillData();
    int before = ArrowTrail.layers(WeaponSkill.BOW, data, true).size();
    for (String skill : new String[]{BowMastery.FLAMING_VOLLEY, BowMastery.MARKED_PREY,
        BowMastery.PINNING_SHOT, BowMastery.FLEET_FOOT, BowMastery.HAWKEYES_WRATH}) {
      data.setSkill(skill, 1);
      int now = ArrowTrail.layers(WeaponSkill.BOW, data, true).size();
      assertTrue(now > before, skill + " adds no layer");
      before = now;
    }
  }

  @Test
  void aCrossbowStartsWithOneLayerAndEveryPerkAddsOne() {
    WeaponSkillData data = new WeaponSkillData();
    assertEquals(1, ArrowTrail.layers(WeaponSkill.CROSSBOW, data, false).size());
    int expected = 1;
    for (String skill : new String[]{CrossbowMastery.CONCUSSION, CrossbowMastery.REND,
        CrossbowMastery.SHRAPNEL, CrossbowMastery.HEAVY_CHARGE, CrossbowMastery.SIEGE_WRATH}) {
      data.setSkill(skill, 1);
      expected++;
      assertEquals(expected, ArrowTrail.layers(WeaponSkill.CROSSBOW, data, false).size(), skill);
    }
  }

  @Test
  void crossbowPerkColorsDifferFromEachOther() {
    WeaponSkillData data = new WeaponSkillData();
    data.setSkill(CrossbowMastery.CONCUSSION, 1);
    data.setSkill(CrossbowMastery.REND, 1);
    List<ArrowTrail.Layer> layers = ArrowTrail.layers(WeaponSkill.CROSSBOW, data, false);
    assertNotEquals(layers.get(1).color, layers.get(2).color);
    assertNotEquals(layers.get(0).color, layers.get(1).color);
  }

  @Test
  void otherSkillsHaveNoArrowTrail() {
    assertTrue(ArrowTrail.layers(WeaponSkill.MACE, new WeaponSkillData(), false).isEmpty());
    assertTrue(ArrowTrail.layers(WeaponSkill.SPEAR, new WeaponSkillData(), false).isEmpty());
  }

  // --- Colors, boss bar, damage numbers

  @Test
  void everySkillHasItsOwnColorAndBarColor() {
    Set<Object> colors = new HashSet<>();
    Set<Object> barColors = new HashSet<>();
    for (WeaponSkill skill : WeaponSkill.values()) {
      assertNotNull(AbilityEffects.color(skill));
      colors.add(AbilityEffects.color(skill));
      barColors.add(AbilityEffects.barColor(skill));
    }
    assertEquals(WeaponSkill.values().length, colors.size());
    assertEquals(WeaponSkill.values().length, barColors.size());
  }

  @Test
  void bossBarTitleShowsNameShotsAndSeconds() {
    String title = AbilityEffects.title(WeaponSkill.BOW, 3, 7_250);
    assertTrue(title.contains("Arrow Storm"));
    assertTrue(title.contains("●●●"));
    assertTrue(title.contains("7.2s") || title.contains("7.3s"));
    assertTrue(AbilityEffects.title(WeaponSkill.CROSSBOW, 0, 0).contains("0.0s"));
    assertFalse(AbilityEffects.title(WeaponSkill.CROSSBOW, 0, 0).contains("●"));
  }

  @Test
  void damageNumbersShowWholeNumbersWithoutDecimals() {
    assertEquals("-6", AbilityEffects.damageText(6.0));
    assertEquals("-4.5", AbilityEffects.damageText(4.5));
    assertEquals("-3.3", AbilityEffects.damageText(3.26));
    assertEquals("-12", AbilityEffects.damageText(11.96));
  }
}
