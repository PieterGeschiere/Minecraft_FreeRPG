package mc.carlton.freerpg.worldevents;

import java.util.List;
import mc.carlton.freerpg.elites.EliteData;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.inventory.ItemStack;

/** The mob twists of Mutator Night: Swift Mobs, Glowing Mobs and Double Trouble. */
public final class MutatorMobListener implements Listener {

  static final String TWIN_TAG = "frpgTwin";
  static final String GLOW_TAG = "frpgMutatorGlow";

  /** Swift Mobs and Glowing Mobs, applied to a natural spawn and to its twin. */
  private static void decorate(LivingEntity mob) {
    World world = mob.getWorld();
    if (MutatorNightEvent.isActive(Mutator.SWIFT_MOBS, world)) {
      AttributeInstance speed = mob.getAttribute(Attribute.MOVEMENT_SPEED);
      if (speed != null) {
        speed.setBaseValue(speed.getBaseValue()
            * DoubleTroubleRules.speedFactor(Mutator.SWIFT_MOBS.number("speedBonus", 0.3)));
      }
    }
    if (MutatorNightEvent.isActive(Mutator.GLOWING_MOBS, world) && !mob.isGlowing()) {
      mob.setGlowing(true);
      EntityTags.set(mob, GLOW_TAG);
    }
  }

  private static int hostilesIn(Chunk chunk) {
    int count = 0;
    for (Entity entity : chunk.getEntities()) {
      if (entity instanceof Monster) {
        count++;
      }
    }
    return count;
  }

  @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
  void onSpawn(CreatureSpawnEvent e) {
    LivingEntity mob = e.getEntity();
    if (!(mob instanceof Monster) || e.getSpawnReason() != SpawnReason.NATURAL
        || mob.hasMetadata("frpgSpawnerMob")) {
      return;
    }
    World world = mob.getWorld();
    decorate(mob);
    if (!MutatorNightEvent.isActive(Mutator.DOUBLE_TROUBLE, world)) {
      return;
    }
    int cap = (int) Mutator.DOUBLE_TROUBLE.number("maxPerChunk", 30);
    if (!DoubleTroubleRules.shouldTwin(e.getSpawnReason(), true, EntityTags.has(mob, TWIN_TAG),
        hostilesIn(mob.getLocation().getChunk()), cap)) {
      return;
    }
    // the twin is spawned with the reason CUSTOM, so it never gets a twin itself
    Entity twin = world.spawnEntity(mob.getLocation(), mob.getType());
    if (twin instanceof LivingEntity living) {
      EntityTags.set(living, TWIN_TAG);
      decorate(living);
    }
  }

  @EventHandler(priority = EventPriority.HIGH)
  void onDeath(EntityDeathEvent e) {
    LivingEntity mob = e.getEntity();
    if (DoubleTroubleRules.twinHasNoLoot(EntityTags.has(mob, TWIN_TAG))) {
      // the loot is gone, but not what the twin carried: that belongs to whoever lost it
      List<ItemStack> carried = LootGuard.split(e.getDrops(), LootGuard.stickyEquipment(mob))
          .sticky();
      e.getDrops().clear();
      e.getDrops().addAll(carried);
      e.setDroppedExp(0);
    }
  }

  /** Whether a mob that loads with a glow of ours should lose it (the twist is not running). */
  static boolean shouldClearGlowOnLoad(boolean hasGlowTag, boolean glowTwistActive) {
    return hasGlowTag && !glowTwistActive;
  }

  private static void clearGlowOf(LivingEntity mob) {
    EntityTags.remove(mob, GLOW_TAG);
    if (!EliteData.isElite(mob)) {
      mob.setGlowing(false);
    }
  }

  /** A mob that was glowing when its chunk unloaded must not keep glowing after the night. */
  @EventHandler
  void onEntitiesLoad(EntitiesLoadEvent e) {
    boolean active = MutatorNightEvent.isActive(Mutator.GLOWING_MOBS, e.getWorld());
    for (Entity entity : e.getEntities()) {
      if (entity instanceof LivingEntity mob
          && shouldClearGlowOnLoad(EntityTags.has(mob, GLOW_TAG), active)) {
        clearGlowOf(mob);
      }
    }
  }

  /** Turns the glow of this night's mobs off again (elites keep their own glow). */
  static void clearGlow(World world) {
    for (LivingEntity mob : world.getLivingEntities()) {
      if (EntityTags.has(mob, GLOW_TAG)) {
        clearGlowOf(mob);
      }
    }
  }
}
