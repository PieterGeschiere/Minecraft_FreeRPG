package mc.carlton.freerpg.skills.building;

import java.util.Locale;

/** Text for the Building skill tree menu. */
public final class BuildingMenu {

  private BuildingMenu() {
  }

  /**
   * The extra block reach of Builder's Reach as shown in the menu, e.g. {@code "+0.25"}: two
   * decimals, never below zero and never above the cap.
   */
  public static String reachLore(int passiveStat, double perPoint, double cap) {
    double reach = Math.max(0.0, Math.min(passiveStat * perPoint, Math.max(0.0, cap)));
    return String.format(Locale.ROOT, "+%.2f", reach);
  }
}
