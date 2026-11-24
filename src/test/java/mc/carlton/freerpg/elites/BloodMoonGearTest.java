package mc.carlton.freerpg.elites;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;
import org.junit.jupiter.api.Test;

class BloodMoonGearTest {

  @Test
  void dropChancesAreAlwaysLow() {
    assertTrue(BloodMoonGear.DROP_CHANCE > 0.0f && BloodMoonGear.DROP_CHANCE <= 0.05f);
  }

  @Test
  void itemsComeOutDamaged() {
    Random random = new Random(4);
    for (int i = 0; i < 500; i++) {
      double left = BloodMoonGear.leftFraction(random);
      assertTrue(left >= 0.4 && left <= 0.9, "left " + left);
    }
  }

  @Test
  void damageIsTheMissingPartOfTheDurability() {
    assertEquals(60, BloodMoonGear.damageFor(100, 0.4));
    assertEquals(10, BloodMoonGear.damageFor(100, 0.9));
    assertEquals(0, BloodMoonGear.damageFor(0, 0.5));
  }

  @Test
  void blockedByMissingDurabilityNeverGoesNegative() {
    assertTrue(BloodMoonGear.damageFor(250, 1.5) >= 0);
  }
}
