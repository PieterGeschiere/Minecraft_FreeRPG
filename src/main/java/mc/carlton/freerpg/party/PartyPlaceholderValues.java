package mc.carlton.freerpg.party;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

/** Values of the party PlaceholderAPI tags (%freerpg_party_...%). Pure. */
public final class PartyPlaceholderValues {

  private PartyPlaceholderValues() {
  }

  /** Returns the value, or null for an unknown key. Outside a party most values are empty. */
  public static String resolve(String key, Optional<Party> party, UUID player,
      Predicate<UUID> isOnline, int maxSize) {
    switch (key.toLowerCase(Locale.ROOT)) {
      case "name":
        return party.map(Party::name).orElse("");
      case "role":
        return party.map(p -> roleName(p.member(player).role())).orElse("");
      case "owner":
        return party.map(p -> p.owner() == null ? "" : String.valueOf(p.owner().lastName()))
            .orElse("");
      case "size":
        return String.valueOf(party.map(Party::size).orElse(0));
      case "maxsize":
        return String.valueOf(maxSize);
      case "online":
        return party.map(p -> String.valueOf(
            p.members().stream().filter(m -> isOnline.test(m.uuid())).count())).orElse("");
      case "open":
        return party.map(p -> String.valueOf(p.open())).orElse("");
      case "friendlyfire":
        return party.map(p -> String.valueOf(p.friendlyFire())).orElse("");
      default:
        return null;
    }
  }

  private static String roleName(PartyRole role) {
    switch (role) {
      case OWNER:
        return "Owner";
      case OFFICER:
        return "Officer";
      default:
        return "Member";
    }
  }
}
