package mc.carlton.freerpg.worldevents;

import org.bukkit.Sound;

/**
 * Forge Fever: smelting and repair give more exp, and flame pick smelts a double drop more often
 * (only for players who have the flame pick perk).
 */
public final class ForgeFeverEvent extends SkillBoostEvent {

  public ForgeFeverEvent() {
    super(WorldEventType.FORGE_FEVER);
  }

  @Override
  public double flamePickBonus() {
    return Math.max(0.0, WorldEventConfig.number(type(), "flamePickBonus", 0.25));
  }

  @Override
  public Sound announceSound() {
    return Sound.BLOCK_ANVIL_USE;
  }
}
