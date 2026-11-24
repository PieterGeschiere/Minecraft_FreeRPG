package mc.carlton.freerpg.core.info.player;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import mc.carlton.freerpg.core.serverFileManagement.PlayerYamlTestSupport;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

class PlayerStatsLoadInTest {

  private static final List<String> SKILLS = Arrays.asList("digging", "woodcutting", "mining",
      "farming", "fishing", "archery", "beastMastery", "swordsmanship", "defense", "axeMastery",
      "repair", "agility", "alchemy", "smelting", "enchanting", "building", "aeronautics");

  private static Map<String, ArrayList<Number>> read(FileConfiguration cfg) {
    PlayerStatsLoadIn in = new PlayerStatsLoadIn(cfg);
    in.readInPlayerStats();
    return in.getStatsMap();
  }

  @Test
  void defaultYamlReadsExpectedShape() {
    Map<String, ArrayList<Number>> stats = read(PlayerYamlTestSupport.newDefaultPlayerYaml());
    assertEquals(18, stats.size());
    assertEquals(31, stats.get("global").size());
    for (String skill : SKILLS) {
      assertEquals(14, stats.get(skill).size(), skill);
      assertEquals(0, stats.get(skill).get(0).intValue(), skill);
    }
    assertEquals(1.0, stats.get("global").get(23).doubleValue());
    assertEquals(0, stats.get("global").get(30).intValue());
  }

  @Test
  void roundTripIsLossless() {
    FileConfiguration cfg = PlayerYamlTestSupport.newDefaultPlayerYaml();
    cfg.set("mining.level", 17);
    cfg.set("globalStats.personalEXPMultiplier", 1.5);
    Map<String, ArrayList<Number>> first = read(cfg);
    assertEquals(17, first.get("mining").get(0).intValue());
    assertEquals(1.5, first.get("global").get(23));

    YamlConfiguration fresh = new YamlConfiguration();
    PlayerStatsLoadIn.applyStatsToYaml(first, fresh);
    Map<String, ArrayList<Number>> second = read(fresh);

    assertEquals(first.keySet(), second.keySet());
    for (String key : first.keySet()) {
      assertEquals(first.get(key), second.get(key), key);
    }
    assertInstanceOf(Double.class, second.get("global").get(23));
    assertEquals(1.5, second.get("global").get(23));
  }

  @Test
  void missingSkillSectionReadsDefaults() {
    FileConfiguration cfg = PlayerYamlTestSupport.newDefaultPlayerYaml();
    cfg.set("mining", null);
    Map<String, ArrayList<Number>> stats = read(cfg);
    assertEquals(14, stats.get("mining").size());
    assertTrue(stats.get("mining").stream().allMatch(n -> n.intValue() == 0));
  }

  @Test
  void savedToggleSettingsComeBackInTheirOwnSlots() {
    Map<String, ArrayList<Number>> stats = read(PlayerYamlTestSupport.newDefaultPlayerYaml());
    FileConfiguration saved = new YamlConfiguration();
    Map<String, Integer> expBar = new java.util.HashMap<>();
    Map<String, Integer> ability = new java.util.HashMap<>();
    expBar.put("mining", 0);      // exp bar OFF for mining
    ability.put("mining", 1);     // ability ON for mining
    expBar.put("farming", 1);     // exp bar ON for farming
    ability.put("farming", 0);    // ability OFF for farming
    PlayerStatsLoadIn.applyStatsToYaml(stats, saved, expBar, ability);

    PlayerStatsLoadIn reloaded = new PlayerStatsLoadIn(saved);
    assertEquals(0, reloaded.getSkillExpBarToggles().get("mining"));
    assertEquals(1, reloaded.getSkillAbilityToggles().get("mining"));
    assertEquals(1, reloaded.getSkillExpBarToggles().get("farming"));
    assertEquals(0, reloaded.getSkillAbilityToggles().get("farming"));
  }
}
