package mc.carlton.freerpg.worldevents;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import mc.carlton.freerpg.elites.RewardCooldown;
import mc.carlton.freerpg.utils.game.ExpFarmTracker;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

/** The combat and loot twists of Mutator Night: Glass Cannon and Exploding Loot. */
public final class MutatorCombatListener implements Listener {

  private static final Random RANDOM = new Random();
  private static final RewardCooldown COOLDOWN = new RewardCooldown();

  /** Who is really behind the damage: the shooter, the TNT's igniter, the tamer of a pet. */
  private static Entity realSource(Entity damager) {
    if (damager instanceof Projectile projectile && projectile.getShooter() instanceof Entity shooter) {
      return shooter;
    }
    if (damager instanceof TNTPrimed tnt && tnt.getSource() != null) {
      return tnt.getSource();
    }
    if (damager instanceof AreaEffectCloud cloud && cloud.getSource() instanceof Entity source) {
      return source;
    }
    if (damager instanceof Tameable pet && pet.getOwner() instanceof Player owner) {
      return owner;
    }
    return damager;
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  void onDamage(EntityDamageByEntityEvent e) {
    if (!(e.getEntity() instanceof LivingEntity victim)
        || !MutatorNightEvent.isActive(Mutator.GLASS_CANNON, victim.getWorld())) {
      return;
    }
    Entity source = realSource(e.getDamager());
    double factor = CombatTwistRules.glassCannonFactor(source instanceof Player,
        source instanceof Mob, victim instanceof Player, victim instanceof Mob,
        Mutator.GLASS_CANNON.number("multiplier", 2.0));
    if (factor != 1.0) {
      e.setDamage(e.getDamage() * factor);
    }
  }

  @EventHandler(priority = EventPriority.HIGH)
  void onDeath(EntityDeathEvent e) {
    LivingEntity mob = e.getEntity();
    Player killer = mob.getKiller();
    if (killer == null || killer.getGameMode() == GameMode.CREATIVE
        || killer.getGameMode() == GameMode.SPECTATOR
        || !MutatorNightEvent.isActive(Mutator.EXPLODING_LOOT, mob.getWorld())
        || !CombatTwistRules.explodingLootApplies(true, mob instanceof Monster,
            mob.hasMetadata("frpgSpawnerMob"), WorldEvents.isTwin(mob))) {
      return;
    }
    if (!new ExpFarmTracker().isExpFarm(mob) && claimChunk(mob)) {
      double chance = Math.max(0.0, Math.min(1.0,
          Mutator.EXPLODING_LOOT.number("dropChance", 0.5)));
      List<ItemStack> extra = new ArrayList<>();
      for (ItemStack drop : LootGuard.split(e.getDrops(), LootGuard.stickyEquipment(mob)).loot()) {
        if (EventRules.rollStart(chance, RANDOM)) {
          extra.add(drop.clone());
        }
      }
      e.getDrops().addAll(extra);
    }
    if (CombatTwistRules.shouldExplode(Mutator.EXPLODING_LOOT.number("explodeChance", 0.3),
        RANDOM)) {
      Location at = mob.getLocation().add(0, 0.5, 0);
      // no fire and no block damage: it only hurts whoever stands too close
      mob.getWorld().createExplosion(at, (float) Mutator.EXPLODING_LOOT.number("power", 1.5),
          false, false);
      WorldEventEffects.explodingLoot(mob);
    }
  }

  /** A chunk pays the extra loot at most once per cooldown, like elite rewards do. */
  private static boolean claimChunk(LivingEntity mob) {
    Chunk chunk = mob.getLocation().getChunk();
    String place = mob.getWorld().getUID() + ":" + chunk.getX() + ":" + chunk.getZ();
    long cooldownMillis = Math.max(0,
        (long) Mutator.EXPLODING_LOOT.number("chunkCooldownSeconds", 60)) * 1000L;
    return COOLDOWN.tryClaim(place, System.currentTimeMillis(), cooldownMillis);
  }
}
