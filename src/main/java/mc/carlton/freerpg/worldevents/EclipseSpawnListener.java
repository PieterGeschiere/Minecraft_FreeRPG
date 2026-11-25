package mc.carlton.freerpg.worldevents;

import java.util.Random;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

/**
 * During an Eclipse a share of the natural hostile spawns does not happen. Spawner mobs, eggs,
 * commands and animals are never touched. This runs before the Blood Moon and elite handlers, so
 * a cancelled mob never becomes an elite.
 */
public final class EclipseSpawnListener implements Listener {

  private static final Random RANDOM = new Random();

  /** Whether this spawn is one of the cancelled ones. */
  static boolean shouldCancel(SpawnReason reason, boolean isMonster, boolean isElite,
      double reduction, Random random) {
    return reason == SpawnReason.NATURAL && isMonster && !isElite
        && EventRules.rollStart(reduction, random);
  }

  /** The share (0 to 1) of natural hostile spawns that is cancelled. */
  static double reduction() {
    return Math.max(0.0, Math.min(1.0,
        WorldEventConfig.number(WorldEventType.ECLIPSE, "spawnReduction", 0.6)));
  }

  @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
  void onSpawn(CreatureSpawnEvent e) {
    LivingEntity mob = e.getEntity();
    if (!WorldEvents.isActive(WorldEventType.ECLIPSE, mob.getWorld())) {
      return;
    }
    // an elite is only rolled later (at NORMAL priority), so none exists yet at this point
    if (shouldCancel(e.getSpawnReason(), mob instanceof Monster, false, reduction(), RANDOM)) {
      e.setCancelled(true);
    }
  }
}
