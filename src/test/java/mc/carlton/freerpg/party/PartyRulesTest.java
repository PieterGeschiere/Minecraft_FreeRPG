package mc.carlton.freerpg.party;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PartyRulesTest {

  @Test
  void nameLengthAndCharacters() {
    assertTrue(PartyRules.isValidName("Dragons"));
    assertTrue(PartyRules.isValidName("a_b"));
    assertTrue(PartyRules.isValidName("1234567890123456"));
    assertFalse(PartyRules.isValidName("ab"));
    assertFalse(PartyRules.isValidName("12345678901234567"));
    assertFalse(PartyRules.isValidName("has space"));
    assertFalse(PartyRules.isValidName("bad-name"));
    assertFalse(PartyRules.isValidName(null));
  }

  @Test
  void rolePermissionTable() {
    for (PartyAction action : PartyAction.values()) {
      assertTrue(PartyRules.can(action, PartyRole.OWNER), "owner " + action);
      assertFalse(PartyRules.can(action, PartyRole.MEMBER), "member " + action);
    }
    PartyAction[] officerAllowed = {PartyAction.TOGGLE_OPEN, PartyAction.TOGGLE_XP_SHARE,
        PartyAction.TOGGLE_FRIENDLY_FIRE, PartyAction.SET_SPAWN, PartyAction.INVITE,
        PartyAction.KICK_MEMBER};
    int allowed = 0;
    for (PartyAction action : PartyAction.values()) {
      if (PartyRules.can(action, PartyRole.OFFICER)) {
        allowed++;
      }
    }
    assertEquals(officerAllowed.length, allowed);
    for (PartyAction action : officerAllowed) {
      assertTrue(PartyRules.can(action, PartyRole.OFFICER), "officer " + action);
    }
    assertFalse(PartyRules.can(PartyAction.KICK_OFFICER, PartyRole.OFFICER));
    assertFalse(PartyRules.can(PartyAction.DISBAND, PartyRole.OFFICER));
  }
}
