package mc.carlton.freerpg.skills.aeronautics;

/** The maths behind the Aeronautics passive and perks. Pure, so it can be tested without a server. */
public final class AeronauticsRules {

  /** Rocket power is stored in a byte-sized field; never go past it. */
  public static final int MAX_ROCKET_POWER = 127;

  private AeronauticsRules() {
  }

  /** Share of normal elytra durability loss that still applies (1.0 = normal, 0.5 = half). */
  public static double durabilityMultiplier(int passiveStat, double perPoint, double cap) {
    double removed = Math.min(cap, Math.max(0, passiveStat) * perPoint);
    return 1.0 - Math.max(0.0, removed);
  }

  /** Damage left after Soft Landing; level 0 or an empty table leaves the damage unchanged. */
  public static double damageAfterSoftLanding(double damage, double[] reductions, int level) {
    if (level <= 0 || reductions == null || reductions.length == 0) {
      return damage;
    }
    double reduction = reductions[Math.min(level, reductions.length) - 1];
    return damage * (1.0 - Math.min(1.0, Math.max(0.0, reduction)));
  }

  /** Extra flight power for a boost: the Long Burn level plus Tailwind on the first rocket. */
  public static int extraRocketPower(int[] longBurn, int longBurnLevel, boolean tailwindUnlocked,
      boolean firstRocketSinceTakeoff, int tailwindPower) {
    int extra = 0;
    if (longBurnLevel > 0 && longBurn != null && longBurn.length > 0) {
      extra += longBurn[Math.min(longBurnLevel, longBurn.length) - 1];
    }
    if (tailwindUnlocked && firstRocketSinceTakeoff) {
      extra += tailwindPower;
    }
    return Math.max(0, extra);
  }

  /** The rocket's new power, kept within what a firework can hold. */
  public static int boostedPower(int currentPower, int extra) {
    return Math.min(MAX_ROCKET_POWER, Math.max(0, currentPower) + Math.max(0, extra));
  }

  /** Lore text for the Glide Efficiency passive, e.g. "12.5%". */
  public static String efficiencyLore(int passiveStat, double perPoint, double cap) {
    double removed = 1.0 - durabilityMultiplier(passiveStat, perPoint, cap);
    return String.format(java.util.Locale.ROOT, "%.1f%%", removed * 100.0);
  }
}
