package mc.carlton.freerpg.elites;

import java.util.List;
import java.util.Locale;
import java.util.Random;
import org.bukkit.Chunk;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

/** Turns a mob into an elite: the stats, name and affixes. */
public final class EliteManager {

  private static final Random RANDOM = new Random();

  private EliteManager() {
  }

  /**
   * The spawn pipeline: world and chunk limits, the dice, the affixes. Returns true when the mob
   * became an elite.
   */
  public static boolean tryMakeElite(LivingEntity mob, boolean bloodMoon) {
    if (!EliteConfig.enabled() || EliteConfig.disabledWorlds().contains(mob.getWorld().getName())) {
      return false;
    }
    if (!EliteRoller.rollElite(EliteConfig.chance(), bloodMoon, EliteConfig.bloodMoonMultiplier(),
        RANDOM)) {
      return false;
    }
    if (eliteCountInChunk(mob.getLocation().getChunk()) >= EliteConfig.chunkCap()) {
      return false;
    }
    List<String> available = AffixRegistry.idsFor(mob.getType());
    List<String> chosen = EliteRoller.pickAffixes(
        EliteRoller.rollAffixCount(EliteConfig.affixCountWeights(), RANDOM), available, RANDOM);
    if (chosen.isEmpty()) {
      return false;
    }
    makeElite(mob, chosen);
    return true;
  }

  /** Makes the mob an elite with exactly these affixes (also used by the admin command). */
  public static void makeElite(LivingEntity mob, List<String> affixIds) {
    EliteData.mark(mob, affixIds);
    scale(mob, Attribute.MAX_HEALTH, EliteConfig.healthMultiplier());
    scale(mob, Attribute.ATTACK_DAMAGE, EliteConfig.damageMultiplier());
    scale(mob, Attribute.SCALE, EliteConfig.sizeScale());
    AttributeInstance health = mob.getAttribute(Attribute.MAX_HEALTH);
    if (health != null) {
      mob.setHealth(health.getValue());
    }
    mob.setCustomName(ChatColor.RED + "Elite " + displayName(mob.getType()));
    mob.setCustomNameVisible(true);
    for (String id : affixIds) {
      Affix affix = AffixRegistry.get(id);
      if (affix != null) {
        affix.onApply(mob);
      }
    }
    EliteTracker.track(mob);
    playSpawnEffects(mob.getLocation());
  }

  private static void scale(LivingEntity mob, Attribute attribute, double factor) {
    AttributeInstance instance = mob.getAttribute(attribute);
    if (instance != null) {
      instance.setBaseValue(instance.getBaseValue() * factor);
    }
  }

  private static void playSpawnEffects(Location location) {
    if (location.getWorld() == null) {
      return;
    }
    location.getWorld().playSound(location, Sound.ENTITY_WITHER_AMBIENT, 0.6f, 1.4f);
    location.getWorld().spawnParticle(Particle.FLAME, location.clone().add(0, 1, 0), 25, 0.4, 0.6,
        0.4, 0.02);
  }

  /** "ZOMBIE_VILLAGER" becomes "Zombie Villager". */
  static String displayName(EntityType type) {
    StringBuilder name = new StringBuilder();
    for (String word : type.name().toLowerCase(Locale.ROOT).split("_")) {
      if (name.length() > 0) {
        name.append(' ');
      }
      name.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
    }
    return name.toString();
  }

  /** How many elites (not minions) are in this chunk right now. */
  public static int eliteCountInChunk(Chunk chunk) {
    int count = 0;
    for (Entity entity : chunk.getEntities()) {
      if (entity instanceof LivingEntity && EliteData.isElite(entity)) {
        count++;
      }
    }
    return count;
  }

}
