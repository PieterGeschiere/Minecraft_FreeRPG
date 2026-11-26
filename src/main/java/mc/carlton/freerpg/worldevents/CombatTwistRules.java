package mc.carlton.freerpg.worldevents;

import java.util.Random;

/** The pure rules of the combat and loot twists of Mutator Night. */
public final class CombatTwistRules {

  private CombatTwistRules() {
  }

  /**
   * The damage factor of Glass Cannon: the multiplier when a player and a mob fight (either way
   * round), and 1.0 for everything else: player against player, mobs among themselves, and damage
   * that comes from neither (lightning, falling blocks). The multiplier never makes damage smaller.
   */
  static double glassCannonFactor(boolean attackerIsPlayer, boolean attackerIsMob,
      boolean victimIsPlayer, boolean victimIsMob, double multiplier) {
    boolean playerHitsMob = attackerIsPlayer && victimIsMob;
    boolean mobHitsPlayer = attackerIsMob && victimIsPlayer;
    return playerHitsMob || mobHitsPlayer ? Math.max(1.0, multiplier) : 1.0;
  }

  /** Exploding Loot only works for hostile mobs killed by a player, not spawner mobs or twins. */
  static boolean explodingLootApplies(boolean killedByPlayer, boolean isHostile,
      boolean fromSpawner, boolean isTwin) {
    return killedByPlayer && isHostile && !fromSpawner && !isTwin;
  }

  static boolean shouldExplode(double chance, Random random) {
    return EventRules.rollStart(chance, random);
  }
}
