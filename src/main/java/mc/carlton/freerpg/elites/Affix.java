package mc.carlton.freerpg.elites;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;

/**
 * One special ability of an elite mob. Affixes keep no state of their own (one instance serves
 * every elite); anything an affix must remember goes in the mob's metadata.
 */
public interface Affix {

  /** The id used in saved data and in elites.yml (for example "thorns"). */
  String id();

  /** Whether this affix makes sense for the mob type. */
  default boolean canApplyTo(EntityType type) {
    return true;
  }

  /** Called once, when the mob becomes an elite. Not called again after a restart. */
  default void onApply(LivingEntity mob) {
  }

  /** The elite hurts someone. */
  default void onHit(LivingEntity mob, LivingEntity victim, EntityDamageByEntityEvent event) {
  }

  /** The elite takes damage. */
  default void onDamaged(LivingEntity mob, EntityDamageEvent event) {
  }

  /** The elite dies. */
  default void onDeath(LivingEntity mob, EntityDeathEvent event) {
  }

  /** Called about once a second while the elite is loaded. */
  default void tick(LivingEntity mob) {
  }
}
