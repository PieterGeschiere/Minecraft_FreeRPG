package mc.carlton.freerpg.quests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.Test;

class WheelRollerTest {

  private static final List<WheelRoller.EnchantChoice> ENCHANTS = List.of(
      new WheelRoller.EnchantChoice("minecraft:mending", 1),
      new WheelRoller.EnchantChoice("minecraft:fortune", 3),
      new WheelRoller.EnchantChoice("minecraft:sharpness", 5));
  private static final List<String> SKILLS = List.of("mining", "mace");
  private static final int[] MC = {100, 1000};
  private static final int[] FRPG = {200, 2000};

  private static WheelPrize roll(Random random, int book, int mc, int frpg) {
    return WheelRoller.rollPrize(random, ENCHANTS, SKILLS, book, mc, frpg, MC, FRPG);
  }

  @Test
  void everyEnchantAndLevelIsReachable() {
    Random random = new Random(11);
    Set<String> seen = new HashSet<>();
    for (int i = 0; i < 20000; i++) {
      WheelPrize prize = roll(random, 1, 0, 0);
      assertEquals(WheelPrize.Kind.BOOK, prize.kind());
      seen.add(prize.enchantKey() + ":" + prize.level());
    }
    assertEquals(9, seen.size(), seen.toString());
  }

  @Test
  void levelNeverExceedsMax() {
    Random random = new Random(12);
    for (int i = 0; i < 5000; i++) {
      WheelPrize prize = roll(random, 1, 0, 0);
      int max = ENCHANTS.stream().filter(e -> e.key().equals(prize.enchantKey())).findFirst()
          .orElseThrow().maxLevel();
      assertTrue(prize.level() >= 1 && prize.level() <= max);
    }
  }

  @Test
  void amountsStayInRange() {
    Random random = new Random(13);
    for (int i = 0; i < 3000; i++) {
      WheelPrize mc = roll(random, 0, 1, 0);
      assertEquals(WheelPrize.Kind.MINECRAFT_EXP, mc.kind());
      assertTrue(mc.amount() >= 100 && mc.amount() <= 1000);
      WheelPrize frpg = roll(random, 0, 0, 1);
      assertEquals(WheelPrize.Kind.FRPG_EXP, frpg.kind());
      assertTrue(frpg.amount() >= 200 && frpg.amount() <= 2000);
      assertTrue(SKILLS.contains(frpg.skill()));
    }
  }

  @Test
  void zeroWeightTypeNeverRolled() {
    Random random = new Random(14);
    for (int i = 0; i < 3000; i++) {
      assertFalse(roll(random, 1, 1, 0).kind() == WheelPrize.Kind.FRPG_EXP);
    }
  }

  @Test
  void allZeroWeightsFallBackToEqual() {
    Random random = new Random(15);
    Set<WheelPrize.Kind> kinds = new HashSet<>();
    for (int i = 0; i < 500; i++) {
      kinds.add(roll(random, 0, 0, 0).kind());
    }
    assertEquals(3, kinds.size());
  }

  @Test
  void emptyEnchantListFallsBackToExpPrize() {
    Random random = new Random(16);
    for (int i = 0; i < 200; i++) {
      WheelPrize prize = WheelRoller.rollPrize(random, List.of(), SKILLS, 1, 0, 0, MC, FRPG);
      assertEquals(WheelPrize.Kind.MINECRAFT_EXP, prize.kind());
    }
  }

  @Test
  void emptySkillListNeverRollsFrpgExp() {
    Random random = new Random(17);
    for (int i = 0; i < 500; i++) {
      WheelPrize prize = WheelRoller.rollPrize(random, ENCHANTS, List.of(), 0, 0, 1, MC, FRPG);
      assertEquals(WheelPrize.Kind.MINECRAFT_EXP, prize.kind());
    }
  }

  @Test
  void rollRingReturnsRequestedSize() {
    List<WheelPrize> ring = WheelRoller.rollRing(new Random(18), ENCHANTS, SKILLS, 1, 1, 1, MC,
        FRPG, 26);
    assertEquals(26, ring.size());
  }

  @Test
  void pickLandingUniform() {
    Random random = new Random(19);
    int[] counts = new int[26];
    for (int i = 0; i < 26000; i++) {
      counts[WheelRoller.pickLanding(random, 26)]++;
    }
    for (int count : counts) {
      assertTrue(count > 600 && count < 1400, "count " + count);
    }
  }
}
