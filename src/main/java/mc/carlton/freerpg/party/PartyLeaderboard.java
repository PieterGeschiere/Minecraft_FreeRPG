package mc.carlton.freerpg.party;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/** Ranks parties by the total level of their members. Pure. */
public final class PartyLeaderboard {

  /** One party on the board. */
  public record Entry(String name, int members, long score) {
  }

  private PartyLeaderboard() {
  }

  /** Highest score first; a tie goes to the bigger party, then to the name (A to Z). */
  public static List<Entry> rank(List<Entry> entries) {
    List<Entry> ranked = new ArrayList<>(entries);
    ranked.sort(Comparator.comparingLong(Entry::score).reversed()
        .thenComparing(Comparator.comparingInt(Entry::members).reversed())
        .thenComparing(entry -> entry.name().toLowerCase(Locale.ROOT)));
    return ranked;
  }

  /** The 1-based place of the party (names compare ignoring case), or -1 when it is not there. */
  public static int positionOf(List<Entry> ranked, String partyName) {
    for (int i = 0; i < ranked.size(); i++) {
      if (ranked.get(i).name().equalsIgnoreCase(partyName)) {
        return i + 1;
      }
    }
    return -1;
  }
}
