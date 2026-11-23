package mc.carlton.freerpg.party;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class InviteManagerTest {

  private final UUID from = UUID.randomUUID();
  private final UUID to = UUID.randomUUID();

  @Test
  void inviteExpiresAfterExpiry() {
    InviteManager invites = new InviteManager(60_000);
    invites.invite(from, to, "Dragons", 1_000);
    assertTrue(invites.pending(to, "Dragons", 60_999).isPresent());
    assertFalse(invites.pending(to, "Dragons", 61_001).isPresent());
  }

  @Test
  void acceptWithoutPartyTakesNewest() {
    InviteManager invites = new InviteManager(60_000);
    invites.invite(from, to, "Dragons", 1_000);
    invites.invite(from, to, "Wolves", 2_000);
    assertEquals("Wolves", invites.pending(to, null, 3_000).get().party());
    assertEquals("Dragons", invites.pending(to, "dragons", 3_000).get().party());
  }

  @Test
  void consumedInviteCannotBeReused() {
    InviteManager invites = new InviteManager(60_000);
    invites.invite(from, to, "Dragons", 1_000);
    assertTrue(invites.consume(to, "Dragons"));
    assertFalse(invites.consume(to, "Dragons"));
    assertFalse(invites.pending(to, "Dragons", 2_000).isPresent());
  }

  @Test
  void denyRemovesTheInvite() {
    InviteManager invites = new InviteManager(60_000);
    invites.invite(from, to, "Dragons", 1_000);
    assertTrue(invites.deny(to, null, 2_000));
    assertFalse(invites.pending(to, null, 2_000).isPresent());
  }

  @Test
  void changingTheExpiryKeepsPendingInvites() {
    InviteManager invites = new InviteManager(60_000);
    invites.invite(from, to, "Dragons", 1_000);
    invites.setExpiryMillis(10_000);
    assertTrue(invites.pending(to, "Dragons", 2_000).isPresent());
    invites.invite(from, to, "Wolves", 2_000);
    assertFalse(invites.pending(to, "Wolves", 12_001).isPresent());
  }
}
