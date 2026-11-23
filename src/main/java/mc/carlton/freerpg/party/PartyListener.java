package mc.carlton.freerpg.party;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
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
        || e.getFrom().getBlockZ() != e.getTo().getBlockZ()
        || e.getFrom().getYaw() != e.getTo().getYaw()
        || e.getFrom().getPitch() != e.getTo().getPitch()) {
      touch(e.getPlayer().getUniqueId());
    }
  }

  /** Remembers that the player is active (not AFK) right now. */
  private static void touch(UUID id) {
    lastMoved.put(id, System.currentTimeMillis());
  }

  // Mining, farming, fishing and fighting in one spot is activity too, not only walking.

  @EventHandler(ignoreCancelled = true)
  public void onBreak(BlockBreakEvent e) {
    touch(e.getPlayer().getUniqueId());
  }

  @EventHandler(ignoreCancelled = true)
  public void onPlace(BlockPlaceEvent e) {
    touch(e.getPlayer().getUniqueId());
  }

  @EventHandler
  public void onInteract(PlayerInteractEvent e) {
    touch(e.getPlayer().getUniqueId());
  }

  @EventHandler
  public void onFish(PlayerFishEvent e) {
    touch(e.getPlayer().getUniqueId());
  }

  @EventHandler(ignoreCancelled = true)
  public void onAttack(EntityDamageByEntityEvent e) {
    if (e.getDamager() instanceof Player p) {
      touch(p.getUniqueId());
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
    TeleportService.forget(e.getEntity().getUniqueId());
    TeleportService.cancelInvolving(e.getEntity().getUniqueId(), "partyTpCancelOther");
  }
}
