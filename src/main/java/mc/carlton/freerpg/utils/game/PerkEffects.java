package mc.carlton.freerpg.utils.game;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import mc.carlton.freerpg.FreeRPG;
import org.bukkit.Bukkit;
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

  // ---------------------------------------------------------------------------------------------
  // Helpers
  // ---------------------------------------------------------------------------------------------

  /** When a cue last played for a player, so perks that fire in quick bursts do not spam sound. */
  private static final Map<String, Long> LAST_PLAYED = new HashMap<>();

  /** True at most once per {@code millis} for the player and key. */
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

  /** Forgets the throttle state of a player that left. */
  public static void forget(UUID id) {
    LAST_PLAYED.keySet().removeIf(k -> k.startsWith(id.toString()));
  }

  private static Location chest(Player p) {
    return p.getLocation().add(0, 1.0, 0);
  }

  private static Location middle(Entity e) {
    return e.getLocation().add(0, e.getHeight() / 2, 0);
  }

  private static void later(Runnable task, long ticks) {
    Bukkit.getScheduler().runTaskLater(FreeRPG.getPlugin(FreeRPG.class), task, ticks);
  }

  // ---------------------------------------------------------------------------------------------
  // Defense
  // ---------------------------------------------------------------------------------------------

  /** Hard Body: the blow is shrugged off by the armor. */
  public static void hardBody(Player p) {
    World world = p.getWorld();
    Location at = chest(p);
    spawn(world, Particle.CRIT, at, 14, 0.35, 0.5, 0.35, 0.25);
    spawn(world, Particle.ENCHANTED_HIT, at, 10, 0.35, 0.5, 0.35, 0.2);
    sound(world, at, Sound.ITEM_SHIELD_BLOCK, 0.8f, 1.3f);
    sound(world, at, Sound.ITEM_ARMOR_EQUIP_NETHERITE, 0.7f, 0.9f);
  }

  /** Reactions: a flash of resistance. Level II gets the stronger version. */
  public static void reactions(Player p, boolean strong) {
    if (!throttle(p.getUniqueId(), "reactions", 400)) {
      return;
    }
    World world = p.getWorld();
    Location at = chest(p);
    spawn(world, Particle.ENCHANTED_HIT, at, strong ? 20 : 10, 0.4, 0.6, 0.4, 0.15);
    if (strong) {
      spawn(world, Particle.END_ROD, at, 8, 0.3, 0.6, 0.3, 0.05);
    }
    sound(world, at, Sound.ITEM_SHIELD_BLOCK, 0.5f, strong ? 1.6f : 1.2f);
    sound(world, at, Sound.ENTITY_IRON_GOLEM_REPAIR, 0.5f, strong ? 1.5f : 1.1f);
  }

  /** Healer: hearts rise and a soft chime plays. */
  public static void healer(Player p, boolean healed) {
    if (!throttle(p.getUniqueId(), "healer", 600)) {
      return;
    }
    World world = p.getWorld();
    Location at = p.getLocation().add(0, 1.6, 0);
    spawn(world, Particle.HEART, at, healed ? 6 : 3, 0.35, 0.3, 0.35, 0);
    spawn(world, Particle.HAPPY_VILLAGER, at, 6, 0.4, 0.5, 0.4, 0);
    sound(world, at, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.7f, healed ? 1.6f : 1.2f);
    sound(world, at, Sound.ENTITY_PLAYER_LEVELUP, 0.25f, 1.9f);
  }

  /** Hearty: extra hearts were granted or taken away. */
  public static void hearty(Player p, boolean gained) {
    World world = p.getWorld();
    Location at = p.getLocation().add(0, 1.6, 0);
    spawn(world, gained ? Particle.HEART : Particle.SMOKE, at, gained ? 8 : 10, 0.4, 0.4, 0.4, 0);
    sound(world, at, gained ? Sound.ENTITY_PLAYER_LEVELUP : Sound.BLOCK_BEACON_DEACTIVATE,
        0.4f, gained ? 1.5f : 1.3f);
  }

  // ---------------------------------------------------------------------------------------------
  // Swordsmanship and Axe Mastery
  // ---------------------------------------------------------------------------------------------

  /** Thirst for Blood: a gulp and a few red drops. */
  public static void thirstForBlood(Player p) {
    if (!throttle(p.getUniqueId(), "thirst", 500)) {
      return;
    }
    World world = p.getWorld();
    Location at = p.getLocation().add(0, 1.4, 0);
    spawn(world, Particle.DAMAGE_INDICATOR, at, 4, 0.25, 0.2, 0.25, 0.05);
    spawn(world, Particle.DUST, at, 8, 0.25, 0.25, 0.25, 0,
        new Particle.DustOptions(Color.fromRGB(170, 20, 20), 1.0f));
    sound(world, at, Sound.ENTITY_GENERIC_DRINK, 0.5f, 0.8f);
  }

  /** Double Hit: the follow up strike lands on the target. */
  public static void doubleHit(Entity target) {
    World world = target.getWorld();
    Location at = middle(target);
    spawn(world, Particle.SWEEP_ATTACK, at, 1, 0.2, 0.2, 0.2, 0);
    spawn(world, Particle.CRIT, at, 12, 0.3, 0.4, 0.3, 0.3);
    sound(world, at, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.9f, 1.4f);
    sound(world, at, Sound.ENTITY_PLAYER_ATTACK_STRONG, 0.8f, 1.2f);
  }

  /** Kill buffs (Adrenaline, Killing Spree): a rush of power after a kill. */
  public static void killBuff(Player p) {
    if (!throttle(p.getUniqueId(), "killBuff", 800)) {
      return;
    }
    World world = p.getWorld();
    Location at = p.getLocation().add(0, 1.0, 0);
    spawn(world, Particle.FLAME, at, 10, 0.3, 0.6, 0.3, 0.03);
    spawn(world, Particle.CRIT, at, 10, 0.3, 0.6, 0.3, 0.3);
    sound(world, at, Sound.ENTITY_PLAYER_ATTACK_CRIT, 0.6f, 0.9f);
    sound(world, at, Sound.ENTITY_BLAZE_SHOOT, 0.25f, 1.7f);
  }

  /** Great Axe slam: the shock where the axe hits. */
  public static void axeSlam(Location at, double radius) {
    World world = at.getWorld();
    spawn(world, Particle.EXPLOSION, at, 1, 0, 0, 0, 0);
    spawn(world, Particle.CRIT, at.clone().add(0, 0.5, 0), 25, radius / 2, 0.3, radius / 2, 0.3);
    sound(world, at, Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 1.2f);
    sound(world, at, Sound.BLOCK_ANVIL_LAND, 0.5f, 0.7f);
  }

  /** Divine Critical: golden sparks and a ring. */
  public static void divineCritical(Player p) {
    World world = p.getWorld();
    Location at = p.getLocation().add(0, 1.2, 0);
    spawn(world, Particle.CRIT, at, 25, 0.4, 0.5, 0.4, 0.4);
    spawn(world, Particle.END_ROD, at, 10, 0.3, 0.5, 0.3, 0.08);
    sound(world, at, Sound.ENTITY_PLAYER_ATTACK_CRIT, 1f, 0.8f);
    sound(world, at, Sound.BLOCK_BEACON_POWER_SELECT, 0.4f, 1.8f);
  }

  /** Revitalized: fully healed. */
  public static void revitalized(Player p) {
    World world = p.getWorld();
    Location at = p.getLocation().add(0, 1.0, 0);
    spawn(world, Particle.TOTEM_OF_UNDYING, at, 25, 0.4, 0.7, 0.4, 0.3);
    spawn(world, Particle.HEART, at.clone().add(0, 0.7, 0), 6, 0.4, 0.3, 0.4, 0);
    sound(world, at, Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.3f);
    sound(world, at, Sound.ITEM_TOTEM_USE, 0.25f, 1.6f);
  }

  /** Warrior Blood: a growl and angry sparks. */
  public static void warriorBlood(Player p) {
    if (!throttle(p.getUniqueId(), "warrior", 800)) {
      return;
    }
    World world = p.getWorld();
    Location at = p.getLocation().add(0, 1.8, 0);
    spawn(world, Particle.ANGRY_VILLAGER, at, 3, 0.3, 0.2, 0.3, 0);
    spawn(world, Particle.DUST, p.getLocation().add(0, 1.0, 0), 14, 0.4, 0.6, 0.4, 0,
        new Particle.DustOptions(Color.fromRGB(200, 30, 30), 1.2f));
    sound(world, at, Sound.ENTITY_ZOGLIN_ANGRY, 0.5f, 1.3f);
  }

  /** Holy Axe: sparkles at the spot where the lightning comes down. */
  public static void holyAxe(Entity target) {
    World world = target.getWorld();
    Location at = middle(target);
    spawn(world, Particle.END_ROD, at, 25, 0.3, 0.8, 0.3, 0.15);
    spawn(world, Particle.ELECTRIC_SPARK, at, 20, 0.4, 0.6, 0.4, 0.3);
    sound(world, at, Sound.BLOCK_BEACON_POWER_SELECT, 0.6f, 1.6f);
  }

  // ---------------------------------------------------------------------------------------------
  // Drops, treasures and groups of blocks
  // ---------------------------------------------------------------------------------------------

  /** An extra drop from a double drop perk: a sparkle on the spot and a short ding. */
  public static void bonusDrop(Player p, Location at) {
    World world = at.getWorld();
    Location spot = at.clone().add(0.5, 0.6, 0.5);
    spawn(world, Particle.HAPPY_VILLAGER, spot, 6, 0.25, 0.25, 0.25, 0);
    spawn(world, Particle.END_ROD, spot, 3, 0.2, 0.2, 0.2, 0.02);
    if (p == null || throttle(p.getUniqueId(), "bonusDrop", 250)) {
      sound(world, spot, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.6f);
      sound(world, spot, Sound.ENTITY_ITEM_PICKUP, 0.4f, 1.3f);
    }
  }

  /** The same for a drop that comes from a mob. */
  public static void bonusDrop(Player p, Entity from) {
    bonusDrop(p, from.getLocation().add(-0.5, 0, -0.5));
  }

  /** A treasure found while digging, mining or fishing. {@code rare} plays the bigger version. */
  public static void treasure(Player p, Location at, boolean rare) {
    World world = at.getWorld();
    Location spot = at.clone().add(0.5, 0.6, 0.5);
    spawn(world, Particle.END_ROD, spot, rare ? 25 : 10, 0.3, 0.4, 0.3, 0.06);
    spawn(world, Particle.HAPPY_VILLAGER, spot, rare ? 14 : 6, 0.3, 0.3, 0.3, 0);
    if (rare) {
      spawn(world, Particle.FIREWORK, spot, 20, 0.3, 0.5, 0.3, 0.12);
      spawn(world, Particle.TOTEM_OF_UNDYING, spot, 15, 0.3, 0.5, 0.3, 0.2);
    }
    if (p == null || throttle(p.getUniqueId(), "treasure", 200)) {
      sound(world, spot, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1f, rare ? 1.9f : 1.5f);
      sound(world, spot, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.7f, rare ? 1.2f : 1.0f);
      if (rare) {
        sound(world, spot, Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.5f, 1.4f);
      }
    }
  }

  /** Timber: the trunk creaks, then the whole tree comes down. */
  public static void timber(Player p, Collection<Block> logs) {
    World world = p.getWorld();
    Location at = p.getLocation();
    int shown = 0;
    for (Block block : logs) {
      if (shown++ >= MAX_BLOCK_PARTICLES) {
        break;
      }
      spawn(world, Particle.BLOCK, block.getLocation().add(0.5, 0.5, 0.5), 5, 0.3, 0.3, 0.3,
          0.05, block.getBlockData());
    }
    sound(world, at, Sound.BLOCK_WOOD_BREAK, 1f, 0.6f);
    sound(world, at, Sound.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR, 0.5f, 0.6f);
    later(() -> {
      sound(world, at, Sound.BLOCK_WOOD_FALL, 1f, 0.5f);
      sound(world, at, Sound.BLOCK_AZALEA_LEAVES_BREAK, 0.8f, 0.7f);
    }, 6L);
  }

  /** Mega Dig: dust from one block, played just before it breaks. */
  public static void megaDigBlock(Block block) {
    spawn(block.getWorld(), Particle.BLOCK, block.getLocation().add(0.5, 0.5, 0.5), 5, 0.3, 0.3,
        0.3, 0.05, block.getBlockData());
  }

  /** Mega Dig: the sound and cloud once the whole area is gone. */
  public static void megaDigDone(Player p) {
    World world = p.getWorld();
    spawn(world, Particle.CLOUD, p.getLocation().add(0, 0.5, 0), 8, 0.6, 0.2, 0.6, 0.03);
    sound(world, p.getLocation(), Sound.BLOCK_GRAVEL_BREAK, 1f, 0.6f);
    sound(world, p.getLocation(), Sound.BLOCK_SAND_BREAK, 0.8f, 0.8f);
    sound(world, p.getLocation(), Sound.BLOCK_ROOTED_DIRT_BREAK, 0.7f, 0.7f);
  }

  /** Mining TNT: the fuse lights. */
  public static void tntLit(Location at) {
    World world = at.getWorld();
    spawn(world, Particle.FLAME, at.clone().add(0, 0.6, 0), 6, 0.15, 0.15, 0.15, 0.02);
    sound(world, at, Sound.ENTITY_TNT_PRIMED, 1f, 1f);
  }

  /** Mining TNT: a bigger bang with a rumble. */
  public static void tntBlast(Location at) {
    World world = at.getWorld();
    spawn(world, Particle.EXPLOSION_EMITTER, at, 1, 0, 0, 0, 0);
    spawn(world, Particle.LAVA, at, 12, 1.0, 0.6, 1.0, 0);
    sound(world, at, Sound.ENTITY_GENERIC_EXPLODE, 0.6f, 0.6f);
    sound(world, at, Sound.BLOCK_DEEPSLATE_BREAK, 1f, 0.5f);
  }

  // ---------------------------------------------------------------------------------------------
  // Global skill
  // ---------------------------------------------------------------------------------------------

  /** Soul Harvest: a wisp of the dead mob's soul flies to the player. */
  public static void soulGained(Player p, Entity from, int amount) {
    World world = p.getWorld();
    Location start = middle(from);
    Location end = p.getLocation().add(0, 1.2, 0);
    Vector line = end.toVector().subtract(start.toVector());
    int steps = (int) Math.min(12, Math.max(3, line.length() * 2));
    Vector step = line.clone().multiply(1.0 / steps);
    boolean big = amount >= 100;
    for (int i = 0; i <= steps; i++) {
      Location point = start.clone().add(step.clone().multiply(i));
      spawn(world, Particle.SOUL, point, 1, 0.05, 0.05, 0.05, 0.01);
    }
    spawn(world, Particle.SOUL_FIRE_FLAME, end, big ? 40 : 6, 0.3, 0.5, 0.3, 0.03);
    if (big || throttle(p.getUniqueId(), "soul", 400)) {
      sound(world, end, Sound.PARTICLE_SOUL_ESCAPE, big ? 1.5f : 0.7f, big ? 0.8f : 1.3f);
    }
  }

  /** Souls were spent or lost. */
  public static void soulsLost(Player p) {
    World world = p.getWorld();
    Location at = p.getLocation().add(0, 1.0, 0);
    spawn(world, Particle.SOUL, at, 20, 0.4, 0.7, 0.4, 0.05);
    spawn(world, Particle.ASH, at, 15, 0.5, 0.7, 0.5, 0);
    sound(world, at, Sound.PARTICLE_SOUL_ESCAPE, 1f, 0.5f);
  }

  /** Avatar: a column of light. */
  public static void avatar(Player p) {
    World world = p.getWorld();
    Location base = p.getLocation();
    for (double y = 0; y < 3.0; y += 0.25) {
      spawn(world, Particle.END_ROD, base.clone().add(0, y, 0), 2, 0.25, 0.0, 0.25, 0.01);
    }
    spawn(world, Particle.TOTEM_OF_UNDYING, base.clone().add(0, 1.0, 0), 30, 0.4, 0.8, 0.4, 0.3);
    sound(world, base, Sound.BLOCK_BEACON_ACTIVATE, 0.8f, 1.5f);
    sound(world, base, Sound.ENTITY_PLAYER_LEVELUP, 0.6f, 1.0f);
  }

  /** Better Resurrection: the saved items come back with a totem flash. */
  public static void resurrection(Player p) {
    World world = p.getWorld();
    Location at = p.getLocation().add(0, 1.0, 0);
    spawn(world, Particle.TOTEM_OF_UNDYING, at, 40, 0.4, 0.8, 0.4, 0.35);
    spawn(world, Particle.SOUL_FIRE_FLAME, at, 15, 0.3, 0.6, 0.3, 0.05);
    sound(world, at, Sound.ITEM_TOTEM_USE, 0.6f, 1.2f);
  }

  // ---------------------------------------------------------------------------------------------
  // Farming
  // ---------------------------------------------------------------------------------------------

  /** Natural Regeneration (Green Thumb): the crop pops back up. */
  public static void cropRegrow(Block block) {
    World world = block.getWorld();
    Location at = block.getLocation().add(0.5, 0.4, 0.5);
    spawn(world, Particle.HAPPY_VILLAGER, at, 8, 0.3, 0.3, 0.3, 0);
    sound(world, at, Sound.ITEM_BONE_MEAL_USE, 0.5f, 1.4f);
    sound(world, at, Sound.BLOCK_GRASS_PLACE, 0.6f, 1.2f);
  }

  /** Tall crops (cane, cactus, bamboo, kelp) cut in one go. */
  public static void tallCrops(Player p, Collection<Block> blocks) {
    World world = p.getWorld();
    int shown = 0;
    for (Block block : blocks) {
      if (shown++ >= MAX_BLOCK_PARTICLES) {
        break;
      }
      spawn(world, Particle.BLOCK, block.getLocation().add(0.5, 0.5, 0.5), 4, 0.3, 0.3, 0.3,
          0.05, block.getBlockData());
    }
    if (blocks.size() > 2) {
      sound(world, p.getLocation(), Sound.BLOCK_BAMBOO_BREAK, 0.8f, 1.2f);
      sound(world, p.getLocation(), Sound.ENTITY_PHANTOM_FLAP, 0.3f, 1.8f);
    }
  }

  /** Better Fertilizer: the bone meal was not used up. */
  public static void fertilizerSaved(Player p, Block block) {
    World world = block.getWorld();
    Location at = block.getLocation().add(0.5, 0.8, 0.5);
    spawn(world, Particle.HAPPY_VILLAGER, at, 10, 0.3, 0.3, 0.3, 0);
    spawn(world, Particle.END_ROD, at, 4, 0.2, 0.3, 0.2, 0.02);
    sound(world, at, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.7f);
  }

  /** The composter is full of compost. */
  public static void composterFull(Block block) {
    World world = block.getWorld();
    Location at = block.getLocation().add(0.5, 1.0, 0.5);
    spawn(world, Particle.COMPOSTER, at, 12, 0.3, 0.2, 0.3, 0);
    sound(world, at, Sound.BLOCK_COMPOSTER_READY, 0.8f, 1.2f);
  }

  /** One With Nature: a soft green shimmer when the grass heals the player. */
  public static void oneWithNature(Player p) {
    World world = p.getWorld();
    Location at = p.getLocation().add(0, 0.5, 0);
    spawn(world, Particle.HAPPY_VILLAGER, at, 12, 0.4, 0.5, 0.4, 0);
    spawn(world, Particle.SPORE_BLOSSOM_AIR, at, 8, 0.5, 0.6, 0.5, 0);
    sound(world, at, Sound.BLOCK_MOSS_BREAK, 0.7f, 1.2f);
    sound(world, at, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.35f, 1.9f);
  }

  // ---------------------------------------------------------------------------------------------
  // Beast Mastery
  // ---------------------------------------------------------------------------------------------

  /** A new tamed animal: a bond forms. */
  public static void tamed(Player p, Entity animal) {
    World world = animal.getWorld();
    Location at = animal.getLocation().add(0, animal.getHeight() + 0.2, 0);
    spawn(world, Particle.HAPPY_VILLAGER, at, 10, 0.4, 0.3, 0.4, 0);
    spawn(world, Particle.NOTE, at, 4, 0.4, 0.3, 0.4, 1.0);
    sound(world, at, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.8f, 1.5f);
    sound(world, at, Sound.ENTITY_PLAYER_LEVELUP, 0.3f, 1.7f);
  }

  /** The compass check of an animal's stats. */
  public static void inspect(Player p, Entity animal) {
    World world = animal.getWorld();
    Location at = animal.getLocation().add(0, animal.getHeight() / 2, 0);
    spawn(world, Particle.ENCHANT, at, 20, 0.4, 0.5, 0.4, 0.5);
    sound(p.getWorld(), p.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.4f);
    sound(world, at, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 0.4f, 1.6f);
  }

  /** The tamed dog finished off a mob. */
  public static void dogKill(Player p, Entity victim) {
    if (!throttle(p.getUniqueId(), "dogKill", 500)) {
      return;
    }
    World world = victim.getWorld();
    Location at = middle(victim);
    spawn(world, Particle.CRIT, at, 10, 0.3, 0.4, 0.3, 0.3);
    sound(world, at, Sound.ENTITY_WOLF_GROWL, 0.5f, 1.2f);
    sound(p.getWorld(), p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.3f, 1.2f);
  }

  /** Riding XP while moving on a mount: a few puffs of dust behind it. */
  public static void riding(Entity mount) {
    World world = mount.getWorld();
    spawn(world, Particle.CLOUD, mount.getLocation().add(0, 0.1, 0), 4, 0.3, 0.05, 0.3, 0.02);
  }

  // ---------------------------------------------------------------------------------------------
  // Agility and Fishing
  // ---------------------------------------------------------------------------------------------

  /** Graceful Feet: a soft burst of speed. */
  public static void gracefulFeet(Player p, boolean on) {
    if (!throttle(p.getUniqueId(), "graceful", 1500)) {
      return;
    }
    World world = p.getWorld();
    Location feet = p.getLocation().add(0, 0.1, 0);
    spawn(world, on ? Particle.CLOUD : Particle.POOF, feet, on ? 8 : 5, 0.3, 0.05, 0.3, 0.03);
    sound(world, feet, Sound.ENTITY_PHANTOM_FLAP, 0.25f, on ? 1.8f : 1.2f);
    sound(world, feet, Sound.BLOCK_WOOL_STEP, 0.4f, on ? 1.5f : 1.0f);
  }

  /** Fish Person: the water grants its gifts. */
  public static void fishPerson(Player p) {
    World world = p.getWorld();
    Location at = p.getLocation().add(0, 1.0, 0);
    spawn(world, Particle.BUBBLE_POP, at, 15, 0.4, 0.6, 0.4, 0.05);
    spawn(world, Particle.DOLPHIN, at, 8, 0.4, 0.5, 0.4, 0.05);
    sound(world, at, Sound.ENTITY_DOLPHIN_SPLASH, 0.5f, 1.3f);
    sound(world, at, Sound.AMBIENT_UNDERWATER_ENTER, 0.5f, 1.2f);
  }

  /** A better catch from the fishing treasure tiers (1 to 5). Tier 1 stays silent. */
  public static void fishCatch(Player p, int tier) {
    if (tier < 2) {
      return;
    }
    World world = p.getWorld();
    Location at = p.getLocation().add(0, 1.2, 0);
    spawn(world, Particle.BUBBLE_POP, at, 6 + tier * 3, 0.4, 0.4, 0.4, 0.05);
    if (tier >= 3) {
      spawn(world, Particle.END_ROD, at, tier * 4, 0.4, 0.5, 0.4, 0.05);
    }
    if (tier >= 5) {
      spawn(world, Particle.TOTEM_OF_UNDYING, at, 25, 0.4, 0.6, 0.4, 0.3);
    }
    sound(world, at, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.7f, 0.9f + tier * 0.2f);
    if (tier >= 4) {
      sound(world, at, Sound.UI_TOAST_CHALLENGE_COMPLETE, tier >= 5 ? 0.8f : 0.45f, 1.2f);
    }
  }

  // ---------------------------------------------------------------------------------------------
  // Smelting, Alchemy, Enchanting and Repair
  // ---------------------------------------------------------------------------------------------

  /** Flame Pick: the block is smelted on the spot. */
  public static void flamePick(Block block, boolean doubled) {
    World world = block.getWorld();
    Location at = block.getLocation().add(0.5, 0.5, 0.5);
    spawn(world, Particle.FLAME, at, doubled ? 18 : 8, 0.3, 0.3, 0.3, 0.04);
    spawn(world, Particle.LAVA, at, 3, 0.2, 0.2, 0.2, 0);
    sound(world, at, Sound.ITEM_FIRECHARGE_USE, 0.5f, 1.4f);
    sound(world, at, Sound.BLOCK_LAVA_POP, 0.6f, 1.0f);
    if (doubled) {
      sound(world, at, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.4f, 1.5f);
    }
  }

  /** Double Smelt: the furnace gives an extra item. */
  public static void doubleSmelt(Location furnace) {
    World world = furnace.getWorld();
    Location at = furnace.clone().add(0.5, 1.1, 0.5);
    spawn(world, Particle.FLAME, at, 8, 0.2, 0.1, 0.2, 0.03);
    spawn(world, Particle.HAPPY_VILLAGER, at, 4, 0.2, 0.1, 0.2, 0);
    sound(world, at, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.4f, 1.5f);
    sound(world, at, Sound.BLOCK_BLASTFURNACE_FIRE_CRACKLE, 0.5f, 1.2f);
  }

  /** A brew is finished. */
  public static void brewDone(Location stand) {
    World world = stand.getWorld();
    Location at = stand.clone().add(0.5, 1.0, 0.5);
    spawn(world, Particle.WITCH, at, 14, 0.25, 0.3, 0.25, 0.02);
    spawn(world, Particle.BUBBLE_POP, at, 6, 0.2, 0.2, 0.2, 0.02);
    sound(world, at, Sound.BLOCK_BREWING_STAND_BREW, 0.8f, 1.2f);
    sound(world, at, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.6f, 1.4f);
  }

  /** Potion Master: the potion is stronger than usual. */
  public static void potionMaster(Player p) {
    if (!throttle(p.getUniqueId(), "potionMaster", 600)) {
      return;
    }
    World world = p.getWorld();
    Location at = p.getLocation().add(0, 1.0, 0);
    spawn(world, Particle.WITCH, at, 12, 0.4, 0.6, 0.4, 0.05);
    spawn(world, Particle.END_ROD, at, 5, 0.3, 0.6, 0.3, 0.03);
    sound(world, at, Sound.BLOCK_BEACON_POWER_SELECT, 0.4f, 1.8f);
  }

  /** An item was enchanted at the table. */
  public static void enchanted(Player p) {
    World world = p.getWorld();
    Location at = p.getLocation().add(0, 1.2, 0);
    spawn(world, Particle.ENCHANT, at, 40, 0.5, 0.8, 0.5, 0.8);
    spawn(world, Particle.END_ROD, at, 6, 0.3, 0.6, 0.3, 0.03);
    sound(world, at, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.8f, 1.2f);
    sound(world, at, Sound.ENTITY_PLAYER_LEVELUP, 0.3f, 1.6f);
  }

  /** An item was repaired. */
  public static void repaired(Player p) {
    World world = p.getWorld();
    Location at = p.getLocation().add(0, 1.2, 0);
    spawn(world, Particle.CRIT, at, 12, 0.3, 0.3, 0.3, 0.2);
    spawn(world, Particle.ENCHANTED_HIT, at, 8, 0.3, 0.3, 0.3, 0.2);
    sound(world, at, Sound.ENTITY_IRON_GOLEM_REPAIR, 0.6f, 1.4f);
  }

  /** An item was salvaged into its materials. */
  public static void salvaged(Player p) {
    World world = p.getWorld();
    Location at = p.getLocation().add(0, 1.0, 0);
    spawn(world, Particle.CRIT, at, 15, 0.4, 0.3, 0.4, 0.3);
    spawn(world, Particle.SMOKE, at, 6, 0.3, 0.2, 0.3, 0.02);
    sound(world, at, Sound.BLOCK_GRINDSTONE_USE, 0.8f, 1.0f);
    sound(world, at, Sound.ENTITY_ITEM_PICKUP, 0.6f, 0.8f);
  }

  /** Magic Repair kept the enchantments. */
  public static void magicRepair(Player p) {
    World world = p.getWorld();
    Location at = p.getLocation().add(0, 1.2, 0);
    spawn(world, Particle.ENCHANT, at, 30, 0.4, 0.6, 0.4, 0.6);
    sound(world, at, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 0.6f, 1.3f);
  }

  /** A repair went wrong and enchantments were lost. */
  public static void repairFailed(Player p) {
    World world = p.getWorld();
    Location at = p.getLocation().add(0, 1.2, 0);
    spawn(world, Particle.SMOKE, at, 15, 0.3, 0.3, 0.3, 0.03);
    sound(world, at, Sound.BLOCK_ANVIL_BREAK, 0.6f, 1.2f);
  }

  // ---------------------------------------------------------------------------------------------
  // Custom recipes and potions
  // ---------------------------------------------------------------------------------------------

  /** A custom recipe (egg, enchanted book, potion) was crafted. */
  public static void customCrafted(Player p) {
    World world = p.getWorld();
    Location at = p.getLocation().add(0, 1.2, 0);
    spawn(world, Particle.ENCHANT, at, 30, 0.4, 0.6, 0.4, 0.7);
    spawn(world, Particle.HAPPY_VILLAGER, at, 8, 0.4, 0.5, 0.4, 0);
    sound(world, at, Sound.BLOCK_SMITHING_TABLE_USE, 0.7f, 1.2f);
    sound(world, at, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.8f, 1.3f);
  }

  /** A custom recipe was refused because a perk is missing. */
  public static void craftDenied(Player p) {
    World world = p.getWorld();
    Location at = p.getLocation().add(0, 1.0, 0);
    spawn(world, Particle.SMOKE, at, 10, 0.3, 0.4, 0.3, 0.02);
    sound(world, at, Sound.BLOCK_NOTE_BLOCK_BASS, 0.8f, 0.6f);
    sound(world, at, Sound.ENTITY_VILLAGER_NO, 0.4f, 1.0f);
  }

  /** The player drank a potion with custom effects. */
  public static void customPotion(Player p) {
    World world = p.getWorld();
    Location at = p.getLocation().add(0, 1.0, 0);
    spawn(world, Particle.WITCH, at, 20, 0.4, 0.7, 0.4, 0.05);
    spawn(world, Particle.ENCHANT, at, 15, 0.4, 0.7, 0.4, 0.6);
    sound(world, at, Sound.ENTITY_WITCH_DRINK, 0.4f, 1.3f);
    sound(world, at, Sound.BLOCK_BEACON_POWER_SELECT, 0.3f, 1.9f);
  }
}
