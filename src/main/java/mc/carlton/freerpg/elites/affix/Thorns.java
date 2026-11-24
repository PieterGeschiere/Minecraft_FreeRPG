package mc.carlton.freerpg.elites.affix;

import mc.carlton.freerpg.elites.Affix;
import mc.carlton.freerpg.elites.EliteConfig;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

/** Reflects a part of the melee damage back at the player who hit it. */
public class Thorns implements Affix {

  @Override
  public String id() {
    return "thorns";
  }

  /** The damage given back for a hit of this size. */
  static double reflected(double damage, double fraction) {
    return Math.max(0.0, damage * fraction);
  }

  @Override
  public void onDamaged(LivingEntity mob, EntityDamageEvent event) {
    if (event instanceof EntityDamageByEntityEvent byEntity
        && byEntity.getDamager() instanceof Player attacker) {
      double back = reflected(event.getDamage(), EliteConfig.affixDouble(id(), "reflect", 0.25));
      if (back > 0.0) {
        attacker.damage(back, mob);
      }
    }
  }
}
