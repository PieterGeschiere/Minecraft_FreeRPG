package mc.carlton.freerpg.worldevents;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import mc.carlton.freerpg.FreeRPG;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

/**
 * The sounds and particles around the world events that are not the announcement itself: the
 * countdown, the end, Titan mobs, festival scores and payouts, Gold Rush noise and the reveal of
 * the Mutator Night twists. The announcement stays in {@link EventVisuals}.
 */
public final class WorldEventEffects {

  private static final Random RANDOM = new Random();

  /** The last countdown second that played for a player, so a second never plays twice. */
  private static final Map<UUID, Long> LAST_COUNTDOWN = new HashMap<>();

  private WorldEventEffects() {
  }

  /** The seconds before the end at which the countdown makes a sound. */
  static boolean isCountdownSecond(long secondsLeft) {
    return secondsLeft == 60 || secondsLeft == 10 || (secondsLeft >= 1 && secondsLeft <= 3);
  }

  /** Called every beat for each player in the event with the time that is left. */
  public static void countdown(Player p, long secondsLeft) {
    if (!isCountdownSecond(secondsLeft)) {
      return;
    }
    Long last = LAST_COUNTDOWN.get(p.getUniqueId());
    if (last != null && last == secondsLeft) {
      return;
    }
    LAST_COUNTDOWN.put(p.getUniqueId(), secondsLeft);
    Location at = p.getLocation();
    if (secondsLeft <= 3) {
      p.playSound(at, Sound.BLOCK_NOTE_BLOCK_HAT, 0.8f, 1.0f + (3 - secondsLeft) * 0.25f);
    } else if (secondsLeft == 10) {
      p.playSound(at, Sound.BLOCK_NOTE_BLOCK_BELL, 0.8f, 1.2f);
    } else {
      p.playSound(at, Sound.BLOCK_NOTE_BLOCK_CHIME, 0.7f, 0.9f);
    }
  }

  /** The event is over for this player. */
  public static void ended(Player p) {
    LAST_COUNTDOWN.remove(p.getUniqueId());
    Location at = p.getLocation();
    p.playSound(at, Sound.BLOCK_BEACON_DEACTIVATE, 0.7f, 1.0f);
    p.playSound(at, Sound.ENTITY_PLAYER_LEVELUP, 0.3f, 0.7f);
    p.getWorld().spawnParticle(Particle.END_ROD, at.clone().add(0, 1.0, 0), 20, 0.5, 0.8, 0.5,
        0.05);
  }

  /** Forgets a player that left. */
  public static void forget(UUID id) {
    LAST_COUNTDOWN.remove(id);
  }

  // ------------------------------------------------------------------ Eclipse

  /** Now and then a distant, uneasy sound for each player during the Eclipse. */
  public static void eclipseAmbience(Player p) {
    if (RANDOM.nextInt(45) == 0) {
      p.playSound(p.getLocation(), Sound.AMBIENT_CAVE, 0.5f, 0.8f);
    }
  }

  // ------------------------------------------------------------------ Titan Night

  /** A Titan mob appears: a short rumble and a puff of dust at its feet. */
  public static void titanSpawn(LivingEntity mob) {
    World world = mob.getWorld();
    Location at = mob.getLocation();
    world.spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, at, 8, 0.6, 0.1, 0.6, 0.02);
    world.playSound(at, Sound.ENTITY_IRON_GOLEM_STEP, 0.6f, 0.5f);
  }

  /** The killer of a Titan mob got the bonus. */
  public static void titanKill(Player killer, LivingEntity mob) {
    World world = mob.getWorld();
    Location at = mob.getLocation().add(0, mob.getHeight() / 2, 0);
    world.spawnParticle(Particle.EXPLOSION, at, 1);
    world.spawnParticle(Particle.TOTEM_OF_UNDYING, at, 25, 0.5, 0.8, 0.5, 0.3);
    world.playSound(at, Sound.ENTITY_GENERIC_EXPLODE, 0.4f, 1.4f);
    killer.playSound(killer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 0.9f);
  }

  // ------------------------------------------------------------------ Fishing Festival

  /** A catch scored points. */
  public static void festivalCatch(Player p, int points) {
    Location at = p.getLocation();
    p.playSound(at, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.6f, 0.9f + Math.min(points, 10) * 0.07f);
    p.getWorld().spawnParticle(Particle.BUBBLE_POP, at.clone().add(0, 1.2, 0), 8, 0.4, 0.4, 0.4,
        0.05);
  }

  /** The festival paid a prize. */
  public static void festivalPayout(Player p, boolean bonusSpin) {
    Location at = p.getLocation();
    p.playSound(at, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
    p.playSound(at, Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, 0.8f, 1.0f);
    p.getWorld().spawnParticle(Particle.FIREWORK, at.clone().add(0, 1.5, 0), bonusSpin ? 40 : 20,
        0.5, 0.8, 0.5, 0.15);
  }

  // ------------------------------------------------------------------ Scholar's Moon

  /** Levels were given back for an enchant. */
  public static void scholarRefund(Player p) {
    Location at = p.getLocation().add(0, 1.2, 0);
    p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, 1.2f);
    p.getWorld().spawnParticle(Particle.ENCHANT, at, 25, 0.4, 0.6, 0.4, 0.7);
  }

  // ------------------------------------------------------------------ Gold Rush

  /** The break of the ore was loud: a rumble around the miner. */
  public static void goldRushNoise(Player p) {
    Location at = p.getLocation();
    p.getWorld().playSound(at, Sound.BLOCK_AMETHYST_CLUSTER_BREAK, 1.0f, 0.5f);
    p.getWorld().playSound(at, Sound.BLOCK_DEEPSLATE_BREAK, 0.8f, 0.6f);
  }

  /** A mob that the noise drew shows up. */
  public static void goldRushMob(Location spot) {
    World world = spot.getWorld();
    if (world == null) {
      return;
    }
    world.spawnParticle(Particle.SMOKE, spot.clone().add(0, 0.5, 0), 12, 0.3, 0.5, 0.3, 0.03);
    world.spawnParticle(Particle.BLOCK, spot.clone().add(0, 0.2, 0), 10, 0.3, 0.1, 0.3, 0.05,
        spot.clone().subtract(0, 1, 0).getBlock().getBlockData());
    world.playSound(spot, Sound.BLOCK_DEEPSLATE_BRICKS_BREAK, 0.8f, 0.7f);
  }

  // ------------------------------------------------------------------ Mutator Night

  private static Sound mutatorSound(Mutator mutator) {
    switch (mutator) {
      case LOW_GRAVITY:
        return Sound.ENTITY_BREEZE_JUMP;
      case GLASS_CANNON:
        return Sound.BLOCK_GLASS_BREAK;
      case EXPLODING_LOOT:
        return Sound.ENTITY_CREEPER_PRIMED;
      case HUNGRY_NIGHT:
        return Sound.ENTITY_GENERIC_EAT;
      case SWIFT_MOBS:
        return Sound.ENTITY_PHANTOM_SWOOP;
      case DOUBLE_TROUBLE:
        return Sound.ENTITY_ILLUSIONER_MIRROR_MOVE;
      case SOUL_STORM:
        return Sound.PARTICLE_SOUL_ESCAPE;
      case GLOWING_MOBS:
        return Sound.BLOCK_BEACON_AMBIENT;
      default:
        return Sound.BLOCK_NOTE_BLOCK_CHIME;
    }
  }

  /** Reveals the twists one after the other, each with its own sound. */
  public static void mutatorReveal(Player p, Iterable<Mutator> twists) {
    long delay = 30;
    for (Mutator mutator : twists) {
      Sound sound = mutatorSound(mutator);
      Bukkit.getScheduler().runTaskLater(FreeRPG.getPlugin(FreeRPG.class), () -> {
        if (p.isOnline()) {
          p.playSound(p.getLocation(), sound, 0.8f, 1.0f);
          p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.5f, 1.3f);
        }
      }, delay);
      delay += 14;
    }
  }

  /** An explosion of the Exploding Loot twist: a bit of extra sparkle where the loot lies. */
  public static void explodingLoot(Entity mob) {
    mob.getWorld().spawnParticle(Particle.FLAME, mob.getLocation().add(0, 0.5, 0), 15, 0.4, 0.4,
        0.4, 0.05);
  }
}
