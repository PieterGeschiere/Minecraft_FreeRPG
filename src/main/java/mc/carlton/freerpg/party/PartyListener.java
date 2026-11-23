package mc.carlton.freerpg.party;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/** Keeps party data in step with players: login, logout, movement (AFK), damage and death. */
public final class PartyListener implements Listener {

  /** When each online player last changed block position. */
  private static final Map<UUID, Long> lastMoved = new HashMap<>();

  public static boolean isAfk(UUID id, long now) {
    Long last = lastMoved.get(id);
    return last != null && now - last > PartyConfig.afkSeconds() * 1000L;
  }

  @EventHandler
  public void onJoin(PlayerJoinEvent e) {
    Player p = e.getPlayer();
    long now = System.currentTimeMillis();
    lastMoved.put(p.getUniqueId(), now);
    Parties.manager().markSeen(p.getUniqueId(), p.getName(), now);
    Parties.manager().partyOf(p.getUniqueId()).ifPresent(Parties::save);
    PartyNotices.deliverPending(p);
    PartyDisplay.refreshAll();
    // other plugins may set up scoreboards a moment after the join
    Bukkit.getScheduler().runTaskLater(Parties.plugin(), PartyDisplay::refreshAll, 20L);
  }

  @EventHandler
  public void onQuit(PlayerQuitEvent e) {
    Player p = e.getPlayer();
    UUID id = p.getUniqueId();
    lastMoved.remove(id);
    TeleportService.forget(id);
    TeleportService.cancelInvolving(id, "partyTpCancelOther");
    Parties.manager().markSeen(id, p.getName(), System.currentTimeMillis());
    Parties.manager().partyOf(id).ifPresent(Parties::save);
  }

  @EventHandler(ignoreCancelled = true)
  public void onMove(PlayerMoveEvent e) {
    if (e.getTo() == null) {
      return;
    }
    if (e.getFrom().getBlockX() != e.getTo().getBlockX()
        || e.getFrom().getBlockY() != e.getTo().getBlockY()
        || e.getFrom().getBlockZ() != e.getTo().getBlockZ()) {
      lastMoved.put(e.getPlayer().getUniqueId(), System.currentTimeMillis());
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onDamage(EntityDamageEvent e) {
    if (e.getEntity() instanceof Player p && e.getFinalDamage() > 0) {
      TeleportService.noteDamage(p.getUniqueId());
    }
  }

  @EventHandler
  public void onDeath(PlayerDeathEvent e) {
    TeleportService.cancelInvolving(e.getEntity().getUniqueId(), "partyTpCancelOther");
  }
}
