package mc.carlton.freerpg.worldevents;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Lumberjack's Day, Forge Fever, Scholar's Moon and Adventurer's Wind. */
class SkillEventsTest {

  @BeforeEach
  void empty() {
    WorldEventConfig.loadFrom(new YamlConfiguration());
  }

  // ---------------------------------------------------------------- the new types

  @Test
  void theFourNewTypesExistAfterTheOldOnesInRollOrder() {
    assertEquals(11, WorldEventType.values().length);
    assertEquals(WorldEventType.LUMBERJACKS_DAY, WorldEventType.fromCommandName("lumberjacks_day").orElseThrow());
    assertEquals(WorldEventType.FORGE_FEVER, WorldEventType.fromCommandName("forge_fever").orElseThrow());
    assertEquals(WorldEventType.SCHOLARS_MOON, WorldEventType.fromCommandName("scholars_moon").orElseThrow());
    assertEquals(WorldEventType.ADVENTURERS_WIND, WorldEventType.fromCommandName("adventurers_wind").orElseThrow());
    assertTrue(WorldEventType.LUMBERJACKS_DAY.ordinal() > WorldEventType.FISHING_FESTIVAL.ordinal());
  }

  @Test
  void theNewTypesHaveTheirDefaults() {
    WorldEventType t = WorldEventType.LUMBERJACKS_DAY;
    assertEquals(0.05, WorldEventConfig.chance(t));
    assertEquals(0, WorldEventConfig.startTick(t));
    assertEquals(12000, WorldEventConfig.endTick(t));
    assertFalse(WorldEventConfig.randomStart(t));
    assertEquals(0, WorldEventConfig.durationSeconds(t));

    t = WorldEventType.FORGE_FEVER;
    assertEquals(0.04, WorldEventConfig.chance(t));
    assertEquals(23000, WorldEventConfig.endTick(t));
    assertTrue(WorldEventConfig.randomStart(t));
    assertEquals(600, WorldEventConfig.durationSeconds(t));

    t = WorldEventType.SCHOLARS_MOON;
    assertEquals(0.05, WorldEventConfig.chance(t));
    assertEquals(13000, WorldEventConfig.startTick(t));
    assertEquals(23000, WorldEventConfig.endTick(t));
    assertFalse(WorldEventConfig.randomStart(t));
    assertEquals(0, WorldEventConfig.durationSeconds(t));

    t = WorldEventType.ADVENTURERS_WIND;
    assertEquals(0.05, WorldEventConfig.chance(t));
    assertEquals(0, WorldEventConfig.startTick(t));
    assertEquals(12000, WorldEventConfig.endTick(t));
    assertFalse(WorldEventConfig.randomStart(t));
  }

  // ---------------------------------------------------------------- Lumberjack's Day

  @Test
  void lumberjackGivesWoodcuttingAndAxeMasteryExpAndDropsLogsTwice() {
    LumberjackEvent event = new LumberjackEvent();
    assertEquals(WorldEventType.LUMBERJACKS_DAY, event.type());
    assertEquals(1.5, event.xpMultiplier("woodcutting"));
    assertEquals(1.5, event.xpMultiplier("axeMastery"));
    assertEquals(1.0, event.xpMultiplier("farming"));
    assertTrue(EventDefaults.dropBlocks(WorldEventType.LUMBERJACKS_DAY).contains(Material.OAK_LOG));
    assertTrue(EventDefaults.dropBlocks(WorldEventType.LUMBERJACKS_DAY).contains(Material.CRIMSON_STEM));
    assertFalse(EventDefaults.dropBlocks(WorldEventType.LUMBERJACKS_DAY).contains(Material.WHEAT));
    assertEquals(0.25, EventDefaults.dropChance(WorldEventType.LUMBERJACKS_DAY));
  }

  // ---------------------------------------------------------------- Forge Fever

  @Test
  void forgeFeverBoostsSmeltingAndRepairAndFlamePick() {
    ForgeFeverEvent event = new ForgeFeverEvent();
    assertEquals(WorldEventType.FORGE_FEVER, event.type());
    assertEquals(1.5, event.xpMultiplier("smelting"));
    assertEquals(1.5, event.xpMultiplier("repair"));
    assertEquals(1.0, event.xpMultiplier("mining"));
    assertEquals(0.25, event.flamePickBonus());
  }

  @Test
  void theFlamePickBonusComesFromTheConfigAndNeverGoesNegative() {
    YamlConfiguration yaml = new YamlConfiguration();
    yaml.set("events.forgeFever.flamePickBonus", 0.4);
    WorldEventConfig.loadFrom(yaml);
    assertEquals(0.4, new ForgeFeverEvent().flamePickBonus());
    yaml.set("events.forgeFever.flamePickBonus", -1.0);
    assertEquals(0.0, new ForgeFeverEvent().flamePickBonus());
  }

  @Test
  void otherEventsGiveNoFlamePickBonus() {
    assertEquals(0.0, new HarvestEvent().flamePickBonus());
  }

  // ---------------------------------------------------------------- Scholar's Moon

  @Test
  void scholarsMoonBoostsEnchantingAndAlchemy() {
    ScholarsMoonEvent event = new ScholarsMoonEvent();
    assertEquals(WorldEventType.SCHOLARS_MOON, event.type());
    assertEquals(1.5, event.xpMultiplier("enchanting"));
    assertEquals(1.5, event.xpMultiplier("alchemy"));
    assertEquals(1.0, event.xpMultiplier("smelting"));
  }

  @Test
  void enchantingGivesBackPartOfTheLevels() {
    // 0.5: the 1 level button is free, 2 levels cost 1, 3 levels cost 1
    assertEquals(1, ScholarsMoonEvent.levelRefund(1, 0.5));
    assertEquals(1, ScholarsMoonEvent.levelRefund(2, 0.5));
    assertEquals(2, ScholarsMoonEvent.levelRefund(3, 0.5));
    // 1.0 = no discount, 0.0 = everything is free
    assertEquals(0, ScholarsMoonEvent.levelRefund(3, 1.0));
    assertEquals(3, ScholarsMoonEvent.levelRefund(3, 0.0));
    // nonsense values are clamped, never a negative refund or more than was paid
    assertEquals(0, ScholarsMoonEvent.levelRefund(3, 5.0));
    assertEquals(3, ScholarsMoonEvent.levelRefund(3, -2.0));
    assertEquals(0, ScholarsMoonEvent.levelRefund(0, 0.5));
    assertEquals(0, ScholarsMoonEvent.levelRefund(-4, 0.5));
  }

  @Test
  void theLevelCostMultiplierDefaultsToHalf() {
    assertEquals(0.5, ScholarsMoonEvent.costMultiplier());
    YamlConfiguration yaml = new YamlConfiguration();
    yaml.set("events.scholarsMoon.levelCostMultiplier", 9.0);
    WorldEventConfig.loadFrom(yaml);
    assertEquals(1.0, ScholarsMoonEvent.costMultiplier());
  }

  // ---------------------------------------------------------------- Adventurer's Wind

  @Test
  void adventurersWindBoostsAgilityAndAeronautics() {
    AdventurersWindEvent event = new AdventurersWindEvent();
    assertEquals(WorldEventType.ADVENTURERS_WIND, event.type());
    assertEquals(1.5, event.xpMultiplier("agility"));
    assertEquals(1.5, event.xpMultiplier("aeronautics"));
    assertEquals(1.0, event.xpMultiplier("mining"));
  }

  @Test
  void theGlidePushStopsAtTheSpeedCap() {
    assertEquals(0.12, AdventurersWindEvent.glideNudge(0.5, 0.12, 1.6));
    assertEquals(0.0, AdventurersWindEvent.glideNudge(1.6, 0.12, 1.6));
    assertEquals(0.0, AdventurersWindEvent.glideNudge(2.5, 0.12, 1.6));
    assertEquals(0.0, AdventurersWindEvent.glideNudge(0.5, -1.0, 1.6), "no negative push");
  }

  @Test
  void theSpeedEffectNeverReplacesAStrongerOrLongerOne() {
    // nothing yet: give ours
    assertTrue(AdventurersWindEvent.shouldRefreshSpeed(false, 0, 0, 0));
    // our own short effect: refresh it
    assertTrue(AdventurersWindEvent.shouldRefreshSpeed(true, 0, 30, 0));
    // a long potion of the same level: leave it
    assertFalse(AdventurersWindEvent.shouldRefreshSpeed(true, 0, 3000, 0));
    // a stronger potion: leave it
    assertFalse(AdventurersWindEvent.shouldRefreshSpeed(true, 1, 30, 0));
  }

  @Test
  void theWindSettingsHaveDefaults() {
    assertEquals(0, AdventurersWindEvent.speedAmplifier());
    assertEquals(0.12, AdventurersWindEvent.glideBoost());
    assertEquals(1.6, AdventurersWindEvent.maxGlideSpeed());
    assertEquals(1.5, new AdventurersWindEvent().xpMultiplier("agility"));
  }
}
