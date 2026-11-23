package mc.carlton.freerpg.party;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class XpShareMathTest {

  @Test
  void shareIsFlooredPercent() {
    assertEquals(25, XpShareMath.share(100, 25));
    assertEquals(0, XpShareMath.share(3, 25));
    assertEquals(1, XpShareMath.share(7, 25));
    assertEquals(0, XpShareMath.share(100, 0));
    assertEquals(0, XpShareMath.share(-5, 25));
  }

  @Test
  void eligibilityRequiresEveryCondition() {
    // partyOn, memberOn, sameWorld, distSq, radius, online, afk, spectator
    assertTrue(XpShareMath.eligible(true, true, true, 100, 30, true, false, false));
    assertTrue(XpShareMath.eligible(true, true, true, 900, 30, true, false, false)); // 30 blocks
    assertFalse(XpShareMath.eligible(false, true, true, 100, 30, true, false, false));
    assertFalse(XpShareMath.eligible(true, false, true, 100, 30, true, false, false));
    assertFalse(XpShareMath.eligible(true, true, false, 100, 30, true, false, false));
    assertFalse(XpShareMath.eligible(true, true, true, 901, 30, true, false, false));
    assertFalse(XpShareMath.eligible(true, true, true, 100, 30, false, false, false));
    assertFalse(XpShareMath.eligible(true, true, true, 100, 30, true, true, false));
    assertFalse(XpShareMath.eligible(true, true, true, 100, 30, true, false, true));
  }

  @Test
  void sharedXpDoesNotReshare() {
    List<String> granted = new ArrayList<>();
    // A earns and shares with B. While B receives, B's own sharing attempt must do nothing.
    XpShareGuard.share(() -> {
      granted.add("B receives");
      XpShareGuard.share(() -> granted.add("B forwards to C and A"));
      assertTrue(XpShareGuard.isSharing());
    });
    assertEquals(List.of("B receives"), granted);
    assertFalse(XpShareGuard.isSharing());
  }

  @Test
  void guardIsReleasedEvenWhenGrantingThrows() {
    try {
      XpShareGuard.share(() -> {
        throw new IllegalStateException("boom");
      });
    } catch (IllegalStateException expected) {
      // expected
    }
    assertFalse(XpShareGuard.isSharing());
  }
}
