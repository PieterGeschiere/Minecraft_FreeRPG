package mc.carlton.freerpg.core.info.player;

import mc.carlton.freerpg.FreeRPG;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Firework;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.meta.FireworkMeta;

/**
 * Sounds, fireworks and chat posts for level ups. Every skill level up gets a very short blip;
 * every 100th skill level and every 100th global (power) level gets more noise, a firework
 * everybody nearby can see and a post in chat for the whole server.
 */
public final class LevelCelebration {

  /** Metadata key on the celebration fireworks, so their explosion never hurts anyone. */
  private static final String TAG = "frpgCelebration";

  /** Every this many levels is a milestone. */
  static final int MILESTONE = 100;

  private LevelCelebration() {
  }

  /** True when going from {@code oldLevel} to {@code newLevel} passes a multiple of 100. */
  static boolean crossedMilestone(int oldLevel, int newLevel) {
    return newLevel / MILESTONE > oldLevel / MILESTONE;
  }

  /** The highest multiple of 100 that is at or below the level. */
  static int milestoneOf(int level) {
    return level / MILESTONE * MILESTONE;
  }

  /** The very short sound of any level up. */
  public static void blip(Player p) {
    p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.6f, 1.4f);
  }

  /**
   * A skill reached a multiple of 100.
   *
   * @param quiet true when the player turned level up notifications off: they still get the chat
   *              post and the firework, but no extra sound
   */
  public static void skillMilestone(Player p, String skillTitle, int level, boolean quiet) {
    int milestone = milestoneOf(level);
    Bukkit.broadcastMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "★ " + ChatColor.YELLOW
        + p.getName() + ChatColor.GOLD + " reached " + ChatColor.AQUA + skillTitle + " level "
        + milestone + ChatColor.GOLD + "!");
    if (!quiet) {
      Location at = p.getLocation();
      p.playSound(at, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
      p.playSound(at, Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.9f, 1.0f);
    }
    firework(p, 1, Color.AQUA, Color.WHITE);
    sparkle(p, Particle.END_ROD, Particle.TOTEM_OF_UNDYING, 1);
  }

  /** The global (power) level reached a multiple of 100. */
  public static void globalMilestone(Player p, int globalLevel, boolean quiet) {
    int milestone = milestoneOf(globalLevel);
    Bukkit.broadcastMessage(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "✦ "
        + ChatColor.LIGHT_PURPLE + p.getName() + ChatColor.DARK_PURPLE + " reached "
        + ChatColor.GOLD + "Power Level " + milestone + ChatColor.DARK_PURPLE + "!");
    if (!quiet) {
      Location at = p.getLocation();
      p.playSound(at, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 0.8f);
      p.playSound(at, Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, 1.0f, 1.0f);
      p.playSound(at, Sound.BLOCK_BEACON_POWER_SELECT, 1.0f, 1.2f);
    }
    firework(p, 3, Color.FUCHSIA, Color.YELLOW);
    sparkle(p, Particle.WITCH, Particle.TOTEM_OF_UNDYING, 3);
  }

  /** Shoots {@code count} fireworks (a few ticks apart) that burst right above the player. */
  private static void firework(Player p, int count, Color main, Color fade) {
    for (int i = 0; i < count; i++) {
      Bukkit.getScheduler().runTaskLater(FreeRPG.getPlugin(FreeRPG.class), () -> {
        if (!p.isOnline()) {
          return;
        }
        World world = p.getWorld();
        Location at = p.getLocation().add(0, 5.0, 0); // well above the head, not in the face
        Firework fw = world.spawn(at, Firework.class);
        fw.setMetadata(TAG, new org.bukkit.metadata.FixedMetadataValue(
            FreeRPG.getPlugin(FreeRPG.class), true));
        FireworkMeta meta = fw.getFireworkMeta();
        meta.addEffect(FireworkEffect.builder().with(FireworkEffect.Type.BALL_LARGE)
            .withColor(main, Color.WHITE).withFade(fade).withTrail().withFlicker().build());
        fw.setFireworkMeta(meta);
        fw.detonate();
      }, i * 8L);
    }
  }

  /**
   * A rising double helix of sparks around the player, topped with a burst of totem particles.
   * {@code strength} (1 or 3) makes it taller and denser.
   */
  private static void sparkle(Player p, Particle trail, Particle burst, int strength) {
    int duration = 20 + strength * 10;
    new org.bukkit.scheduler.BukkitRunnable() {
      int tick = 0;

      @Override
      public void run() {
        try {
          step();
        } catch (RuntimeException e) {
          cancel();
        }
      }

      private void step() {
        if (!p.isOnline()) {
          cancel();
          return;
        }
        Location base = p.getLocation();
        World world = p.getWorld();
        if (tick >= duration) {
          world.spawnParticle(burst, base.getX(), base.getY() + 5.0, base.getZ(), 40 * strength,
              0.5, 0.5, 0.5, 0.4);
          world.spawnParticle(Particle.FIREWORK, base.getX(), base.getY() + 4.0, base.getZ(),
              30 * strength, 0.5, 0.8, 0.5, 0.25);
          cancel();
          return;
        }
        double height = tick / (double) duration * 4.5;
        for (int i = 0; i < 2 * strength; i++) {
          double angle = tick * 0.55 + i * Math.PI * 2 / (2 * strength);
          world.spawnParticle(trail, base.getX() + Math.cos(angle) * 1.0, base.getY() + height,
              base.getZ() + Math.sin(angle) * 1.0, 1, 0.0, 0.0, 0.0, 0.0);
        }
        tick++;
      }
    }.runTaskTimer(FreeRPG.getPlugin(FreeRPG.class), 0L, 1L);
  }

  /** The explosion of a celebration firework hurts nobody. */
  public static final class NoFireworkDamage implements Listener {

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent e) {
      Entity damager = e.getDamager();
      if (damager instanceof Firework && damager.hasMetadata(TAG)) {
        e.setCancelled(true);
      }
    }
  }
}
