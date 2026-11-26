package mc.carlton.freerpg.elites;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import mc.carlton.freerpg.utils.game.EffectSettings;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

/**
 * Sounds and particles of the elite mobs: what each affix does when it triggers, the death of an
 * elite and the reward for killing one. Switched by the same effects: settings as the perks.
 */
public final class EliteEffects {

  /** When a cue last played for an entity and key, for affixes that trigger on every hit. */
  private static final Map<String, Long> LAST_PLAYED = new HashMap<>();

  private EliteEffects() {
  }

  private static boolean throttle(UUID id, String key, long millis) {
    long now = System.currentTimeMillis();
    String k = id + key;
    Long last = LAST_PLAYED.get(k);
    if (last != null && now - last < millis) {
      return false;
    }
    if (LAST_PLAYED.size() > 2000) {
      LAST_PLAYED.clear();
    }
    LAST_PLAYED.put(k, now);
    return true;
  }

  private static void sound(World world, Location at, Sound sound, float volume, float pitch) {
    if (EffectSettings.perkSounds()) {
      world.playSound(at, sound, volume, pitch);
    }
  }

  private static void spawn(World world, Particle particle, Location at, int count, double dx,
      double dy, double dz, double speed) {
    if (EffectSettings.perkParticles()) {
      world.spawnParticle(particle, at, count, dx, dy, dz, speed);
    }
  }

  private static Location middle(Entity entity) {
    return entity.getLocation().add(0, entity.getHeight() / 2, 0);
  }

  /** Burning: the victim catches fire. */
  public static void burning(LivingEntity victim) {
    World world = victim.getWorld();
    Location at = middle(victim);
    spawn(world, Particle.FLAME, at, 14, 0.3, 0.5, 0.3, 0.04);
    spawn(world, Particle.LAVA, at, 3, 0.3, 0.4, 0.3, 0);
    sound(world, at, Sound.ITEM_FIRECHARGE_USE, 0.6f, 1.0f);
  }

  /** Frostbound: the victim is chilled. */
  public static void frostbound(LivingEntity victim) {
    World world = victim.getWorld();
    Location at = middle(victim);
    spawn(world, Particle.SNOWFLAKE, at, 18, 0.4, 0.6, 0.4, 0.03);
    spawn(world, Particle.ITEM_SNOWBALL, at, 8, 0.3, 0.4, 0.3, 0.05);
    sound(world, at, Sound.BLOCK_GLASS_BREAK, 0.5f, 1.6f);
    sound(world, at, Sound.ENTITY_PLAYER_HURT_FREEZE, 0.6f, 1.2f);
  }

  /** Thorns: the blow is thrown back at the attacker. */
  public static void thorns(LivingEntity mob, Player attacker) {
    if (!throttle(mob.getUniqueId(), "thorns", 300)) {
      return;
    }
    World world = attacker.getWorld();
    Location at = middle(attacker);
    spawn(world, Particle.CRIT, at, 12, 0.3, 0.4, 0.3, 0.3);
    spawn(world, Particle.DAMAGE_INDICATOR, at, 3, 0.3, 0.3, 0.3, 0.05);
    sound(world, at, Sound.ENCHANT_THORNS_HIT, 0.8f, 1.0f);
  }

  /** Vampiric: the elite drinks. */
  public static void vampiric(LivingEntity mob, LivingEntity victim) {
    if (!throttle(mob.getUniqueId(), "vampiric", 400)) {
      return;
    }
    World world = mob.getWorld();
    Location from = middle(victim);
    Location to = middle(mob);
    org.bukkit.util.Vector step = to.toVector().subtract(from.toVector()).multiply(1.0 / 6);
    Particle.DustOptions blood = new Particle.DustOptions(Color.fromRGB(160, 10, 20), 1.1f);
    if (EffectSettings.perkParticles()) {
      for (int i = 0; i <= 6; i++) {
        world.spawnParticle(Particle.DUST, from.clone().add(step.clone().multiply(i)), 2, 0.05,
            0.05, 0.05, 0, blood);
      }
      world.spawnParticle(Particle.HEART, to.clone().add(0, 0.5, 0), 3, 0.3, 0.2, 0.3, 0);
    }
    sound(world, to, Sound.ENTITY_GENERIC_DRINK, 0.5f, 0.7f);
  }

  /** Armored: the blow glances off. */
  public static void armored(LivingEntity mob) {
    if (!throttle(mob.getUniqueId(), "armored", 350)) {
      return;
    }
    World world = mob.getWorld();
    Location at = middle(mob);
    spawn(world, Particle.ENCHANTED_HIT, at, 8, 0.3, 0.4, 0.3, 0.2);
    sound(world, at, Sound.ITEM_SHIELD_BLOCK, 0.5f, 1.4f);
    sound(world, at, Sound.BLOCK_ANVIL_PLACE, 0.2f, 1.8f);
  }

  /** Swift: the elite lunges. */
  public static void swiftLunge(LivingEntity mob) {
    if (!throttle(mob.getUniqueId(), "swift", 700)) {
      return;
    }
    World world = mob.getWorld();
    Location at = mob.getLocation().add(0, 0.2, 0);
    spawn(world, Particle.CLOUD, at, 6, 0.3, 0.1, 0.3, 0.03);
    spawn(world, Particle.SWEEP_ATTACK, middle(mob), 1, 0, 0, 0, 0);
    sound(world, at, Sound.ENTITY_PHANTOM_FLAP, 0.5f, 1.6f);
  }

  /** Splitting: the corpse bursts into copies. */
  public static void splitting(LivingEntity mob) {
    World world = mob.getWorld();
    Location at = middle(mob);
    spawn(world, Particle.POOF, at, 20, 0.5, 0.5, 0.5, 0.08);
    spawn(world, Particle.ITEM_SLIME, at, 15, 0.4, 0.4, 0.4, 0.1);
    sound(world, at, Sound.ENTITY_SLIME_SQUISH, 1.0f, 0.8f);
    sound(world, at, Sound.ENTITY_SLIME_JUMP, 0.7f, 1.2f);
  }

  /** Summoner: the call for help. */
  public static void summon(LivingEntity mob) {
    if (!throttle(mob.getUniqueId(), "summon", 1000)) {
      return;
    }
    World world = mob.getWorld();
    Location at = mob.getLocation().add(0, 1.0, 0);
    spawn(world, Particle.SOUL, at, 15, 0.5, 0.6, 0.5, 0.05);
    spawn(world, Particle.SMOKE, at, 10, 0.4, 0.5, 0.4, 0.02);
    sound(world, at, Sound.ENTITY_EVOKER_PREPARE_SUMMON, 0.8f, 1.0f);
  }

  /** Teleporter: it reappears behind the target. */
  public static void blinkArrival(Location spot) {
    World world = spot.getWorld();
    if (world == null) {
      return;
    }
    spawn(world, Particle.PORTAL, spot.clone().add(0, 1, 0), 25, 0.3, 0.6, 0.3, 0.3);
    spawn(world, Particle.REVERSE_PORTAL, spot.clone().add(0, 1, 0), 10, 0.3, 0.6, 0.3, 0.05);
  }

  /** Volatile: a bigger explosion than the plain one. */
  public static void volatileBlast(Location where, double radius) {
    World world = where.getWorld();
    if (world == null) {
      return;
    }
    spawn(world, Particle.FLAME, where, 30, radius / 3, 0.5, radius / 3, 0.15);
    spawn(world, Particle.LAVA, where, 8, radius / 3, 0.3, radius / 3, 0);
    spawn(world, Particle.SMOKE, where, 20, radius / 3, 0.5, radius / 3, 0.05);
    sound(world, where, Sound.ENTITY_BLAZE_SHOOT, 0.8f, 0.6f);
  }

  /** Any elite dying: a burst of smoke and a low toll. */
  public static void death(LivingEntity mob) {
    if (!EliteData.isElite(mob)) {
      return;
    }
    World world = mob.getWorld();
    Location at = middle(mob);
    spawn(world, Particle.SOUL_FIRE_FLAME, at, 25, 0.4, 0.6, 0.4, 0.06);
    spawn(world, Particle.SMOKE, at, 15, 0.4, 0.6, 0.4, 0.04);
    sound(world, at, Sound.ENTITY_WITHER_BREAK_BLOCK, 0.35f, 1.5f);
    sound(world, at, Sound.BLOCK_BELL_USE, 0.6f, 0.6f);
  }

  /** The killer gets the elite's loot. */
  public static void rewarded(Player killer, LivingEntity mob) {
    World world = mob.getWorld();
    Location at = middle(mob);
    spawn(world, Particle.TOTEM_OF_UNDYING, at, 25, 0.4, 0.7, 0.4, 0.3);
    spawn(world, Particle.END_ROD, at, 10, 0.3, 0.6, 0.3, 0.08);
    sound(killer.getWorld(), killer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.1f);
    sound(killer.getWorld(), killer.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.9f, 1.3f);
  }
}
