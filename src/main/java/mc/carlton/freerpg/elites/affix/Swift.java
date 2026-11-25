package mc.carlton.freerpg.elites.affix;

import mc.carlton.freerpg.elites.Affix;
import mc.carlton.freerpg.elites.EliteConfig;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

/** Moves faster and now and then lunges at its target. */
public class Swift implements Affix {

  @Override
  public String id() {
    return "swift";
  }

  @Override
  public boolean canApplyTo(EntityType type) {
    return AffixSupport.isGroundFighter(type);
  }

  @Override
  public void onApply(LivingEntity mob) {
    AttributeInstance speed = mob.getAttribute(Attribute.MOVEMENT_SPEED);
    if (speed != null) {
      speed.setBaseValue(speed.getBaseValue() * (1.0 + EliteConfig.affixDouble(id(), "speedBonus", 0.4)));
    }
  }

  @Override
  public void tick(LivingEntity mob) {
    LivingEntity target = AffixSupport.target(mob);
    if (target == null || !target.getWorld().equals(mob.getWorld())) {
      return;
    }
    double distance = target.getLocation().distance(mob.getLocation());
    if (distance > 3.0 && distance < 10.0) {
      Vector lunge = SwiftMath.lunge(mob.getLocation().toVector(),
          target.getLocation().toVector());
      mob.setVelocity(mob.getVelocity().add(lunge));
    }
  }
}
