package mc.carlton.freerpg.elites;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;

/**
 * What players see during a Blood Moon besides the shared title, sound and boss bar of the world
 * events: red particles and a red screen edge (the warning of a per-player world border). The
 * world event layer calls these every beat for every player in the Blood Moon's world.
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
  private static final Map<UUID, WorldBorder> BORDERS = new HashMap<>();

  private BloodMoonVisuals() {
  }

  /**
   * The screen edge turns red when the player is close to the border. The border is only shown to
   * this player and follows them, so it never blocks anyone and never hurts.
   */
  static void updateEdge(Player player) {
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

  static void spawnParticles(Player player) {
    Location at = player.getLocation();
    Particle.DustOptions red = new Particle.DustOptions(Color.fromRGB(150, 0, 0), 1.6f);
    for (int i = 0; i < 4; i++) {
      Location spot = at.clone().add((RANDOM.nextDouble() - 0.5) * 12, RANDOM.nextDouble() * 4,
          (RANDOM.nextDouble() - 0.5) * 12);
      player.spawnParticle(Particle.DUST, spot, 1, 0, 0, 0, 0, red);
    }
  }

  /** Removes the red edge from one player. */
  static void clear(UUID id) {
    WorldBorder border = BORDERS.remove(id);
    Player player = Bukkit.getPlayer(id);
    if (player != null && border != null) {
      player.setWorldBorder(null);
    }
  }

  /** Removes the red edge from every player (end, shutdown and reload). */
  public static void clearAll() {
    for (UUID id : new ArrayList<>(BORDERS.keySet())) {
      clear(id);
    }
  }
}
