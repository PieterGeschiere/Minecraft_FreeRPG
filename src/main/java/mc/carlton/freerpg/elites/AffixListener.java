package mc.carlton.freerpg.elites;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;

/** Passes combat events to the affixes of the elites involved. */
public class AffixListener implements Listener {

  /** A dying, dead or removed elite must not act (Vampiric would heal it back to life). */
  static boolean canAct(boolean isDead, boolean isValid, double health) {
    return !isDead && isValid && health > 0.0;
  }

  /** The living elite that dealt the damage (directly or with a projectile), or null. */
  private static LivingEntity eliteAttacker(EntityDamageByEntityEvent e) {
    Entity damager = e.getDamager();
    if (damager instanceof Projectile projectile
        && projectile.getShooter() instanceof Entity shooter) {
      damager = shooter;
    }
    if (damager instanceof LivingEntity living && EliteData.isElite(living)
        && canAct(living.isDead(), living.isValid(), living.getHealth())) {
      return living;
    }
    return null;
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  void onDamaged(EntityDamageEvent e) {
    if (e.getEntity() instanceof LivingEntity mob && EliteData.isElite(mob)) {
      for (String id : EliteData.affixes(mob)) {
        Affix affix = AffixRegistry.get(id);
        if (affix != null) {
          affix.onDamaged(mob, e);
        }
      }
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  void onHit(EntityDamageByEntityEvent e) {
    LivingEntity mob = eliteAttacker(e);
    if (mob == null || !(e.getEntity() instanceof LivingEntity victim)) {
      return;
    }
    for (String id : EliteData.affixes(mob)) {
      Affix affix = AffixRegistry.get(id);
      if (affix != null) {
        affix.onHit(mob, victim, e);
      }
    }
  }

  @EventHandler(priority = EventPriority.HIGH)
  void onDeath(EntityDeathEvent e) {
    LivingEntity mob = e.getEntity();
    if (!EliteData.isElite(mob)) {
      return;
    }
    for (String id : EliteData.affixes(mob)) {
      Affix affix = AffixRegistry.get(id);
      if (affix != null) {
        affix.onDeath(mob, e);
      }
    }
  }
}
