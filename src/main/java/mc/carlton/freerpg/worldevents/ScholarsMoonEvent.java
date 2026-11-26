package mc.carlton.freerpg.worldevents;

import org.bukkit.Sound;

/**
 * Scholar's Moon: enchanting and alchemy give more exp, and enchanting costs fewer levels (see
 * {@link ScholarEnchantListener}).
 */
public final class ScholarsMoonEvent extends SkillBoostEvent {

  public ScholarsMoonEvent() {
    super(WorldEventType.SCHOLARS_MOON);
  }

  /**
   * How many of the levels an enchant cost are given back. {@code multiplier} is the share that is
   * still paid (0.5 = half, rounded down, so the 1 level button becomes free).
   */
  static int levelRefund(int levelsSpent, double multiplier) {
    int spent = Math.max(0, levelsSpent);
    double share = Math.max(0.0, Math.min(1.0, multiplier));
    return spent - (int) Math.floor(spent * share);
  }

  /** The share (0 to 1) of the levels that is still paid while the event runs. */
  static double costMultiplier() {
    return Math.max(0.0, Math.min(1.0,
        WorldEventConfig.number(WorldEventType.SCHOLARS_MOON, "levelCostMultiplier", 0.5)));
  }

  @Override
  public Sound announceSound() {
    return Sound.BLOCK_ENCHANTMENT_TABLE_USE;
  }
}
