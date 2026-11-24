package mc.carlton.freerpg.elites.affix;

import mc.carlton.freerpg.elites.Affix;
import mc.carlton.freerpg.elites.EliteConfig;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

/** Blinks behind its target now and then. */
public class Teleporter implements Affix {

  @Override
  public String id() {
    return "teleporter";
  }

  @Override
  public boolean canApplyTo(EntityType type) {
    return AffixSupport.isGroundFighter(type);
  }

  /** The spot {@code distance} blocks behind where the target is looking, at the same height. */
  static Location behind(Location target, double distance) {
    return target.clone().subtract(AffixSupport.flatDirection(target).multiply(distance));
  }

  @Override
  public void tick(LivingEntity mob) {
    LivingEntity target = AffixSupport.target(mob);
    if (target == null || !target.getWorld().equals(mob.getWorld())) {
      return;
    }
    if (!AffixSupport.due(mob, "frpgBlinkTimer",
        (int) EliteConfig.affixDouble(id(), "intervalSeconds", 8))) {
      return;
    }
    double range = EliteConfig.affixDouble(id(), "range", 12.0);
    double distance = target.getLocation().distance(mob.getLocation());
    if (distance < 4.0 || distance > range) {
      return;
    }
    Location spot = behind(target.getLocation(), 1.5);
    if (!spot.getBlock().isPassable() || !spot.clone().add(0, 1, 0).getBlock().isPassable()) {
      return;
    }
    Location from = mob.getLocation();
    mob.getWorld().spawnParticle(Particle.CLOUD, from.clone().add(0, 1, 0), 12, 0.3, 0.5, 0.3, 0.02);
    mob.teleport(spot);
    mob.getWorld().playSound(spot, Sound.ENTITY_ENDERMAN_TELEPORT, 0.8f, 1.2f);
  }
}
