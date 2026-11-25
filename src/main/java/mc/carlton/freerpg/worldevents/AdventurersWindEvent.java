package mc.carlton.freerpg.worldevents;

import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Adventurer's Wind: agility and aeronautics give more exp, everyone in the world is a little
 * faster, and gliding players get a small push (up to a speed limit).
 */
public final class AdventurersWindEvent extends SkillBoostEvent {

  /** The speed effect is renewed every beat and lasts this many ticks. */
  private static final int EFFECT_TICKS = 40;

  public AdventurersWindEvent() {
    super(WorldEventType.ADVENTURERS_WIND);
  }

  /** The push (blocks per tick) a glider gets now: none once the speed cap is reached. */
  static double glideNudge(double currentSpeed, double boost, double cap) {
    return boost <= 0.0 || currentSpeed >= cap ? 0.0 : boost;
  }

  /**
   * Whether our speed effect should be (re)applied: when the player has none, or only our own short
   * one. A stronger effect or a longer one of the same strength is never replaced.
   */
  static boolean shouldRefreshSpeed(boolean hasSpeed, int existingAmplifier, int existingTicks,
      int ourAmplifier) {
    return !hasSpeed || (existingAmplifier <= ourAmplifier && existingTicks <= EFFECT_TICKS);
  }

  /** The level of the speed effect, 0 = Speed I. */
  static int speedAmplifier() {
    return Math.max(0, Math.min(4,
        WorldEventConfig.integer(WorldEventType.ADVENTURERS_WIND, "speedAmplifier", 0)));
  }

  /** The push per beat for a gliding player, in blocks per tick. */
  static double glideBoost() {
    return Math.max(0.0, Math.min(1.0,
        WorldEventConfig.number(WorldEventType.ADVENTURERS_WIND, "glideBoost", 0.12)));
  }

  /** The glide speed (blocks per tick) above which the push stops. */
  static double maxGlideSpeed() {
    return Math.max(0.1,
        WorldEventConfig.number(WorldEventType.ADVENTURERS_WIND, "maxGlideSpeed", 1.6));
  }

  @Override
  public Sound announceSound() {
    return Sound.ITEM_ELYTRA_FLYING;
  }

  @Override
  public void decorate(Player player, ActiveEvent event) {
    if (player.getGameMode() == GameMode.SPECTATOR) {
      return;
    }
    PotionEffect existing = player.getPotionEffect(PotionEffectType.SPEED);
    int amplifier = speedAmplifier();
    if (shouldRefreshSpeed(existing != null, existing == null ? 0 : existing.getAmplifier(),
        existing == null ? 0 : existing.getDuration(), amplifier)) {
      player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, EFFECT_TICKS, amplifier,
          true, false, false));
    }
    if (player.isGliding()) {
      double nudge = glideNudge(player.getVelocity().length(), glideBoost(), maxGlideSpeed());
      if (nudge > 0.0) {
        player.setVelocity(player.getVelocity()
            .add(player.getLocation().getDirection().multiply(nudge)));
      }
    }
  }

  @Override
  public void clearPlayer(UUID id) {
    Player player = Bukkit.getPlayer(id);
    if (player == null) {
      return;
    }
    PotionEffect effect = player.getPotionEffect(PotionEffectType.SPEED);
    if (effect != null && effect.getDuration() <= EFFECT_TICKS
        && effect.getAmplifier() == speedAmplifier()) {
      player.removePotionEffect(PotionEffectType.SPEED);
    }
  }
}
