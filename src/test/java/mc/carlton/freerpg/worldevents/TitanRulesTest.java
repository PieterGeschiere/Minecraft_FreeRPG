package mc.carlton.freerpg.worldevents;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TitanRulesTest {

  @BeforeEach
  void empty() {
    WorldEventConfig.loadFrom(new YamlConfiguration());
  }

  @Test
  void theScaleStaysInsideTheBoundsAndVaries() {
    Random random = new Random(4);
    Set<Double> seen = new HashSet<>();
    for (int i = 0; i < 1000; i++) {
      double scale = TitanRules.pickScale(1.4, 2.2, random);
      assertTrue(scale >= 1.4 && scale <= 2.2, "scale " + scale);
      seen.add(scale);
    }
    assertTrue(seen.size() > 10);
  }

  @Test
  void swappedOrEqualBoundsStillGiveAValidScale() {
    Random random = new Random(4);
    for (int i = 0; i < 200; i++) {
      double swapped = TitanRules.pickScale(2.0, 1.0, random);
      assertTrue(swapped >= 1.0 && swapped <= 2.0);
    }
    assertEquals(1.5, TitanRules.pickScale(1.5, 1.5, random));
    assertEquals(1.0, TitanRules.pickScale(0.2, 0.5, random), "never smaller than normal");
  }

  @Test
  void aMobNeverGrowsIntoTheCeiling() {
    assertEquals(2.2, TitanRules.fitScale(2.2, 1.95, 10, 1.25));
    assertEquals(4 / (1.95 * 1.25), TitanRules.fitScale(2.2, 1.95, 4, 1.25), 1e-9);
    assertEquals(1.0, TitanRules.fitScale(2.2, 1.95, 2, 1.25), "never shrinks below normal");
    assertEquals(1.0, TitanRules.fitScale(2.2, 1.95, 0, 1.25));
  }

  @Test
  void anUnknownHeightDoesNotDivideByZero() {
    assertDoesNotThrow(() -> TitanRules.fitScale(2.2, 0.0, 5, 1.25));
    assertEquals(1.0, TitanRules.fitScale(2.2, 0.0, 5, 1.25));
    assertEquals(1.0, TitanRules.fitScale(2.2, 1.95, 5, 0.0));
  }

  @Test
  void aBarelyBiggerMobIsLeftAlone() {
    assertFalse(TitanRules.worthScaling(1.14));
    assertTrue(TitanRules.worthScaling(1.15));
  }

  @Test
  void biggerMeansToughButNeverWeaker() {
    assertEquals(3.0, TitanRules.healthFactor(2.0, 2.0));
    assertEquals(1.5, TitanRules.damageFactor(2.0, 0.5));
    assertEquals(1.0, TitanRules.healthFactor(1.0, 2.0));
    assertEquals(1.0, TitanRules.healthFactor(0.5, 2.0));
    assertEquals(1.0, TitanRules.damageFactor(2.0, -1.0));
  }

  @Test
  void titanNightDoesNotMultiplyAllCombatExpBecauseTheBonusIsPerKill() {
    TitanNightEvent event = new TitanNightEvent();
    assertEquals(WorldEventType.TITAN_NIGHT, event.type());
    assertEquals(1.0, event.xpMultiplier("swordsmanship"));
    assertEquals(1.0, event.xpMultiplier("defense"));
    assertTrue(event.canStart());
  }

  @Test
  void theKillBonusIsTheExtraPartOfTheMultiplier() {
    assertEquals(10, TitanRules.bonusExp(20, 1.5));
    assertEquals(0, TitanRules.bonusExp(0, 1.5));
    assertEquals(0, TitanRules.bonusExp(20, 1.0));
    assertEquals(0, TitanRules.bonusExp(20, 0.5));
    assertEquals(1.5, TitanRules.killExpMultiplier());
  }

  @Test
  void aMobMustFitWithItsWidthAndHeightAndTheEliteReserve() {
    // a 0.6 wide, 1.95 high mob; the room is 1.0 wide and 3.0 high; elite reserve 1.25
    java.util.function.BiPredicate<Double, Double> room = (w, h) -> w <= 1.0 && h <= 3.0;
    double fitted = TitanRules.fitBox(2.2, 0.6, 1.95, 1.25, room);
    assertTrue(fitted > 1.1 && fitted <= 1.25, "fitted " + fitted);
    assertTrue(room.test(0.6 * fitted * 1.25, 1.95 * fitted * 1.25));
  }

  @Test
  void aMobThatHasRoomEverywhereGetsItsWholeSize() {
    assertEquals(2.2, TitanRules.fitBox(2.2, 0.6, 1.95, 1.25, (w, h) -> true));
  }

  @Test
  void aMobThatFitsNowhereStaysNormalSized() {
    assertEquals(1.0, TitanRules.fitBox(2.2, 0.6, 1.95, 1.25, (w, h) -> false));
    assertEquals(1.0, TitanRules.fitBox(2.2, 0.0, 1.95, 1.25, (w, h) -> true),
        "an unknown width means no growth");
  }

  @Test
  void mobsWithRidersOrMountsAreLeftAlone() {
    assertTrue(TitanRules.skipBecauseOfRiders(true, false));
    assertTrue(TitanRules.skipBecauseOfRiders(false, true));
    assertFalse(TitanRules.skipBecauseOfRiders(false, false));
  }

  @Test
  void theExcludedTypesHaveDefaultsAndIgnoreUnknownNames() {
    assertTrue(TitanSpawnListener.excludedTypes().contains(EntityType.SLIME));
    assertTrue(TitanSpawnListener.excludedTypes().contains(EntityType.WARDEN));
    assertFalse(TitanSpawnListener.excludedTypes().contains(EntityType.ZOMBIE));
    YamlConfiguration yaml = new YamlConfiguration();
    yaml.set("events.titanNight.excludedTypes", java.util.List.of("CREEPER", "NOT_A_MOB"));
    WorldEventConfig.loadFrom(yaml);
    assertEquals(Set.of(EntityType.CREEPER), TitanSpawnListener.excludedTypes());
  }
}
