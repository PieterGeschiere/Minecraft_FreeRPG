package mc.carlton.freerpg.utils.game;

import java.util.Collection;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 * Sounds and particles of the perks that a player triggers on purpose (grappling hook, vein miner,
 * leaf blower and so on). Everything is played in the world, so everyone nearby sees and hears it.
 */
public final class PerkEffects {

  /** At most this many blocks get their own particles when a whole group breaks at once. */
  private static final int MAX_BLOCK_PARTICLES = 24;

  private PerkEffects() {
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

  private static <T> void spawn(World world, Particle particle, Location at, int count, double dx,
      double dy, double dz, double speed, T data) {
    if (EffectSettings.perkParticles()) {
      world.spawnParticle(particle, at, count, dx, dy, dz, speed, data);
    }
  }

  /** Grappling Hook: a rope of dust from the player to the hook and a whoosh when pulled. */
  public static void grapple(Player p, Location hook) {
    World world = p.getWorld();
    Location from = p.getLocation().add(0, 1.0, 0);
    Vector line = hook.toVector().subtract(from.toVector());
    double length = line.length();
    if (length > 0.1) {
      Vector step = line.clone().normalize().multiply(0.5);
      Location point = from.clone();
      Particle.DustOptions rope = new Particle.DustOptions(Color.fromRGB(150, 110, 60), 1.0f);
      for (double d = 0; d < Math.min(length, 40); d += 0.5) {
        spawn(world, Particle.DUST, point, 1, 0, 0, 0, 0, rope);
        point.add(step);
      }
    }
    spawn(world, Particle.CRIT, hook, 12, 0.2, 0.2, 0.2, 0.2);
    spawn(world, Particle.CLOUD, p.getLocation().add(0, 0.1, 0), 10, 0.3, 0.05, 0.3, 0.05);
    sound(world, hook, Sound.ENTITY_FISHING_BOBBER_RETRIEVE, 1f, 0.8f);
    sound(world, p.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.8f, 0.7f);
    sound(world, p.getLocation(), Sound.ITEM_TRIDENT_RIPTIDE_1, 0.5f, 1.5f);
  }

  /** Super Bait: a fountain of water where the extra catch comes out. */
  public static void superBait(Location water) {
    World world = water.getWorld();
    spawn(world, Particle.SPLASH, water, 40, 0.4, 0.3, 0.4, 0.3);
    spawn(world, Particle.BUBBLE_POP, water, 15, 0.3, 0.3, 0.3, 0.05);
    spawn(world, Particle.FISHING, water, 15, 0.4, 0.1, 0.4, 0.05);
    sound(world, water, Sound.ENTITY_FISHING_BOBBER_SPLASH, 1f, 1.2f);
    sound(world, water, Sound.ENTITY_PLAYER_SPLASH, 0.6f, 1.4f);
  }

  /** Rob: a quick snatch of an item from the mob on the hook. */
  public static void rob(Entity victim) {
    World world = victim.getWorld();
    Location at = victim.getLocation().add(0, victim.getHeight() / 2, 0);
    spawn(world, Particle.CRIT, at, 15, 0.3, 0.3, 0.3, 0.3);
    spawn(world, Particle.ENCHANTED_HIT, at, 10, 0.3, 0.3, 0.3, 0.2);
    sound(world, at, Sound.ENTITY_ITEM_PICKUP, 1f, 0.7f);
    sound(world, at, Sound.ENTITY_FISHING_BOBBER_RETRIEVE, 0.8f, 1.3f);
  }

  /** Vein Miner: the whole vein crumbles at once. */
  public static void veinMiner(Player p, Collection<Block> blocks) {
    World world = p.getWorld();
    int shown = 0;
    for (Block block : blocks) {
      if (shown++ >= MAX_BLOCK_PARTICLES) {
        break;
      }
      Location center = block.getLocation().add(0.5, 0.5, 0.5);
      spawn(world, Particle.BLOCK, center, 6, 0.3, 0.3, 0.3, 0.05, block.getBlockData());
      spawn(world, Particle.CRIT, center, 2, 0.3, 0.3, 0.3, 0.2);
    }
    sound(world, p.getLocation(), Sound.BLOCK_DEEPSLATE_BREAK, 1f, 0.7f);
    sound(world, p.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.3f, 1.6f);
    sound(world, p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.6f, 0.8f);
  }

  /** Leaf Blower: a gust that sweeps the leaves away. */
  public static void leafBlower(Player p, Collection<Block> leaves) {
    World world = p.getWorld();
    int shown = 0;
    for (Block block : leaves) {
      if (shown++ >= MAX_BLOCK_PARTICLES) {
        break;
      }
      Location center = block.getLocation().add(0.5, 0.5, 0.5);
      spawn(world, Particle.BLOCK, center, 5, 0.3, 0.3, 0.3, 0.05, block.getBlockData());
      spawn(world, Particle.CLOUD, center, 1, 0.2, 0.2, 0.2, 0.05);
    }
    sound(world, p.getLocation(), Sound.ENTITY_PHANTOM_FLAP, 1f, 1.4f);
    sound(world, p.getLocation(), Sound.BLOCK_AZALEA_LEAVES_BREAK, 1f, 0.9f);
  }

  /** Growth Hormones: the baby animal shoots up. */
  public static void grow(Entity animal) {
    World world = animal.getWorld();
    Location at = animal.getLocation().add(0, 0.6, 0);
    spawn(world, Particle.HAPPY_VILLAGER, at, 20, 0.4, 0.4, 0.4, 0);
    spawn(world, Particle.CLOUD, at, 8, 0.3, 0.3, 0.3, 0.05);
    sound(world, at, Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.8f);
    sound(world, at, Sound.ENTITY_GENERIC_EAT, 0.8f, 1.2f);
  }

  /** Roll: dust at the feet and a tumble. */
  public static void roll(Player p) {
    World world = p.getWorld();
    Location feet = p.getLocation().add(0, 0.1, 0);
    spawn(world, Particle.CLOUD, feet, 15, 0.4, 0.05, 0.4, 0.05);
    spawn(world, Particle.POOF, feet, 8, 0.3, 0.1, 0.3, 0.05);
    sound(world, feet, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.8f, 0.8f);
    sound(world, feet, Sound.BLOCK_WOOL_FALL, 1f, 1f);
  }

  /** Dodge: a quick sidestep, leaving puffs behind. */
  public static void dodge(Player p) {
    World world = p.getWorld();
    Location at = p.getLocation().add(0, 1.0, 0);
    spawn(world, Particle.POOF, at, 12, 0.4, 0.5, 0.4, 0.08);
    spawn(world, Particle.SWEEP_ATTACK, at, 1, 0.2, 0.2, 0.2, 0);
    sound(world, at, Sound.ENTITY_PLAYER_ATTACK_NODAMAGE, 1f, 1.4f);
    sound(world, at, Sound.ENTITY_BREEZE_WIND_BURST, 0.4f, 1.8f);
  }
}
