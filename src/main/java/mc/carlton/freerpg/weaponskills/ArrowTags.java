package mc.carlton.freerpg.weaponskills;

import mc.carlton.freerpg.FreeRPG;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

/**
 * Marks the arrows shot by the bow and crossbow weapon skills, so later events (hit, kill, impact)
 * know which skill and which ability a projectile belongs to.
 */
final class ArrowTags {

  static final String BOW = "bow";
  static final String BOW_VOLLEY = "bow_volley";
  static final String CROSSBOW = "crossbow";
  static final String CROSSBOW_BLAST = "crossbow_blast";

  private ArrowTags() {
  }

  private static NamespacedKey key() {
    return new NamespacedKey(FreeRPG.getPlugin(FreeRPG.class), "weapon_arrow");
  }

  static void tag(Entity arrow, String value) {
    arrow.getPersistentDataContainer().set(key(), PersistentDataType.STRING, value);
  }

  /** The tag of the projectile, or null if it is not an arrow of a weapon skill. */
  static String get(Entity entity) {
    if (!(entity instanceof AbstractArrow)) {
      return null;
    }
    return entity.getPersistentDataContainer().get(key(), PersistentDataType.STRING);
  }

  /** The player who shot the arrow, or null. */
  static Player shooter(Entity entity) {
    if (entity instanceof AbstractArrow && ((AbstractArrow) entity).getShooter() instanceof Player) {
      return (Player) ((AbstractArrow) entity).getShooter();
    }
    return null;
  }

  /** Whether the tag belongs to the given skill ("bow" and "bow_volley" belong to the bow). */
  static boolean belongsTo(String tag, String skillPrefix) {
    return tag != null && tag.startsWith(skillPrefix);
  }
}
