package mc.carlton.freerpg.elites.affix;

import mc.carlton.freerpg.elites.Affix;
import mc.carlton.freerpg.elites.EliteConfig;
import mc.carlton.freerpg.elites.EliteEffects;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/** Heals itself with a part of the damage it deals. */
public class Vampiric implements Affix {

  @Override
  public String id() {
    return "vampiric";
  }

  /** The health gained: a fraction of the damage, never more than what is missing. */
  static double healAmount(double damage, double fraction, double missingHealth) {
    return Math.max(0.0, Math.min(damage * fraction, missingHealth));
  }

  @Override
  public void onHit(LivingEntity mob, LivingEntity victim, EntityDamageByEntityEvent event) {
    AttributeInstance max = mob.getAttribute(Attribute.MAX_HEALTH);
    if (max == null) {
      return;
    }
    double fraction = EliteConfig.affixDouble(id(), "healFraction", 0.5);
    double heal = healAmount(event.getFinalDamage(), fraction, max.getValue() - mob.getHealth());
    mob.setHealth(Math.min(max.getValue(), mob.getHealth() + heal));
    if (heal > 0.0) {
      EliteEffects.vampiric(mob, victim);
    }
  }
}
