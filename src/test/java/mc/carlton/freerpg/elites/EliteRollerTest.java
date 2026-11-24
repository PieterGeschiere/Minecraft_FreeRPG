package mc.carlton.freerpg.elites;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.junit.jupiter.api.Test;

class EliteRollerTest {

  private static final Set<EntityType> EXCLUDED = Set.of(EntityType.WARDEN);

  @Test
  void onlyNaturalSpawnsAreEligible() {
    for (SpawnReason reason : SpawnReason.values()) {
      boolean eligible = EliteRoller.eligible(reason, true, EntityType.ZOMBIE, false, EXCLUDED);
      assertEquals(reason == SpawnReason.NATURAL, eligible, reason.name());
    }
  }

  @Test
  void excludedTypesAreNotEligible() {
    assertFalse(EliteRoller.eligible(SpawnReason.NATURAL, true, EntityType.WARDEN, false, EXCLUDED));
  }

  @Test
  void nonMonstersAreNotEligible() {
    assertFalse(EliteRoller.eligible(SpawnReason.NATURAL, false, EntityType.COW, false, EXCLUDED));
  }

  @Test
  void spawnerTaggedMobIsNotEligible() {
    assertFalse(EliteRoller.eligible(SpawnReason.NATURAL, true, EntityType.ZOMBIE, true, EXCLUDED));
  }

  @Test
  void bloodMoonMultipliesChance() {
    Random random = new Random(42);
    int normal = 0;
    int bloody = 0;
    for (int i = 0; i < 20000; i++) {
      if (EliteRoller.rollElite(0.03, false, 4.0, random)) {
        normal++;
      }
      if (EliteRoller.rollElite(0.03, true, 4.0, random)) {
        bloody++;
      }
    }
    assertTrue(Math.abs(normal / 20000.0 - 0.03) < 0.01, "normal rate " + normal);
    assertTrue(Math.abs(bloody / 20000.0 - 0.12) < 0.02, "blood moon rate " + bloody);
  }

  @Test
  void chanceOverOneAlwaysRollsAndZeroNever() {
    Random random = new Random(1);
    for (int i = 0; i < 100; i++) {
      assertTrue(EliteRoller.rollElite(0.5, true, 4.0, random));
      assertFalse(EliteRoller.rollElite(0.0, true, 4.0, random));
    }
  }

  @Test
  void affixCountFollowsWeights() {
    Random random = new Random(7);
    int[] counts = new int[4];
    for (int i = 0; i < 20000; i++) {
      counts[EliteRoller.rollAffixCount(new int[] {60, 30, 10}, random)]++;
    }
    assertEquals(0, counts[0]);
    assertTrue(Math.abs(counts[1] / 20000.0 - 0.6) < 0.03);
    assertTrue(Math.abs(counts[2] / 20000.0 - 0.3) < 0.03);
    assertTrue(Math.abs(counts[3] / 20000.0 - 0.1) < 0.03);
  }

  @Test
  void pickAffixesNeverDuplicates() {
    Random random = new Random(3);
    List<String> available = List.of("a", "b", "c", "d", "e");
    for (int i = 0; i < 200; i++) {
      List<String> picked = EliteRoller.pickAffixes(3, available, random);
      assertEquals(3, picked.size());
      assertEquals(3, new HashSet<>(picked).size());
    }
  }

  @Test
  void pickAffixesCapsAtAvailable() {
    assertEquals(2, EliteRoller.pickAffixes(3, List.of("a", "b"), new Random(1)).size());
    assertTrue(EliteRoller.pickAffixes(2, List.of(), new Random(1)).isEmpty());
  }
}
