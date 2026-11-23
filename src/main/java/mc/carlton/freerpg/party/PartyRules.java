package mc.carlton.freerpg.party;

import java.util.regex.Pattern;

/** Pure rules of parties: name validation and the role permission table. */
public final class PartyRules {

  private static final Pattern NAME = Pattern.compile("[A-Za-z0-9_]{3,16}");

  private PartyRules() {
  }

  public static boolean isValidName(String name) {
    return name != null && NAME.matcher(name).matches();
  }

  public static boolean can(PartyAction action, PartyRole role) {
    switch (role) {
      case OWNER:
        return true;
      case OFFICER:
        switch (action) {
          case TOGGLE_OPEN:
          case TOGGLE_XP_SHARE:
          case TOGGLE_FRIENDLY_FIRE:
          case SET_SPAWN:
          case INVITE:
          case KICK_MEMBER:
            return true;
          default:
            return false;
        }
      default:
        return false;
    }
  }
}
