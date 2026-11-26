package mc.carlton.freerpg.elites.affix;

import mc.carlton.freerpg.elites.Affix;
import mc.carlton.freerpg.elites.EliteConfig;
import mc.carlton.freerpg.elites.EliteEffects;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDeathEvent;

/** Splits into weaker copies when it dies. The copies are not elites. */
public class Splitting implements Affix {

  @Override
  public String id() {
    return "splitting";
  }

  @Override
  public boolean canApplyTo(EntityType type) {
    return AffixSupport.isCopyable(type);
  }

  /** How many copies to make: the configured number, at least one. */
  static int childCount(int configured) {
    return Math.max(1, configured);
  }

  @Override
  public void onDeath(LivingEntity mob, EntityDeathEvent event) {
    EliteEffects.splitting(mob);
    int copies = childCount((int) EliteConfig.affixDouble(id(), "copies", 2));
    double fraction = Math.max(0.05, EliteConfig.affixDouble(id(), "healthFraction", 0.25));
    for (int i = 0; i < copies; i++) {
      LivingEntity child = AffixSupport.spawnMinion(mob,
          mob.getLocation().add((i - copies / 2.0) * 0.6, 0, 0));
      if (child == null) {
        continue;
      }
      AttributeInstance max = child.getAttribute(Attribute.MAX_HEALTH);
      if (max != null) {
        child.setHealth(Math.max(1.0, Math.min(max.getValue(), max.getValue() * fraction)));
      }
      LivingEntity killer = mob.getKiller();
      if (killer != null && child instanceof org.bukkit.entity.Mob m) {
        m.setTarget(killer);
      }
    }
  }
}
