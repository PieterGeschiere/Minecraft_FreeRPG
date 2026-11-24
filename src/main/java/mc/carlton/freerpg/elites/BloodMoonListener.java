package mc.carlton.freerpg.elites;

import java.util.Random;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

/**
 * Hostile mobs that spawn naturally during a Blood Moon are tougher and wear random gear. This runs
 * before the elite roll, so an elite on a Blood Moon night is stronger still.
 */
public class BloodMoonListener implements Listener {

  private static final Random RANDOM = new Random();

  @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
  void onSpawn(CreatureSpawnEvent e) {
    LivingEntity mob = e.getEntity();
    if (!(mob instanceof Monster) || !BloodMoonManager.isActive(mob.getWorld())
        || !EliteRoller.eligible(e.getSpawnReason(), true, mob.getType(),
            mob.hasMetadata("frpgSpawnerMob"), EliteConfig.excludedTypes())) {
      return;
    }
    boost(mob, Attribute.MAX_HEALTH, EliteConfig.bloodMoonHealthBonus());
    boost(mob, Attribute.ATTACK_DAMAGE, EliteConfig.bloodMoonDamageBonus());
    AttributeInstance health = mob.getAttribute(Attribute.MAX_HEALTH);
    if (health != null) {
      mob.setHealth(health.getValue());
    }
    BloodMoonGear.equip(mob, RANDOM);
  }

  private static void boost(LivingEntity mob, Attribute attribute, double bonus) {
    AttributeInstance instance = mob.getAttribute(attribute);
    if (instance != null && bonus > 0.0) {
      instance.setBaseValue(instance.getBaseValue() * (1.0 + bonus));
    }
  }
}
