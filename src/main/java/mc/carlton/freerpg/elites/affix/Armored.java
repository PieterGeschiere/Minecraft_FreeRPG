package mc.carlton.freerpg.elites.affix;

import mc.carlton.freerpg.elites.Affix;
import mc.carlton.freerpg.elites.EliteConfig;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent;

/** Takes less damage from everything. */
public class Armored implements Affix {

  @Override
  public String id() {
    return "armored";
  }

  /** The damage after the reduction (0.3 means 30% less). */
  static double reduce(double damage, double reduction) {
    return damage * (1.0 - Math.max(0.0, Math.min(1.0, reduction)));
  }

  @Override
  public void onDamaged(LivingEntity mob, EntityDamageEvent event) {
    event.setDamage(reduce(event.getDamage(), EliteConfig.affixDouble(id(), "reduction", 0.3)));
  }
}
