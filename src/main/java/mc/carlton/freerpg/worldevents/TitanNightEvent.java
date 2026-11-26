package mc.carlton.freerpg.worldevents;

import org.bukkit.Sound;

/**
 * Titan Night: natural hostile mobs spawn bigger and tougher ({@link TitanSpawnListener}). Kills
 * of Titan mobs give bonus exp and sometimes double drops ({@link TitanLootListener}); nothing
 * changes for kills of other mobs, so spawner farms get no bonus.
 */
public final class TitanNightEvent implements WorldEvent {

  @Override
  public WorldEventType type() {
    return WorldEventType.TITAN_NIGHT;
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
  public Sound announceSound() {
    return Sound.ENTITY_RAVAGER_ROAR;
  }
}
