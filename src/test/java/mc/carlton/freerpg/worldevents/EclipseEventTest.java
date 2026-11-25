package mc.carlton.freerpg.worldevents;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.junit.jupiter.api.Test;

class EclipseEventTest {

  @Test
  void onlyNaturalMonsterSpawnsAreCancelled() {
    Random random = new Random(1);
    assertFalse(EclipseSpawnListener.shouldCancel(SpawnReason.SPAWNER, true, false, 1.0, random));
    assertFalse(EclipseSpawnListener.shouldCancel(SpawnReason.NATURAL, false, false, 1.0, random),
        "animals are not touched");
    assertFalse(EclipseSpawnListener.shouldCancel(SpawnReason.NATURAL, true, true, 1.0, random),
        "elites stay");
    assertTrue(EclipseSpawnListener.shouldCancel(SpawnReason.NATURAL, true, false, 1.0, random));
  }

  @Test
  void theCancelledShareFollowsTheReduction() {
    Random random = new Random(11);
    int cancelled = 0;
    for (int i = 0; i < 10000; i++) {
      if (EclipseSpawnListener.shouldCancel(SpawnReason.NATURAL, true, false, 0.6, random)) {
        cancelled++;
      }
    }
    assertTrue(Math.abs(cancelled / 10000.0 - 0.6) < 0.03, "share " + cancelled);
    for (int i = 0; i < 1000; i++) {
      assertFalse(EclipseSpawnListener.shouldCancel(SpawnReason.NATURAL, true, false, 0.0, random));
    }
  }

  @Test
  void theReductionDefaultsToSixtyPercent() {
    WorldEventConfig.loadFrom(new YamlConfiguration());
    assertEquals(0.6, EclipseSpawnListener.reduction());
    assertEquals(WorldEventType.ECLIPSE, new EclipseEvent().type());
    assertEquals(1.0, new EclipseEvent().xpMultiplier("mining"));
  }
}
