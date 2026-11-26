package mc.carlton.freerpg.events.misc;

import mc.carlton.freerpg.FreeRPG;
import mc.carlton.freerpg.skills.perksAndAbilities.Fishing;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;

/**
 * Grappling Hook: the hook sticks when it hits any block (top, side or underside); a left click
 * (or arm swing) with the fishing rod then pulls the player to it.
 */
public class GrappleClick implements Listener {

  /** Logs what happens to the server log, to find out why a pull did not happen. */
  public static void debug(Player p, String message) {
    FreeRPG.getPlugin(FreeRPG.class).getLogger().info("[Grapple] " + p.getName() + ": " + message);
  }

  private static boolean canGrapple(Player p) {
    return p.getInventory().getItemInMainHand().getType() == Material.FISHING_ROD
        && p.getGameMode() != GameMode.CREATIVE && p.hasPermission("freeRPG.fish");
  }

  @EventHandler(priority = EventPriority.MONITOR)
  void onHookHit(ProjectileHitEvent e) {
    if (!(e.getEntity() instanceof FishHook hook) || e.getHitBlock() == null
        || !(hook.getShooter() instanceof Player p) || !canGrapple(p)) {
      return;
    }
    new Fishing(p).grapplingHook(hook, hook.getWorld(), hook.getLocation(),
        e.getHitBlockFace() == null ? org.bukkit.block.BlockFace.UP : e.getHitBlockFace());
  }

  @EventHandler(priority = EventPriority.HIGH)
  void onClick(PlayerInteractEvent e) {
    if (e.getAction() != Action.LEFT_CLICK_AIR && e.getAction() != Action.LEFT_CLICK_BLOCK) {
      return;
    }
    if (e.getHand() != EquipmentSlot.HAND || !canGrapple(e.getPlayer())) {
      return;
    }
    if (new Fishing(e.getPlayer()).grappleLaunch()) {
      e.setCancelled(true);
    }
  }

  @EventHandler
  void onQuit(PlayerQuitEvent e) {
    Fishing.clearGrapple(e.getPlayer().getUniqueId());
  }
}
