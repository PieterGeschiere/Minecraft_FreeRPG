package mc.carlton.freerpg.events.gui;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import mc.carlton.freerpg.FreeRPG;
import mc.carlton.freerpg.weaponskills.WeaponSkillConfig;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

/** A soft click when the player presses an item in one of the FreeRPG menus. */
public class MenuSounds implements Listener {

  @EventHandler(priority = EventPriority.MONITOR)
  public void onClick(InventoryClickEvent e) {
    if (!(e.getWhoClicked() instanceof Player) || !isFreeRpgMenu(e.getInventory())
        || e.getClickedInventory() == null || e.getClickedInventory() != e.getInventory()) {
      return;
    }
    ItemStack item = e.getCurrentItem();
    if (item == null || item.getType() == Material.AIR || !isEnabled()) {
      return;
    }
    Player p = (Player) e.getWhoClicked();
    p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.6f, 1.0f);
  }

  /** When a FreeRPG menu closed, per player: a menu that opens right after is a page change. */
  private static final Map<UUID, Long> LAST_CLOSE = new HashMap<>();

  @EventHandler(priority = EventPriority.MONITOR)
  public void onOpen(InventoryOpenEvent e) {
    if (!(e.getPlayer() instanceof Player) || !isFreeRpgMenu(e.getInventory()) || !isEnabled()) {
      return;
    }
    Player p = (Player) e.getPlayer();
    Long closed = LAST_CLOSE.get(p.getUniqueId());
    if (closed != null && System.currentTimeMillis() - closed < 400) {
      return; // switching between menus, not opening one
    }
    p.playSound(p.getLocation(), Sound.BLOCK_ENDER_CHEST_OPEN, 0.25f, 1.8f);
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onClose(InventoryCloseEvent e) {
    if (!(e.getPlayer() instanceof Player) || !isFreeRpgMenu(e.getInventory()) || !isEnabled()) {
      return;
    }
    Player p = (Player) e.getPlayer();
    LAST_CLOSE.put(p.getUniqueId(), System.currentTimeMillis());
    // wait a moment: when another menu opens in its place nothing should play
    Bukkit.getScheduler().runTaskLater(FreeRPG.getPlugin(FreeRPG.class), () -> {
      if (p.isOnline() && !isFreeRpgMenu(p.getOpenInventory().getTopInventory())) {
        p.playSound(p.getLocation(), Sound.BLOCK_ENDER_CHEST_CLOSE, 0.2f, 1.8f);
      }
    }, 2L);
  }

  /** The menus of the original skills have the player as holder, the weapon menus their own. */
  private static boolean isFreeRpgMenu(Inventory inventory) {
    InventoryHolder holder = inventory.getHolder();
    if (holder instanceof Player) {
      // the player's own crafting grid and ender chest have the player as holder as well, so only
      // a plain chest style inventory counts
      return inventory.getType() == InventoryType.CHEST;
    }
    return holder != null && holder.getClass().getName().startsWith("mc.carlton.freerpg.");
  }

  private static boolean isEnabled() {
    return WeaponSkillConfig.getBoolean("effects.sounds", true);
  }
}
