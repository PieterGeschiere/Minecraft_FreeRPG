package mc.carlton.freerpg.party;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PartyPlaceholderValuesTest {

  private final UUID owner = UUID.randomUUID();
  private final UUID officer = UUID.randomUUID();
  private final UUID member = UUID.randomUUID();
  private final Party party = new Party("Dragons");

  PartyPlaceholderValuesTest() {
    party.addMember(new PartyMember(owner, "Alice", PartyRole.OWNER, 0));
    party.addMember(new PartyMember(officer, "Bob", PartyRole.OFFICER, 1));
    party.addMember(new PartyMember(member, "Carol", PartyRole.MEMBER, 2));
    party.setOpen(true);
    party.setFriendlyFire(false);
  }

  private String value(String key, UUID player) {
    return PartyPlaceholderValues.resolve(key, Optional.of(party), player,
        Set.of(owner, member)::contains, 5);
  }

  @Test
  void valuesInsideAParty() {
    assertEquals("Dragons", value("name", officer));
    assertEquals("Officer", value("role", officer));
    assertEquals("Owner", value("role", owner));
    assertEquals("Member", value("role", member));
    assertEquals("Alice", value("owner", member));
    assertEquals("3", value("size", member));
    assertEquals("5", value("maxSize", member));
    assertEquals("2", value("online", member));
    assertEquals("true", value("open", member));
    assertEquals("false", value("friendlyFire", member));
  }

  @Test
  void keysAreCaseInsensitive() {
    assertEquals("5", value("MAXSIZE", member));
    assertEquals("false", value("friendlyfire", member));
  }

  @Test
  void emptyStringOutsideAPartyAndSizeIsZero() {
    UUID loner = UUID.randomUUID();
    for (String key : new String[] {"name", "role", "owner", "online", "open",
        "friendlyFire"}) {
      assertEquals("", PartyPlaceholderValues.resolve(key, Optional.empty(), loner, id -> false, 5),
          key);
    }
    assertEquals("0",
        PartyPlaceholderValues.resolve("size", Optional.empty(), loner, id -> false, 5));
    assertEquals("5",
        PartyPlaceholderValues.resolve("maxSize", Optional.empty(), loner, id -> false, 5));
  }

  @Test
  void unknownKeyReturnsNull() {
    assertNull(value("nonsense", member));
  }
}
