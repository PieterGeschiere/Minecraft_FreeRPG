package mc.carlton.freerpg.elites;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

/** Scales arrow and explosion damage of elites and Blood Moon mobs. */
public class DamageScalingListener implements Listener {

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  void onDamage(EntityDamageByEntityEvent e) {
    boolean ranged = e.getCause() == DamageCause.PROJECTILE
        || e.getCause() == DamageCause.ENTITY_EXPLOSION;
    if (!ranged) {
      return;
    }
    Entity source = e.getDamager();
    if (source instanceof Projectile projectile && projectile.getShooter() instanceof Entity shooter) {
      source = shooter;
    }
    if (!(source instanceof LivingEntity attacker)) {
      return;
    }
    double factor = DamageScaling.factor(true, EliteData.isElite(attacker),
        EliteData.isBloodMoonMob(attacker), EliteConfig.damageMultiplier(),
        EliteConfig.bloodMoonDamageBonus());
    if (factor != 1.0) {
      e.setDamage(e.getDamage() * factor);
    }
  }
}
