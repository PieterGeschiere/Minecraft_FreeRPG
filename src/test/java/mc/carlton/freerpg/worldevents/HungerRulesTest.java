package mc.carlton.freerpg.worldevents;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import org.junit.jupiter.api.Test;

class HungerRulesTest {

  @Test
  void foodIsOnlyBoostedWhenItGoesUp() {
    assertEquals(10, HungerRules.boostedFood(10, 10, 1.5));
    assertEquals(8, HungerRules.boostedFood(10, 8, 1.5), "losing food is not changed");
  }

  @Test
  void theGainIsMultipliedAndRoundedUp() {
    assertEquals(13, HungerRules.boostedFood(10, 12, 1.5));
    assertEquals(16, HungerRules.boostedFood(10, 14, 1.5));
    assertEquals(4, HungerRules.boostedFood(0, 4, 1.0));
  }

  @Test
  void foodNeverGoesAboveTwenty() {
    assertEquals(20, HungerRules.boostedFood(18, 20, 1.5));
    assertEquals(20, HungerRules.boostedFood(19, 20, 3.0));
  }

  @Test
  void aMultiplierBelowOneCountsAsOne() {
    assertEquals(14, HungerRules.boostedFood(10, 14, 0.5));
  }

  @Test
  void everyLowGravityPartHasItsOwnModifierKey() {
    var suffixes = MutatorPlayerEffects.partSuffixes();
    assertEquals(3, suffixes.size());
    assertEquals(3, new HashSet<>(suffixes).size());
  }

  @Test
  void theExtraHungerStopsBeforeItCanStarveAnyone() {
    assertTrue(HungerRules.shouldDrain(20, 6));
    assertTrue(HungerRules.shouldDrain(7, 6));
    assertFalse(HungerRules.shouldDrain(6, 6));
    assertFalse(HungerRules.shouldDrain(0, 6));
  }

  @Test
  void theHungerSettingsHaveGentleDefaults() {
    org.bukkit.configuration.file.YamlConfiguration yaml =
        new org.bukkit.configuration.file.YamlConfiguration();
    WorldEventConfig.loadFrom(yaml);
    assertEquals(0.05, HungerRules.exhaustionPerBeat());
    assertEquals(6, HungerRules.minFood());
    yaml.set("events.mutatorNight.mutators.hungryNight.exhaustionPerBeat", 9.0);
    yaml.set("events.mutatorNight.mutators.hungryNight.minFood", 99);
    assertEquals(1.0, HungerRules.exhaustionPerBeat(), "clamped");
    assertEquals(20, HungerRules.minFood(), "clamped");
  }
}
