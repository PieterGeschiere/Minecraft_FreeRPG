package mc.carlton.freerpg.elites;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

/** The dice of the elite system. No server access, so it can be tested on its own. */
public final class EliteRoller {

  private EliteRoller() {
  }

  /**
   * Whether a spawn may become an elite at all: natural spawns of hostile mobs that are not on the
   * excluded list and did not come from a spawner.
   */
  public static boolean eligible(SpawnReason reason, boolean isMonster, EntityType type,
      boolean hasSpawnerTag, Set<EntityType> excluded) {
    return reason == SpawnReason.NATURAL && isMonster && !hasSpawnerTag
        && !excluded.contains(type);
  }

  /** One roll of the elite chance, multiplied during a Blood Moon. */
  public static boolean rollElite(double chance, boolean bloodMoon, double multiplier,
      Random random) {
    double effective = bloodMoon ? chance * multiplier : chance;
    return effective > 0.0 && random.nextDouble() < effective;
  }

  /** How many affixes (1 to 3) an elite gets, by weight. */
  public static int rollAffixCount(int[] weights, Random random) {
    int total = 0;
    for (int weight : weights) {
      total += weight;
    }
    if (total <= 0) {
      return 1;
    }
    int roll = random.nextInt(total);
    for (int i = 0; i < weights.length; i++) {
      roll -= weights[i];
      if (roll < 0) {
        return i + 1;
      }
    }
    return weights.length;
  }

  /** Picks distinct affix ids, at most as many as are available. */
  public static List<String> pickAffixes(int count, List<String> available, Random random) {
    List<String> pool = new ArrayList<>(available);
    List<String> picked = new ArrayList<>();
    while (picked.size() < count && !pool.isEmpty()) {
      picked.add(pool.remove(random.nextInt(pool.size())));
    }
    return picked;
  }
}
