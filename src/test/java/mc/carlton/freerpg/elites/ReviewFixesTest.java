package mc.carlton.freerpg.elites;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.bukkit.util.Vector;
import org.junit.jupiter.api.Test;

/** One test (or more) for every Critical / Important finding of the whole-branch review. */
class ReviewFixesTest {

  // Finding 1: minions of Splitting and Summoner must not be a loot or exp source
  @Test
  void minionsGiveNoLootOrVanillaExp() {
    assertTrue(EliteRewards.suppressLoot(true));
    assertFalse(EliteRewards.suppressLoot(false));
  }

  // Finding 2: a dying elite must not act (Vampiric healing a dead mob brings it back)
  @Test
  void aDeadOrInvalidElitePerformsNoAffixActions() {
    assertTrue(AffixListener.canAct(false, true, 10.0));
    assertFalse(AffixListener.canAct(true, true, 10.0));
    assertFalse(AffixListener.canAct(false, false, 10.0));
    assertFalse(AffixListener.canAct(false, true, 0.0));
  }

  // Finding 3 (a Blood Moon ends when time jumps back or after a day of server time) is now
  // covered for every world event by worldevents.EventRulesTest.

  // Finding 4: the red edge must not be a wall the player can see or reach
  @Test
  void theRedEdgeBorderIsFarOutsideTheRenderDistance() {
    assertTrue(BloodMoonVisuals.EDGE_HALF_SIZE > 32 * 16, "farther than a 32 chunk render distance");
    assertTrue(BloodMoonVisuals.edgeIntensity() >= 0.5, "still a clearly red screen edge");
  }

  // Finding 5: Swift must never produce a NaN velocity (target straight above)
  @Test
  void swiftLungeIsFiniteWhenTheTargetIsStraightAbove() {
    Vector lunge = mc.carlton.freerpg.elites.affix.SwiftMath.lunge(new Vector(0, 64, 0),
        new Vector(0, 70, 0));
    assertTrue(Double.isFinite(lunge.getX()) && Double.isFinite(lunge.getY())
        && Double.isFinite(lunge.getZ()));
    assertEquals(0.0, lunge.getX(), 1e-9);
  }

  @Test
  void swiftLungeLeansTowardsTheTarget() {
    Vector lunge = mc.carlton.freerpg.elites.affix.SwiftMath.lunge(new Vector(0, 64, 0),
        new Vector(5, 64, 0));
    assertTrue(lunge.getX() > 0.5);
    assertTrue(lunge.getY() > 0.0);
  }

  // Finding 6: elite rewards at most once per chunk per cooldown
  @Test
  void aChunkPaysOutOnlyOncePerCooldown() {
    RewardCooldown cooldown = new RewardCooldown();
    assertTrue(cooldown.tryClaim("w:1:2", 1_000, 120_000));
    assertFalse(cooldown.tryClaim("w:1:2", 60_000, 120_000));
    assertTrue(cooldown.tryClaim("w:1:3", 60_000, 120_000), "another chunk is separate");
    assertTrue(cooldown.tryClaim("w:1:2", 121_001, 120_000), "after the cooldown it pays again");
  }

  @Test
  void aZeroCooldownAlwaysPays() {
    RewardCooldown cooldown = new RewardCooldown();
    assertTrue(cooldown.tryClaim("w:0:0", 5, 0));
    assertTrue(cooldown.tryClaim("w:0:0", 6, 0));
  }
}
