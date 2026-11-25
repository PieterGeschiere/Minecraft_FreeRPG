package mc.carlton.freerpg.worldevents;

/**
 * Bountiful Harvest: farming, fishing and woodcutting give more exp, and crops, logs and fish
 * sometimes drop twice. The bonus drops are made by {@link BonusDropListener} and
 * {@link HarvestFishListener}.
 */
public final class HarvestEvent implements WorldEvent {

  @Override
  public WorldEventType type() {
    return WorldEventType.BOUNTIFUL_HARVEST;
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
}
