package mc.carlton.freerpg.party;

/** The numbers behind bonus XP sharing. Pure. */
public final class XpShareMath {

  private XpShareMath() {
  }

  /** The share of the earned XP one member receives: the percentage, rounded down. */
  public static int share(int earned, double percent) {
    if (earned <= 0 || percent <= 0) {
      return 0;
    }
    return (int) Math.floor(earned * percent / 100.0);
  }

  /** Whether a member receives shared XP right now. Every condition has to hold. */
  public static boolean eligible(boolean partyOn, boolean memberOn, boolean sameWorld,
      double distanceSquared, double radius, boolean online, boolean afk, boolean spectator) {
    return partyOn && memberOn && sameWorld && online && !afk && !spectator
        && distanceSquared <= radius * radius;
  }
}
