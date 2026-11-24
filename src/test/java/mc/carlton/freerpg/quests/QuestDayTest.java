package mc.carlton.freerpg.quests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class QuestDayTest {

  private static final ZoneId BRUSSELS = ZoneId.of("Europe/Brussels");

  private static long millis(String dateTime, ZoneId zone) {
    return LocalDateTime.parse(dateTime).atZone(zone).toInstant().toEpochMilli();
  }

  @Test
  void midnightResetSwitchesDate() {
    assertEquals("2026-11-23", QuestDay.keyFor(millis("2026-11-23T23:59:59", BRUSSELS), BRUSSELS, 0));
    assertEquals("2026-11-24", QuestDay.keyFor(millis("2026-11-24T00:00:00", BRUSSELS), BRUSSELS, 0));
  }

  @Test
  void resetHourThreeKeepsEarlyMorningInPreviousDay() {
    assertEquals("2026-11-23", QuestDay.keyFor(millis("2026-11-24T02:59:59", BRUSSELS), BRUSSELS, 3));
    assertEquals("2026-11-24", QuestDay.keyFor(millis("2026-11-24T03:00:00", BRUSSELS), BRUSSELS, 3));
  }

  @Test
  void sameInstantDifferentZonesDifferentKeys() {
    long instant = millis("2026-11-23T23:30:00", ZoneId.of("UTC"));
    assertEquals("2026-11-23", QuestDay.keyFor(instant, ZoneId.of("UTC"), 0));
    assertEquals("2026-11-24", QuestDay.keyFor(instant, ZoneId.of("Asia/Tokyo"), 0));
  }

  @Test
  void dstChangeDayStillGivesOneKeyPerDay() {
    List<String> keys = new ArrayList<>();
    ZonedDateTime time = ZonedDateTime.of(2026, 3, 29, 0, 0, 0, 0, BRUSSELS);
    ZonedDateTime end = time.plusDays(1);
    while (time.isBefore(end)) {
      String key = QuestDay.keyFor(time.toInstant().toEpochMilli(), BRUSSELS, 0);
      if (keys.isEmpty() || !keys.get(keys.size() - 1).equals(key)) {
        keys.add(key);
      }
      time = time.plusHours(1);
    }
    assertEquals(List.of("2026-03-29"), keys);
    long nextDay = time.toInstant().toEpochMilli();
    assertEquals("2026-03-30", QuestDay.keyFor(nextDay, BRUSSELS, 0));
  }

  @Test
  void isCurrentFalseForNullOrGarbageKey() {
    long now = millis("2026-11-23T12:00:00", BRUSSELS);
    assertFalse(QuestDay.isCurrent(null, now, BRUSSELS, 0));
    assertFalse(QuestDay.isCurrent("garbage", now, BRUSSELS, 0));
    assertFalse(QuestDay.isCurrent("2026-11-22", now, BRUSSELS, 0));
    assertTrue(QuestDay.isCurrent("2026-11-23", now, BRUSSELS, 0));
  }

  @Test
  void resetHourOutOfRangeIsClamped() {
    long instant = millis("2026-11-24T22:30:00", BRUSSELS);
    assertEquals(QuestDay.keyFor(instant, BRUSSELS, 23), QuestDay.keyFor(instant, BRUSSELS, 25));
    assertEquals(QuestDay.keyFor(instant, BRUSSELS, 0), QuestDay.keyFor(instant, BRUSSELS, -1));
    assertNotEquals(QuestDay.keyFor(instant, BRUSSELS, 23), QuestDay.keyFor(instant, BRUSSELS, 0));
  }
}
