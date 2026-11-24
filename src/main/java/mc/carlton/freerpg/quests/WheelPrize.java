package mc.carlton.freerpg.quests;

/**
 * One prize on the wheel.
 *
 * @param kind       what kind of prize
 * @param enchantKey namespaced enchantment key, like "minecraft:mending" (books only)
 * @param level      enchantment level (books only)
 * @param skill      main skill name or weapon skill config key (FRPG exp only)
 * @param amount     experience amount (exp prizes only)
 */
public record WheelPrize(Kind kind, String enchantKey, int level, String skill, int amount) {

  public enum Kind {
    BOOK,
    MINECRAFT_EXP,
    FRPG_EXP
  }

  public static WheelPrize book(String enchantKey, int level) {
    return new WheelPrize(Kind.BOOK, enchantKey, level, null, 0);
  }

  public static WheelPrize minecraftExp(int amount) {
    return new WheelPrize(Kind.MINECRAFT_EXP, null, 0, null, amount);
  }

  public static WheelPrize frpgExp(String skill, int amount) {
    return new WheelPrize(Kind.FRPG_EXP, null, 0, skill, amount);
  }
}
