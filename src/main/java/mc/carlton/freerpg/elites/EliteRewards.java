package mc.carlton.freerpg.elites;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.bukkit.Material;

/** The rules for what killing an elite gives. No server access, so it can be tested on its own. */
public final class EliteRewards {

  private EliteRewards() {
  }

  /**
   * Whether a kill earns the elite bonus: a real elite (not a minion of Splitting or Summoner)
   * that did not come from a spawner and was not killed in an exp farm.
   */
  public static boolean qualifies(boolean isElite, boolean isMinion, boolean spawnerTagged,
      boolean expFarm) {
    return isElite && !isMinion && !spawnerTagged && !expFarm;
  }

  /** Minions of Splitting and Summoner drop nothing and give no vanilla exp (no farming). */
  public static boolean suppressLoot(boolean isMinion) {
    return isMinion;
  }

  /** The extra exp on top of the normal kill exp, so that the total is {@code multiplier} times. */
  public static int bonusExp(int baseExp, double multiplier) {
    if (baseExp <= 0 || multiplier <= 1.0) {
      return 0;
    }
    return (int) Math.floor(baseExp * (multiplier - 1.0));
  }

  /** The skill that receives the bonus: the weapon's own skill, Defense for everything else. */
  public static String skillFor(boolean holdingSword, boolean holdingAxe) {
    if (holdingSword) {
      return "swordsmanship";
    }
    if (holdingAxe) {
      return "axeMastery";
    }
    return "defense";
  }

  /** Rolls every drop entry once and returns the amounts that dropped. */
  public static Map<Material, Integer> rollDrops(List<EliteConfig.DropEntry> entries,
      Random random) {
    Map<Material, Integer> drops = new EnumMap<>(Material.class);
    for (EliteConfig.DropEntry entry : entries) {
      if (entry.chance() <= 0.0 || random.nextDouble() >= entry.chance()) {
        continue;
      }
      int amount = entry.min() + random.nextInt(entry.max() - entry.min() + 1);
      drops.merge(entry.material(), amount, Integer::sum);
    }
    return drops;
  }
}
