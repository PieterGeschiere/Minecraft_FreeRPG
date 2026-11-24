package mc.carlton.freerpg.quests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class QuestManagerTest {

  private static final ZoneId ZONE = ZoneId.of("UTC");

  @TempDir
  File folder;

  private final AtomicLong clock = new AtomicLong(Instant.parse("2026-11-23T10:00:00Z")
      .toEpochMilli());
  private final List<QuestManager.QuestEvent> events = new ArrayList<>();
  private List<QuestDefinition> pool;
  private QuestStorage storage;
  private QuestManager manager;
  private final UUID player = UUID.randomUUID();

  private static QuestDefinition def(String id, QuestType type, String filter, int target) {
    return new QuestDefinition(id, type, filter, target, target, 1, id + " {target}");
  }

  private QuestManager newManager() {
    return new QuestManager(storage, () -> pool, () -> 3, clock::get, () -> ZONE, () -> 0,
        new Random(42), events::add);
  }

  @BeforeEach
  void setUp() {
    pool = new ArrayList<>(List.of(
        def("blocks", QuestType.BREAK_BLOCKS, "ANY", 5),
        def("fish", QuestType.CATCH_FISH, "ANY", 2),
        def("kills", QuestType.KILL_MOBS, "HOSTILE", 3),
        def("crafts", QuestType.CRAFT_ITEMS, "ANY", 4)));
    storage = new QuestStorage(folder);
    manager = newManager();
  }

  private void completeEverything(UUID uuid) {
    for (ActiveQuest quest : manager.today(uuid).quests()) {
      Set<String> tags = quest.filter().equals("HOSTILE") ? Set.of("ANY", "HOSTILE")
          : Set.of("ANY");
      manager.record(uuid, quest.type(), tags, quest.target());
    }
  }

  @Test
  void firstAccessDealsConfiguredNumberOfQuests() {
    PlayerQuests data = manager.today(player);
    assertEquals(3, data.quests().size());
    assertEquals(SpinState.NONE, data.spin());
    assertEquals("2026-11-23", data.dayKey());
  }

  @Test
  void questNameHasTargetFilledIn() {
    for (ActiveQuest quest : manager.today(player).quests()) {
      assertFalse(quest.name().contains("{target}"));
      assertTrue(quest.name().endsWith(" " + quest.target()));
    }
  }

  @Test
  void sameDayKeepsSameQuests() {
    List<String> first = ids(manager.today(player));
    clock.addAndGet(5 * 60 * 60 * 1000L);
    assertEquals(first, ids(manager.today(player)));
    assertEquals(first, ids(newManager().today(player)));
  }

  private static List<String> ids(PlayerQuests data) {
    return data.quests().stream().map(ActiveQuest::id).toList();
  }

  @Test
  void nextDayDealsNewQuestsAndDropsUnclaimedSpin() {
    completeEverything(player);
    assertEquals(SpinState.READY, manager.today(player).spin());
    clock.addAndGet(24 * 60 * 60 * 1000L);
    PlayerQuests next = manager.today(player);
    assertEquals("2026-11-24", next.dayKey());
    assertEquals(SpinState.NONE, next.spin());
    assertTrue(next.quests().stream().noneMatch(ActiveQuest::done));
    assertFalse(manager.claimSpin(player));
  }

  @Test
  void recordCountsEveryMatchingQuest() {
    pool = List.of(def("a", QuestType.BREAK_BLOCKS, "ANY", 10),
        def("b", QuestType.BREAK_BLOCKS, "ORES", 10),
        def("c", QuestType.CATCH_FISH, "ANY", 10));
    manager.record(player, QuestType.BREAK_BLOCKS, Set.of("ANY", "ORES"), 2);
    PlayerQuests data = manager.today(player);
    assertEquals(2, data.quests().stream().filter(q -> q.id().equals("a")).findFirst()
        .orElseThrow().progress());
    assertEquals(2, data.quests().stream().filter(q -> q.id().equals("b")).findFirst()
        .orElseThrow().progress());
    assertEquals(0, data.quests().stream().filter(q -> q.id().equals("c")).findFirst()
        .orElseThrow().progress());
  }

  @Test
  void recordIgnoresNonMatchingTags() {
    pool = List.of(def("ores", QuestType.BREAK_BLOCKS, "ORES", 10));
    manager.record(player, QuestType.BREAK_BLOCKS, Set.of("ANY"), 5);
    assertEquals(0, manager.today(player).quests().get(0).progress());
    assertTrue(events.isEmpty());
  }

  @Test
  void progressAndQuestDoneEventsAreEmitted() {
    pool = List.of(def("fish", QuestType.CATCH_FISH, "ANY", 2));
    manager.record(player, QuestType.CATCH_FISH, Set.of("ANY"), 1);
    manager.record(player, QuestType.CATCH_FISH, Set.of("ANY"), 1);
    assertEquals(QuestManager.QuestEvent.Kind.PROGRESS, events.get(0).kind());
    assertEquals(QuestManager.QuestEvent.Kind.QUEST_DONE, events.get(1).kind());
  }

  @Test
  void completingLastQuestMakesSpinReadyAndEmitsAllDoneOnce() {
    completeEverything(player);
    assertEquals(SpinState.READY, manager.today(player).spin());
    long allDone = events.stream()
        .filter(e -> e.kind() == QuestManager.QuestEvent.Kind.ALL_DONE).count();
    assertEquals(1, allDone);
    manager.record(player, QuestType.BREAK_BLOCKS, Set.of("ANY"), 50);
    assertEquals(1, events.stream()
        .filter(e -> e.kind() == QuestManager.QuestEvent.Kind.ALL_DONE).count());
  }

  @Test
  void recordAfterMidnightDealsNewDayBeforeCounting() {
    pool = List.of(def("fish", QuestType.CATCH_FISH, "ANY", 2));
    manager.record(player, QuestType.CATCH_FISH, Set.of("ANY"), 1);
    clock.addAndGet(24 * 60 * 60 * 1000L);
    manager.record(player, QuestType.CATCH_FISH, Set.of("ANY"), 1);
    PlayerQuests data = manager.today(player);
    assertEquals("2026-11-24", data.dayKey());
    assertEquals(1, data.quests().get(0).progress());
    assertFalse(data.quests().get(0).done());
  }

  @Test
  void claimSpinWorksOnceThenFalse() {
    completeEverything(player);
    assertTrue(manager.claimSpin(player));
    assertFalse(manager.claimSpin(player));
    assertEquals(SpinState.CLAIMED, manager.today(player).spin());
  }

  @Test
  void claimSpinFalseWhenNotReady() {
    assertFalse(manager.claimSpin(player));
    assertEquals(SpinState.NONE, manager.today(player).spin());
  }

  @Test
  void claimSurvivesRestart() {
    completeEverything(player);
    assertTrue(manager.claimSpin(player));
    QuestManager restarted = newManager();
    assertFalse(restarted.claimSpin(player));
    assertEquals(SpinState.CLAIMED, restarted.today(player).spin());
  }

  @Test
  void readySpinSurvivesRestart() {
    completeEverything(player);
    QuestManager restarted = newManager();
    assertTrue(restarted.claimSpin(player));
  }

  @Test
  void savedQuestWorksAfterPoolChange() {
    pool = List.of(def("fish", QuestType.CATCH_FISH, "ANY", 2));
    manager.today(player);
    pool = List.of();
    manager.record(player, QuestType.CATCH_FISH, Set.of("ANY"), 1);
    assertEquals(1, manager.today(player).quests().get(0).progress());
  }

  @Test
  void emptyPoolGivesNoQuestsAndNoSpin() {
    pool = List.of();
    PlayerQuests data = manager.today(player);
    assertTrue(data.quests().isEmpty());
    manager.record(player, QuestType.BREAK_BLOCKS, Set.of("ANY"), 5);
    assertFalse(manager.claimSpin(player));
    assertEquals(SpinState.NONE, data.spin());
  }

  @Test
  void corruptFileGetsFreshDeal() throws Exception {
    Files.writeString(new File(folder, player + ".yml").toPath(), "::: broken [");
    assertEquals(3, manager.today(player).quests().size());
  }

  @Test
  void resetPlayerGivesFreshQuests() {
    completeEverything(player);
    manager.resetPlayer(player);
    PlayerQuests data = manager.today(player);
    assertEquals(SpinState.NONE, data.spin());
    assertTrue(data.quests().stream().noneMatch(ActiveQuest::done));
  }

  @Test
  void forgetDropsOnlyTheCache() {
    pool = List.of(def("fish", QuestType.CATCH_FISH, "ANY", 5));
    manager.record(player, QuestType.CATCH_FISH, Set.of("ANY"), 2);
    manager.forget(player);
    assertEquals(2, manager.today(player).quests().get(0).progress());
  }
}
