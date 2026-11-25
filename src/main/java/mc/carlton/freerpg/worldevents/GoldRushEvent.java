package mc.carlton.freerpg.worldevents;

import org.bukkit.Sound;

/**
 * Gold Rush: mining gives more exp and ore sometimes drops twice (see {@link BonusDropListener}),
 * but breaking natural ore is loud and can draw mobs (see {@link GoldRushNoiseListener}).
 */
public final class GoldRushEvent implements WorldEvent {

  @Override
  public WorldEventType type() {
    return WorldEventType.GOLD_RUSH;
  }

  @Override
  public boolean canStart() {
    return true;
  }

  @Override
  public void onStart(ActiveEvent event) {
  }

  @Override
  public void onEnd(ActiveEvent event, boolean byAdmin) {
  }

  @Override
  public double xpMultiplier(String skillName) {
    return EventDefaults.skills(type()).contains(skillName) ? EventDefaults.xpMultiplier(type())
        : 1.0;
  }

  @Override
  public Sound announceSound() {
    return Sound.BLOCK_AMETHYST_BLOCK_CHIME;
  }
}
