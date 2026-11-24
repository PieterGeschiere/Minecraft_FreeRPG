package mc.carlton.freerpg.skills.building;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * Anti-farming rules for Building experience: no experience for placing at a spot the same player
 * just broke, and a per-material limit per minute. The clock is passed in so it can be tested.
 */
public class BuildingXpRules {

  private static final long WINDOW_MILLIS = 60_000L;
  private static final int PRUNE_THRESHOLD = 5_000;

  private final long graceMillis;
  private final int perMaterialPerMinute;
  private final Map<String, Long> recentBreaks = new HashMap<>();
  private final Map<String, long[]> materialWindows = new HashMap<>(); // {windowStart, count}

  public BuildingXpRules(long graceMillis, int perMaterialPerMinute) {
    this.graceMillis = graceMillis;
    this.perMaterialPerMinute = perMaterialPerMinute;
  }

  public synchronized void recordBreak(UUID player, UUID world, int x, int y, int z,
      long nowMillis) {
    recentBreaks.put(spotKey(player, world, x, y, z), nowMillis);
    if (recentBreaks.size() > PRUNE_THRESHOLD) {
      Iterator<Map.Entry<String, Long>> it = recentBreaks.entrySet().iterator();
      while (it.hasNext()) {
        if (nowMillis - it.next().getValue() >= graceMillis) {
          it.remove();
        }
      }
    }
  }

  /** Whether this placement earns experience; also counts it toward the per-minute limit. */
  public synchronized boolean shouldAward(UUID player, String materialName, UUID world, int x,
      int y, int z, long nowMillis) {
    Long brokenAt = recentBreaks.get(spotKey(player, world, x, y, z));
    if (brokenAt != null && nowMillis - brokenAt < graceMillis) {
      return false;
    }
    String key = player + "|" + materialName;
    long[] window = materialWindows.get(key);
    if (window == null || nowMillis - window[0] >= WINDOW_MILLIS) {
      window = new long[] {nowMillis, 0};
      materialWindows.put(key, window);
    }
    if (window[1] >= perMaterialPerMinute) {
      return false;
    }
    window[1]++;
    return true;
  }

  private static String spotKey(UUID player, UUID world, int x, int y, int z) {
    return player + "|" + world + "|" + BuildingBlockKey.pack(x, y, z);
  }
}
