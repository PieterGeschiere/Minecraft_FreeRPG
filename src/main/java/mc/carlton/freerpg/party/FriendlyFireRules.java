package mc.carlton.freerpg.party;

import java.util.Optional;
import java.util.UUID;

/** Decides whether damage between two players is blocked by the party friendly fire setting. */
public final class FriendlyFireRules {

  private FriendlyFireRules() {
  }

  public static boolean shouldCancel(Optional<Party> attackerParty, Optional<Party> victimParty,
      UUID attacker, UUID victim) {
    if (attackerParty.isEmpty() || victimParty.isEmpty() || attacker.equals(victim)) {
      return false;
    }
    Party party = attackerParty.get();
    return party == victimParty.get() && !party.friendlyFire();
  }
}
