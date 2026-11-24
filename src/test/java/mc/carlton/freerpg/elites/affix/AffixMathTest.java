package mc.carlton.freerpg.elites.affix;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.bukkit.Location;
import org.junit.jupiter.api.Test;

class AffixMathTest {

  @Test
  void armoredCutsDamageByThePercentage() {
    assertEquals(7.0, Armored.reduce(10.0, 0.3), 1e-9);
    assertEquals(10.0, Armored.reduce(10.0, 0.0), 1e-9);
  }

  @Test
  void thornsReflectsAFractionOfTheDamage() {
    assertEquals(2.5, Thorns.reflected(10.0, 0.25), 1e-9);
  }

  @Test
  void vampiricNeverHealsMoreThanTheMissingHealth() {
    assertEquals(5.0, Vampiric.healAmount(10.0, 0.5, 20.0), 1e-9);
    assertEquals(2.0, Vampiric.healAmount(10.0, 0.5, 2.0), 1e-9);
    assertEquals(0.0, Vampiric.healAmount(10.0, 0.5, 0.0), 1e-9);
  }

  @Test
  void splittingAlwaysMakesAtLeastOneCopy() {
    assertEquals(1, Splitting.childCount(0));
    assertEquals(1, Splitting.childCount(-4));
    assertEquals(3, Splitting.childCount(3));
  }

  @Test
  void summonerNeverGoesOverTheMaximumAlive() {
    assertEquals(2, Summoner.minionsToSpawn(0, 2, 6));
    assertEquals(1, Summoner.minionsToSpawn(5, 2, 6));
    assertEquals(0, Summoner.minionsToSpawn(6, 2, 6));
    assertEquals(0, Summoner.minionsToSpawn(9, 2, 6));
  }

  @Test
  void teleporterLandsBehindTheTarget() {
    Location target = new Location(null, 10, 64, 10, 0f, 0f); // yaw 0 looks towards +z
    Location behind = Teleporter.behind(target, 2.0);
    assertEquals(10.0, behind.getX(), 1e-6);
    assertEquals(64.0, behind.getY(), 1e-6);
    assertEquals(8.0, behind.getZ(), 1e-6);
  }
}
