package mc.carlton.freerpg.elites;

import java.util.Random;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.util.Vector;

/**
 * Elites and Titans are bigger, and their arrows tend to miss more than a normal mob's. This aims
 * the arrows of a mob that is bigger than normal straight at its target, with a small miss.
 */
public final class BigShooterListener implements Listener {

  private static final Random RANDOM = new Random();

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  void onLaunch(ProjectileLaunchEvent e) {
    double spread = EliteConfig.bigShooterSpread();
    if (spread < 0.0 || !(e.getEntity() instanceof AbstractArrow arrow)
        || !(arrow.getShooter() instanceof Mob shooter)) {
      return;
    }
    AttributeInstance scale = shooter.getAttribute(Attribute.SCALE);
    LivingEntity target = shooter.getTarget();
    if (scale == null || scale.getValue() < BigShooterAim.MIN_SCALE || target == null
        || !target.getWorld().equals(shooter.getWorld())) {
      return;
    }
    Location from = arrow.getLocation();
    Location to = target.getLocation().add(0.0, target.getHeight() * 0.5, 0.0);
    double speed = arrow.getVelocity().length();
    double[] v = BigShooterAim.velocity(new double[] {from.getX(), from.getY(), from.getZ()},
        new double[] {to.getX(), to.getY(), to.getZ()}, speed, spread, RANDOM);
    arrow.setVelocity(new Vector(v[0], v[1], v[2]));
  }
}
