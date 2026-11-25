package mc.carlton.freerpg.elites;

/**
 * Melee damage of elites and Blood Moon mobs is raised through the attack damage attribute. Arrows
 * and explosions do not use that attribute, so their damage is scaled here instead.
 */
public final class DamageScaling {

  private DamageScaling() {
  }

  /**
   * The factor for one hit: 1.0 for melee (the attribute already did it), otherwise the elite
   * multiplier and/or the Blood Moon bonus (1 + bonus), multiplied when both apply.
   */
  public static double factor(boolean rangedOrExplosion, boolean elite, boolean bloodMoonMob,
      double eliteMultiplier, double bloodMoonBonus) {
    if (!rangedOrExplosion) {
      return 1.0;
    }
    double factor = 1.0;
    if (elite) {
      factor *= eliteMultiplier;
    }
    if (bloodMoonMob) {
      factor *= 1.0 + bloodMoonBonus;
    }
    return factor;
  }
}
