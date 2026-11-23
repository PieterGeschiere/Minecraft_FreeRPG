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
        world.spawnParticle(Particle.DUST, point, 1, 0, 0, 0, 0, rope);
        point.add(step);
      }
    }
    world.spawnParticle(Particle.CRIT, hook, 12, 0.2, 0.2, 0.2, 0.2);
    world.spawnParticle(Particle.CLOUD, p.getLocation().add(0, 0.1, 0), 10, 0.3, 0.05, 0.3, 0.05);
    world.playSound(hook, Sound.ENTITY_FISHING_BOBBER_RETRIEVE, 1f, 0.8f);
    world.playSound(p.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.8f, 0.7f);
    world.playSound(p.getLocation(), Sound.ITEM_TRIDENT_RIPTIDE_1, 0.5f, 1.5f);
  }

  /** Super Bait: a fountain of water where the extra catch comes out. */
  public static void superBait(Location water) {
    World world = water.getWorld();
    world.spawnParticle(Particle.SPLASH, water, 40, 0.4, 0.3, 0.4, 0.3);
    world.spawnParticle(Particle.BUBBLE_POP, water, 15, 0.3, 0.3, 0.3, 0.05);
    world.spawnParticle(Particle.FISHING, water, 15, 0.4, 0.1, 0.4, 0.05);
    world.playSound(water, Sound.ENTITY_FISHING_BOBBER_SPLASH, 1f, 1.2f);
    world.playSound(water, Sound.ENTITY_PLAYER_SPLASH, 0.6f, 1.4f);
  }

  /** Rob: a quick snatch of an item from the mob on the hook. */
  public static void rob(Entity victim) {
    World world = victim.getWorld();
    Location at = victim.getLocation().add(0, victim.getHeight() / 2, 0);
    world.spawnParticle(Particle.CRIT, at, 15, 0.3, 0.3, 0.3, 0.3);
    world.spawnParticle(Particle.ENCHANTED_HIT, at, 10, 0.3, 0.3, 0.3, 0.2);
    world.playSound(at, Sound.ENTITY_ITEM_PICKUP, 1f, 0.7f);
    world.playSound(at, Sound.ENTITY_FISHING_BOBBER_RETRIEVE, 0.8f, 1.3f);
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
      world.spawnParticle(Particle.BLOCK, center, 6, 0.3, 0.3, 0.3, 0.05, block.getBlockData());
      world.spawnParticle(Particle.CRIT, center, 2, 0.3, 0.3, 0.3, 0.2);
    }
    world.playSound(p.getLocation(), Sound.BLOCK_DEEPSLATE_BREAK, 1f, 0.7f);
    world.playSound(p.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.3f, 1.6f);
    world.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.6f, 0.8f);
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
      world.spawnParticle(Particle.BLOCK, center, 5, 0.3, 0.3, 0.3, 0.05, block.getBlockData());
      world.spawnParticle(Particle.CLOUD, center, 1, 0.2, 0.2, 0.2, 0.05);
    }
    world.playSound(p.getLocation(), Sound.ENTITY_PHANTOM_FLAP, 1f, 1.4f);
    world.playSound(p.getLocation(), Sound.BLOCK_AZALEA_LEAVES_BREAK, 1f, 0.9f);
  }

  /** Growth Hormones: the baby animal shoots up. */
  public static void grow(Entity animal) {
    World world = animal.getWorld();
    Location at = animal.getLocation().add(0, 0.6, 0);
    world.spawnParticle(Particle.HAPPY_VILLAGER, at, 20, 0.4, 0.4, 0.4, 0);
    world.spawnParticle(Particle.CLOUD, at, 8, 0.3, 0.3, 0.3, 0.05);
    world.playSound(at, Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.8f);
    world.playSound(at, Sound.ENTITY_GENERIC_EAT, 0.8f, 1.2f);
  }

  /** Roll: dust at the feet and a tumble. */
  public static void roll(Player p) {
    World world = p.getWorld();
    Location feet = p.getLocation().add(0, 0.1, 0);
    world.spawnParticle(Particle.CLOUD, feet, 15, 0.4, 0.05, 0.4, 0.05);
    world.spawnParticle(Particle.POOF, feet, 8, 0.3, 0.1, 0.3, 0.05);
    world.playSound(feet, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.8f, 0.8f);
    world.playSound(feet, Sound.BLOCK_WOOL_FALL, 1f, 1f);
  }

  /** Dodge: a quick sidestep, leaving puffs behind. */
  public static void dodge(Player p) {
    World world = p.getWorld();
    Location at = p.getLocation().add(0, 1.0, 0);
    world.spawnParticle(Particle.POOF, at, 12, 0.4, 0.5, 0.4, 0.08);
    world.spawnParticle(Particle.SWEEP_ATTACK, at, 1, 0.2, 0.2, 0.2, 0);
    world.playSound(at, Sound.ENTITY_PLAYER_ATTACK_NODAMAGE, 1f, 1.4f);
    world.playSound(at, Sound.ENTITY_BREEZE_WIND_BURST, 0.4f, 1.8f);
  }
}
