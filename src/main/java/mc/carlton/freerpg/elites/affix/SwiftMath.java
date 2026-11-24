package mc.carlton.freerpg.elites.affix;

import org.bukkit.util.Vector;

/** The lunge of the Swift affix, kept apart so it can be tested without a server. */
public final class SwiftMath {

  private SwiftMath() {
  }

  /**
   * The velocity to add to a mob at {@code from} that lunges at {@code to}: 0.7 horizontally
   * towards the target and a small hop. Never contains NaN, also when the target is straight above
   * or below.
   */
  public static Vector lunge(Vector from, Vector to) {
    double dx = to.getX() - from.getX();
    double dz = to.getZ() - from.getZ();
    double length = Math.sqrt(dx * dx + dz * dz);
    if (length < 1.0e-6) {
      return new Vector(0.0, 0.25, 0.0);
    }
    return new Vector(dx / length * 0.7, 0.25, dz / length * 0.7);
  }
}
