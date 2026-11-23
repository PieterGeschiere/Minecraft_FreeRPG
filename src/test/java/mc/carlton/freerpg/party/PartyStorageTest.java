package mc.carlton.freerpg.party;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PartyStorageTest {

  @TempDir
  File folder;

  private Party sampleParty(String name, UUID owner, UUID member) {
    Party party = new Party(name);
    PartyMember ownerMember = new PartyMember(owner, "Alice", PartyRole.OWNER, 1000L);
    ownerMember.setLastSeen(5000L);
    PartyMember other = new PartyMember(member, "Bob", PartyRole.OFFICER, 2000L);
    other.setReceiveSharedXp(false);
    other.setAcceptTeleportRequests(false);
    party.addMember(ownerMember);
    party.addMember(other);
    party.setOpen(true);
    party.setXpShareEnabled(false);
    party.setFriendlyFire(true);
    party.setSpawn(new PartySpawn("world", 1.5, 64, -3.25, 90f, 10f));
    party.pendingNotices().put(member, new ArrayList<>(List.of("You were promoted")));
    return party;
  }

  @Test
  void roundTripKeepsEverything() {
    UUID alice = UUID.randomUUID();
    UUID bob = UUID.randomUUID();
    PartyStorage storage = new PartyStorage(folder);
    storage.save(sampleParty("Dragons", alice, bob));

    List<String> warnings = new ArrayList<>();
    List<Party> loaded = storage.loadAll(warnings::add);
    assertEquals(0, warnings.size());
    assertEquals(1, loaded.size());
    Party party = loaded.get(0);
    assertEquals("Dragons", party.name());
    assertTrue(party.open());
    assertFalse(party.xpShareEnabled());
    assertTrue(party.friendlyFire());
    assertEquals(new PartySpawn("world", 1.5, 64, -3.25, 90f, 10f), party.spawn().get());
    assertEquals(List.of(alice, bob),
        party.members().stream().map(PartyMember::uuid).toList());
    PartyMember loadedBob = party.member(bob);
    assertEquals(PartyRole.OFFICER, loadedBob.role());
    assertEquals("Bob", loadedBob.lastName());
    assertEquals(2000L, loadedBob.joinedAt());
    assertFalse(loadedBob.receiveSharedXp());
    assertFalse(loadedBob.acceptTeleportRequests());
    assertEquals(5000L, party.member(alice).lastSeen());
    assertEquals(List.of("You were promoted"), party.pendingNotices().get(bob));
  }

  @Test
  void corruptFileIsSkippedOthersLoad() throws Exception {
    PartyStorage storage = new PartyStorage(folder);
    storage.save(sampleParty("Dragons", UUID.randomUUID(), UUID.randomUUID()));
    Files.writeString(new File(folder, "broken.yml").toPath(), "name: [unclosed\n  - : :\n\t");
    Files.writeString(new File(folder, "empty.yml").toPath(), "");
    List<String> warnings = new ArrayList<>();
    List<Party> loaded = storage.loadAll(warnings::add);
    assertEquals(1, loaded.size());
    assertEquals("Dragons", loaded.get(0).name());
    assertEquals(2, warnings.size());
  }

  @Test
  void wrongVersionIsSkippedWithWarning() throws Exception {
    PartyStorage storage = new PartyStorage(folder);
    storage.save(sampleParty("Dragons", UUID.randomUUID(), UUID.randomUUID()));
    File file = new File(folder, "dragons.yml");
    String text = Files.readString(file.toPath()).replace("version: 1", "version: 99");
    Files.writeString(file.toPath(), text);
    List<String> warnings = new ArrayList<>();
    assertEquals(0, storage.loadAll(warnings::add).size());
    assertEquals(1, warnings.size());
  }

  @Test
  void deleteRemovesFile() {
    PartyStorage storage = new PartyStorage(folder);
    storage.save(sampleParty("Dragons", UUID.randomUUID(), UUID.randomUUID()));
    assertTrue(new File(folder, "dragons.yml").exists());
    storage.delete("Dragons");
    assertFalse(new File(folder, "dragons.yml").exists());
  }
}
