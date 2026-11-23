package mc.carlton.freerpg.party;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class TeleportRequestManagerTest {

  private final UUID a = UUID.randomUUID();
  private final UUID b = UUID.randomUUID();
  private final UUID c = UUID.randomUUID();

  @Test
  void newRequestReplacesOldOne() {
    TeleportRequestManager manager = new TeleportRequestManager(60_000, 30_000);
    manager.request(a, c, 1_000);
    manager.request(b, c, 2_000);
    TeleportRequestManager.Request accepted = manager.accept(c, 3_000).get();
    assertEquals(b, accepted.from());
    assertFalse(manager.accept(c, 3_000).isPresent());
  }

  @Test
  void requestExpiresAfter60s() {
    TeleportRequestManager manager = new TeleportRequestManager(60_000, 30_000);
    manager.request(a, b, 1_000);
    assertTrue(manager.hasPending(b, 61_000));
    assertFalse(manager.hasPending(b, 61_001));
    assertFalse(manager.accept(b, 61_001).isPresent());
  }

  @Test
  void denyRemovesRequestAndTellsWhoSentIt() {
    TeleportRequestManager manager = new TeleportRequestManager(60_000, 30_000);
    manager.request(a, b, 1_000);
    assertEquals(a, manager.deny(b, 2_000).get().from());
    assertFalse(manager.deny(b, 2_000).isPresent());
  }

  @Test
  void cooldownBlocksUntilElapsed() {
    TeleportRequestManager manager = new TeleportRequestManager(60_000, 30_000);
    assertEquals(0, manager.cooldownRemaining(a, 1_000));
    manager.markUsed(a, 1_000);
    assertEquals(30_000, manager.cooldownRemaining(a, 1_000));
    assertEquals(10_000, manager.cooldownRemaining(a, 21_000));
    assertEquals(0, manager.cooldownRemaining(a, 31_000));
    assertEquals(0, manager.cooldownRemaining(a, 99_000));
  }

  @Test
  void cooldownIsSharedBetweenTpaAndSpawn() {
    TeleportRequestManager manager = new TeleportRequestManager(60_000, 30_000);
    // both features finish their teleport with markUsed for the same player
    manager.markUsed(a, 5_000);
    assertTrue(manager.cooldownRemaining(a, 6_000) > 0);
  }
}
