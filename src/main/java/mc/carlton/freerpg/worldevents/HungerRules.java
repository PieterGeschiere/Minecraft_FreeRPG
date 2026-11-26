package mc.carlton.freerpg.worldevents;

/** The pure rule of Hungry Night: food restores more. */
public final class HungerRules {

  private static final int MAX_FOOD = 20;

  private HungerRules() {
  }

  /** The extra hunger only drains while the player has more than this much food. */
  static boolean shouldDrain(int foodLevel, int minFood) {
    return foodLevel > minFood;
  }

  /** The extra exhaustion added every half second (default 0.05, at most 1). */
  static double exhaustionPerBeat() {
    return Math.max(0.0, Math.min(1.0, Mutator.HUNGRY_NIGHT.number("exhaustionPerBeat", 0.05)));
  }

  /** The food level at which the extra hunger stops (default 6, so it never starves anyone). */
  static int minFood() {
    return Math.max(0, Math.min(MAX_FOOD, (int) Mutator.HUNGRY_NIGHT.number("minFood", 6)));
  }

  /**
   * The food level after eating: the gain is multiplied (rounded up) and the level never goes above
   * 20. A level that does not go up is not changed, and a multiplier below 1 counts as 1.
   */
  static int boostedFood(int oldLevel, int newLevel, double multiplier) {
    if (newLevel <= oldLevel) {
      return newLevel;
    }
    double factor = Math.max(1.0, multiplier);
    int boosted = oldLevel + (int) Math.ceil((newLevel - oldLevel) * factor);
    return Math.min(MAX_FOOD, boosted);
  }
}
