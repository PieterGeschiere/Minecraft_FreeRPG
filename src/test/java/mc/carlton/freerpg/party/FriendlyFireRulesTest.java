package mc.carlton.freerpg.party;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class FriendlyFireRulesTest {

  private final UUID a = UUID.randomUUID();
  private final UUID b = UUID.randomUUID();

  private Party party(String name, boolean friendlyFire) {
    Party party = new Party(name);
    party.addMember(new PartyMember(a, "A", PartyRole.OWNER, 0));
    party.addMember(new PartyMember(b, "B", PartyRole.MEMBER, 0));
    party.setFriendlyFire(friendlyFire);
    return party;
  }

  @Test
  void blocksSamePartyWhenOff() {
    Party p = party("Dragons", false);
    assertTrue(FriendlyFireRules.shouldCancel(Optional.of(p), Optional.of(p), a, b));
  }

  @Test
  void allowsWhenOn() {
    Party p = party("Dragons", true);
    assertFalse(FriendlyFireRules.shouldCancel(Optional.of(p), Optional.of(p), a, b));
  }

  @Test
  void allowsDifferentPartiesAndNoParty() {
    Party p = party("Dragons", false);
    Party q = party("Wolves", false);
    assertFalse(FriendlyFireRules.shouldCancel(Optional.of(p), Optional.of(q), a, b));
    assertFalse(FriendlyFireRules.shouldCancel(Optional.of(p), Optional.empty(), a, b));
    assertFalse(FriendlyFireRules.shouldCancel(Optional.empty(), Optional.of(p), a, b));
  }

  @Test
  void allowsSelfDamage() {
    Party p = party("Dragons", false);
    assertFalse(FriendlyFireRules.shouldCancel(Optional.of(p), Optional.of(p), a, a));
  }
}
