package mc.carlton.freerpg.party;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class WarmupTest {

  @Test
  void warmupFinishesAfterFiveSeconds() {
    Warmup warmup = new Warmup(10_000, 5);
    assertEquals(Warmup.State.RUNNING, warmup.tick(10_000, false, false));
    assertEquals(Warmup.State.RUNNING, warmup.tick(14_999, false, false));
    assertEquals(Warmup.State.DONE, warmup.tick(15_000, false, false));
  }

  @Test
  void blockMovementCancels() {
    Warmup warmup = new Warmup(0, 5);
    assertEquals(Warmup.State.CANCELLED, warmup.tick(2_000, true, false));
  }

  @Test
  void damageCancels() {
    Warmup warmup = new Warmup(0, 5);
    assertEquals(Warmup.State.CANCELLED, warmup.tick(2_000, false, true));
  }

  @Test
  void headTurnOnlyDoesNotCancel() {
    // head rotation is not an input of tick: no movement and no damage keeps it running
    Warmup warmup = new Warmup(0, 5);
    for (long t = 0; t < 5_000; t += 250) {
      assertEquals(Warmup.State.RUNNING, warmup.tick(t, false, false));
    }
  }

  @Test
  void cancelledStaysCancelled() {
    Warmup warmup = new Warmup(0, 5);
    warmup.tick(1_000, true, false);
    assertEquals(Warmup.State.CANCELLED, warmup.tick(9_000, false, false));
  }

  @Test
  void secondsLeftCountsDownFiveToOne() {
    Warmup warmup = new Warmup(0, 5);
    assertEquals(5, warmup.secondsLeft(0));
    assertEquals(5, warmup.secondsLeft(999));
    assertEquals(4, warmup.secondsLeft(1_000));
    assertEquals(1, warmup.secondsLeft(4_999));
    assertEquals(0, warmup.secondsLeft(5_000));
    assertEquals(0, warmup.secondsLeft(9_000));
  }
}
