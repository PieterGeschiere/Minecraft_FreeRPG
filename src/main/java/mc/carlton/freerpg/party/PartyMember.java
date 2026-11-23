package mc.carlton.freerpg.party;

import java.util.UUID;

/** One member of a party. Times are epoch milliseconds. */
public final class PartyMember {

  private final UUID uuid;
  private String lastName;
  private PartyRole role;
  private final long joinedAt;
  private long lastSeen;
  private boolean receiveSharedXp = true;
  private boolean acceptTeleportRequests = true;

  public PartyMember(UUID uuid, String lastName, PartyRole role, long joinedAt) {
    this.uuid = uuid;
    this.lastName = lastName;
    this.role = role;
    this.joinedAt = joinedAt;
    this.lastSeen = joinedAt;
  }

  public UUID uuid() {
    return uuid;
  }

  public String lastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public PartyRole role() {
    return role;
  }

  public void setRole(PartyRole role) {
    this.role = role;
  }

  public long joinedAt() {
    return joinedAt;
  }

  public long lastSeen() {
    return lastSeen;
  }

  public void setLastSeen(long lastSeen) {
    this.lastSeen = lastSeen;
  }

  public boolean receiveSharedXp() {
    return receiveSharedXp;
  }

  public void setReceiveSharedXp(boolean receiveSharedXp) {
    this.receiveSharedXp = receiveSharedXp;
  }

  public boolean acceptTeleportRequests() {
    return acceptTeleportRequests;
  }

  public void setAcceptTeleportRequests(boolean acceptTeleportRequests) {
    this.acceptTeleportRequests = acceptTeleportRequests;
  }
}
