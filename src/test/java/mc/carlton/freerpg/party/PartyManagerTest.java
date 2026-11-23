package mc.carlton.freerpg.party;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PartyManagerTest {

  private final List<PartyEvent> events = new ArrayList<>();
  private PartyManager manager;
  private final UUID alice = UUID.randomUUID();
  private final UUID bob = UUID.randomUUID();
  private final UUID carol = UUID.randomUUID();
  private final UUID dave = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    manager = new PartyManager(5, events::add);
  }

  private void makeAliceParty() {
    assertEquals(PartyResult.OK, manager.create(alice, "Alice", "Dragons", 1000));
    manager.setOpen(alice, true);
    assertEquals(PartyResult.OK, manager.join(bob, "Bob", "Dragons", false, 2000));
    assertEquals(PartyResult.OK, manager.join(carol, "Carol", "Dragons", false, 3000));
    events.clear();
  }

  @Test
  void createMakesOwnerAndRejectsDuplicateNameCaseInsensitive() {
    assertEquals(PartyResult.OK, manager.create(alice, "Alice", "Dragons", 1000));
    assertEquals(PartyRole.OWNER, manager.partyOf(alice).get().member(alice).role());
    assertEquals(PartyResult.NAME_TAKEN, manager.create(bob, "Bob", "dragons", 1000));
    assertEquals(PartyResult.NAME_INVALID, manager.create(bob, "Bob", "x", 1000));
  }

  @Test
  void playerCanBeInOnePartyOnly() {
    manager.create(alice, "Alice", "Dragons", 1000);
    assertEquals(PartyResult.ALREADY_IN_PARTY, manager.create(alice, "Alice", "Other", 1000));
    manager.create(bob, "Bob", "Wolves", 1000);
    manager.setOpen(bob, true);
    assertEquals(PartyResult.ALREADY_IN_PARTY, manager.join(alice, "Alice", "Wolves", false, 1));
  }

  @Test
  void joinResolvesPartyNameBeforeOwnerName() {
    // "Bob" is the name of one party and also the owner of another party
    manager.create(alice, "Bob", "Wolves", 1000);
    manager.setOpen(alice, true);
    manager.create(bob, "Bobby", "Bob", 1000);
    manager.setOpen(bob, true);
    assertEquals(PartyResult.OK, manager.join(carol, "Carol", "bob", false, 2000));
    assertEquals("Bob", manager.partyOf(carol).get().name());
    // by owner name (case-insensitive) when no party has that name
    assertEquals(PartyResult.OK, manager.join(dave, "Dave", "BOBBY", false, 2000));
    assertEquals("Bob", manager.partyOf(dave).get().name());
  }

  @Test
  void closedPartyNeedsInviteOpenDoesNot() {
    manager.create(alice, "Alice", "Dragons", 1000);
    assertEquals(PartyResult.PARTY_CLOSED, manager.join(bob, "Bob", "Dragons", false, 2000));
    assertEquals(PartyResult.OK, manager.join(bob, "Bob", "Dragons", true, 2000));
    manager.setOpen(alice, true);
    assertEquals(PartyResult.OK, manager.join(carol, "Carol", "Alice", false, 2000));
    assertEquals(PartyResult.NOT_FOUND, manager.join(dave, "Dave", "Nope", false, 2000));
  }

  @Test
  void partyFullAtMaxSize() {
    makeAliceParty();
    assertEquals(PartyResult.OK, manager.join(dave, "Dave", "Dragons", false, 4000));
    assertEquals(PartyResult.OK,
        manager.join(UUID.randomUUID(), "E", "Dragons", false, 4000));
    assertEquals(PartyResult.PARTY_FULL,
        manager.join(UUID.randomUUID(), "F", "Dragons", false, 4000));
  }

  @Test
  void ownerLeavingPassesToLongestStandingOfficerElseMember() {
    makeAliceParty();
    // no officers: Bob joined first
    assertEquals(PartyResult.OK, manager.leave(alice, 5000));
    assertEquals(PartyRole.OWNER, manager.partyOf(bob).get().member(bob).role());
    assertTrue(manager.partyOf(alice).isEmpty());

    // with an officer: Carol is promoted and wins over longer-standing Bob? Bob is owner now
    manager.promote(bob, carol);
    manager.join(dave, "Dave", "Dragons", false, 6000);
    manager.promote(bob, dave);
    manager.leave(bob, 7000);
    assertEquals(PartyRole.OWNER, manager.partyOf(carol).get().member(carol).role());
    assertEquals(PartyRole.OFFICER, manager.partyOf(dave).get().member(dave).role());
  }

  @Test
  void officerBeatsLongerStandingMemberOnSuccession() {
    makeAliceParty();
    manager.promote(alice, carol);
    manager.leave(alice, 5000);
    assertEquals(PartyRole.OWNER, manager.partyOf(carol).get().member(carol).role());
    assertEquals(PartyRole.MEMBER, manager.partyOf(bob).get().member(bob).role());
  }

  @Test
  void lastMemberLeavingDisbandsAndEmitsEvent() {
    manager.create(alice, "Alice", "Dragons", 1000);
    events.clear();
    assertEquals(PartyResult.OK, manager.leave(alice, 2000));
    assertTrue(manager.byName("Dragons").isEmpty());
    assertTrue(manager.all().isEmpty());
    assertEquals(PartyEventType.DISBANDED, events.get(events.size() - 1).type());
  }

  @Test
  void officerCannotKickOfficerOrOwner() {
    makeAliceParty();
    manager.promote(alice, bob);
    manager.promote(alice, carol);
    assertEquals(PartyResult.TARGET_PROTECTED, manager.kick(bob, carol));
    assertEquals(PartyResult.TARGET_PROTECTED, manager.kick(bob, alice));
    manager.join(dave, "Dave", "Dragons", false, 4000);
    assertEquals(PartyResult.OK, manager.kick(bob, dave));
    assertTrue(manager.partyOf(dave).isEmpty());
    // plain member has no right
    manager.join(dave, "Dave", "Dragons", false, 4000);
    assertEquals(PartyResult.NO_PERMISSION, manager.kick(dave, carol));
    // owner can kick an officer
    assertEquals(PartyResult.OK, manager.kick(alice, carol));
  }

  @Test
  void transferAndPromoteDemoteChangeRoles() {
    makeAliceParty();
    assertEquals(PartyResult.OK, manager.promote(alice, bob));
    assertEquals(PartyRole.OFFICER, manager.partyOf(bob).get().member(bob).role());
    assertEquals(PartyResult.NO_PERMISSION, manager.promote(bob, carol));
    assertEquals(PartyResult.OK, manager.demote(alice, bob));
    assertEquals(PartyRole.MEMBER, manager.partyOf(bob).get().member(bob).role());
    assertEquals(PartyResult.OK, manager.transfer(alice, carol));
    Party party = manager.partyOf(carol).get();
    assertEquals(PartyRole.OWNER, party.member(carol).role());
    assertEquals(PartyRole.OFFICER, party.member(alice).role());
    assertEquals(carol, party.ownerUuid());
  }

  @Test
  void onlyOwnerCanDisband() {
    makeAliceParty();
    manager.promote(alice, bob);
    assertEquals(PartyResult.NO_PERMISSION, manager.disband(bob));
    assertEquals(PartyResult.OK, manager.disband(alice));
    assertTrue(manager.partyOf(bob).isEmpty());
    assertTrue(manager.partyOf(carol).isEmpty());
  }

  @Test
  void settingsNeedOfficerOrOwner() {
    makeAliceParty();
    assertEquals(PartyResult.NO_PERMISSION, manager.setXpShare(bob, false));
    manager.promote(alice, bob);
    assertEquals(PartyResult.OK, manager.setXpShare(bob, false));
    assertFalse(manager.partyOf(alice).get().xpShareEnabled());
    assertEquals(PartyResult.OK, manager.setFriendlyFire(bob, true));
    assertTrue(manager.partyOf(alice).get().friendlyFire());
    PartySpawn spawn = new PartySpawn("world", 1, 2, 3, 0, 0);
    assertEquals(PartyResult.OK, manager.setSpawn(bob, spawn));
    assertEquals(spawn, manager.partyOf(alice).get().spawn().get());
    assertEquals(PartyResult.NO_PERMISSION, manager.setOpen(carol, false));
  }

  @Test
  void eventsAreEmittedForEachChangeWithActorAndTarget() {
    makeAliceParty();
    manager.promote(alice, bob);
    PartyEvent promoted = events.get(0);
    assertEquals(PartyEventType.PROMOTED, promoted.type());
    assertEquals(alice, promoted.actor());
    assertEquals(bob, promoted.target());
    assertEquals("Dragons", promoted.party().name());
    manager.kick(alice, carol);
    assertEquals(PartyEventType.MEMBER_KICKED, events.get(1).type());
    assertEquals(carol, events.get(1).target());
    manager.setOpen(alice, false);
    assertEquals(PartyEventType.OPEN_CHANGED, events.get(2).type());
  }

  @Test
  void adminKickAndDisbandIgnoreRoles() {
    makeAliceParty();
    assertEquals(PartyResult.OK, manager.adminKick(alice));
    assertEquals(PartyRole.OWNER, manager.partyOf(bob).get().member(bob).role());
    assertEquals(PartyResult.OK, manager.adminDisband("dragons"));
    assertNull(manager.partyOf(bob).orElse(null));
    assertEquals(PartyResult.NOT_FOUND, manager.adminDisband("dragons"));
  }
}
