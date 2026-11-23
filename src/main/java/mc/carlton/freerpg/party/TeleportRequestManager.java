package mc.carlton.freerpg.party;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** Pending teleport requests (one per target) and the cooldown shared by every teleport. */
public final class TeleportRequestManager {

  /** {@code from} asks to teleport to {@code to}. */
  public record Request(UUID from, UUID to, long expiresAt) {
  }

  private final long expiryMillis;
  private final long cooldownMillis;
  private final Map<UUID, Request> byTarget = new HashMap<>();
  private final Map<UUID, Long> lastUsed = new HashMap<>();

  public TeleportRequestManager(long expiryMillis, long cooldownMillis) {
    this.expiryMillis = expiryMillis;
    this.cooldownMillis = cooldownMillis;
  }

  /** Stores the request, replacing the previous one for the same target. */
  public Request request(UUID from, UUID to, long now) {
    Request request = new Request(from, to, now + expiryMillis);
    byTarget.put(to, request);
    return request;
  }

  public boolean hasPending(UUID to, long now) {
    Request request = byTarget.get(to);
    return request != null && now <= request.expiresAt();
  }

  /** Takes the valid pending request of the target (it is gone afterwards). */
  public Optional<Request> accept(UUID to, long now) {
    return take(to, now);
  }

  public Optional<Request> deny(UUID to, long now) {
    return take(to, now);
  }

  private Optional<Request> take(UUID to, long now) {
    Request request = byTarget.remove(to);
    if (request == null || now > request.expiresAt()) {
      return Optional.empty();
    }
    return Optional.of(request);
  }

  public long cooldownRemaining(UUID player, long now) {
    Long last = lastUsed.get(player);
    if (last == null) {
      return 0;
    }
    return Math.max(0, cooldownMillis - (now - last));
  }

  /** Starts the cooldown (called when a teleport, by request or party spawn, succeeds). */
  public void markUsed(UUID player, long now) {
    lastUsed.put(player, now);
  }
}
