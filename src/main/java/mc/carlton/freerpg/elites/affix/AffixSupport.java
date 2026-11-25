package mc.carlton.freerpg.elites.affix;

import java.util.Set;
import mc.carlton.freerpg.FreeRPG;
import mc.carlton.freerpg.elites.EliteData;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

/** Small helpers shared by the affixes. */
final class AffixSupport {

  /** Mobs that fly, teleport or behave in ways the movement affixes cannot handle. */
  private static final Set<EntityType> SPECIAL_MOVERS = Set.of(EntityType.ENDERMAN,
      EntityType.VEX, EntityType.GUARDIAN, EntityType.ELDER_GUARDIAN, EntityType.BLAZE,
      EntityType.BREEZE, EntityType.SILVERFISH, EntityType.ENDERMITE);

  private AffixSupport() {
  }

  /** Whether the movement affixes (Swift, Teleporter) make sense for this mob. */
  static boolean isGroundFighter(EntityType type) {
    return !SPECIAL_MOVERS.contains(type);
  }

  /** Whether the mob can be copied by Splitting and Summoner. */
  static boolean isCopyable(EntityType type) {
    return isGroundFighter(type) && type != EntityType.CREEPER && type.getEntityClass() != null;
  }

  /**
   * A once-a-second counter kept in the mob's metadata. Returns true once every
   * {@code everySeconds} calls.
   */
  static boolean due(LivingEntity mob, String key, int everySeconds) {
    int count = mob.hasMetadata(key) ? mob.getMetadata(key).get(0).asInt() : 0;
    count++;
    boolean due = count >= Math.max(1, everySeconds);
    mob.setMetadata(key, new FixedMetadataValue(FreeRPG.getPlugin(FreeRPG.class), due ? 0 : count));
    return due;
  }

  /** The mob's current target, or null. */
  static LivingEntity target(LivingEntity mob) {
    return mob instanceof Mob m ? m.getTarget() : null;
  }

  /** The horizontal direction the entity looks in (never zero). */
  static Vector flatDirection(Location location) {
    Vector direction = location.getDirection().setY(0);
    if (direction.lengthSquared() < 1.0e-6) {
      return new Vector(0, 0, 1);
    }
    return direction.normalize();
  }

  /**
   * Spawns a plain copy of the mob (not an elite, marked as a minion) at the location. Returns
   * null when the type cannot be spawned.
   */
  static LivingEntity spawnMinion(LivingEntity original, Location where) {
    Class<? extends Entity> type = original.getType().getEntityClass();
    if (type == null || where.getWorld() == null) {
      return null;
    }
    Entity spawned = where.getWorld().spawn(where, type, entity -> {
      EliteData.markMinion(entity);
      // the same tag spawner mobs get, so the plugin's spawner exp reduction applies to minions
      entity.setMetadata("frpgSpawnerMob",
          new FixedMetadataValue(FreeRPG.getPlugin(FreeRPG.class), "frpg"));
    });
    return spawned instanceof LivingEntity living ? living : null;
  }
}
