package mc.carlton.freerpg.party;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** Pending party invites with an expiry. Pure Java. */
public final class InviteManager {

  /** An invite of {@code from} for {@code to} to join {@code party}. */
  public record Invite(UUID from, UUID to, String party, long expiresAt) {
  }

  private long expiryMillis;
  private final Map<UUID, List<Invite>> byTarget = new HashMap<>();

  public InviteManager(long expiryMillis) {
    this.expiryMillis = expiryMillis;
  }

  /** Changes how long new invites stay valid; pending invites are kept. */
  public void setExpiryMillis(long expiryMillis) {
    this.expiryMillis = expiryMillis;
  }

  public Invite invite(UUID from, UUID to, String party, long now) {
    List<Invite> list = byTarget.computeIfAbsent(to, k -> new ArrayList<>());
    list.removeIf(i -> i.expiresAt() < now || i.party().equalsIgnoreCase(party));
    Invite invite = new Invite(from, to, party, now + expiryMillis);
    list.add(invite);
    return invite;
  }

  /** The newest valid invite to {@code to}, optionally only for the given party name. */
  public Optional<Invite> pending(UUID to, String partyOrNull, long now) {
    List<Invite> list = byTarget.get(to);
    if (list == null) {
      return Optional.empty();
    }
    for (int i = list.size() - 1; i >= 0; i--) {
      Invite invite = list.get(i);
      if (invite.expiresAt() < now) {
        continue;
      }
      if (partyOrNull == null || invite.party().equalsIgnoreCase(partyOrNull)) {
        return Optional.of(invite);
      }
    }
    return Optional.empty();
  }

  /** Removes the invites of one party for {@code to}. Returns whether there was one. */
  public boolean consume(UUID to, String party) {
    List<Invite> list = byTarget.get(to);
    return list != null && list.removeIf(i -> i.party().equalsIgnoreCase(party));
  }

  /** Declines the newest valid invite (or the one of the given party). */
  public boolean deny(UUID to, String partyOrNull, long now) {
    Optional<Invite> invite = pending(to, partyOrNull, now);
    return invite.isPresent() && consume(to, invite.get().party());
  }

  /** Forgets every invite of a player (after joining a party). */
  public void clear(UUID to) {
    byTarget.remove(to);
  }
}
