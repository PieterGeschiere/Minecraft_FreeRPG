package mc.carlton.freerpg.quests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

class QuestConfigTest {

  private static YamlConfiguration yaml(String text) throws Exception {
    YamlConfiguration yaml = new YamlConfiguration();
    yaml.loadFromString(text);
    return yaml;
  }

  @Test
  void parsePoolReadsEveryField() throws Exception {
    YamlConfiguration yaml = yaml("""
        quests:
          mineOres:
            type: BREAK_BLOCKS
            filter: ores
            min: 20
            max: 60
            weight: 3
            name: "Mine {target} ores"
        """);
    List<QuestDefinition> pool = QuestConfig.parsePool(yaml.getConfigurationSection("quests"));
    assertEquals(1, pool.size());
    QuestDefinition q = pool.get(0);
    assertEquals("mineOres", q.id());
    assertEquals(QuestType.BREAK_BLOCKS, q.type());
    assertEquals("ORES", q.filter());
    assertEquals(20, q.minTarget());
    assertEquals(60, q.maxTarget());
    assertEquals(3, q.weight());
    assertEquals("Mine {target} ores", q.name());
  }

  @Test
  void parsePoolSkipsEntriesWithUnknownType() throws Exception {
    YamlConfiguration yaml = yaml("""
        quests:
          bad:
            type: DANCE
            min: 1
            max: 2
          good:
            type: CATCH_FISH
            min: 1
            max: 2
        """);
    List<QuestDefinition> pool = QuestConfig.parsePool(yaml.getConfigurationSection("quests"));
    assertEquals(1, pool.size());
    assertEquals("good", pool.get(0).id());
  }

  @Test
  void parsePoolRepairsReversedBoundsAndMissingValues() throws Exception {
    YamlConfiguration yaml = yaml("""
        quests:
          odd:
            type: CRAFT_ITEMS
            min: 30
            max: 10
        """);
    QuestDefinition q = QuestConfig.parsePool(yaml.getConfigurationSection("quests")).get(0);
    assertTrue(q.minTarget() <= q.maxTarget());
    assertEquals("ANY", q.filter());
    assertEquals(1, q.weight());
  }

  @Test
  void parsePoolOfNullSectionIsEmpty() {
    assertTrue(QuestConfig.parsePool(null).isEmpty());
  }

  @Test
  void parseZoneFallsBackOnInvalid() {
    assertEquals(ZoneId.systemDefault(), QuestConfig.parseZone("Not/AZone"));
    assertEquals(ZoneId.systemDefault(), QuestConfig.parseZone(""));
    assertEquals(ZoneId.systemDefault(), QuestConfig.parseZone(null));
    assertEquals(ZoneId.of("Europe/Brussels"), QuestConfig.parseZone("Europe/Brussels"));
  }

  @Test
  void defaultQuestsYmlIsValid() throws Exception {
    try (InputStream stream = QuestConfigTest.class.getClassLoader()
        .getResourceAsStream("quests.yml")) {
      assertNotNull(stream);
      YamlConfiguration yaml = YamlConfiguration.loadConfiguration(
          new InputStreamReader(stream, StandardCharsets.UTF_8));
      List<QuestDefinition> pool = QuestConfig.parsePool(yaml.getConfigurationSection("quests"));
      assertTrue(pool.size() >= 8, "pool size " + pool.size());
      Set<QuestType> seen = EnumSet.noneOf(QuestType.class);
      for (QuestDefinition q : pool) {
        assertTrue(q.minTarget() <= q.maxTarget(), q.id());
        assertTrue(q.minTarget() >= 1, q.id());
        assertTrue(q.name().contains("{target}"), q.id());
        seen.add(q.type());
      }
      assertEquals(EnumSet.allOf(QuestType.class), seen);
    }
  }
}
