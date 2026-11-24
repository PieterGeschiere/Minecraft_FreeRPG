package mc.carlton.freerpg.skills.building;

/** Durability maths for Demolition Expert. */
public final class DemolitionRules {

  private DemolitionRules() {
  }

  /** Expected value is damage * multiplier: round down, plus one extra point when roll < fraction. */
  public static int reducedDamage(int damage, double multiplier, double roll) {
    double scaled = damage * multiplier;
    int base = (int) Math.floor(scaled);
    return base + (roll < scaled - base ? 1 : 0);
  }
}
