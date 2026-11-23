package mc.carlton.freerpg.party;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PartyInactivityTest {

  private static final long DAY = 24L * 60 * 60 * 1000;
  private static final long NOW = 100 * DAY;

  private final List<PartyEvent> events = new ArrayList<>();
  private final Set<UUID> online = new HashSet<>();
  private PartyManager manager;
  private final UUID owner = UUID.randomUUID();
  private final UUID officer = UUID.randomUUID();
  private final UUID member = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    manager = new PartyManager(5, events::add);
    manager.create(owner, "Owner", "Dragons", 0);
    manager.setOpen(owner, true);
    manager.join(officer, "Officer", "Dragons", false, 1);
    manager.join(member, "Member", "Dragons", false, 2);
    manager.promote(owner, officer);
    events.clear();
  }

  private void seen(UUID player, long daysAgo) {
    manager.partyOf(player).get().member(player).setLastSeen(NOW - daysAgo * DAY);
  }

  private void run() {
    PartyInactivity.apply(manager, NOW, 30 * DAY, 60 * DAY, online::contains);
  }

  @Test
  void ownerOfflineJustUnderThresholdKeepsOwnership() {
    seen(owner, 29);
    seen(officer, 0);
    seen(member, 0);
    run();
    assertEquals(owner, manager.byName("Dragons").get().ownerUuid());
  }

  @Test
  void ownerOfflineThresholdTransfersToActiveOfficer() {
    seen(owner, 30);
    seen(officer, 1);
    seen(member, 1);
    run();
    Party party = manager.byName("Dragons").get();
    assertEquals(officer, party.ownerUuid());
    assertEquals(PartyRole.OFFICER, party.member(owner).role());
    assertEquals(PartyEventType.OWNER_CHANGED, events.get(0).type());
  }

  @Test
  void fallsBackToActiveMemberWhenNoOfficerActive() {
    seen(owner, 40);
    seen(officer, 35);
    seen(member, 2);
    run();
    assertEquals(member, manager.byName("Dragons").get().ownerUuid());
  }

  @Test
  void noActiveCandidateMeansNoChange() {
    seen(owner, 40);
    seen(officer, 35);
    seen(member, 31);
    run();
    assertEquals(owner, manager.byName("Dragons").get().ownerUuid());
  }

  @Test
  void onlineOwnerIsNeverReplacedEvenWithOldLastSeen() {
    seen(owner, 90);
    seen(officer, 0);
    online.add(owner);
    online.add(officer);
    run();
    assertEquals(owner, manager.byName("Dragons").get().ownerUuid());
  }

  @Test
  void allMembersOfflineDeleteThresholdDisbands() {
    seen(owner, 60);
    seen(officer, 61);
    seen(member, 70);
    run();
    assertTrue(manager.byName("Dragons").isEmpty());
    assertEquals(PartyEventType.DISBANDED, events.get(events.size() - 1).type());
  }

  @Test
  void oneOnlineMemberPreventsDelete() {
    seen(owner, 90);
    seen(officer, 90);
    seen(member, 90);
    online.add(member);
    run();
    assertTrue(manager.byName("Dragons").isPresent());
  }

  @Test
  void zeroDisablesRules() {
    seen(owner, 90);
    seen(officer, 90);
    seen(member, 90);
    PartyInactivity.apply(manager, NOW, 0, 0, online::contains);
    assertEquals(owner, manager.byName("Dragons").get().ownerUuid());
  }

  @Test
  void serverDowntimeIsNotCountedAsInactivity() {
    // the server was down for the last 61 days; everybody was last seen 2 days before it went down
    long heartbeat = NOW - 61 * DAY;
    seen(owner, 63);
    seen(officer, 63);
    seen(member, 63);
    PartyInactivity.compensateDowntime(manager, heartbeat, NOW);
    run();
    assertTrue(manager.byName("Dragons").isPresent());
    assertEquals(owner, manager.byName("Dragons").get().ownerUuid());
  }

  @Test
  void aPlayerAlreadyAwayBeforeTheDowntimeStillCountsAsAway() {
    long heartbeat = NOW - 10 * DAY;
    seen(owner, 50); // away for 40 days before the 10 day downtime, so 40 days of absence
    seen(officer, 1);
    seen(member, 1);
    PartyInactivity.compensateDowntime(manager, heartbeat, NOW);
    run();
    assertEquals(officer, manager.byName("Dragons").get().ownerUuid());
  }
}
