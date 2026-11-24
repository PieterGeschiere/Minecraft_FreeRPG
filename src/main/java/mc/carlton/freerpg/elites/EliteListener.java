package mc.carlton.freerpg.elites;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.world.EntitiesLoadEvent;

/** Makes natural spawns elite and keeps the tracker up to date. */
public class EliteListener implements Listener {

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  void onSpawn(CreatureSpawnEvent e) {
    LivingEntity mob = e.getEntity();
    boolean eligible = EliteRoller.eligible(e.getSpawnReason(), mob instanceof Monster,
        mob.getType(), mob.hasMetadata("frpgSpawnerMob"), EliteConfig.excludedTypes());
    if (eligible && !EliteData.isMinion(mob)) {
      EliteManager.tryMakeElite(mob, BloodMoonManager.isActive(mob.getWorld()));
    }
  }

  /** Elites that were saved with their chunk are tracked again when it loads. */
  @EventHandler
  void onEntitiesLoad(EntitiesLoadEvent e) {
    for (Entity entity : e.getEntities()) {
      if (entity instanceof LivingEntity living && EliteData.isElite(entity)) {
        EliteTracker.track(living);
      }
    }
  }

  @EventHandler(priority = EventPriority.MONITOR)
  void onDeath(EntityDeathEvent e) {
    EliteTracker.untrack(e.getEntity().getUniqueId());
  }
}
