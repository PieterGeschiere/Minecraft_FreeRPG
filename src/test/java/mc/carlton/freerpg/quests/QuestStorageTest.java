package mc.carlton.freerpg.quests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class QuestStorageTest {

  @TempDir
  File folder;

  private PlayerQuests sample(UUID uuid) {
    PlayerQuests data = new PlayerQuests(uuid, "2026-11-23");
    data.quests().add(new ActiveQuest("mineOres", QuestType.BREAK_BLOCKS, "ORES", 40, 12,
        "Mine 40 ores"));
    data.quests().add(new ActiveQuest("catchFish", QuestType.CATCH_FISH, "ANY", 8, 8,
        "Catch 8 fish"));
    data.quests().add(new ActiveQuest("earnMiningExp", QuestType.EARN_SKILL_EXP, "MINING", 900,
        0, "Earn 900 Mining exp"));
    data.setSpin(SpinState.READY);
    return data;
  }

  @Test
  void roundTripKeepsEverything() {
    UUID uuid = UUID.randomUUID();
    QuestStorage storage = new QuestStorage(folder);
    storage.save(sample(uuid));
    PlayerQuests loaded = storage.load(uuid).orElseThrow();
    assertEquals(uuid, loaded.uuid());
    assertEquals("2026-11-23", loaded.dayKey());
    assertEquals(SpinState.READY, loaded.spin());
    assertEquals(3, loaded.quests().size());
    ActiveQuest first = loaded.quests().get(0);
    assertEquals("mineOres", first.id());
    assertEquals(QuestType.BREAK_BLOCKS, first.type());
    assertEquals("ORES", first.filter());
    assertEquals(40, first.target());
    assertEquals(12, first.progress());
    assertEquals("Mine 40 ores", first.name());
    assertTrue(loaded.quests().get(1).done());
    assertEquals("MINING", loaded.quests().get(2).filter());
  }

  @Test
  void missingFileIsEmpty() {
    assertTrue(new QuestStorage(folder).load(UUID.randomUUID()).isEmpty());
  }

  @Test
  void wrongVersionIsEmpty() throws Exception {
    UUID uuid = UUID.randomUUID();
    QuestStorage storage = new QuestStorage(folder);
    storage.save(sample(uuid));
    File file = new File(folder, uuid + ".yml");
    Files.writeString(file.toPath(), Files.readString(file.toPath()).replace("version: 1",
        "version: 99"));
    assertTrue(storage.load(uuid).isEmpty());
  }

  @Test
  void garbageFileIsEmpty() throws Exception {
    UUID uuid = UUID.randomUUID();
    Files.writeString(new File(folder, uuid + ".yml").toPath(), "::: not [valid yaml");
    assertTrue(new QuestStorage(folder).load(uuid).isEmpty());
  }

  @Test
  void unknownQuestTypeInFileDropsWholeFile() throws Exception {
    UUID uuid = UUID.randomUUID();
    QuestStorage storage = new QuestStorage(folder);
    storage.save(sample(uuid));
    File file = new File(folder, uuid + ".yml");
    Files.writeString(file.toPath(), Files.readString(file.toPath()).replace("CATCH_FISH",
        "DANCE"));
    assertTrue(storage.load(uuid).isEmpty());
  }

  @Test
  void unknownSpinStateFallsBackToNone() throws Exception {
    UUID uuid = UUID.randomUUID();
    QuestStorage storage = new QuestStorage(folder);
    storage.save(sample(uuid));
    File file = new File(folder, uuid + ".yml");
    Files.writeString(file.toPath(), Files.readString(file.toPath()).replace("READY", "WHAT"));
    assertEquals(SpinState.NONE, storage.load(uuid).orElseThrow().spin());
  }

  @Test
  void progressNeverExceedsTarget() {
    ActiveQuest quest = new ActiveQuest("a", QuestType.CATCH_FISH, "ANY", 10, 0, "x");
    quest.add(1000);
    assertEquals(10, quest.progress());
    assertTrue(quest.done());
  }

  @Test
  void negativeAddIsIgnored() {
    ActiveQuest quest = new ActiveQuest("a", QuestType.CATCH_FISH, "ANY", 10, 3, "x");
    quest.add(-5);
    assertEquals(3, quest.progress());
  }

  @Test
  void allDoneFalseWhenNoQuests() {
    assertFalse(new PlayerQuests(UUID.randomUUID(), "2026-11-23").allDone());
  }

  @Test
  void allDoneNeedsEveryQuest() {
    PlayerQuests data = sample(UUID.randomUUID());
    assertFalse(data.allDone());
    data.quests().get(0).add(100);
    data.quests().get(2).add(1000);
    assertTrue(data.allDone());
  }

  @Test
  void saveLeavesNoTempFile() {
    QuestStorage storage = new QuestStorage(folder);
    storage.save(sample(UUID.randomUUID()));
    String[] files = folder.list();
    assertEquals(1, files.length);
    assertTrue(files[0].endsWith(".yml"));
  }
}
