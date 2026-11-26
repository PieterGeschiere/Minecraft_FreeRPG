package mc.carlton.freerpg.worldevents;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NightEventsConfigTest {

  @BeforeEach
  void empty() {
    WorldEventConfig.loadFrom(new YamlConfiguration());
  }

  @Test
  void bothNightEventsHaveTheirDefaults() {
    for (WorldEventType type : new WorldEventType[] {WorldEventType.TITAN_NIGHT,
        WorldEventType.MUTATOR_NIGHT}) {
      assertEquals(0.05, WorldEventConfig.chance(type));
      assertEquals(13000, WorldEventConfig.startTick(type));
      assertEquals(23000, WorldEventConfig.endTick(type));
      assertFalse(WorldEventConfig.randomStart(type));
      assertEquals(0, WorldEventConfig.durationSeconds(type));
      assertTrue(WorldEventConfig.enabled(type));
    }
  }

  @Test
  void theTypesAreFoundByCommandNameAndRollLast() {
    assertEquals(WorldEventType.TITAN_NIGHT,
        WorldEventType.fromCommandName("titan_night").orElseThrow());
    assertEquals(WorldEventType.MUTATOR_NIGHT,
        WorldEventType.fromCommandName("mutator_night").orElseThrow());
    assertEquals(11, WorldEventType.values().length);
    assertTrue(WorldEventType.TITAN_NIGHT.ordinal() > WorldEventType.ADVENTURERS_WIND.ordinal());
    assertTrue(WorldEventType.MUTATOR_NIGHT.ordinal() > WorldEventType.ADVENTURERS_WIND.ordinal());
  }

  @Test
  void otherEventsDoNotChangeSoulsOrAnnouncements() {
    assertEquals(1.0, new HarvestEvent().soulMultiplier());
    assertEquals(0, new HarvestEvent().announcePlaceholders(null).length);
  }
}
