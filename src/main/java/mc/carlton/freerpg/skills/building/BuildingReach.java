package mc.carlton.freerpg.skills.building;

/** Extra block interaction range from the Builder's Reach passive. */
public final class BuildingReach {

  private BuildingReach() {
  }

  public static double bonus(int passiveStat, double perPoint, double cap) {
    return Math.min(cap, Math.max(0, passiveStat) * perPoint);
  }
}
