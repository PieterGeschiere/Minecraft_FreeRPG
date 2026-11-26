package mc.carlton.freerpg.worldevents;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

/** The countdown of world events and the switch for party notification sounds. */
class WorldEventEffectsTest {

  @Test
  void countdownSoundsPlayAtOneMinuteTenSecondsAndTheLastThree() {
    for (long seconds : new long[]{60, 10, 3, 2, 1}) {
      assertTrue(WorldEventEffects.isCountdownSecond(seconds), seconds + " should sound");
    }
  }

  @Test
  void otherSecondsAreSilent() {
    for (long seconds : new long[]{-1, 0, 4, 5, 9, 11, 30, 59, 61, 600}) {
      assertFalse(WorldEventEffects.isCountdownSecond(seconds), seconds + " should be silent");
    }
  }

  @Test
  void partyNotificationSoundsAreInTheBundledConfigAndOn() throws Exception {
    try (InputStream stream = WorldEventEffectsTest.class.getClassLoader()
        .getResourceAsStream("party.yml")) {
      assertNotNull(stream, "party.yml is not on the classpath");
      YamlConfiguration yaml = new YamlConfiguration();
      yaml.load(new InputStreamReader(stream, StandardCharsets.UTF_8));
      assertTrue(yaml.contains("notificationSounds"), "notificationSounds is missing");
      assertTrue(yaml.getBoolean("notificationSounds"), "should be on by default");
    }
  }
}
