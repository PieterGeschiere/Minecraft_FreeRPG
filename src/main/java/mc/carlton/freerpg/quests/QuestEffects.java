package mc.carlton.freerpg.quests;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

/**
 * The sounds and particles of the quest system: progress ticks, finished quests, a whole day
 * done, prizes and opening the quest menu. All of it follows {@link QuestConfig#effectsEnabled()}.
 */
public final class QuestEffects {

  private QuestEffects() {
  }

  /** A quest moved forward: a tick that gets higher the closer the quest is to done. */
  public static void progress(Player p, int progress, int target) {
    if (!QuestConfig.effectsEnabled()) {
      return;
    }
    double fraction = target <= 0 ? 0 : Math.min(1.0, progress / (double) target);
    p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.25f,
        (float) (0.8 + 1.0 * fraction));
  }

  /** One quest is done: a ring of sparks and a chime on top of the level up sound. */
  public static void questDone(Player p) {
    if (!QuestConfig.effectsEnabled()) {
      return;
    }
    Location at = p.getLocation().add(0, 1.0, 0);
    p.playSound(p.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.9f, 1.4f);
    p.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, at, 14, 0.4, 0.6, 0.4, 0);
    p.getWorld().spawnParticle(Particle.END_ROD, at, 8, 0.3, 0.6, 0.3, 0.05);
  }

  /** Every quest of a period is done: the bigger the period, the bigger the show. */
  public static void allDone(Player p, QuestPeriod period) {
    if (!QuestConfig.effectsEnabled()) {
      return;
    }
    Location at = p.getLocation().add(0, 1.0, 0);
    int scale = period == QuestPeriod.DAILY ? 1 : period == QuestPeriod.WEEKLY ? 2 : 3;
    p.playSound(p.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 0.8f, 1.0f);
    p.playSound(p.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, 0.8f, 1.0f);
    p.getWorld().spawnParticle(Particle.FIREWORK, at, 20 * scale, 0.5, 0.8, 0.5, 0.15);
    p.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, at, 15 * scale, 0.5, 0.8, 0.5, 0.3);
  }

  /** A prize was handed over. */
  public static void prize(Player p, WheelPrize prize) {
    if (!QuestConfig.effectsEnabled()) {
      return;
    }
    Location at = p.getLocation();
    switch (prize.kind()) {
      case BOOK -> {
        p.playSound(at, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 0.8f, 1.2f);
        p.playSound(at, Sound.ITEM_BOOK_PAGE_TURN, 0.8f, 1.0f);
        p.getWorld().spawnParticle(Particle.ENCHANT, at.clone().add(0, 1.5, 0), 30, 0.4, 0.5, 0.4,
            0.8);
      }
      case MINECRAFT_EXP -> {
        p.playSound(at, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.7f, 1.0f);
        p.playSound(at, Sound.ENTITY_EXPERIENCE_BOTTLE_THROW, 0.3f, 1.4f);
      }
      case FRPG_EXP -> {
        p.playSound(at, Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.4f);
        p.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, at.clone().add(0, 1.0, 0), 12, 0.4,
            0.6, 0.4, 0);
      }
      default -> {
      }
    }
  }
}
