package mc.carlton.freerpg.worldevents;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.Test;

class EventRulesTest {

  @Test
  void theRollIsOnlyDueInsideTheNightWindow() {
    assertFalse(EventRules.due(5, 4, 12999, 13000, 23000));
    assertTrue(EventRules.due(5, 4, 13000, 13000, 23000));
    assertTrue(EventRules.due(5, 4, 22999, 13000, 23000));
    assertFalse(EventRules.due(5, 4, 23000, 13000, 23000));
  }

  @Test
  void theRollHappensAtMostOncePerDay() {
    assertFalse(EventRules.due(5, 5, 14000, 13000, 23000));
    assertTrue(EventRules.due(6, 5, 14000, 13000, 23000));
  }

  @Test
  void rollStartHonoursZeroAndOne() {
    Random random = new Random(9);
    for (int i = 0; i < 100; i++) {
      assertFalse(EventRules.rollStart(0.0, random));
      assertTrue(EventRules.rollStart(1.0, random));
    }
  }

  @Test
  void rollStartRateFollowsTheChance() {
    Random random = new Random(21);
    int started = 0;
    for (int i = 0; i < 40000; i++) {
      if (EventRules.rollStart(0.0625, random)) {
        started++;
      }
    }
    assertTrue(Math.abs(started / 40000.0 - 0.0625) < 0.01, "rate " + started);
  }

  @Test
  void itEndsAtTheNextEndTick() {
    assertEquals(24000L * 3 + 23000, EventRules.nextEnd(24000L * 3 + 13000, 23000));
    // started by an admin in the daytime: ends at the end of the coming night
    assertEquals(24000L * 3 + 23000, EventRules.nextEnd(24000L * 3 + 6000, 23000));
    // started after the end tick: ends at the end of the next night
    assertEquals(24000L * 4 + 23000, EventRules.nextEnd(24000L * 3 + 23500, 23000));
    // started exactly on the end tick: a full day later, never instantly
    assertEquals(24000L * 4 + 23000, EventRules.nextEnd(24000L * 3 + 23000, 23000));
  }

  @Test
  void onlyConfiguredWorldsCanStart() {
    Set<String> worlds = Set.of("world");
    assertTrue(EventRules.worldAllowed("world", true, worlds));
    assertFalse(EventRules.worldAllowed("world_nether", true, worlds));
    assertFalse(EventRules.worldAllowed("world", false, worlds));
  }

  @Test
  void aTimeOfDayEventEndsWhenItsTimeComesOrTheClockGoesBack() {
    assertFalse(EventRules.shouldEnd(14000, 13000, 23000, 10, 2400));
    assertTrue(EventRules.shouldEnd(23000, 13000, 23000, 10, 2400));
    assertTrue(EventRules.shouldEnd(1000, 500000, 510000, 10, 2400));
  }

  @Test
  void anyEventEndsAtTheBeatCap() {
    assertFalse(EventRules.shouldEnd(13000, 13000, 23000, 2399, 2400));
    assertTrue(EventRules.shouldEnd(13000, 13000, 23000, 2400, 2400));
  }

  @Test
  void aDurationEventEndsOnlyByBeats() {
    assertFalse(EventRules.shouldEnd(5, 100, -1, 1199, 1200));
    assertTrue(EventRules.shouldEnd(5, 100, -1, 1200, 1200));
    // the clock being earlier than the start does not end a duration event
    assertFalse(EventRules.shouldEnd(5, 100, -1, 10, 1200));
  }

  @Test
  void theStartTickIsFixedOrRandomInsideTheWindow() {
    Random random = new Random(3);
    assertEquals(13000, EventRules.pickStartTick(13000, 23000, false, random));
    Set<Integer> seen = new HashSet<>();
    for (int i = 0; i < 1000; i++) {
      int tick = EventRules.pickStartTick(13000, 23000, true, random);
      assertTrue(tick >= 13000 && tick < 23000, "tick " + tick);
      seen.add(tick);
    }
    assertTrue(seen.size() > 1);
  }

  @Test
  void aPendingStartIsReachedInsideTheWindowOnly() {
    assertFalse(EventRules.startReached(12999, 13000, 23000));
    assertTrue(EventRules.startReached(13000, 13000, 23000));
    assertFalse(EventRules.startReached(23000, 13000, 23000));
  }

  @Test
  void durationsAreCountedInBeats() {
    assertEquals(1200, EventRules.durationBeats(600));
  }

  @Test
  void theGapBetweenEventsIsCountedInBeats() {
    assertTrue(EventRules.gapElapsed(100, -1, 2));
    assertFalse(EventRules.gapElapsed(4799 + 1000, 1000, 2));
    assertTrue(EventRules.gapElapsed(4800 + 1000, 1000, 2));
    assertTrue(EventRules.gapElapsed(1500, 1000, 0));
  }
}
