package mc.carlton.freerpg.quests;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Rolls the prizes on the wheel and where it lands. Pure logic, no Bukkit. */
public final class WheelRoller {

  /** An enchantment the wheel can give, with its highest level. */
  public record EnchantChoice(String key, int maxLevel) {
  }

  private WheelRoller() {
  }

  /**
   * Rolls one prize: the kind by weight, then everything inside it uniformly at random (any
   * enchantment at any level from 1 to its maximum, or an amount within the range). A kind that
   * cannot be given (no enchantments, no skills) is skipped, and Minecraft exp is the fallback.
   */
  public static WheelPrize rollPrize(Random random, List<EnchantChoice> enchants,
                                     List<String> skills, int weightBook, int weightMinecraftExp,
                                     int weightFrpgExp, int[] minecraftExpRange,
                                     int[] frpgExpRange) {
    int book = Math.max(0, weightBook);
    int minecraft = Math.max(0, weightMinecraftExp);
    int frpg = Math.max(0, weightFrpgExp);
    if (book + minecraft + frpg == 0) {
      book = 1;
      minecraft = 1;
      frpg = 1;
    }
    if (enchants.isEmpty()) {
      book = 0;
    }
    if (skills.isEmpty()) {
      frpg = 0;
    }
    int total = book + minecraft + frpg;
    if (total == 0) {
      return WheelPrize.minecraftExp(amount(random, minecraftExpRange));
    }
    int roll = random.nextInt(total);
    if (roll < book) {
      EnchantChoice choice = enchants.get(random.nextInt(enchants.size()));
      int level = 1 + random.nextInt(Math.max(1, choice.maxLevel()));
      return WheelPrize.book(choice.key(), level);
    }
    if (roll < book + minecraft) {
      return WheelPrize.minecraftExp(amount(random, minecraftExpRange));
    }
    return WheelPrize.frpgExp(skills.get(random.nextInt(skills.size())),
        amount(random, frpgExpRange));
  }

  /** Rolls {@code size} prizes for the ring. */
  public static List<WheelPrize> rollRing(Random random, List<EnchantChoice> enchants,
                                          List<String> skills, int weightBook,
                                          int weightMinecraftExp, int weightFrpgExp,
                                          int[] minecraftExpRange, int[] frpgExpRange,
                                          int size) {
    List<WheelPrize> ring = new ArrayList<>();
    for (int i = 0; i < size; i++) {
      ring.add(rollPrize(random, enchants, skills, weightBook, weightMinecraftExp, weightFrpgExp,
          minecraftExpRange, frpgExpRange));
    }
    return ring;
  }

  /** The ring position the wheel lands on, uniformly at random. */
  public static int pickLanding(Random random, int size) {
    return random.nextInt(size);
  }

  private static int amount(Random random, int[] range) {
    int min = Math.max(1, range[0]);
    int max = Math.max(min, range[1]);
    return min + random.nextInt(max - min + 1);
  }
}
