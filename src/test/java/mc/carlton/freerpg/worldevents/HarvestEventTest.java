package mc.carlton.freerpg.worldevents;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HarvestEventTest {

  @BeforeEach
  void empty() {
    WorldEventConfig.loadFrom(new YamlConfiguration());
  }

  @Test
  void farmingFishingAndWoodcuttingGetTheBonusOthersDoNot() {
    HarvestEvent event = new HarvestEvent();
    assertEquals(1.5, event.xpMultiplier("farming"));
    assertEquals(1.5, event.xpMultiplier("fishing"));
    assertEquals(1.5, event.xpMultiplier("woodcutting"));
    assertEquals(1.0, event.xpMultiplier("mining"));
  }

  @Test
  void theMultiplierComesFromTheConfig() {
    YamlConfiguration yaml = new YamlConfiguration();
    yaml.set("events.bountifulHarvest.xpMultiplier", 2.0);
    WorldEventConfig.loadFrom(yaml);
    assertEquals(2.0, new HarvestEvent().xpMultiplier("farming"));
  }

  @Test
  void bonusDropsNeverComeFromPlayerPlacedBlocksOrUnripeCrops() {
    assertFalse(BonusDropListener.qualifies(false, true, true), "player placed");
    assertFalse(BonusDropListener.qualifies(true, true, false), "unripe crop");
    assertTrue(BonusDropListener.qualifies(true, true, true));
    assertTrue(BonusDropListener.qualifies(true, false, false), "a log is not a crop");
  }

  @Test
  void theDropBlocksAndChanceHaveDefaults() {
    assertEquals(0.25, EventDefaults.dropChance(WorldEventType.BOUNTIFUL_HARVEST));
    assertTrue(EventDefaults.dropBlocks(WorldEventType.BOUNTIFUL_HARVEST).contains(Material.WHEAT));
    assertTrue(EventDefaults.dropBlocks(WorldEventType.BOUNTIFUL_HARVEST).contains(Material.OAK_LOG));
    assertFalse(EventDefaults.dropBlocks(WorldEventType.BOUNTIFUL_HARVEST)
        .contains(Material.DIAMOND_ORE));
    assertTrue(EventDefaults.dropBlocks(WorldEventType.GOLD_RUSH).contains(Material.DIAMOND_ORE));
    assertTrue(EventDefaults.dropBlocks(WorldEventType.GOLD_RUSH)
        .contains(Material.ANCIENT_DEBRIS));
  }

  @Test
  void unknownMaterialNamesInTheConfigAreIgnored() {
    YamlConfiguration yaml = new YamlConfiguration();
    yaml.set("events.bountifulHarvest.dropBlocks", java.util.List.of("WHEAT", "NOT_A_BLOCK"));
    WorldEventConfig.loadFrom(yaml);
    assertEquals(java.util.Set.of(Material.WHEAT),
        EventDefaults.dropBlocks(WorldEventType.BOUNTIFUL_HARVEST));
  }

  @Test
  void onlyRealFishAreDuplicatedNotTreasure() {
    assertTrue(HarvestFishListener.isFish(Material.COD));
    assertTrue(HarvestFishListener.isFish(Material.SALMON));
    assertTrue(HarvestFishListener.isFish(Material.TROPICAL_FISH));
    assertTrue(HarvestFishListener.isFish(Material.PUFFERFISH));
    assertFalse(HarvestFishListener.isFish(Material.ENCHANTED_BOOK));
    assertFalse(HarvestFishListener.isFish(Material.SADDLE));
    assertFalse(HarvestFishListener.isFish(Material.NAME_TAG));
  }
}
