package mc.carlton.freerpg.skills.aeronautics;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

class AeronauticsLogicTest {

  private static final UUID ALICE = new UUID(1, 1);
  private static final UUID BOB = new UUID(2, 2);

  private static FileConfiguration bundled(String name) throws Exception {
    try (InputStream in = AeronauticsLogicTest.class.getClassLoader().getResourceAsStream(name)) {
      return YamlConfiguration.loadConfiguration(
          new InputStreamReader(in, StandardCharsets.UTF_8));
    }
  }

  // ---- config ----

  @Test
  void bundledConfigMatchesTheDesignValues() throws Exception {
    FileConfiguration perk = bundled("perkConfig.yml");
    FileConfiguration advanced = bundled("advancedConfig.yml");
    assertArrayEquals(new double[] {0.10, 0.20, 0.30, 0.40, 0.50},
        AeronauticsConfig.softLandingReductions(perk));
    assertArrayEquals(new int[] {1, 1, 2, 2, 3}, AeronauticsConfig.longBurnPower(perk));
    assertEquals(1, AeronauticsConfig.tailwindPower(perk));
    assertEquals(0.005, AeronauticsConfig.efficiencyPerPoint(perk), 1e-12);
    assertEquals(0.5, AeronauticsConfig.efficiencyCap(advanced), 1e-12);
    assertEquals(50.0, AeronauticsConfig.stepBlocks(advanced), 1e-12);
    assertEquals(30, AeronauticsConfig.maxStepsPerMinute(advanced));
    assertEquals(3, advanced.getInt("aeronautics.expDrops.glideStep"));
  }

  @Test
  void configWithoutAeronauticsFallsBackToDefaults() {
    FileConfiguration empty = new YamlConfiguration();
    assertArrayEquals(AeronauticsConfig.DEFAULT_SOFT_LANDING,
        AeronauticsConfig.softLandingReductions(empty));
    assertArrayEquals(AeronauticsConfig.DEFAULT_LONG_BURN, AeronauticsConfig.longBurnPower(empty));
    assertEquals(AeronauticsConfig.DEFAULT_TAILWIND_POWER, AeronauticsConfig.tailwindPower(empty));
    assertEquals(AeronauticsConfig.DEFAULT_EFFICIENCY_PER_POINT,
        AeronauticsConfig.efficiencyPerPoint(empty));
    assertEquals(AeronauticsConfig.DEFAULT_STEP_BLOCKS, AeronauticsConfig.stepBlocks(empty));
    assertEquals(AeronauticsConfig.DEFAULT_MAX_STEPS_PER_MINUTE,
        AeronauticsConfig.maxStepsPerMinute(empty));
    // a missing file (null) must not throw either
    assertArrayEquals(AeronauticsConfig.DEFAULT_LONG_BURN, AeronauticsConfig.longBurnPower(null));
    assertEquals(AeronauticsConfig.DEFAULT_EFFICIENCY_CAP, AeronauticsConfig.efficiencyCap(null));
  }

  @Test
  void aNonPositiveStepSizeIsIgnored() {
    FileConfiguration cfg = new YamlConfiguration();
    cfg.set("aeronautics.stepBlocks", 0);
    assertEquals(AeronauticsConfig.DEFAULT_STEP_BLOCKS, AeronauticsConfig.stepBlocks(cfg));
  }

  // ---- Glide Efficiency ----

  @Test
  void durabilityMultiplierIsCapped() {
    assertEquals(1.0, AeronauticsRules.durabilityMultiplier(0, 0.005, 0.5), 1e-12);
    assertEquals(0.875, AeronauticsRules.durabilityMultiplier(25, 0.005, 0.5), 1e-12);
    assertEquals(0.5, AeronauticsRules.durabilityMultiplier(10_000, 0.005, 0.5), 1e-12);
    assertEquals(1.0, AeronauticsRules.durabilityMultiplier(-5, 0.005, 0.5), 1e-12);
  }

  @Test
  void efficiencyLoreShowsTheSavedShare() {
    assertEquals("0.0%", AeronauticsRules.efficiencyLore(0, 0.005, 0.5));
    assertEquals("12.5%", AeronauticsRules.efficiencyLore(25, 0.005, 0.5));
    assertEquals("50.0%", AeronauticsRules.efficiencyLore(10_000, 0.005, 0.5));
  }

  // ---- Soft Landing ----

  @Test
  void softLandingReducesWallDamage() {
    double[] table = {0.10, 0.20, 0.30, 0.40, 0.50};
    assertEquals(10.0, AeronauticsRules.damageAfterSoftLanding(10.0, table, 0), 1e-12);
    assertEquals(9.0, AeronauticsRules.damageAfterSoftLanding(10.0, table, 1), 1e-12);
    assertEquals(5.0, AeronauticsRules.damageAfterSoftLanding(10.0, table, 5), 1e-12);
    assertEquals(5.0, AeronauticsRules.damageAfterSoftLanding(10.0, table, 99), 1e-12);
    assertEquals(10.0, AeronauticsRules.damageAfterSoftLanding(10.0, null, 3), 1e-12);
    assertEquals(10.0, AeronauticsRules.damageAfterSoftLanding(10.0, table, -2), 1e-12);
  }

  // ---- Long Burn and Tailwind ----

  @Test
  void extraRocketPower() {
    int[] table = {1, 1, 2, 2, 3};
    assertEquals(0, AeronauticsRules.extraRocketPower(table, 0, false, true, 1));
    assertEquals(2, AeronauticsRules.extraRocketPower(table, 3, false, true, 1));
    assertEquals(3, AeronauticsRules.extraRocketPower(table, 99, false, false, 1));
    // Tailwind only helps the first rocket after takeoff, and only once unlocked
    assertEquals(3, AeronauticsRules.extraRocketPower(table, 3, true, true, 1));
    assertEquals(2, AeronauticsRules.extraRocketPower(table, 3, true, false, 1));
    assertEquals(1, AeronauticsRules.extraRocketPower(table, 0, true, true, 1));
    assertEquals(2, AeronauticsRules.extraRocketPower(table, 3, false, true, 1));
  }

  @Test
  void rocketPowerNeverExceedsTheFireworkLimit() {
    assertEquals(4, AeronauticsRules.boostedPower(3, 1));
    assertEquals(3, AeronauticsRules.boostedPower(3, 0));
    assertEquals(127, AeronauticsRules.boostedPower(126, 50));
    assertEquals(0, AeronauticsRules.boostedPower(-4, -2));
  }

  // ---- gliding experience ----

  @Test
  void distanceTurnsIntoStepsAndKeepsTheRemainder() {
    GlideXpRules rules = new GlideXpRules(50, 30);
    assertEquals(0, rules.addDistance(ALICE, 30, 0));
    assertEquals(1, rules.addDistance(ALICE, 30, 100)); // 60 total -> one step, 10 left over
    assertEquals(0, rules.addDistance(ALICE, 30, 200)); // 40
    assertEquals(1, rules.addDistance(ALICE, 30, 300)); // 70 -> one step, 20 left over
    assertEquals(3, rules.addDistance(ALICE, 130, 400)); // 150 -> three steps
    assertEquals(0, rules.addDistance(ALICE, 0, 500));
    assertEquals(0, rules.addDistance(ALICE, -10, 500));
  }

  @Test
  void stepsAreCappedPerMinuteAndResetAfterwards() {
    GlideXpRules rules = new GlideXpRules(50, 30);
    assertEquals(30, rules.addDistance(ALICE, 50 * 40, 1_000)); // wanted 40 steps, only 30 allowed
    assertEquals(0, rules.addDistance(ALICE, 500, 2_000)); // still inside the same minute
    assertEquals(10, rules.addDistance(ALICE, 500, 1_000 + 60_000)); // new minute
    // another player has their own allowance
    assertEquals(4, rules.addDistance(BOB, 200, 2_000));
  }

  @Test
  void landingForgetsTheUnfinishedStep() {
    GlideXpRules rules = new GlideXpRules(50, 30);
    rules.addDistance(ALICE, 40, 0);
    rules.reset(ALICE);
    assertEquals(0, rules.addDistance(ALICE, 40, 10)); // not 80 -> would have been 1 step
  }
}
