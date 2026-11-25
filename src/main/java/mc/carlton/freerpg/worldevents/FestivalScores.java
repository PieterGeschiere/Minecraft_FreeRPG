package mc.carlton.freerpg.worldevents;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** The score tracker of one Fishing Festival. */
public final class FestivalScores {

  /** One player's place in the ranking. */
  public record Entry(UUID player, int score) {
  }

  private final Map<UUID, Integer> scores = new HashMap<>();
  /** When each player reached his current score (a counter), to settle ties. */
  private final Map<UUID, Long> reachedAt = new HashMap<>();
  private long counter;

  /** Adds points. Zero and negative amounts are ignored. */
  public synchronized void add(UUID player, int points) {
    if (points <= 0) {
      return;
    }
    scores.merge(player, points, Integer::sum);
    reachedAt.put(player, ++counter);
  }

  public synchronized int score(UUID player) {
    return scores.getOrDefault(player, 0);
  }

  /** Everyone with points, best first; a tie goes to whoever reached the score first. */
  public synchronized List<Entry> ranking() {
    List<Entry> ranking = new ArrayList<>();
    scores.forEach((player, score) -> ranking.add(new Entry(player, score)));
    ranking.sort(Comparator.comparingInt(Entry::score).reversed()
        .thenComparingLong(entry -> reachedAt.get(entry.player())));
    return ranking;
  }
}
