package mc.carlton.freerpg.party;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/** Stops party members from hurting each other while friendly fire is off. */
public final class PartyFriendlyFire implements Listener {

  private final Map<UUID, Long> lastMessage = new HashMap<>();

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onDamage(EntityDamageByEntityEvent e) {
    if (!(e.getEntity() instanceof Player victim) || Parties.manager() == null) {
      return;
    }
    Player attacker = attackerOf(e);
    if (attacker == null) {
      return;
    }
    PartyManager manager = Parties.manager();
    if (FriendlyFireRules.shouldCancel(manager.partyOf(attacker.getUniqueId()),
        manager.partyOf(victim.getUniqueId()), attacker.getUniqueId(), victim.getUniqueId())) {
      e.setCancelled(true);
      long now = System.currentTimeMillis();
      Long last = lastMessage.get(attacker.getUniqueId());
      if (last == null || now - last > 1000) {
        lastMessage.put(attacker.getUniqueId(), now);
        attacker.sendMessage(PartyMessages.prefixed(attacker, "partyFriendlyFireBlocked"));
      }
    }
  }

  private static Player attackerOf(EntityDamageByEntityEvent e) {
    if (e.getDamager() instanceof Player player) {
      return player;
    }
    if (e.getDamager() instanceof Projectile projectile
        && projectile.getShooter() instanceof Player shooter) {
      return shooter;
    }
    if (e.getDamager() instanceof AreaEffectCloud cloud
        && cloud.getSource() instanceof Player source) {
      return source;
    }
    return null;
  }
}
