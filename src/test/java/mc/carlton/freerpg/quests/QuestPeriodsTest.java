package mc.carlton.freerpg.quests;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class QuestPeriodsTest {

  private static final ZoneId UTC = ZoneOffset.UTC;

  private static long at(String instant) {
    return Instant.parse(instant).toEpochMilli();
  }

  // ---- period names ----

  @Test
  void periodsAreFoundByNameInAnyCase() {
    assertEquals(QuestPeriod.DAILY, QuestPeriod.parse("daily"));
    assertEquals(QuestPeriod.WEEKLY, QuestPeriod.parse(" Weekly "));
    assertEquals(QuestPeriod.SEASONAL, QuestPeriod.parse("SEASONAL"));
    assertNull(QuestPeriod.parse("monthly"));
    assertNull(QuestPeriod.parse(null));
  }

  // ---- period keys ----

  @Test
  void dailyKeyIsUnchanged() {
    assertEquals("2026-11-23", QuestDay.keyFor(QuestPeriod.DAILY, at("2026-11-23T10:00:00Z"), UTC, 0));
    assertEquals(QuestDay.keyFor(at("2026-11-23T10:00:00Z"), UTC, 0),
        QuestDay.keyFor(QuestPeriod.DAILY, at("2026-11-23T10:00:00Z"), UTC, 0));
  }

  @Test
  void weekRunsFromMondayToSunday() {
    // 2026-11-23 is a Monday
    assertEquals("2026-W48", QuestDay.keyFor(QuestPeriod.WEEKLY, at("2026-11-23T00:00:00Z"), UTC, 0));
    assertEquals("2026-W48", QuestDay.keyFor(QuestPeriod.WEEKLY, at("2026-11-29T23:59:00Z"), UTC, 0));
    assertEquals("2026-W49", QuestDay.keyFor(QuestPeriod.WEEKLY, at("2026-11-30T00:00:00Z"), UTC, 0));
  }

  @Test
  void weekChangesAtTheResetHour() {
    // with reset hour 3, Monday 02:00 still belongs to the previous quest day (Sunday)
    assertEquals("2026-W48", QuestDay.keyFor(QuestPeriod.WEEKLY, at("2026-11-30T02:59:00Z"), UTC, 3));
    assertEquals("2026-W49", QuestDay.keyFor(QuestPeriod.WEEKLY, at("2026-11-30T03:00:00Z"), UTC, 3));
  }

  @Test
  void isoWeekAtTheTurnOfTheYearUsesTheWeekBasedYear() {
    // 2026-01-01 is a Thursday, so it is still week 1 of 2026; 2024-12-30 is week 1 of 2025
    assertEquals("2026-W01", QuestDay.keyFor(QuestPeriod.WEEKLY, at("2026-01-01T12:00:00Z"), UTC, 0));
    assertEquals("2025-W01", QuestDay.keyFor(QuestPeriod.WEEKLY, at("2024-12-30T12:00:00Z"), UTC, 0));
  }

  @Test
  void seasonIsACalendarQuarter() {
    assertEquals("2026-Q1", QuestDay.keyFor(QuestPeriod.SEASONAL, at("2026-03-31T23:00:00Z"), UTC, 0));
    assertEquals("2026-Q2", QuestDay.keyFor(QuestPeriod.SEASONAL, at("2026-04-01T00:00:00Z"), UTC, 0));
    assertEquals("2026-Q4", QuestDay.keyFor(QuestPeriod.SEASONAL, at("2026-12-31T12:00:00Z"), UTC, 0));
    assertEquals("2027-Q1", QuestDay.keyFor(QuestPeriod.SEASONAL, at("2027-01-01T12:00:00Z"), UTC, 0));
  }

  // ---- scaled quests ----

  @Test
  void scaledQuestsAskForMoreAndKeepEverythingElse() {
    QuestDefinition def = new QuestDefinition("mine", QuestType.BREAK_BLOCKS, "ORES", 20, 60, 2,
        "Mine {target} ores");
    QuestDefinition weekly = def.scaled(5);
    assertEquals(100, weekly.minTarget());
    assertEquals(300, weekly.maxTarget());
    assertEquals("mine", weekly.id());
    assertEquals(QuestType.BREAK_BLOCKS, weekly.type());
    assertEquals("ORES", weekly.filter());
    assertEquals(2, weekly.weight());
    assertEquals("Mine {target} ores", weekly.name());
    assertEquals(20, def.scaled(1).minTarget());
    assertEquals(20, def.scaled(0).minTarget());
    assertEquals(20, def.scaled(-3).minTarget());
  }

  @Test
  void scalingCannotOverflow() {
    QuestDefinition def = new QuestDefinition("x", QuestType.KILL_MOBS, "ANY", 1_000_000_000,
        2_000_000_000, 1, "x {target}");
    assertEquals(Integer.MAX_VALUE, def.scaled(100).maxTarget());
    assertEquals(Integer.MAX_VALUE, def.scaled(100).minTarget());
  }

  // ---- bigger wheel prizes ----

  @Test
  void wheelRangesAreMultiplied() {
    assertArrayEquals(new int[] {300, 3000}, WheelMenu.scaled(new int[] {100, 1000}, 3));
    assertArrayEquals(new int[] {100, 1000}, WheelMenu.scaled(new int[] {100, 1000}, 1));
    assertArrayEquals(new int[] {100, 1000}, WheelMenu.scaled(new int[] {100, 1000}, 0));
    assertArrayEquals(new int[] {Integer.MAX_VALUE, Integer.MAX_VALUE},
        WheelMenu.scaled(new int[] {Integer.MAX_VALUE, Integer.MAX_VALUE}, 8));
  }

  // ---- menu layout ----

  @Test
  void questSlotsFitTheMiddleRowForAnyCount() {
    for (int count = 0; count <= 12; count++) {
      int[] slots = QuestMenu.slotsFor(count);
      assertTrue(slots.length >= Math.min(Math.max(count, 1), 7), "enough slots for " + count);
      for (int slot : slots) {
        assertTrue(slot >= 9 && slot <= 17, "slot " + slot + " is in the middle row");
      }
      assertEquals(slots.length, java.util.Arrays.stream(slots).distinct().count());
    }
    assertArrayEquals(new int[] {11, 13, 15}, QuestMenu.slotsFor(3));
  }

  // ---- a weekly manager ----

  @TempDir
  File folder;

  @Test
  void weeklyQuestsLastTheWholeWeekAndAreDealtAgainNextWeek() {
    AtomicLong clock = new AtomicLong(at("2026-11-23T10:00:00Z")); // Monday
    List<QuestManager.QuestEvent> events = new ArrayList<>();
    List<QuestDefinition> pool = List.of(
        new QuestDefinition("blocks", QuestType.BREAK_BLOCKS, "ANY", 500, 500, 1, "Break {target}"),
        new QuestDefinition("fish", QuestType.CATCH_FISH, "ANY", 50, 50, 1, "Fish {target}"));
    QuestManager weekly = new QuestManager(QuestPeriod.WEEKLY, new QuestStorage(folder),
        () -> pool, () -> 2, clock::get, () -> UTC, () -> 0, new Random(1), events::add);
    UUID player = UUID.randomUUID();

    PlayerQuests monday = weekly.today(player);
    assertEquals("2026-W48", monday.dayKey());
    assertEquals(2, monday.quests().size());

    weekly.record(player, QuestType.BREAK_BLOCKS, Set.of("ANY"), 120);
    clock.set(at("2026-11-26T18:00:00Z")); // Thursday, same week
    PlayerQuests thursday = weekly.today(player);
    assertEquals("2026-W48", thursday.dayKey());
    ActiveQuest blocks = thursday.quests().stream().filter(q -> q.id().equals("blocks")).findFirst()
        .orElseThrow();
    assertEquals(120, blocks.progress());

    clock.set(at("2026-11-30T09:00:00Z")); // next Monday
    PlayerQuests nextWeek = weekly.today(player);
    assertEquals("2026-W49", nextWeek.dayKey());
    assertTrue(nextWeek.quests().stream().allMatch(q -> q.progress() == 0));
  }

  @Test
  void dailyAndWeeklyManagersKeepSeparateFilesAndProgress() {
    AtomicLong clock = new AtomicLong(at("2026-11-23T10:00:00Z"));
    List<QuestDefinition> pool = List.of(
        new QuestDefinition("blocks", QuestType.BREAK_BLOCKS, "ANY", 10, 10, 1, "Break {target}"));
    File dailyFolder = new File(folder, "quests");
    File weeklyFolder = new File(folder, "quests-weekly");
    QuestManager daily = new QuestManager(new QuestStorage(dailyFolder), () -> pool, () -> 1,
        clock::get, () -> UTC, () -> 0, new Random(1), e -> { });
    QuestManager weekly = new QuestManager(QuestPeriod.WEEKLY, new QuestStorage(weeklyFolder),
        () -> pool, () -> 1, clock::get, () -> UTC, () -> 0, new Random(1), e -> { });
    UUID player = UUID.randomUUID();
    daily.record(player, QuestType.BREAK_BLOCKS, Set.of("ANY"), 10); // finishes the daily quest
    assertTrue(daily.today(player).allDone());
    assertEquals(SpinState.READY, daily.today(player).spin());
    assertEquals(0, weekly.today(player).quests().get(0).progress());
    assertEquals(SpinState.NONE, weekly.today(player).spin());
    assertTrue(new File(dailyFolder, player + ".yml").exists());
    assertTrue(new File(weeklyFolder, player + ".yml").exists());
  }
}
