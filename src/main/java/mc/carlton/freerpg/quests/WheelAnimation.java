package mc.carlton.freerpg.quests;

/** The shape and timing of the wheel animation. Pure logic, no Bukkit. */
public final class WheelAnimation {

  /**
   * The 26 outer slots of a 54-slot chest in clockwise order: the top row left to right, down the
   * right side, the bottom row right to left, then up the left side.
   */
  public static final int[] RING_SLOTS = {
      0, 1, 2, 3, 4, 5, 6, 7, 8,
      17, 26, 35, 44,
      53, 52, 51, 50, 49, 48, 47, 46, 45,
      36, 27, 18, 9
  };

  private WheelAnimation() {
  }

  /**
   * The ring positions the highlight visits, starting next to position 0, taking at least
   * {@code minSteps} steps and ending exactly on {@code landingIndex}.
   */
  public static int[] stepSequence(int ringSize, int landingIndex, int minSteps) {
    int steps = Math.max(1, minSteps);
    while (steps % ringSize != landingIndex) {
      steps++;
    }
    int[] sequence = new int[steps];
    for (int i = 0; i < steps; i++) {
      sequence[i] = (i + 1) % ringSize;
    }
    return sequence;
  }

  /**
   * Ticks to wait after a step: 1 tick for the first 60 % of the steps, then slower and slower,
   * and at least 8 ticks before the last step so the stop feels heavy.
   */
  public static int delayTicks(int step, int totalSteps) {
    int fast = (int) (totalSteps * 0.6);
    int delay = 1;
    if (step >= fast) {
      int slowCount = totalSteps - fast;
      int k = step - fast;
      delay = 2 + (k * 7) / Math.max(1, slowCount - 1);
    }
    if (step == totalSteps - 1) {
      delay = Math.max(delay, 8);
    }
    return delay;
  }
}
