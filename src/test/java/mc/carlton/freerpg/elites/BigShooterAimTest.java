package mc.carlton.freerpg.elites;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;
import org.junit.jupiter.api.Test;

class BigShooterAimTest {

  private static double length(double[] v) {
    return Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
  }

  @Test
  void keepsTheSpeed() {
    double[] v = BigShooterAim.velocity(new double[] {0, 0, 0}, new double[] {10, 1, 5}, 1.6, 2.0,
        new Random(1));
    assertEquals(1.6, length(v), 1.0e-9);
  }

  @Test
  void withoutSpreadFlysStraightAtTheTargetAndLiftsTheShot() {
    double[] v = BigShooterAim.velocity(new double[] {0, 0, 0}, new double[] {10, 0, 0}, 1.6, 0.0,
        new Random(1));
    assertTrue(v[0] > 0.0);
    assertEquals(0.0, v[2], 1.0e-9);
    assertTrue(v[1] > 0.0, "a fifth of the flat distance is added to the height");
  }

  @Test
  void aSmallSpreadStaysCloseToTheAimedDirection() {
    double[] aimed = BigShooterAim.velocity(new double[] {0, 0, 0}, new double[] {12, 0, 3}, 1.0,
        0.0, new Random(1));
    Random random = new Random(7);
    for (int i = 0; i < 200; i++) {
      double[] v = BigShooterAim.velocity(new double[] {0, 0, 0}, new double[] {12, 0, 3}, 1.0, 2.0,
          random);
      double dot = v[0] * aimed[0] + v[1] * aimed[1] + v[2] * aimed[2];
      assertTrue(dot > Math.cos(Math.toRadians(12.0)), "shot " + i + " strayed too far");
    }
  }

  @Test
  void standingOnTheTargetGivesNoShot() {
    double[] v = BigShooterAim.velocity(new double[] {1, 1, 1}, new double[] {1, 1, 1}, 1.6, 2.0,
        new Random(1));
    assertEquals(0.0, length(v), 1.0e-9);
  }
}
