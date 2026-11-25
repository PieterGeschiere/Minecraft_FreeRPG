package mc.carlton.freerpg.elites.affix;

import mc.carlton.freerpg.elites.Affix;
import mc.carlton.freerpg.elites.EliteConfig;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;

/** Explodes when it dies and hurts the players around it. Never breaks blocks. */
public class Volatile implements Affix {

  @Override
  public String id() {
    return "volatile";
  }

  @Override
  public boolean canApplyTo(EntityType type) {
    return type != EntityType.CREEPER;
  }

  @Override
  public void onDeath(LivingEntity mob, EntityDeathEvent event) {
    double radius = EliteConfig.affixDouble(id(), "radius", 3.0);
    double damage = EliteConfig.affixDouble(id(), "damage", 6.0);
    Location where = mob.getLocation().add(0, 0.5, 0);
    if (where.getWorld() == null) {
      return;
    }
    where.getWorld().spawnParticle(Particle.EXPLOSION, where, 1);
    where.getWorld().playSound(where, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
    for (Player player : where.getWorld().getPlayers()) {
      if (player.getLocation().distanceSquared(where) <= radius * radius) {
        player.damage(damage, mob);
      }
    }
  }
}
