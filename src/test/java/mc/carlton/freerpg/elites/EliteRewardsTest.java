package mc.carlton.freerpg.elites;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Random;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

class EliteRewardsTest {

  @Test
  void aPlainEliteQualifies() {
    assertTrue(EliteRewards.qualifies(true, false, false, false));
  }

  @Test
  void aNonEliteNeverQualifies() {
    assertFalse(EliteRewards.qualifies(false, false, false, false));
  }

  @Test
  void minionNeverQualifies() {
    assertFalse(EliteRewards.qualifies(true, true, false, false));
  }

  @Test
  void spawnerTaggedNeverQualifies() {
    assertFalse(EliteRewards.qualifies(true, false, true, false));
  }

  @Test
  void farmKillNeverQualifies() {
    assertFalse(EliteRewards.qualifies(true, false, false, true));
  }

  @Test
  void bonusExpIsOnlyTheExtraPart() {
    assertEquals(200, EliteRewards.bonusExp(100, 3.0));
    assertEquals(0, EliteRewards.bonusExp(100, 1.0));
    assertEquals(0, EliteRewards.bonusExp(0, 3.0));
    assertEquals(0, EliteRewards.bonusExp(-5, 3.0));
  }

  @Test
  void skillFollowsTheWeaponWithDefenseAsFallback() {
    assertEquals("swordsmanship", EliteRewards.skillFor(true, false));
    assertEquals("axeMastery", EliteRewards.skillFor(false, true));
    assertEquals("defense", EliteRewards.skillFor(false, false));
  }

  @Test
  void dropsRespectChanceZeroAndOne() throws Exception {
    YamlConfiguration yaml = new YamlConfiguration();
    yaml.loadFromString("rewards:\n  drops:\n"
        + "    - {material: DIAMOND, min: 2, max: 2, chance: 1.0}\n"
        + "    - {material: EMERALD, min: 1, max: 3, chance: 0.0}\n"
        + "    - {material: NOT_AN_ITEM, min: 1, max: 1, chance: 1.0}\n");
    EliteConfig.loadFrom(yaml);
    Map<Material, Integer> drops = EliteRewards.rollDrops(EliteConfig.drops(), new Random(5));
    assertEquals(Map.of(Material.DIAMOND, 2), drops);
  }

  @Test
  void dropAmountStaysInsideMinAndMax() {
    List<EliteConfig.DropEntry> entries =
        List.of(new EliteConfig.DropEntry(Material.GOLD_INGOT, 2, 5, 1.0));
    Random random = new Random(11);
    for (int i = 0; i < 200; i++) {
      int amount = EliteRewards.rollDrops(entries, random).get(Material.GOLD_INGOT);
      assertTrue(amount >= 2 && amount <= 5, "amount " + amount);
    }
  }

  @Test
  void defaultDropsExistWhenTheFileHasNone() {
    EliteConfig.loadFrom(new YamlConfiguration());
    assertFalse(EliteConfig.drops().isEmpty());
  }
}
