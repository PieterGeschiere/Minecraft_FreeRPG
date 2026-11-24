package mc.carlton.freerpg.elites.affix;

import mc.carlton.freerpg.elites.Affix;
import mc.carlton.freerpg.elites.EliteConfig;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/** Slows whoever it hits. */
public class Frostbound implements Affix {

  @Override
  public String id() {
    return "frostbound";
  }

  @Override
  public void onHit(LivingEntity mob, LivingEntity victim, EntityDamageByEntityEvent event) {
    int ticks = (int) (EliteConfig.affixDouble(id(), "seconds", 3) * 20);
    victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, ticks, 1));
  }
}
