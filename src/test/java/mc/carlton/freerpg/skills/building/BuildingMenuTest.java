package mc.carlton.freerpg.skills.building;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class BuildingMenuTest {

  @Test
  void reachIsShownWithTwoDecimals() {
    assertEquals("+0.25", BuildingMenu.reachLore(25, 0.01, 3.0));
    assertEquals("+0.00", BuildingMenu.reachLore(0, 0.01, 3.0));
    assertEquals("+1.07", BuildingMenu.reachLore(107, 0.01, 3.0));
  }

  @Test
  void reachIsCapped() {
    assertEquals("+3.00", BuildingMenu.reachLore(10000, 0.01, 3.0));
    assertEquals("+3.00", BuildingMenu.reachLore(300, 0.01, 3.0));
  }

  @Test
  void reachDoesNotDependOnTheServerLocale() {
    java.util.Locale old = java.util.Locale.getDefault();
    try {
      java.util.Locale.setDefault(java.util.Locale.GERMANY);
      assertEquals("+0.25", BuildingMenu.reachLore(25, 0.01, 3.0));
    } finally {
      java.util.Locale.setDefault(old);
    }
  }

  @Test
  void aNegativePointCountOrCapGivesNoReach() {
    assertEquals("+0.00", BuildingMenu.reachLore(-5, 0.01, 3.0));
    assertEquals("+0.00", BuildingMenu.reachLore(50, 0.01, -1.0));
  }
}
