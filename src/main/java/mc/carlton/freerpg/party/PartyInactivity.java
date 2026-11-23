package mc.carlton.freerpg.party;

import java.util.UUID;
import java.util.function.Predicate;

/**
 * Inactivity rules. A member who is online counts as seen right now. A threshold of 0 turns the
 * rule off.
 */
public final class PartyInactivity {

  private PartyInactivity() {
  }

  private static long effectiveLastSeen(PartyMember member, long now, Predicate<UUID> isOnline) {
    return isOnline.test(member.uuid()) ? now : member.lastSeen();
  }

  public static void apply(PartyManager manager, long now, long ownerTransferMillis,
      long deleteMillis, Predicate<UUID> isOnline) {
    for (Party party : manager.all()) {
      if (deleteMillis > 0 && allInactiveSince(party, now - deleteMillis, now, isOnline)) {
        manager.disbandParty(party, null);
        continue;
      }
      if (ownerTransferMillis > 0) {
        transferIfOwnerInactive(manager, party, now, ownerTransferMillis, isOnline);
      }
    }
  }

  private static boolean allInactiveSince(Party party, long threshold, long now,
      Predicate<UUID> isOnline) {
    for (PartyMember member : party.members()) {
      if (effectiveLastSeen(member, now, isOnline) > threshold) {
        return false;
      }
    }
    return true;
  }

  private static void transferIfOwnerInactive(PartyManager manager, Party party, long now,
      long thresholdMillis, Predicate<UUID> isOnline) {
    PartyMember owner = party.owner();
    if (owner == null) {
      return;
    }
    long since = now - thresholdMillis;
    if (effectiveLastSeen(owner, now, isOnline) > since) {
      return;
    }
    PartyMember candidate = firstActive(party, PartyRole.OFFICER, since, now, isOnline);
    if (candidate == null) {
      candidate = firstActive(party, PartyRole.MEMBER, since, now, isOnline);
    }
    if (candidate != null) {
      manager.forceTransfer(party, candidate.uuid());
    }
  }

  private static PartyMember firstActive(Party party, PartyRole role, long since, long now,
      Predicate<UUID> isOnline) {
    for (PartyMember member : party.members()) {
      if (member.role() == role && effectiveLastSeen(member, now, isOnline) > since) {
        return member;
      }
    }
    return null;
  }
}
