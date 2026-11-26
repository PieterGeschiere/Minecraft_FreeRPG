package mc.carlton.freerpg.worldevents;

import java.util.Random;

/** The pure rules of Titan Night: how big a mob grows and how much tougher that makes it. */
public final class TitanRules {

  private TitanRules() {
  }

  /** A random size between the bounds (swapped bounds are fixed, never smaller than normal). */
  static double pickScale(double min, double max, Random random) {
    double low = Math.max(1.0, Math.min(min, max));
    double high = Math.max(1.0, Math.max(min, max));
    if (high <= low) {
      return low;
    }
    return low + random.nextDouble() * (high - low);
  }

  /**
   * The biggest size up to {@code desired} that still fits: {@code freeBlocks} is the free height
   * from the mob's feet upward, and {@code reserve} is extra room kept for a later size boost (an
   * elite). Never below normal size. An unknown height or reserve means no growth.
   */
  static double fitScale(double desired, double baseHeight, int freeBlocks, double reserve) {
    if (baseHeight <= 0.0 || reserve <= 0.0) {
      return 1.0;
    }
    double limit = Math.max(0, freeBlocks) / (baseHeight * reserve);
    return Math.max(1.0, Math.min(desired, limit));
  }

  /**
   * The biggest size up to {@code desired} whose whole box still fits: {@code free} says whether a
   * box of the given width and height is free of solid blocks around the mob. The reserve keeps
   * room for an elite's size boost. Never below normal size; an unknown size means no growth.
   */
  static double fitBox(double desired, double baseWidth, double baseHeight, double reserve,
      java.util.function.BiPredicate<Double, Double> free) {
    if (baseWidth <= 0.0 || baseHeight <= 0.0 || reserve <= 0.0) {
      return 1.0;
    }
    double scale = Math.max(1.0, desired);
    while (scale > 1.0 && !free.test(baseWidth * scale * reserve, baseHeight * scale * reserve)) {
      scale -= 0.05;
    }
    return Math.max(1.0, scale);
  }

  /** A mob that carries riders (jockeys) or rides something is left alone. */
  static boolean skipBecauseOfRiders(boolean hasPassengers, boolean insideVehicle) {
    return hasPassengers || insideVehicle;
  }

  /** The extra kill exp on top of the normal kill exp, so the total is {@code multiplier} times. */
  static int bonusExp(int baseExp, double multiplier) {
    if (baseExp <= 0 || multiplier <= 1.0) {
      return 0;
    }
    return (int) Math.floor(baseExp * (multiplier - 1.0));
  }

  /** The kill exp multiplier of a Titan kill. */
  static double killExpMultiplier() {
    return Math.max(1.0, WorldEventConfig.number(WorldEventType.TITAN_NIGHT, "xpMultiplier", 1.5));
  }

  /** A mob that would barely grow is left alone. */
  static boolean worthScaling(double scale) {
    return scale >= 1.15;
  }

  static double healthFactor(double scale, double perScale) {
    return 1.0 + Math.max(0.0, scale - 1.0) * Math.max(0.0, perScale);
  }

  static double damageFactor(double scale, double perScale) {
    return 1.0 + Math.max(0.0, scale - 1.0) * Math.max(0.0, perScale);
  }
}
