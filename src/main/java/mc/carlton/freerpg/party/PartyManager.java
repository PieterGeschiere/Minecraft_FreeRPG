package mc.carlton.freerpg.party;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

/** All parties in memory and every change to them. Pure Java; changes are announced as events. */
public final class PartyManager {

  private final int maxSize;
  private final Consumer<PartyEvent> sink;
  private final Map<String, Party> parties = new LinkedHashMap<>();
  private final Map<UUID, Party> index = new HashMap<>();

  public PartyManager(int maxSize, Consumer<PartyEvent> sink) {
    this.maxSize = maxSize;
    this.sink = sink;
  }

  public int maxSize() {
    return maxSize;
  }

  // ---------------------------------------------------------------- queries

  public Optional<Party> partyOf(UUID player) {
    return Optional.ofNullable(index.get(player));
  }

  public Optional<Party> byName(String name) {
    return name == null ? Optional.empty() : Optional.ofNullable(parties.get(key(name)));
  }

  public Collection<Party> all() {
    return new ArrayList<>(parties.values());
  }

  private static String key(String name) {
    return name.toLowerCase(Locale.ROOT);
  }

  private void emit(PartyEventType type, Party party, UUID actor, UUID target) {
    if (sink != null) {
      sink.accept(new PartyEvent(type, party, actor, target));
    }
  }

  /** Adds a loaded party (used by storage); no events. */
  public void register(Party party) {
    parties.put(key(party.name()), party);
    for (PartyMember member : party.members()) {
      index.put(member.uuid(), party);
    }
  }

  /** Removes a party silently (used by storage and cleanup). */
  public void remove(String name) {
    Party party = parties.remove(key(name));
    if (party != null) {
      for (PartyMember member : party.members()) {
        index.remove(member.uuid());
      }
    }
  }

  // --------------------------------------------------------------- creating

  public PartyResult create(UUID owner, String lastName, String partyName, long now) {
    if (index.containsKey(owner)) {
      return PartyResult.ALREADY_IN_PARTY;
    }
    if (!PartyRules.isValidName(partyName)) {
      return PartyResult.NAME_INVALID;
    }
    if (parties.containsKey(key(partyName))) {
      return PartyResult.NAME_TAKEN;
    }
    Party party = new Party(partyName);
    party.addMember(new PartyMember(owner, lastName, PartyRole.OWNER, now));
    register(party);
    emit(PartyEventType.MEMBER_JOINED, party, owner, owner);
    return PartyResult.OK;
  }

  /**
   * Joins by party name first, then by the last known name of a party owner (case-insensitive). A
   * closed party needs an invite.
   */
  public PartyResult join(UUID player, String lastName, String target, boolean hasInvite,
      long now) {
    if (index.containsKey(player)) {
      return PartyResult.ALREADY_IN_PARTY;
    }
    Party party = target == null ? null : parties.get(key(target));
    if (party == null && target != null) {
      for (Party candidate : parties.values()) {
        PartyMember owner = candidate.owner();
        if (owner != null && owner.lastName() != null
            && owner.lastName().equalsIgnoreCase(target)) {
          party = candidate;
          break;
        }
      }
    }
    if (party == null) {
      return PartyResult.NOT_FOUND;
    }
    if (!party.open() && !hasInvite) {
      return PartyResult.PARTY_CLOSED;
    }
    if (party.size() >= maxSize) {
      return PartyResult.PARTY_FULL;
    }
    party.addMember(new PartyMember(player, lastName, PartyRole.MEMBER, now));
    index.put(player, party);
    emit(PartyEventType.MEMBER_JOINED, party, player, player);
    return PartyResult.OK;
  }

  // ---------------------------------------------------------------- leaving

  public PartyResult leave(UUID player, long now) {
    Party party = index.get(player);
    if (party == null) {
      return PartyResult.NOT_IN_PARTY;
    }
    removeMember(party, player, PartyEventType.MEMBER_LEFT, player);
    return PartyResult.OK;
  }

  /** Removes a member, disbanding the party if nobody is left and passing on ownership. */
  private void removeMember(Party party, UUID player, PartyEventType type, UUID actor) {
    boolean wasOwner = player.equals(party.ownerUuid());
    party.removeMember(player);
    index.remove(player);
    if (party.size() == 0) {
      parties.remove(key(party.name()));
      emit(type, party, actor, player);
      emit(PartyEventType.DISBANDED, party, actor, null);
      return;
    }
    emit(type, party, actor, player);
    if (wasOwner) {
      PartyMember successor = party.firstWithRole(PartyRole.OFFICER, Long.MIN_VALUE);
      if (successor == null) {
        successor = party.firstWithRole(PartyRole.MEMBER, Long.MIN_VALUE);
      }
      successor.setRole(PartyRole.OWNER);
      emit(PartyEventType.OWNER_CHANGED, party, player, successor.uuid());
    }
  }

  public PartyResult kick(UUID actor, UUID target) {
    Party party = index.get(actor);
    if (party == null) {
      return PartyResult.NOT_IN_PARTY;
    }
    if (actor.equals(target)) {
      return PartyResult.SELF_TARGET;
    }
    PartyMember targetMember = party.member(target);
    if (targetMember == null) {
      return PartyResult.TARGET_NOT_MEMBER;
    }
    PartyRole actorRole = party.member(actor).role();
    if (!PartyRules.can(PartyAction.KICK_MEMBER, actorRole)) {
      return PartyResult.NO_PERMISSION;
    }
    if (targetMember.role() == PartyRole.OWNER) {
      return PartyResult.TARGET_PROTECTED;
    }
    if (targetMember.role() == PartyRole.OFFICER
        && !PartyRules.can(PartyAction.KICK_OFFICER, actorRole)) {
      return PartyResult.TARGET_PROTECTED;
    }
    removeMember(party, target, PartyEventType.MEMBER_KICKED, actor);
    return PartyResult.OK;
  }

  // ------------------------------------------------------------------ roles

  public PartyResult promote(UUID actor, UUID target) {
    return changeRole(actor, target, PartyAction.PROMOTE, PartyRole.MEMBER, PartyRole.OFFICER,
        PartyEventType.PROMOTED);
  }

  public PartyResult demote(UUID actor, UUID target) {
    return changeRole(actor, target, PartyAction.DEMOTE, PartyRole.OFFICER, PartyRole.MEMBER,
        PartyEventType.DEMOTED);
  }

  private PartyResult changeRole(UUID actor, UUID target, PartyAction action, PartyRole from,
      PartyRole to, PartyEventType type) {
    Party party = index.get(actor);
    if (party == null) {
      return PartyResult.NOT_IN_PARTY;
    }
    if (!PartyRules.can(action, party.member(actor).role())) {
      return PartyResult.NO_PERMISSION;
    }
    if (actor.equals(target)) {
      return PartyResult.SELF_TARGET;
    }
    PartyMember member = party.member(target);
    if (member == null) {
      return PartyResult.TARGET_NOT_MEMBER;
    }
    if (member.role() != from) {
      return PartyResult.TARGET_PROTECTED;
    }
    member.setRole(to);
    emit(type, party, actor, target);
    return PartyResult.OK;
  }

  public PartyResult transfer(UUID actor, UUID target) {
    Party party = index.get(actor);
    if (party == null) {
      return PartyResult.NOT_IN_PARTY;
    }
    if (!PartyRules.can(PartyAction.TRANSFER, party.member(actor).role())) {
      return PartyResult.NO_PERMISSION;
    }
    if (actor.equals(target)) {
      return PartyResult.SELF_TARGET;
    }
    PartyMember member = party.member(target);
    if (member == null) {
      return PartyResult.TARGET_NOT_MEMBER;
    }
    party.member(actor).setRole(PartyRole.OFFICER);
    member.setRole(PartyRole.OWNER);
    emit(PartyEventType.OWNER_CHANGED, party, actor, target);
    return PartyResult.OK;
  }

  /**
   * Gives ownership to a member without any permission check (inactivity rule). The former owner
   * becomes an officer.
   */
  public void forceTransfer(Party party, UUID newOwner) {
    PartyMember owner = party.owner();
    PartyMember member = party.member(newOwner);
    if (member == null || member == owner) {
      return;
    }
    UUID oldOwner = owner == null ? null : owner.uuid();
    if (owner != null) {
      owner.setRole(PartyRole.OFFICER);
    }
    member.setRole(PartyRole.OWNER);
    emit(PartyEventType.OWNER_CHANGED, party, oldOwner, newOwner);
  }

  // ------------------------------------------------------------- disbanding

  public PartyResult disband(UUID actor) {
    Party party = index.get(actor);
    if (party == null) {
      return PartyResult.NOT_IN_PARTY;
    }
    if (!PartyRules.can(PartyAction.DISBAND, party.member(actor).role())) {
      return PartyResult.NO_PERMISSION;
    }
    disbandParty(party, actor);
    return PartyResult.OK;
  }

  public PartyResult adminDisband(String partyName) {
    Party party = byName(partyName).orElse(null);
    if (party == null) {
      return PartyResult.NOT_FOUND;
    }
    disbandParty(party, null);
    return PartyResult.OK;
  }

  /** Disbands without any permission check (admin and inactivity). */
  public void disbandParty(Party party, UUID actor) {
    emit(PartyEventType.DISBANDED, party, actor, null);
    remove(party.name());
  }

  public PartyResult adminKick(UUID target) {
    Party party = index.get(target);
    if (party == null) {
      return PartyResult.NOT_IN_PARTY;
    }
    removeMember(party, target, PartyEventType.MEMBER_KICKED, null);
    return PartyResult.OK;
  }

  // --------------------------------------------------------------- settings

  public PartyResult setOpen(UUID actor, boolean open) {
    return setting(actor, PartyAction.TOGGLE_OPEN, party -> {
      if (party.open() == open) {
        return false;
      }
      party.setOpen(open);
      return true;
    }, PartyEventType.OPEN_CHANGED);
  }

  public PartyResult setXpShare(UUID actor, boolean enabled) {
    return setting(actor, PartyAction.TOGGLE_XP_SHARE, party -> {
      if (party.xpShareEnabled() == enabled) {
        return false;
      }
      party.setXpShareEnabled(enabled);
      return true;
    }, PartyEventType.XP_SHARE_CHANGED);
  }

  public PartyResult setFriendlyFire(UUID actor, boolean enabled) {
    return setting(actor, PartyAction.TOGGLE_FRIENDLY_FIRE, party -> {
      if (party.friendlyFire() == enabled) {
        return false;
      }
      party.setFriendlyFire(enabled);
      return true;
    }, PartyEventType.FRIENDLY_FIRE_CHANGED);
  }

  public PartyResult setSpawn(UUID actor, PartySpawn spawn) {
    return setting(actor, PartyAction.SET_SPAWN, party -> {
      party.setSpawn(spawn);
      return true;
    }, PartyEventType.SPAWN_SET);
  }

  private PartyResult setting(UUID actor, PartyAction action,
      java.util.function.Predicate<Party> change, PartyEventType type) {
    Party party = index.get(actor);
    if (party == null) {
      return PartyResult.NOT_IN_PARTY;
    }
    if (!PartyRules.can(action, party.member(actor).role())) {
      return PartyResult.NO_PERMISSION;
    }
    if (change.test(party)) {
      emit(type, party, actor, null);
    }
    return PartyResult.OK;
  }

  // -------------------------------------------------------------- bookkeeping

  /** Remembers the current name and time a member was seen. */
  public void markSeen(UUID player, String name, long now) {
    Party party = index.get(player);
    if (party == null) {
      return;
    }
    PartyMember member = party.member(player);
    member.setLastSeen(now);
    if (name != null) {
      member.setLastName(name);
    }
  }

  /** Members of the given party as plain UUIDs (join order). */
  public static List<UUID> memberIds(Party party) {
    List<UUID> ids = new ArrayList<>();
    for (PartyMember member : party.members()) {
      ids.add(member.uuid());
    }
    return ids;
  }
}
