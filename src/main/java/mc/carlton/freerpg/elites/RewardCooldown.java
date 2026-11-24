package mc.carlton.freerpg.elites;

import java.util.HashMap;
import java.util.Map;

/**
 * Lets a place (a chunk) pay out elite rewards only once per cooldown, so a farm that happens to
 * escape the exp farm detection cannot turn elites into a steady source of drops.
 */
public final class RewardCooldown {

  private static final int PRUNE_AT = 2000;

  private final Map<String, Long> lastClaim = new HashMap<>();

  /** True (and the claim is recorded) when this key has not been paid out within the cooldown. */
  public boolean tryClaim(String key, long nowMillis, long cooldownMillis) {
    if (cooldownMillis <= 0) {
      return true;
    }
    Long last = lastClaim.get(key);
    if (last != null && nowMillis - last < cooldownMillis) {
      return false;
    }
    if (lastClaim.size() >= PRUNE_AT) {
      lastClaim.values().removeIf(time -> nowMillis - time >= cooldownMillis);
    }
    lastClaim.put(key, nowMillis);
    return true;
  }
}
