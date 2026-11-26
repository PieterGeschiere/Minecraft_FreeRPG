package mc.carlton.freerpg.elites;

import java.util.Random;

/** The aim math of {@link BigShooterListener}, free of the server so it can be tested. */
public final class BigShooterAim {

  /** Only mobs at least this much bigger than normal get the help. */
  static final double MIN_SCALE = 1.15;

  private BigShooterAim() {
  }

  /**
   * The launch velocity that flies from the start to the target, as {x, y, z}. It lifts the shot
   * by a fifth of the flat distance, like a vanilla skeleton, and then adds a random miss of about
   * {@code spreadDegrees}. The speed stays {@code speed}.
   */
  static double[] velocity(double[] from, double[] to, double speed, double spreadDegrees,
      Random random) {
    double dx = to[0] - from[0];
    double dz = to[2] - from[2];
    double flat = Math.sqrt(dx * dx + dz * dz);
    double dy = to[1] - from[1] + flat * 0.2;
    double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
    if (length < 1.0e-6 || speed <= 0.0) {
      return new double[] {0.0, 0.0, 0.0};
    }
    double miss = Math.tan(Math.toRadians(Math.max(0.0, spreadDegrees))) / 2.0;
    double x = dx / length + random.nextGaussian() * miss;
    double y = dy / length + random.nextGaussian() * miss;
    double z = dz / length + random.nextGaussian() * miss;
    double norm = Math.sqrt(x * x + y * y + z * z);
    return new double[] {x / norm * speed, y / norm * speed, z / norm * speed};
  }
}
