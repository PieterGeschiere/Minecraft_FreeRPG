package mc.carlton.freerpg.elites;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** The deferred minors: honest command replies, weather cleanup, ranged damage, death blast. */
class PolishFixesTest {

  // 1. start/stop must say what really happened
  @Test
  void startAndStopRepliesFollowTheResult() {
    assertEquals("bloodMoonStarted", BloodMoonCommands.replyKey("start", true));
    assertEquals("bloodMoonAlreadyActive", BloodMoonCommands.replyKey("start", false));
    assertEquals("bloodMoonStopped", BloodMoonCommands.replyKey("stop", true));
    assertEquals("bloodMoonNotActive", BloodMoonCommands.replyKey("stop", false));
  }

  // 2. only clear weather the Blood Moon itself made
  @Test
  void weatherIsOnlyClearedWhenTheBloodMoonMadeIt() {
    assertTrue(BloodMoonManager.shouldClearWeather(true, false));
    assertFalse(BloodMoonManager.shouldClearWeather(true, true), "it was already storming");
    assertFalse(BloodMoonManager.shouldClearWeather(false, false), "thunder effect is off");
  }

  // 4. a dying elite's death blast is not an attack that applies Burning or Frostbound
  @Test
  void aDyingEliteCannotApplyOnHitAffixes() {
    assertFalse(AffixListener.canAct(true, true, 0.0));
    assertFalse(AffixListener.canAct(true, true, 3.0), "dead flag wins over leftover health");
  }

  // 5. ranged and explosion damage must follow the same multipliers as melee
  @Test
  void meleeDamageIsNotScaledAgainBecauseTheAttributeAlreadyDoesIt() {
    assertEquals(1.0, DamageScaling.factor(false, true, true, 1.5, 0.25), 1e-9);
  }

  @Test
  void rangedAndExplosionDamageUseTheEliteAndBloodMoonMultipliers() {
    assertEquals(1.5, DamageScaling.factor(true, true, false, 1.5, 0.25), 1e-9);
    assertEquals(1.25, DamageScaling.factor(true, false, true, 1.5, 0.25), 1e-9);
    assertEquals(1.875, DamageScaling.factor(true, true, true, 1.5, 0.25), 1e-9);
    assertEquals(1.0, DamageScaling.factor(true, false, false, 1.5, 0.25), 1e-9);
  }
}
