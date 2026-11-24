package mc.carlton.freerpg.elites;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.Test;

class BloodMoonStateTest {

  @Test
  void theRollIsOnlyDueInsideTheNightWindow() {
    assertFalse(BloodMoonManager.due(5, 4, 12999, 13000, 23000));
    assertTrue(BloodMoonManager.due(5, 4, 13000, 13000, 23000));
    assertTrue(BloodMoonManager.due(5, 4, 22999, 13000, 23000));
    assertFalse(BloodMoonManager.due(5, 4, 23000, 13000, 23000));
  }

  @Test
  void theRollHappensAtMostOncePerDay() {
    assertFalse(BloodMoonManager.due(5, 5, 14000, 13000, 23000));
    assertTrue(BloodMoonManager.due(6, 5, 14000, 13000, 23000));
  }

  @Test
  void rollStartHonoursZeroAndOne() {
    Random random = new Random(9);
    for (int i = 0; i < 100; i++) {
      assertFalse(BloodMoonManager.rollStart(0.0, random));
      assertTrue(BloodMoonManager.rollStart(1.0, random));
    }
  }

  @Test
  void rollStartRateFollowsTheChance() {
    Random random = new Random(21);
    int started = 0;
    for (int i = 0; i < 40000; i++) {
      if (BloodMoonManager.rollStart(0.0625, random)) {
        started++;
      }
    }
    assertTrue(Math.abs(started / 40000.0 - 0.0625) < 0.01, "rate " + started);
  }

  @Test
  void itEndsAtTheNextEndTick() {
    assertEquals(24000L * 3 + 23000, BloodMoonManager.nextEnd(24000L * 3 + 13000, 23000));
    // started by an admin in the daytime: ends at the end of the coming night
    assertEquals(24000L * 3 + 23000, BloodMoonManager.nextEnd(24000L * 3 + 6000, 23000));
    // started after the end tick: ends at the end of the next night
    assertEquals(24000L * 4 + 23000, BloodMoonManager.nextEnd(24000L * 3 + 23500, 23000));
    // started exactly on the end tick: a full day later, never instantly
    assertEquals(24000L * 4 + 23000, BloodMoonManager.nextEnd(24000L * 3 + 23000, 23000));
  }

  @Test
  void onlyConfiguredWorldsCanStart() {
    Set<String> worlds = Set.of("world");
    assertTrue(BloodMoonManager.worldAllowed("world", true, worlds));
    assertFalse(BloodMoonManager.worldAllowed("world_nether", true, worlds));
    assertFalse(BloodMoonManager.worldAllowed("world", false, worlds));
  }

  @Test
  void timeLeftIsShownAsMinutesAndSeconds() {
    assertEquals("8:20", BloodMoonVisuals.formatTime(10000));
    assertEquals("0:05", BloodMoonVisuals.formatTime(100));
    assertEquals("0:00", BloodMoonVisuals.formatTime(-50));
  }
}
