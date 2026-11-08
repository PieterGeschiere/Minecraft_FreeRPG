package mc.carlton.freerpg.weaponskills;

import java.util.ArrayList;
import java.util.List;
import mc.carlton.freerpg.FreeRPG;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * The particle trail of the arrows of Arrow Storm and the bolts of Blast Bolt. Every perk the
 * player has adds a layer with its own color: flames for Flaming Volley, a glow for Marked Prey,
 * ice for Pinning Shot, and so on.
 */
final class ArrowTrail {

  /** One kind of particle in a trail. */
  static final class Layer {

    final Particle particle;
    final Color color; // only for DUST, null otherwise
    final float size;

    Layer(Particle particle, Color color, float size) {
      this.particle = particle;
      this.color = color;
      this.size = size;
    }
  }

  private static final int MAX_TICKS = 20 * 10;

  private ArrowTrail() {
  }

  /**
   * The layers of the trail for a player's perks.
   *
   * @param volley true for the extra arrows of Arrow Storm (only they carry fire)
   */
  static List<Layer> layers(WeaponSkill skill, WeaponSkillData data, boolean volley) {
    List<Layer> layers = new ArrayList<>();
    if (skill == WeaponSkill.BOW) {
      layers.add(new Layer(Particle.DUST, Color.fromRGB(255, 215, 64), 1.1f));
      layers.add(new Layer(Particle.CRIT, null, 1f));
      if (volley && data.getSkill(BowMastery.FLAMING_VOLLEY) > 0) {
        layers.add(new Layer(Particle.FLAME, null, 1f));
      }
      if (data.getSkill(BowMastery.MARKED_PREY) > 0) {
        layers.add(new Layer(Particle.END_ROD, null, 1f));
      }
      if (data.getSkill(BowMastery.PINNING_SHOT) > 0) {
        layers.add(new Layer(Particle.DUST, Color.fromRGB(120, 200, 255), 1f));
        layers.add(new Layer(Particle.SNOWFLAKE, null, 1f));
      }
      if (data.getSkill(BowMastery.FLEET_FOOT) > 0) {
        layers.add(new Layer(Particle.CLOUD, null, 1f));
      }
      if (data.getSkill(BowMastery.HAWKEYES_WRATH) > 0) {
        layers.add(new Layer(Particle.DUST, Color.fromRGB(255, 255, 255), 1.6f));
      }
    } else if (skill == WeaponSkill.CROSSBOW) {
      layers.add(new Layer(Particle.DUST, Color.fromRGB(255, 70, 40), 1.3f));
      if (data.getSkill(CrossbowMastery.CONCUSSION) > 0) {
        layers.add(new Layer(Particle.DUST, Color.fromRGB(80, 140, 255), 1.1f));
      }
      if (data.getSkill(CrossbowMastery.REND) > 0) {
        layers.add(new Layer(Particle.DUST, Color.fromRGB(150, 60, 200), 1.1f));
      }
      if (data.getSkill(CrossbowMastery.SHRAPNEL) > 0) {
        layers.add(new Layer(Particle.CRIT, null, 1f));
      }
      if (data.getSkill(CrossbowMastery.HEAVY_CHARGE) > 0) {
        layers.add(new Layer(Particle.LARGE_SMOKE, null, 1f));
      }
      if (data.getSkill(CrossbowMastery.SIEGE_WRATH) > 0) {
        layers.add(new Layer(Particle.FLAME, null, 1f));
      }
    }
    return layers;
  }

  /** Follows the arrow until it lands, disappears or after ten seconds. */
  static void start(AbstractArrow arrow, WeaponSkill skill, WeaponSkillData data,
      boolean volley) {
    if (!AbilityEffects.on("arrowTrails")) {
      return;
    }
    List<Layer> layers = layers(skill, data, volley);
    new BukkitRunnable() {
      private int ticks = 0;

      @Override
      public void run() {
        if (!arrow.isValid() || arrow.isInBlock() || ticks++ > MAX_TICKS) {
          cancel();
          return;
        }
        Location at = arrow.getLocation();
        for (Layer layer : layers) {
          if (layer.color != null) {
            at.getWorld().spawnParticle(Particle.DUST, at, 1, 0.02, 0.02, 0.02, 0,
                new Particle.DustOptions(layer.color, layer.size));
          } else {
            at.getWorld().spawnParticle(layer.particle, at, 1, 0.03, 0.03, 0.03, 0.005);
          }
        }
      }
    }.runTaskTimer(FreeRPG.getPlugin(FreeRPG.class), 0L, 1L);
  }
}
