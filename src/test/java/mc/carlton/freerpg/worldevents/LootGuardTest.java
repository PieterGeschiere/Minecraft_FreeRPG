package mc.carlton.freerpg.worldevents;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.function.BiPredicate;
import org.junit.jupiter.api.Test;

class LootGuardTest {

  /** Stand-in for ItemStack.isSimilar (which needs a running server): same name before the colon. */
  private static final BiPredicate<String, String> SAME =
      (a, b) -> a.split(":")[0].equals(b.split(":")[0]);

  @Test
  void aCertainDropChanceMeansTheMobPickedItUpOrWasGivenIt() {
    assertTrue(LootGuard.isSticky(1.0f));
    assertTrue(LootGuard.isSticky(2.0f), "vanilla sets 2.0 for picked up items");
    assertFalse(LootGuard.isSticky(0.085f), "the normal chance of worn gear");
  }

  @Test
  void itemsTheMobPickedUpAreNeverPartOfTheCopyableLoot() {
    LootGuard.Split<String> split = LootGuard.splitBy(
        List.of("DIAMOND:64", "ROTTEN_FLESH:2", "BONE:1"), List.of("DIAMOND:64"), SAME);
    assertEquals(List.of("DIAMOND:64"), split.sticky());
    assertEquals(List.of("ROTTEN_FLESH:2", "BONE:1"), split.loot());
  }

  @Test
  void onlyAsManyDropsAreProtectedAsTheMobCarried() {
    LootGuard.Split<String> split = LootGuard.splitBy(
        List.of("IRON_SWORD:1", "IRON_SWORD:1"), List.of("IRON_SWORD:1"), SAME);
    assertEquals(1, split.sticky().size());
    assertEquals(1, split.loot().size());
  }

  @Test
  void withNothingCarriedEverythingIsLoot() {
    LootGuard.Split<String> split = LootGuard.splitBy(List.of("BONE:1"), List.of(), SAME);
    assertTrue(split.sticky().isEmpty());
    assertEquals(List.of("BONE:1"), split.loot());
    assertTrue(LootGuard.splitBy(List.<String>of(), List.of("DIAMOND:1"), SAME).loot().isEmpty());
  }
}
