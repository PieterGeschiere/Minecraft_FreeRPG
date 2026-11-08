package mc.carlton.freerpg.core.info.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Year-based Minecraft versions (26.1 and later) must always be above any 1.x version gate. */
class MinecraftVersionTest {

  private static final double EPS = 1e-9;

  @Test
  void yearBasedVersionsBecomeYearPlusMinorOverHundred() {
    assertEquals(26.03, MinecraftVersion.parseYearBasedVersion("26.3-R0.1-SNAPSHOT"), EPS);
    assertEquals(26.1, MinecraftVersion.parseYearBasedVersion("26.10-R0.1-SNAPSHOT"), EPS);
    assertEquals(27.0, MinecraftVersion.parseYearBasedVersion("27.0"), EPS);
  }

  @Test
  void oldVersionsAreNotYearBased() {
    assertEquals(-1, MinecraftVersion.parseYearBasedVersion("1.21.4-R0.1-SNAPSHOT"), EPS);
    assertEquals(-1, MinecraftVersion.parseYearBasedVersion("1.18"), EPS);
    assertEquals(-1, MinecraftVersion.parseYearBasedVersion(""), EPS);
    assertEquals(-1, MinecraftVersion.parseYearBasedVersion("unknown"), EPS);
  }

  @Test
  void yearBasedVersionIsAboveEveryOldVersionGate() {
    double version = MinecraftVersion.parseYearBasedVersion("26.3-R0.1-SNAPSHOT");
    for (double gate : new double[]{1.16, 1.17, 1.18, 1.21}) {
      assertTrue(version >= gate, "26.3 must pass the gate " + gate);
    }
  }
}
