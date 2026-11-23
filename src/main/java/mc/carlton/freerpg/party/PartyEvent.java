package mc.carlton.freerpg.party;

import java.util.UUID;

/**
 * A change in a party. {@code actor} is who did it and {@code target} who it happened to (either
 * may be null). For MEMBER_LEFT and MEMBER_KICKED the target is no longer in the party. For
 * DISBANDED the party object still lists its former members.
 */
public record PartyEvent(PartyEventType type, Party party, UUID actor, UUID target) {
}
