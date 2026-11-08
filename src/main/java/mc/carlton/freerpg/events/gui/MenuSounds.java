package mc.carlton.freerpg.events.gui;

import mc.carlton.freerpg.weaponskills.WeaponSkillConfig;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
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

  /** The menus of the original skills have the player as holder, the weapon menus their own. */
  private static boolean isFreeRpgMenu(Inventory inventory) {
    InventoryHolder holder = inventory.getHolder();
    return holder instanceof Player
        || (holder != null && holder.getClass().getName().startsWith("mc.carlton.freerpg."));
  }

  private static boolean isEnabled() {
    return WeaponSkillConfig.getBoolean("effects.sounds", true);
  }
}
