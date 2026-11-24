package mc.carlton.freerpg.skills.aeronautics;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Turns distance glided into experience steps. A step is a fixed distance, and each player can earn
 * only so many steps per minute, so flying in circles cannot be farmed. The caller passes in the
 * clock and only reports distance while the player is actually gliding.
 */
public class GlideXpRules {

  private static final long WINDOW_MILLIS = 60_000L;

  private final double stepBlocks;
  private final int maxStepsPerMinute;
  private final Map<UUID, Double> leftover = new HashMap<>();
  private final Map<UUID, long[]> windows = new HashMap<>(); // {windowStart, steps}

  public GlideXpRules(double stepBlocks, int maxStepsPerMinute) {
    this.stepBlocks = stepBlocks;
    this.maxStepsPerMinute = maxStepsPerMinute;
  }

  /** Adds glided distance and returns how many experience steps it earned right now. */
  public synchronized int addDistance(UUID player, double blocks, long nowMillis) {
    if (blocks <= 0 || stepBlocks <= 0) {
      return 0;
    }
    double total = leftover.getOrDefault(player, 0.0) + blocks;
    int steps = (int) Math.floor(total / stepBlocks);
    leftover.put(player, total - steps * stepBlocks);
    if (steps == 0) {
      return 0;
    }
    long[] window = windows.get(player);
    if (window == null || nowMillis - window[0] >= WINDOW_MILLIS) {
      window = new long[] {nowMillis, 0};
      windows.put(player, window);
    }
    int allowed = (int) Math.max(0, Math.min(steps, maxStepsPerMinute - window[1]));
    window[1] += allowed;
    return allowed;
  }

  /** Forgets the unfinished step, e.g. when the player lands. */
  public synchronized void reset(UUID player) {
    leftover.remove(player);
  }
}
