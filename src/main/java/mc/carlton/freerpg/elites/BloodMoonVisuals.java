package mc.carlton.freerpg.elites;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.WorldBorder;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

/**
 * What players see and hear during a Blood Moon: a big title, a sound, a red boss bar, red
 * particles and a red screen edge (the warning of a per-player world border). Every ten ticks
 * {@link #refresh()} brings every player to the right state, so joining, changing world and dying
 * need no special handling.
 */
public final class BloodMoonVisuals {

  /**
   * The red edge border is far away: its edge is more than any render distance (32 chunks = 512
   * blocks) from the player, so no wall is drawn and none can be reached. The warning distance is
   * larger still, which is what makes the screen edge turn red.
   */
  static final double EDGE_HALF_SIZE = 600.0;
  static final int EDGE_WARNING_DISTANCE = 1500;

  /** How red the screen edge gets: 0 (not at all) to 1 (fully). */
  static double edgeIntensity() {
    return Math.max(0.0, 1.0 - EDGE_HALF_SIZE / EDGE_WARNING_DISTANCE);
  }

  private static final Random RANDOM = new Random();
  private static final Set<UUID> SHOWN = new HashSet<>();
  private static final Map<UUID, BossBar> BARS = new HashMap<>();
  private static final Map<UUID, WorldBorder> BORDERS = new HashMap<>();

  private BloodMoonVisuals() {
  }

  /** "8:20" for 10000 ticks. */
  static String formatTime(long ticksLeft) {
    long seconds = Math.max(0, ticksLeft) / 20;
    return (seconds / 60) + ":" + String.format("%02d", seconds % 60);
  }

  public static void refresh() {
    Set<UUID> keep = new HashSet<>();
    for (Player player : Bukkit.getOnlinePlayers()) {
      if (!BloodMoonManager.isActive(player.getWorld())) {
        continue;
      }
      keep.add(player.getUniqueId());
      if (SHOWN.add(player.getUniqueId())) {
        announce(player);
      }
      updateBar(player);
      if (EliteConfig.bloodMoonEffect("redEdge", true)) {
        updateEdge(player);
      }
      if (EliteConfig.bloodMoonEffect("particles", true)) {
        spawnParticles(player);
      }
    }
    for (UUID id : new ArrayList<>(SHOWN)) {
      if (!keep.contains(id)) {
        SHOWN.remove(id);
        clear(id);
      }
    }
  }

  private static void announce(Player player) {
    if (EliteConfig.bloodMoonEffect("title", true)) {
      player.sendTitle(EliteMessages.text(player, "bloodMoonTitle"),
          EliteMessages.text(player, "bloodMoonSubtitle"), 10, 80, 30);
    }
    if (EliteConfig.bloodMoonEffect("sound", true)) {
      player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.6f, 0.6f);
    }
  }

  private static void updateBar(Player player) {
    if (!EliteConfig.bloodMoonEffect("bossBar", true)) {
      return;
    }
    BossBar bar = BARS.computeIfAbsent(player.getUniqueId(),
        id -> Bukkit.createBossBar("", BarColor.RED, BarStyle.SOLID));
    bar.setTitle(EliteMessages.text(player, "bloodMoonBossBar", "time",
        formatTime(BloodMoonManager.ticksLeft(player.getWorld()))));
    bar.setProgress(BloodMoonManager.progress(player.getWorld()));
    if (!bar.getPlayers().contains(player)) {
      bar.addPlayer(player);
    }
  }

  /**
   * The screen edge turns red when the player is close to the border. The border is only shown to
   * this player and follows them, so it never blocks anyone and never hurts.
   */
  private static void updateEdge(Player player) {
    WorldBorder border = BORDERS.computeIfAbsent(player.getUniqueId(), id -> {
      WorldBorder created = Bukkit.createWorldBorder();
      created.setSize(EDGE_HALF_SIZE * 2);
      created.setWarningDistance(EDGE_WARNING_DISTANCE);
      created.setDamageAmount(0.0);
      created.setDamageBuffer(1000.0);
      return created;
    });
    Location at = player.getLocation();
    border.setCenter(at.getX(), at.getZ());
    player.setWorldBorder(border);
  }

  private static void spawnParticles(Player player) {
    Location at = player.getLocation();
    Particle.DustOptions red = new Particle.DustOptions(Color.fromRGB(150, 0, 0), 1.6f);
    for (int i = 0; i < 4; i++) {
      Location spot = at.clone().add((RANDOM.nextDouble() - 0.5) * 12, RANDOM.nextDouble() * 4,
          (RANDOM.nextDouble() - 0.5) * 12);
      player.spawnParticle(Particle.DUST, spot, 1, 0, 0, 0, 0, red);
    }
  }

  private static void clear(UUID id) {
    BossBar bar = BARS.remove(id);
    if (bar != null) {
      bar.removeAll();
    }
    WorldBorder border = BORDERS.remove(id);
    Player player = Bukkit.getPlayer(id);
    if (player != null && border != null) {
      player.setWorldBorder(null);
    }
  }

  /** Removes every effect from every player (shutdown and reload). */
  public static void clearAll() {
    for (UUID id : new ArrayList<>(SHOWN)) {
      clear(id);
    }
    SHOWN.clear();
    for (UUID id : new ArrayList<>(BARS.keySet())) {
      clear(id);
    }
    BORDERS.clear();
  }
}
