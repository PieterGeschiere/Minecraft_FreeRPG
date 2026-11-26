package mc.carlton.freerpg.skills.building;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import mc.carlton.freerpg.config.ConfigLoad;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

/** Building reads its numbers from the bundled config files, and survives files that lack them. */
class BuildingConfigTest {

  private static final double[] WEATHERPROOF = {0.20, 0.40, 0.60, 0.80, 1.00};
  private static final double[] FORTIFIED = {0.10, 0.20, 0.30, 0.40, 0.50};

  private static YamlConfiguration load(String name) throws Exception {
    try (InputStream stream = BuildingConfigTest.class.getClassLoader()
        .getResourceAsStream(name)) {
      assertNotNull(stream, name + " is not on the classpath");
      YamlConfiguration yaml = new YamlConfiguration();
      yaml.load(new InputStreamReader(stream, StandardCharsets.UTF_8));
      return yaml;
    }
  }

  /** An old file from before Building existed: the bundled file without its building section. */
  private static YamlConfiguration without(String name, String... sections) throws Exception {
    YamlConfiguration yaml = load(name);
    for (String section : sections) {
      yaml.set(section, null);
    }
    return yaml;
  }

  @Test
  void bundledPerkConfigHoldsThePlannedValues() throws Exception {
    YamlConfiguration perks = load("perkConfig.yml");
    assertArrayEquals(WEATHERPROOF, BuildingConfig.weatherproofChances(perks), 1e-9);
    assertArrayEquals(FORTIFIED, BuildingConfig.fortifiedChances(perks), 1e-9);
    assertEquals(0.25, BuildingConfig.demolitionSpeedBonus(perks), 1e-9);
    assertEquals(0.5, BuildingConfig.demolitionDurabilityMultiplier(perks), 1e-9);
    assertEquals(0.01, BuildingConfig.reachPerPoint(perks), 1e-9);
    for (String path : new String[]{"passive-1", "skill-1", "skill-2", "skill-3"}) {
      assertTrue(perks.getBoolean("building." + path + ".enabled"), path + " is not enabled");
    }
  }

  @Test
  void bundledAdvancedConfigHoldsThePlannedValues() throws Exception {
    YamlConfiguration advanced = load("advancedConfig.yml");
    assertEquals(3.0, BuildingConfig.reachCap(advanced), 1e-9);
    assertEquals(30_000L, BuildingConfig.xpGraceMillis(advanced));
    assertEquals(40, BuildingConfig.xpPerMaterialPerMinute(advanced));
    assertEquals(2_000_000, BuildingConfig.trackerMaxBlocks(advanced));
    Map<String, Integer> exp = ConfigLoad.readExpDrops(advanced, "building");
    assertEquals(Map.of("basicBlock", 15, "decorativeBlock", 40), exp);
  }

  @Test
  void anOldPerkConfigWithoutBuildingFallsBackToTheDefaults() throws Exception {
    YamlConfiguration old = without("perkConfig.yml", "building");
    assertFalse(old.contains("building"));
    assertArrayEquals(WEATHERPROOF, BuildingConfig.weatherproofChances(old), 1e-9);
    assertArrayEquals(FORTIFIED, BuildingConfig.fortifiedChances(old), 1e-9);
    assertEquals(0.25, BuildingConfig.demolitionSpeedBonus(old), 1e-9);
    assertEquals(0.5, BuildingConfig.demolitionDurabilityMultiplier(old), 1e-9);
    assertEquals(0.01, BuildingConfig.reachPerPoint(old), 1e-9);
  }

  @Test
  void aMissingOrEmptyFileFallsBackToTheDefaults() {
    for (YamlConfiguration empty : new YamlConfiguration[]{new YamlConfiguration(), null}) {
      assertArrayEquals(WEATHERPROOF, BuildingConfig.weatherproofChances(empty), 1e-9);
      assertArrayEquals(FORTIFIED, BuildingConfig.fortifiedChances(empty), 1e-9);
      assertEquals(0.25, BuildingConfig.demolitionSpeedBonus(empty), 1e-9);
      assertEquals(0.5, BuildingConfig.demolitionDurabilityMultiplier(empty), 1e-9);
      assertEquals(0.01, BuildingConfig.reachPerPoint(empty), 1e-9);
      assertEquals(3.0, BuildingConfig.reachCap(empty), 1e-9);
      assertEquals(30_000L, BuildingConfig.xpGraceMillis(empty));
      assertEquals(40, BuildingConfig.xpPerMaterialPerMinute(empty));
      assertEquals(2_000_000, BuildingConfig.trackerMaxBlocks(empty));
    }
  }

  @Test
  void anOldAdvancedConfigWithoutBuildingFallsBackToTheDefaults() throws Exception {
    YamlConfiguration old = without("advancedConfig.yml", "building");
    assertEquals(3.0, BuildingConfig.reachCap(old), 1e-9);
    assertEquals(30_000L, BuildingConfig.xpGraceMillis(old));
    assertEquals(40, BuildingConfig.xpPerMaterialPerMinute(old));
    assertEquals(2_000_000, BuildingConfig.trackerMaxBlocks(old));
    assertEquals(Map.of("basicBlock", 15, "decorativeBlock", 40),
        ConfigLoad.readExpDrops(old, "building"));
    assertTrue(ConfigLoad.expDropsEnabled(old, "building"));
  }

  @Test
  void anIncompleteChanceTableKeepsTheDefaultForTheMissingLevels() {
    YamlConfiguration partial = new YamlConfiguration();
    partial.set("building.skill-1.level-2.surviveChance", 0.5);
    double[] chances = BuildingConfig.weatherproofChances(partial);
    assertEquals(5, chances.length);
    assertEquals(0.20, chances[0], 1e-9);
    assertEquals(0.5, chances[1], 1e-9);
    assertEquals(1.00, chances[4], 1e-9);
  }

  @Test
  void anOldConfigYmlWithoutBuildingIsStillEnabledWithNoLimits() throws Exception {
    YamlConfiguration old = without("config.yml", "building");
    assertTrue(ConfigLoad.skillAllowed(old, "building"));
    assertEquals(-1, ConfigLoad.maxLevel(old, "building"));
    assertEquals(1.0, ConfigLoad.expMultiplier(old, "building"), 1e-9);
  }

  @Test
  void aConfigYmlThatDisablesBuildingIsRespected() {
    YamlConfiguration config = new YamlConfiguration();
    config.set("building.skillAllowed", false);
    config.set("building.maxLevel", 50);
    config.set("building.EXP_Multiplier", 2.5);
    assertFalse(ConfigLoad.skillAllowed(config, "building"));
    assertEquals(50, ConfigLoad.maxLevel(config, "building"));
    assertEquals(2.5, ConfigLoad.expMultiplier(config, "building"), 1e-9);
  }
}
