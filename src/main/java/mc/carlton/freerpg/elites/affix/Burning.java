package mc.carlton.freerpg.elites.affix;

import mc.carlton.freerpg.elites.Affix;
import mc.carlton.freerpg.elites.EliteConfig;
import mc.carlton.freerpg.elites.EliteEffects;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/** Sets whoever it hits on fire. */
public class Burning implements Affix {

  @Override
  public String id() {
    return "burning";
  }

  @Override
  public void onHit(LivingEntity mob, LivingEntity victim, EntityDamageByEntityEvent event) {
    victim.setFireTicks((int) (EliteConfig.affixDouble(id(), "seconds", 4) * 20));
    EliteEffects.burning(victim);
  }
}
