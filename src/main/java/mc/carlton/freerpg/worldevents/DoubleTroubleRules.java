package mc.carlton.freerpg.worldevents;

import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

/** The pure rules of the mob twists of Mutator Night. */
public final class DoubleTroubleRules {

  private DoubleTroubleRules() {
  }

  /**
   * Whether a spawn brings a twin: only natural spawns of monsters, never a twin itself, and not
   * in a chunk that already holds {@code cap} hostile mobs (a cap of 0 switches twins off).
   */
  static boolean shouldTwin(SpawnReason reason, boolean isMonster, boolean isTwin,
      int hostilesInChunk, int cap) {
    return reason == SpawnReason.NATURAL && isMonster && !isTwin && hostilesInChunk < cap;
  }

  /** A twin drops nothing and gives no exp, so twins cannot be farmed. */
  static boolean twinHasNoLoot(boolean isTwin) {
    return isTwin;
  }

  /** The speed multiplier of Swift Mobs; a negative bonus never slows mobs down. */
  static double speedFactor(double bonus) {
    return 1.0 + Math.max(0.0, bonus);
  }
}
