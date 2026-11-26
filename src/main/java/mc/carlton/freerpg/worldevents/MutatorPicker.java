package mc.carlton.freerpg.worldevents;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/** Picks the twists of a Mutator Night: weighted, different from each other. */
public final class MutatorPicker {

  private MutatorPicker() {
  }

  /** The twists that are switched on and have a weight, with their weights. */
  static Map<Mutator, Integer> available() {
    Map<Mutator, Integer> available = new EnumMap<>(Mutator.class);
    for (Mutator mutator : Mutator.values()) {
      int weight = mutator.weight();
      if (mutator.enabled() && weight > 0) {
        available.put(mutator, weight);
      }
    }
    return available;
  }

  /**
   * Picks up to {@code count} different twists by weight. Entries without weight are never picked;
   * fewer are returned when fewer are available. Never throws.
   */
  static Set<Mutator> pick(Map<Mutator, Integer> weights, int count, Random random) {
    Set<Mutator> picked = EnumSet.noneOf(Mutator.class);
    List<Mutator> pool = new ArrayList<>();
    for (Map.Entry<Mutator, Integer> entry : weights.entrySet()) {
      if (entry.getValue() != null && entry.getValue() > 0) {
        pool.add(entry.getKey());
      }
    }
    while (picked.size() < count && !pool.isEmpty()) {
      long total = 0;
      for (Mutator mutator : pool) {
        total += weights.get(mutator);
      }
      long roll = (long) (random.nextDouble() * total);
      Mutator chosen = pool.get(pool.size() - 1);
      for (Mutator mutator : pool) {
        roll -= weights.get(mutator);
        if (roll < 0) {
          chosen = mutator;
          break;
        }
      }
      picked.add(chosen);
      pool.remove(chosen);
    }
    return picked;
  }
}
