package mc.carlton.freerpg.party;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** A party: ordered members (by join time), roles and settings. Pure data, no Bukkit. */
public final class Party {

  private final String name;
  private final Map<UUID, PartyMember> members = new LinkedHashMap<>();
  private boolean open;
  private boolean xpShareEnabled = true;
  private boolean friendlyFire;
  private PartySpawn spawn;
  /** Notices for members who were offline, shown on their next login. */
  private final Map<UUID, List<String>> pendingNotices = new HashMap<>();

  public Party(String name) {
    this.name = name;
  }

  public String name() {
    return name;
  }

  public List<PartyMember> members() {
    return new ArrayList<>(members.values());
  }

  public PartyMember member(UUID uuid) {
    return members.get(uuid);
  }

  public boolean isMember(UUID uuid) {
    return members.containsKey(uuid);
  }

  public int size() {
    return members.size();
  }

  public void addMember(PartyMember member) {
    members.put(member.uuid(), member);
  }

  public PartyMember removeMember(UUID uuid) {
    pendingNotices.remove(uuid);
    return members.remove(uuid);
  }

  public PartyMember owner() {
    for (PartyMember member : members.values()) {
      if (member.role() == PartyRole.OWNER) {
        return member;
      }
    }
    return null;
  }

  public UUID ownerUuid() {
    PartyMember owner = owner();
    return owner == null ? null : owner.uuid();
  }

  public boolean open() {
    return open;
  }

  public void setOpen(boolean open) {
    this.open = open;
  }

  public boolean xpShareEnabled() {
    return xpShareEnabled;
  }

  public void setXpShareEnabled(boolean xpShareEnabled) {
    this.xpShareEnabled = xpShareEnabled;
  }

  public boolean friendlyFire() {
    return friendlyFire;
  }

  public void setFriendlyFire(boolean friendlyFire) {
    this.friendlyFire = friendlyFire;
  }

  public Optional<PartySpawn> spawn() {
    return Optional.ofNullable(spawn);
  }

  public void setSpawn(PartySpawn spawn) {
    this.spawn = spawn;
  }

  public Map<UUID, List<String>> pendingNotices() {
    return pendingNotices;
  }

  /**
   * Longest-standing member (join order) with the given role who was seen at or after the given
   * time, or null if there is none.
   */
  public PartyMember firstWithRole(PartyRole role, long seenSince) {
    for (PartyMember member : members.values()) {
      if (member.role() == role && member.lastSeen() >= seenSince) {
        return member;
      }
    }
    return null;
  }
}
