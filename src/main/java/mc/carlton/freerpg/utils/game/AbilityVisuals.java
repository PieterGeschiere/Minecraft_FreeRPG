package mc.carlton.freerpg.utils.game;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

/**
 * Particles of the abilities of the main skills: a burst when the ability starts, a light aura
 * while it lasts that fits the skill, and a puff when it ends. World particles are sent to every
 * player nearby, so everyone can see them.
 */
public final class AbilityVisuals {

  /** The main skills that have an ability with effects (the same names as in AbilitySounds). */
  public static final java.util.List<String> SKILLS =
      mc.carlton.freerpg.skills.SkillRegistry.mainLabels();

  private AbilityVisuals() {
  }

  private static Color color(String skill) {
    switch (skill) {
      case "digging":
        return Color.fromRGB(140, 95, 55);
      case "woodcutting":
        return Color.fromRGB(110, 170, 60);
      case "mining":
        return Color.fromRGB(255, 150, 40);
      case "farming":
        return Color.fromRGB(90, 220, 90);
      case "fishing":
        return Color.fromRGB(60, 150, 255);
      case "beastMastery":
        return Color.fromRGB(200, 150, 90);
      case "swordsmanship":
        return Color.fromRGB(210, 230, 255);
      case "defense":
        return Color.fromRGB(140, 140, 150);
      case "axeMastery":
        return Color.fromRGB(220, 40, 40);
      default:
        return Color.WHITE;
    }
  }

  private static BlockData block(Material material) {
    return material.createBlockData();
  }

  /** The burst when an ability starts: a ring on the ground and particles of the skill. */
  public static void burst(Player p, String skill) {
    if (!EffectSettings.skillAbilityParticles()) {
      return;
    }
    World world = p.getWorld();
    Location feet = p.getLocation().add(0, 0.15, 0);
    Location chest = p.getLocation().add(0, 1.0, 0);
    Particle.DustOptions dust = new Particle.DustOptions(color(skill), 1.4f);
    for (int i = 0; i < 32; i++) {
      double a = i * Math.PI * 2 / 32;
      world.spawnParticle(Particle.DUST, feet.getX() + Math.cos(a) * 1.8, feet.getY(),
          feet.getZ() + Math.sin(a) * 1.8, 1, 0, 0, 0, 0, dust);
    }
    switch (skill) {
      case "digging":
        world.spawnParticle(Particle.BLOCK, feet, 40, 0.8, 0.2, 0.8, 0.1, block(Material.DIRT));
        break;
      case "woodcutting":
        world.spawnParticle(Particle.BLOCK, chest, 30, 0.6, 0.8, 0.6, 0.1, block(Material.OAK_LOG));
        world.spawnParticle(Particle.BLOCK, chest.clone().add(0, 1, 0), 25, 0.7, 0.4, 0.7, 0.05,
            block(Material.OAK_LEAVES));
        break;
      case "mining":
        world.spawnParticle(Particle.BLOCK, feet, 30, 0.8, 0.2, 0.8, 0.1, block(Material.STONE));
        world.spawnParticle(Particle.FLAME, chest, 25, 0.5, 0.7, 0.5, 0.05);
        world.spawnParticle(Particle.CRIT, chest, 25, 0.5, 0.7, 0.5, 0.3);
        break;
      case "farming":
        world.spawnParticle(Particle.HAPPY_VILLAGER, chest, 30, 0.7, 0.8, 0.7, 0);
        world.spawnParticle(Particle.HEART, chest.clone().add(0, 0.8, 0), 5, 0.4, 0.3, 0.4, 0);
        break;
      case "fishing":
        world.spawnParticle(Particle.SPLASH, chest, 50, 0.7, 0.6, 0.7, 0.2);
        world.spawnParticle(Particle.BUBBLE_POP, chest, 20, 0.6, 0.8, 0.6, 0.05);
        break;
      case "beastMastery":
        world.spawnParticle(Particle.CLOUD, feet, 25, 0.7, 0.1, 0.7, 0.08);
        world.spawnParticle(Particle.BLOCK, feet, 20, 0.6, 0.1, 0.6, 0.1, block(Material.DIRT));
        break;
      case "swordsmanship":
        world.spawnParticle(Particle.SWEEP_ATTACK, chest, 4, 0.7, 0.4, 0.7, 0);
        world.spawnParticle(Particle.CRIT, chest, 30, 0.6, 0.7, 0.6, 0.4);
        break;
      case "defense":
        world.spawnParticle(Particle.BLOCK, chest, 40, 0.6, 0.8, 0.6, 0.05,
            block(Material.STONE_BRICKS));
        world.spawnParticle(Particle.ENCHANTED_HIT, chest, 20, 0.6, 0.8, 0.6, 0.2);
        break;
      case "axeMastery":
        world.spawnParticle(Particle.DAMAGE_INDICATOR, chest, 15, 0.6, 0.7, 0.6, 0.1);
        world.spawnParticle(Particle.CRIT, chest, 30, 0.6, 0.7, 0.6, 0.4);
        world.spawnParticle(Particle.EXPLOSION, chest, 1);
        break;
      default:
        break;
    }
  }

  /** The light effect that repeats while an ability is active (called every 2 ticks). */
  public static void aura(Player p, String skill, int tick) {
    if (!EffectSettings.skillAbilityParticles()) {
      return;
    }
    World world = p.getWorld();
    Location base = p.getLocation();
    Location chest = base.clone().add(0, 1.0, 0);
    Particle.DustOptions dust = new Particle.DustOptions(color(skill), 1.1f);
    double a = tick * 0.3;
    for (int i = 0; i < 2; i++) {
      double angle = a + i * Math.PI;
      world.spawnParticle(Particle.DUST,
          base.clone().add(Math.cos(angle) * 0.8, 0.9 + Math.sin(tick * 0.15 + i) * 0.5,
              Math.sin(angle) * 0.8), 1, 0, 0, 0, 0, dust);
    }
    if (tick % 3 != 0) {
      return;
    }
    switch (skill) {
      case "digging":
        world.spawnParticle(Particle.BLOCK, base.clone().add(0, 0.1, 0), 3, 0.4, 0.05, 0.4, 0.02,
            block(Material.DIRT));
        break;
      case "woodcutting":
        world.spawnParticle(Particle.BLOCK, chest.clone().add(0, 0.8, 0), 2, 0.4, 0.3, 0.4, 0,
            block(Material.OAK_LEAVES));
        break;
      case "mining":
        world.spawnParticle(Particle.FLAME, chest, 1, 0.3, 0.6, 0.3, 0.01);
        world.spawnParticle(Particle.CRIT, chest, 1, 0.4, 0.6, 0.4, 0.05);
        break;
      case "farming":
        world.spawnParticle(Particle.HAPPY_VILLAGER, chest, 2, 0.4, 0.7, 0.4, 0);
        break;
      case "fishing":
        world.spawnParticle(Particle.BUBBLE_POP, chest, 2, 0.4, 0.7, 0.4, 0.01);
        break;
      case "beastMastery":
        world.spawnParticle(Particle.CLOUD, base.clone().add(0, 0.1, 0), 1, 0.3, 0.02, 0.3, 0.01);
        break;
      case "swordsmanship":
        world.spawnParticle(Particle.CRIT, chest, 2, 0.5, 0.6, 0.5, 0.05);
        break;
      case "defense":
        world.spawnParticle(Particle.ENCHANTED_HIT, chest, 2, 0.4, 0.7, 0.4, 0.02);
        break;
      case "axeMastery":
        world.spawnParticle(Particle.CRIT, chest, 1, 0.4, 0.6, 0.4, 0.05);
        world.spawnParticle(Particle.SMALL_FLAME, chest, 1, 0.3, 0.6, 0.3, 0.01);
        break;
      default:
        break;
    }
  }

  /** The puff when an ability ends. */
  public static void end(Player p, String skill) {
    if (!EffectSettings.skillAbilityParticles()) {
      return;
    }
    p.getWorld().spawnParticle(Particle.CLOUD, p.getLocation().add(0, 1, 0), 12, 0.4, 0.5, 0.4,
        0.02);
  }
}
