package mc.carlton.freerpg.core.serverFileManagement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PlayerStatsFilePreparationTest {

  // Extend this list when a new skill is added to PlayerStatsFilePreparation.
  private static final List<String> SKILLS = Arrays.asList("digging", "woodcutting", "mining",
      "farming", "fishing", "archery", "beastMastery", "swordsmanship", "defense", "axeMastery",
      "repair", "agility", "alchemy", "smelting", "enchanting", "building");

  private static final List<String> SKILL_KEYS = Arrays.asList("level", "experience",
      "passiveTokens", "skillTokens", "passive1", "passive2", "passive3", "skill_1a", "skill_1b",
      "skill_2a", "skill_2b", "skill_3a", "skill_3b", "skill_M", "triggerAbilityToggle",
      "showEXPBarToggle");

  private static final List<String> GLOBAL_KEYS = Arrays.asList("totalLevel", "globalTokens",
      "skill_1a", "skill_1b", "skill_1c", "skill_2a", "skill_2b", "skill_2c", "skill_3a",
      "skill_3b", "skill_3c", "skill_M", "flintToggle", "oreToggle", "speedToggle", "potionToggle",
      "grappleToggle", "hotRodToggle", "veinMinerToggle", "megaDigToggle", "souls",
      "levelUpMessageToggle", "abilityPrepareMessageToggle", "personalEXPMultiplier",
      "triggerAbilitiesToggle", "showEXPBarToggle", "leafBlowerToggle", "holyAxeToggle",
      "numberOfCooldownBars", "totalExperience", "heartyToggle");

  private YamlConfiguration config;
  private PlayerStatsFilePreparation prep;

  @BeforeEach
  void setUp() {
    config = new YamlConfiguration();
    prep = new PlayerStatsFilePreparation(config);
  }

  private static ArrayList<Double> tokens() {
    ArrayList<Double> t = new ArrayList<>();
    for (int i = 0; i < 4; i++) {
      t.add(0.0);
    }
    t.add(2.0); // passive
    t.add(3.0); // skill
    t.add(5.0); // global
    return t;
  }

  private static ArrayList<Integer> souls() {
    ArrayList<Integer> s = new ArrayList<>();
    s.add(4);
    return s;
  }

  @Test
  void globalKeyListHas31Keys() {
    assertEquals(31, GLOBAL_KEYS.size());
  }

  @Test
  void createWritesGeneralSection() {
    prep.createPlayerYAML("Steve", "en", tokens(), souls());
    assertEquals("Steve", config.getString("general.username"));
    assertEquals("en", config.getString("general.language"));
    assertEquals(0, config.getInt("general.playTime"));
    assertTrue(config.contains("general.firstLogin"));
    assertTrue(config.contains("general.lastLogin"));
    assertTrue(config.contains("general.lastLogout"));
  }

  @Test
  void createWritesExactlyTheGlobalStatsKeys() {
    prep.createPlayerYAML("Steve", "en", tokens(), souls());
    assertEquals(new java.util.HashSet<>(GLOBAL_KEYS),
        config.getConfigurationSection("globalStats").getKeys(false));
    assertEquals(5, config.getInt("globalStats.globalTokens"));
    assertEquals(4, config.getInt("globalStats.souls"));
    assertEquals(1.0, config.getDouble("globalStats.personalEXPMultiplier"));
    assertEquals(0, config.getInt("globalStats.heartyToggle"));
  }

  @Test
  void createWritesEverySkillWithStartingTokens() {
    prep.createPlayerYAML("Steve", "en", tokens(), souls());
    for (String skill : SKILLS) {
      assertEquals(new java.util.HashSet<>(SKILL_KEYS),
          config.getConfigurationSection(skill).getKeys(false), skill);
      assertEquals(0, config.getInt(skill + ".level"), skill);
      assertEquals(2, config.getInt(skill + ".passiveTokens"), skill);
      assertEquals(3, config.getInt(skill + ".skillTokens"), skill);
      assertEquals(1, config.getInt(skill + ".triggerAbilityToggle"), skill);
      assertEquals(1, config.getInt(skill + ".showEXPBarToggle"), skill);
    }
  }

  @Test
  void updateOnEmptyConfigAddsEveryMissingKey() {
    prep.updatePlayerYAML("Steve", false, "en");
    assertEquals("Steve", config.getString("general.username"));
    assertEquals("en", config.getString("general.language"));
    for (String k : GLOBAL_KEYS) {
      assertTrue(config.contains("globalStats." + k), k);
    }
    for (String skill : SKILLS) {
      for (String k : SKILL_KEYS) {
        assertTrue(config.contains(skill + "." + k), skill + "." + k);
      }
    }
  }

  @Test
  void updateKeepsExistingValues() {
    config.set("mining.level", 42);
    config.set("globalStats.souls", 7);
    prep.updatePlayerYAML("Steve", false, "en");
    assertEquals(42, config.getInt("mining.level"));
    assertEquals(7, config.getInt("globalStats.souls"));
  }

  @Test
  void realLoginChangesStaleUsername() {
    config.set("general.username", "OldName");
    prep.updatePlayerYAML("NewName", true, "en");
    assertEquals("NewName", config.getString("general.username"));
  }

  @Test
  void nonRealLoginKeepsUsername() {
    config.set("general.username", "OldName");
    prep.updatePlayerYAML("NewName", false, "en");
    assertEquals("OldName", config.getString("general.username"));
  }
}
