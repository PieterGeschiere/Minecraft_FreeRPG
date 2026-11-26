package mc.carlton.freerpg.worldevents;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.junit.jupiter.api.Test;

class DoubleTroubleRulesTest {

  @Test
  void onlyNaturalMonstersGetATwin() {
    assertTrue(DoubleTroubleRules.shouldTwin(SpawnReason.NATURAL, true, false, 5, 30));
    assertFalse(DoubleTroubleRules.shouldTwin(SpawnReason.SPAWNER, true, false, 5, 30));
    assertFalse(DoubleTroubleRules.shouldTwin(SpawnReason.CUSTOM, true, false, 5, 30));
    assertFalse(DoubleTroubleRules.shouldTwin(SpawnReason.NATURAL, false, false, 5, 30),
        "animals do not get twins");
  }

  @Test
  void aTwinNeverGetsATwinOfItsOwn() {
    assertFalse(DoubleTroubleRules.shouldTwin(SpawnReason.NATURAL, true, true, 5, 30));
  }

  @Test
  void aCrowdedChunkGetsNoMoreTwins() {
    assertTrue(DoubleTroubleRules.shouldTwin(SpawnReason.NATURAL, true, false, 29, 30));
    assertFalse(DoubleTroubleRules.shouldTwin(SpawnReason.NATURAL, true, false, 30, 30));
    assertFalse(DoubleTroubleRules.shouldTwin(SpawnReason.NATURAL, true, false, 99, 30));
    assertFalse(DoubleTroubleRules.shouldTwin(SpawnReason.NATURAL, true, false, 0, 0),
        "a cap of 0 switches twins off");
  }

  @Test
  void twinsGiveNoLoot() {
    assertTrue(DoubleTroubleRules.twinHasNoLoot(true));
    assertFalse(DoubleTroubleRules.twinHasNoLoot(false));
  }

  @Test
  void swiftMobsAreFasterByTheBonusAndNeverSlower() {
    assertEquals(1.3, DoubleTroubleRules.speedFactor(0.3), 1e-9);
    assertEquals(1.0, DoubleTroubleRules.speedFactor(0.0));
    assertEquals(1.0, DoubleTroubleRules.speedFactor(-0.5));
  }

  @Test
  void aGlowFromAnEarlierNightIsClearedWhenTheMobLoads() {
    assertTrue(MutatorMobListener.shouldClearGlowOnLoad(true, false));
    assertFalse(MutatorMobListener.shouldClearGlowOnLoad(true, true), "the twist is running");
    assertFalse(MutatorMobListener.shouldClearGlowOnLoad(false, false), "not our glow");
  }
}
