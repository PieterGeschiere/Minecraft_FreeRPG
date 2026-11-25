package mc.carlton.freerpg.worldevents;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GoldRushEventTest {

  @BeforeEach
  void empty() {
    WorldEventConfig.loadFrom(new YamlConfiguration());
  }

  @Test
  void onlyMiningGetsTheBonus() {
    GoldRushEvent event = new GoldRushEvent();
    assertEquals(1.5, event.xpMultiplier("mining"));
    assertEquals(1.0, event.xpMultiplier("farming"));
  }

  @Test
  void theNumberOfNoiseMobsStaysInTheRange() {
    Random random = new Random(5);
    Set<Integer> seen = new HashSet<>();
    for (int i = 0; i < 1000; i++) {
      int count = GoldRushNoiseListener.mobCount(random, 1, 3);
      assertTrue(count >= 1 && count <= 3, "count " + count);
      seen.add(count);
    }
    assertEquals(Set.of(1, 2, 3), seen);
  }

  @Test
  void aMaximumBelowTheMinimumCountsAsTheMinimum() {
    Random random = new Random(5);
    for (int i = 0; i < 100; i++) {
      assertEquals(3, GoldRushNoiseListener.mobCount(random, 3, 1));
    }
  }

  @Test
  void noiseHasACooldownPerPlayer() {
    assertFalse(GoldRushNoiseListener.noiseReady(10_000, 6_000, 5));
    assertTrue(GoldRushNoiseListener.noiseReady(11_000, 6_000, 5));
  }

  @Test
  void theNoiseSettingsHaveDefaults() {
    assertEquals(0.15, GoldRushNoiseListener.chance());
    assertEquals(5, GoldRushNoiseListener.cooldownSeconds());
    assertTrue(GoldRushNoiseListener.mobTypes().size() == 5);
  }

  @Test
  void noiseOnlyHappensAfterTheBreakWasNotCancelled() throws Exception {
    var check = GoldRushNoiseListener.class.getDeclaredMethod("onBreakCheck",
        org.bukkit.event.block.BlockBreakEvent.class);
    var confirmed = GoldRushNoiseListener.class.getDeclaredMethod("onBreakConfirmed",
        org.bukkit.event.block.BlockBreakEvent.class);
    var checkAnnotation = check.getAnnotation(org.bukkit.event.EventHandler.class);
    var confirmedAnnotation = confirmed.getAnnotation(org.bukkit.event.EventHandler.class);
    // the natural-block check must run before FreeRPG forgets placed blocks...
    assertEquals(org.bukkit.event.EventPriority.LOWEST, checkAnnotation.priority());
    // ...but the spawn must wait until protection plugins have had their say
    assertEquals(org.bukkit.event.EventPriority.MONITOR, confirmedAnnotation.priority());
    assertTrue(confirmedAnnotation.ignoreCancelled());
  }
}
