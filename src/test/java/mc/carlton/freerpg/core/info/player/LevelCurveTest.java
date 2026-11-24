package mc.carlton.freerpg.core.info.player;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class LevelCurveTest {

  // Shipped advancedConfig.yml values
  private final LevelCurve curve = new LevelCurve(1.00282543, 1000, 10_000_000, 1000, 30_000);

  @Test
  void goldenValues() {
    assertEquals(1788, curve.expFromLevel(1));
    assertEquals(18110, curve.expFromLevel(10));
    assertEquals(95884, curve.expFromLevel(50));
    assertEquals(206295, curve.expFromLevel(100));
    assertEquals(10_000_000, curve.expFromLevel(1000));
    assertEquals(10_300_000, curve.expFromLevel(1010));
  }

  @Test
  void levelZeroNeedsNoExp() {
    assertEquals(0, curve.expFromLevel(0));
  }

  @Test
  void levelFromExpInvertsExpFromLevel() {
    // The exact threshold of a level reaches that level (this used to land one level low).
    assertEquals(0, curve.levelFromExp(curve.expFromLevel(0)));
    for (int n = 1; n <= 200; n++) {
      assertEquals(n, curve.levelFromExp(curve.expFromLevel(n)), "threshold of level " + n);
      assertEquals(n, curve.levelFromExp(curve.expFromLevel(n) + 1), "level " + n);
    }
  }

  @Test
  void oneExpShortStaysOnPreviousLevel() {
    for (int n = 1; n <= 200; n++) {
      assertEquals(n - 1, curve.levelFromExp(curve.expFromLevel(n) - 1), "level " + n);
    }
  }

  @Test
  void expFromLevelStrictlyIncreasing() {
    for (int n = 1; n <= 200; n++) {
      assertTrue(curve.expFromLevel(n) > curve.expFromLevel(n - 1), "level " + n);
    }
  }

  @Test
  void linearRegionAddsLinearExpPerLevel() {
    for (int n = 1001; n <= 1100; n++) {
      assertEquals(30_000, curve.expFromLevel(n) - curve.expFromLevel(n - 1), "level " + n);
    }
    assertEquals(1010, curve.levelFromExp(10_300_000));
    assertEquals(1009, curve.levelFromExp(10_299_999));
  }

  @Test
  void tokensGained() {
    assertEquals(1, LevelCurve.tokensGained(9, 10, 5));
    assertEquals(0, LevelCurve.tokensGained(10, 14, 5));
    assertEquals(5, LevelCurve.tokensGained(0, 25, 5));
    assertEquals(0, LevelCurve.tokensGained(7, 7, 5));
  }
}
