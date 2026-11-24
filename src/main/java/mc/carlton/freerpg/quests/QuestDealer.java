package mc.carlton.freerpg.quests;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Picks the quests of a day. Pure logic, no Bukkit. */
public final class QuestDealer {

  private QuestDealer() {
  }

  /** Weighted draw without replacement of up to {@code count} distinct quests. */
  public static List<QuestDefinition> deal(List<QuestDefinition> pool, int count, Random random) {
    List<QuestDefinition> candidates = new ArrayList<>();
    for (QuestDefinition def : pool) {
      if (def.weight() > 0) {
        candidates.add(def);
      }
    }
    List<QuestDefinition> dealt = new ArrayList<>();
    while (dealt.size() < count && !candidates.isEmpty()) {
      long total = 0;
      for (QuestDefinition def : candidates) {
        total += def.weight();
      }
      long roll = (long) (random.nextDouble() * total);
      int index = 0;
      for (; index < candidates.size() - 1; index++) {
        roll -= candidates.get(index).weight();
        if (roll < 0) {
          break;
        }
      }
      dealt.add(candidates.remove(index));
    }
    return dealt;
  }

  /** A target amount uniformly between the definition's bounds (never below 1). */
  public static int rollTarget(QuestDefinition def, Random random) {
    int min = Math.max(1, def.minTarget());
    int max = Math.max(min, def.maxTarget());
    return min + random.nextInt(max - min + 1);
  }
}
