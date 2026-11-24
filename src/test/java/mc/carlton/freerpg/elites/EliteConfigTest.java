package mc.carlton.freerpg.elites;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.bukkit.entity.EntityType;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

class EliteConfigTest {

  private static void loadYaml(String yaml) throws Exception {
    YamlConfiguration config = new YamlConfiguration();
    config.loadFromString(yaml);
    EliteConfig.loadFrom(config);
  }

  @Test
  void emptyConfigGivesSpecDefaults() throws Exception {
    loadYaml("");
    assertTrue(EliteConfig.enabled());
    assertEquals(0.03, EliteConfig.chance(), 1e-9);
    assertArrayEquals(new int[] {60, 30, 10}, EliteConfig.affixCountWeights());
    assertEquals(3.0, EliteConfig.healthMultiplier(), 1e-9);
    assertEquals(1.5, EliteConfig.damageMultiplier(), 1e-9);
    assertEquals(20, EliteConfig.chunkCap());
    assertEquals(0.0625, EliteConfig.bloodMoonChance(), 1e-9);
    assertEquals(4.0, EliteConfig.bloodMoonMultiplier(), 1e-9);
    assertTrue(EliteConfig.bloodMoonWorlds().contains("world"));
    assertEquals(13000, EliteConfig.bloodMoonStartTick());
  }

  @Test
  void wrongTypesFallBackToDefaults() throws Exception {
    loadYaml("chance: abc\nchunkCap: lots\naffixCountWeights: nope\n");
    assertEquals(0.03, EliteConfig.chance(), 1e-9);
    assertEquals(20, EliteConfig.chunkCap());
    assertArrayEquals(new int[] {60, 30, 10}, EliteConfig.affixCountWeights());
  }

  @Test
  void chanceIsClampedBetween0And1() throws Exception {
    loadYaml("chance: 7\nbloodMoon:\n  chance: -2\n");
    assertEquals(1.0, EliteConfig.chance(), 1e-9);
    assertEquals(0.0, EliteConfig.bloodMoonChance(), 1e-9);
  }

  @Test
  void unknownEntityTypeInExcludedListIsIgnored() throws Exception {
    loadYaml("excludedTypes: [WARDEN, NOT_A_MOB]\n");
    assertTrue(EliteConfig.excludedTypes().contains(EntityType.WARDEN));
    assertEquals(1, EliteConfig.excludedTypes().size());
  }

  @Test
  void affixValuesAndGearTablesAreReadWithDefaults() throws Exception {
    loadYaml("affixes:\n  thorns:\n    reflect: 0.4\nbloodMoon:\n  gear:\n    helmet:\n"
        + "      chance: 0.5\n      materials: [IRON_HELMET, NOT_AN_ITEM]\n");
    assertEquals(0.4, EliteConfig.affixDouble("thorns", "reflect", 0.25), 1e-9);
    assertEquals(0.25, EliteConfig.affixDouble("armored", "reduction", 0.25), 1e-9);
    assertEquals(0.5, EliteConfig.gearChance("helmet"), 1e-9);
    assertEquals(List.of(org.bukkit.Material.IRON_HELMET), EliteConfig.gearTable("helmet"));
    assertFalse(EliteConfig.gearTable("weapon").isEmpty());
  }
}
