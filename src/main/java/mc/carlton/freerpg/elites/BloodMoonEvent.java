package mc.carlton.freerpg.elites;

import java.util.UUID;
import mc.carlton.freerpg.worldevents.ActiveEvent;
import mc.carlton.freerpg.worldevents.WorldEvent;
import mc.carlton.freerpg.worldevents.WorldEventType;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 * The Blood Moon as a world event. The manager decides when it starts and ends; this class does
 * the storm and the red-edge and particle effects. The tougher mobs are made by
 * {@link BloodMoonListener}, which asks whether a Blood Moon is going on.
 */
public final class BloodMoonEvent implements WorldEvent {

  /** Whether the end of a Blood Moon should clear the weather: only when it made the storm. */
  static boolean shouldClearWeather(boolean thunderEffectOn, boolean wasAlreadyStorming) {
    return thunderEffectOn && !wasAlreadyStorming;
  }

  @Override
  public WorldEventType type() {
    return WorldEventType.BLOOD_MOON;
  }

  @Override
  public boolean canStart() {
    return true;
  }

  @Override
  public void onStart(ActiveEvent event) {
    World world = event.world();
    boolean thunder = EliteConfig.bloodMoonEffect("thunder", false);
    boolean madeStorm = shouldClearWeather(thunder, world.hasStorm() || world.isThundering());
    event.setState(madeStorm);
    if (thunder) {
      world.setStorm(true);
      world.setThundering(true);
    }
  }

  @Override
  public void onEnd(ActiveEvent event, boolean byAdmin) {
    Boolean madeStorm = event.state();
    if (Boolean.TRUE.equals(madeStorm)) {
      World world = event.world();
      world.setThundering(false);
      world.setStorm(false);
    }
    BloodMoonVisuals.clearAll();
  }

  @Override
  public Sound announceSound() {
    return Sound.ENTITY_WITHER_SPAWN;
  }

  @Override
  public void decorate(Player player, ActiveEvent event) {
    if (EliteConfig.bloodMoonEffect("redEdge", true)) {
      BloodMoonVisuals.updateEdge(player);
    }
    if (EliteConfig.bloodMoonEffect("particles", true)) {
      BloodMoonVisuals.spawnParticles(player);
    }
  }

  @Override
  public void clearPlayer(UUID player) {
    BloodMoonVisuals.clear(player);
  }
}
