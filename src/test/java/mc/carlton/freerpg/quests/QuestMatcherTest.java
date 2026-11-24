package mc.carlton.freerpg.quests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.Test;

class QuestMatcherTest {

  private static ActiveQuest quest(QuestType type, String filter) {
    return new ActiveQuest("q", type, filter, 10, 0, "x");
  }

  @Test
  void deepslateOreIsOre() {
    assertTrue(QuestTags.forBlock("DEEPSLATE_IRON_ORE", false).contains("ORES"));
    assertTrue(QuestTags.forBlock("NETHER_QUARTZ_ORE", false).contains("ORES"));
  }

  @Test
  void ancientDebrisIsOre() {
    assertTrue(QuestTags.forBlock("ANCIENT_DEBRIS", false).contains("ORES"));
  }

  @Test
  void rawAndStorageBlocksAreNotOres() {
    assertFalse(QuestTags.forBlock("RAW_IRON_BLOCK", false).contains("ORES"));
    assertFalse(QuestTags.forBlock("IRON_BLOCK", false).contains("ORES"));
  }

  @Test
  void oakLogAndStrippedLogAreLogs() {
    assertTrue(QuestTags.forBlock("OAK_LOG", false).contains("LOGS"));
    assertTrue(QuestTags.forBlock("STRIPPED_BIRCH_LOG", false).contains("LOGS"));
    assertTrue(QuestTags.forBlock("CRIMSON_STEM", false).contains("LOGS"));
    assertFalse(QuestTags.forBlock("OAK_PLANKS", false).contains("LOGS"));
  }

  @Test
  void ripeWheatCountsAsRipeCropOnlyWhenRipe() {
    assertTrue(QuestTags.forBlock("WHEAT", true).contains("RIPE_CROPS"));
    assertFalse(QuestTags.forBlock("WHEAT", false).contains("RIPE_CROPS"));
  }

  @Test
  void everyBlockHasAnyTag() {
    assertTrue(QuestTags.forBlock("DIRT", false).contains("ANY"));
  }

  @Test
  void lowerCaseNamesAreAccepted() {
    assertTrue(QuestTags.forBlock("coal_ore", false).contains("ORES"));
  }

  @Test
  void zombieMatchesZombieHostileAndAny() {
    Set<String> tags = QuestTags.forMob("ZOMBIE", true);
    assertTrue(tags.containsAll(Set.of("ANY", "ZOMBIE", "HOSTILE")));
  }

  @Test
  void cowDoesNotMatchHostile() {
    Set<String> tags = QuestTags.forMob("COW", false);
    assertFalse(tags.contains("HOSTILE"));
    assertTrue(tags.contains("COW"));
  }

  @Test
  void skillTagsUpperCase() {
    assertEquals(Set.of("ANY", "MINING"), QuestTags.forSkill("mining"));
  }

  @Test
  void doneQuestNeverMatches() {
    ActiveQuest quest = quest(QuestType.BREAK_BLOCKS, "ANY");
    quest.add(10);
    assertFalse(QuestMatcher.matches(quest, QuestType.BREAK_BLOCKS, Set.of("ANY")));
  }

  @Test
  void wrongTypeNeverMatches() {
    assertFalse(QuestMatcher.matches(quest(QuestType.CATCH_FISH, "ANY"), QuestType.BREAK_BLOCKS,
        Set.of("ANY")));
  }

  @Test
  void anyFilterMatchesEveryTagSet() {
    ActiveQuest quest = quest(QuestType.BREAK_BLOCKS, "ANY");
    assertTrue(QuestMatcher.matches(quest, QuestType.BREAK_BLOCKS, QuestTags.forBlock("DIRT",
        false)));
    assertTrue(QuestMatcher.matches(quest, QuestType.BREAK_BLOCKS, QuestTags.forBlock("COAL_ORE",
        false)));
  }

  @Test
  void specificFilterNeedsItsTag() {
    ActiveQuest quest = quest(QuestType.BREAK_BLOCKS, "ORES");
    assertFalse(QuestMatcher.matches(quest, QuestType.BREAK_BLOCKS, QuestTags.forBlock("DIRT",
        false)));
    assertTrue(QuestMatcher.matches(quest, QuestType.BREAK_BLOCKS, QuestTags.forBlock("COAL_ORE",
        false)));
  }
}
