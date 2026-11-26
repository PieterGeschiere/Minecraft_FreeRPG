package mc.carlton.freerpg.worldevents;

import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import mc.carlton.freerpg.FreeRPG;
import mc.carlton.freerpg.elites.EliteConfig;
import mc.carlton.freerpg.elites.EliteRoller;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.metadata.FixedMetadataValue;

/**
 * During Titan Night a naturally spawned hostile mob grows to a random size and gets the matching
 * health and damage. The size is capped by the room above the mob, with extra room kept for an
 * elite's own size boost, so a Titan never clips into a ceiling.
 */
public final class TitanSpawnListener implements Listener {

  /** Marks a Titan mob; the value is its size. */
  static final String TITAN_TAG = "frpgTitan";

  private static final WorldEventType TYPE = WorldEventType.TITAN_NIGHT;
  private static final List<String> DEFAULT_EXCLUDED = List.of("SLIME", "MAGMA_CUBE", "WARDEN",
      "ENDER_DRAGON", "WITHER", "ELDER_GUARDIAN", "GIANT");
  private static final Random RANDOM = new Random();

  /** The mobs that never grow. Names that are not mobs of this server version are ignored. */
  static Set<EntityType> excludedTypes() {
    Set<EntityType> types = EnumSet.noneOf(EntityType.class);
    for (String name : EventDefaults.list(TYPE, "excludedTypes", DEFAULT_EXCLUDED)) {
      try {
        types.add(EntityType.valueOf(name.toUpperCase(Locale.ROOT)));
      } catch (IllegalArgumentException ignored) {
        // not a mob of this server version
      }
    }
    return types;
  }

  /** Whether a box of this width and height around the mob's feet holds no solid block. */
  private static boolean boxFree(Location at, double width, double height) {
    if (width > 6.0 || height > 8.0) {
      return false;
    }
    World world = at.getWorld();
    int minX = (int) Math.floor(at.getX() - width / 2);
    int maxX = (int) Math.floor(at.getX() + width / 2);
    int minZ = (int) Math.floor(at.getZ() - width / 2);
    int maxZ = (int) Math.floor(at.getZ() + width / 2);
    int minY = (int) Math.floor(at.getY());
    int maxY = (int) Math.floor(at.getY() + height - 1e-4);
    for (int x = minX; x <= maxX; x++) {
      for (int y = minY; y <= maxY; y++) {
        for (int z = minZ; z <= maxZ; z++) {
          if (!world.getBlockAt(x, y, z).isPassable()) {
            return false;
          }
        }
      }
    }
    return true;
  }

  private static void scale(LivingEntity mob, Attribute attribute, double factor) {
    AttributeInstance instance = mob.getAttribute(attribute);
    if (instance != null) {
      instance.setBaseValue(instance.getBaseValue() * factor);
    }
  }

  @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
  void onSpawn(CreatureSpawnEvent e) {
    LivingEntity mob = e.getEntity();
    if (!(mob instanceof Monster) || !WorldEvents.isActive(TYPE, mob.getWorld())
        || !EliteRoller.eligible(e.getSpawnReason(), true, mob.getType(),
            mob.hasMetadata("frpgSpawnerMob"), excludedTypes())) {
      return;
    }
    double desired = TitanRules.pickScale(WorldEventConfig.number(TYPE, "minScale", 1.4),
        WorldEventConfig.number(TYPE, "maxScale", 2.2), RANDOM);
    if (TitanRules.skipBecauseOfRiders(!mob.getPassengers().isEmpty(), mob.isInsideVehicle())) {
      return;
    }
    Location at = mob.getLocation();
    double size = TitanRules.fitBox(desired, mob.getWidth(), mob.getHeight(),
        EliteConfig.sizeScale(), (width, height) -> boxFree(at, width, height));
    if (!TitanRules.worthScaling(size)) {
      return;
    }
    scale(mob, Attribute.SCALE, size);
    scale(mob, Attribute.MAX_HEALTH,
        TitanRules.healthFactor(size, WorldEventConfig.number(TYPE, "healthPerScale", 2.0)));
    scale(mob, Attribute.ATTACK_DAMAGE,
        TitanRules.damageFactor(size, WorldEventConfig.number(TYPE, "damagePerScale", 0.5)));
    AttributeInstance health = mob.getAttribute(Attribute.MAX_HEALTH);
    if (health != null) {
      mob.setHealth(health.getValue());
    }
    mob.setMetadata(TITAN_TAG, new FixedMetadataValue(FreeRPG.getPlugin(FreeRPG.class), size));
    WorldEventEffects.titanSpawn(mob);
  }
}
