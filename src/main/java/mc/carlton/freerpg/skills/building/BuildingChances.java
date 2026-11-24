package mc.carlton.freerpg.skills.building;

/** Turns a perk level into a chance and rolls it. */
public final class BuildingChances {

  private BuildingChances() {
  }

  /** 0.0 for level 0 or below, the last entry for levels above the table. */
  public static double chanceForLevel(double[] perLevel, int level) {
    if (level <= 0 || perLevel == null || perLevel.length == 0) {
      return 0.0;
    }
    return perLevel[Math.min(level, perLevel.length) - 1];
  }

  public static boolean survives(double chance, double roll) {
    return roll < chance;
  }
}
