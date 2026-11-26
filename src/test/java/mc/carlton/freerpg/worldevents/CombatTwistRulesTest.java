package mc.carlton.freerpg.worldevents;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;
import org.junit.jupiter.api.Test;

class CombatTwistRulesTest {

  @Test
  void glassCannonDoublesPlayerVersusMobButNotPlayerVersusPlayer() {
    // glassCannonFactor(attackerIsPlayer, attackerIsMob, victimIsPlayer, victimIsMob, multiplier)
    assertEquals(2.0, CombatTwistRules.glassCannonFactor(true, false, false, true, 2.0));
    assertEquals(2.0, CombatTwistRules.glassCannonFactor(false, true, true, false, 2.0));
    assertEquals(1.0, CombatTwistRules.glassCannonFactor(true, false, true, false, 2.0),
        "duels are unchanged");
    assertEquals(1.0, CombatTwistRules.glassCannonFactor(false, true, false, true, 2.0),
        "mob versus mob");
  }

  @Test
  void glassCannonIgnoresDamageThatIsNeitherFromNorToAMob() {
    // lightning, falling blocks or an unowned TNT against a player
    assertEquals(1.0, CombatTwistRules.glassCannonFactor(false, false, true, false, 2.0));
    // a player hitting an armor stand or a boat
    assertEquals(1.0, CombatTwistRules.glassCannonFactor(true, false, false, false, 2.0));
  }

  @Test
  void glassCannonNeverMakesDamageSmaller() {
    assertEquals(1.0, CombatTwistRules.glassCannonFactor(true, false, false, true, 0.5));
    assertEquals(1.0, CombatTwistRules.glassCannonFactor(true, false, false, true, -3.0));
  }

  @Test
  void explodingLootNeedsAPlayerKillOfARealHostileMob() {
    assertTrue(CombatTwistRules.explodingLootApplies(true, true, false, false));
    assertFalse(CombatTwistRules.explodingLootApplies(true, true, true, false), "spawner mob");
    assertFalse(CombatTwistRules.explodingLootApplies(false, true, false, false), "no player kill");
    assertFalse(CombatTwistRules.explodingLootApplies(true, false, false, false), "not hostile");
    assertFalse(CombatTwistRules.explodingLootApplies(true, true, false, true), "a twin");
  }

  @Test
  void theExplosionChanceHonoursZeroAndOne() {
    Random random = new Random(3);
    for (int i = 0; i < 100; i++) {
      assertFalse(CombatTwistRules.shouldExplode(0.0, random));
      assertTrue(CombatTwistRules.shouldExplode(1.0, random));
    }
  }
}
