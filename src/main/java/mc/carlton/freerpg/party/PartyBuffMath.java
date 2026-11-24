package mc.carlton.freerpg.party;

/** The numbers behind the party buffs. Pure. */
public final class PartyBuffMath {

  private PartyBuffMath() {
  }

  /**
   * The experience multiplier for a player with {@code nearby} party members close by: every member
   * adds {@code perMember} (0.03 = 3%), up to {@code max} in total. Never below 1.
   */
  public static double xpMultiplier(int nearby, double perMember, double max) {
    if (nearby <= 0 || perMember <= 0 || max <= 0) {
      return 1.0;
    }
    return 1.0 + Math.min(max, nearby * perMember);
  }
}
