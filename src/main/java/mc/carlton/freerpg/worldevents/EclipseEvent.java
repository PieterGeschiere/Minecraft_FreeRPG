package mc.carlton.freerpg.worldevents;

import org.bukkit.Sound;

/** Eclipse: a quiet night. Part of the natural hostile spawns is cancelled by {@link EclipseSpawnListener}. */
public final class EclipseEvent implements WorldEvent {

  @Override
  public WorldEventType type() {
    return WorldEventType.ECLIPSE;
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
    return Sound.ENTITY_ENDERMAN_STARE;
  }
}
