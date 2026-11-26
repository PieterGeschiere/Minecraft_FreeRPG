package mc.carlton.freerpg.worldevents;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WorldEventConfigTest {

  @BeforeEach
  void empty() {
    WorldEventConfig.loadFrom(new YamlConfiguration());
  }

  @Test
  void newEventsHaveTheirDefaults() {
    WorldEventType t = WorldEventType.BOUNTIFUL_HARVEST;
    assertEquals(0.05, WorldEventConfig.chance(t));
    assertEquals(0, WorldEventConfig.startTick(t));
    assertEquals(12000, WorldEventConfig.endTick(t));
    assertFalse(WorldEventConfig.randomStart(t));
    assertEquals(0, WorldEventConfig.durationSeconds(t));
    assertEquals(Set.of("world"), WorldEventConfig.worlds(t));
    assertTrue(WorldEventConfig.enabled(t));

    t = WorldEventType.GOLD_RUSH;
    assertEquals(0.04, WorldEventConfig.chance(t));
    assertEquals(0, WorldEventConfig.startTick(t));
    assertEquals(23000, WorldEventConfig.endTick(t));
    assertTrue(WorldEventConfig.randomStart(t));
    assertEquals(600, WorldEventConfig.durationSeconds(t));

    t = WorldEventType.ECLIPSE;
    assertEquals(0.05, WorldEventConfig.chance(t));
    assertEquals(13000, WorldEventConfig.startTick(t));
    assertEquals(23000, WorldEventConfig.endTick(t));
    assertFalse(WorldEventConfig.randomStart(t));
    assertEquals(0, WorldEventConfig.durationSeconds(t));

    t = WorldEventType.FISHING_FESTIVAL;
    assertEquals(0.04, WorldEventConfig.chance(t));
    assertTrue(WorldEventConfig.randomStart(t));
    assertEquals(600, WorldEventConfig.durationSeconds(t));
  }

  @Test
  void theGapDefaultsToTwoDaysAndNeverGoesNegative() {
    assertEquals(2, WorldEventConfig.minGapDays());
    YamlConfiguration yaml = new YamlConfiguration();
    yaml.set("minGapDays", -1);
    WorldEventConfig.loadFrom(yaml);
    assertEquals(0, WorldEventConfig.minGapDays());
  }

  @Test
  void theChanceIsClampedAndWorldsAreRead() {
    YamlConfiguration yaml = new YamlConfiguration();
    yaml.set("events.eclipse.chance", 7.0);
    yaml.set("events.eclipse.worlds", java.util.List.of("a", "b"));
    WorldEventConfig.loadFrom(yaml);
    assertEquals(1.0, WorldEventConfig.chance(WorldEventType.ECLIPSE));
    assertEquals(Set.of("a", "b"), WorldEventConfig.worlds(WorldEventType.ECLIPSE));
    yaml.set("events.eclipse.chance", -2.0);
    assertEquals(0.0, WorldEventConfig.chance(WorldEventType.ECLIPSE));
  }

  @Test
  void bloodMoonReadsItsSettingsFromTheEliteConfig() {
    YamlConfiguration yaml = new YamlConfiguration();
    yaml.set("bloodMoon.chance", 0.5);
    yaml.set("bloodMoon.startTick", 14000);
    mc.carlton.freerpg.elites.EliteConfig.loadFrom(yaml);
    assertEquals(0.5, WorldEventConfig.chance(WorldEventType.BLOOD_MOON));
    assertEquals(14000, WorldEventConfig.startTick(WorldEventType.BLOOD_MOON));
    assertFalse(WorldEventConfig.randomStart(WorldEventType.BLOOD_MOON));
    assertEquals(0, WorldEventConfig.durationSeconds(WorldEventType.BLOOD_MOON));
    mc.carlton.freerpg.elites.EliteConfig.loadFrom(new YamlConfiguration());
  }

  @Test
  void typesAreFoundByTheirCommandName() {
    assertEquals(WorldEventType.GOLD_RUSH,
        WorldEventType.fromCommandName("GOLD_RUSH").orElseThrow());
    assertTrue(WorldEventType.fromCommandName("nope").isEmpty());
  }
}
