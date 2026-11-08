package mc.carlton.freerpg.events.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;

public class MainGUIclick implements Listener {

  @EventHandler
  public void clickEvent(InventoryClickEvent e) {
    boolean proceed = true;
    try {
      InventoryType invType = e.getClickedInventory().getType();
    } catch (Exception except) {
      proceed = false;
    }
    //Check to see if its the GUI menu
    if (proceed) {
      if (e.getView().getTitle().equalsIgnoreCase("Skills")) {
        if (e.getClickedInventory().getType() == InventoryType.PLAYER) {
          e.setCancelled(true);
          return;
        }
        if (e.getClick() != ClickType.LEFT) {
          e.setCancelled(true);
        }
        if (e.getCursor().getType() != Material.AIR) {
          e.setCancelled(true);
        }
        Player p = (Player) e.getWhoClicked();
        //Determine what they selected and what to do
        if (e.getCurrentItem() != null) {
          switch (e.getCurrentItem().getType()) {
            case MACE:
              p.performCommand("frpg weapons");
              break;
            case NETHER_STAR:
              p.performCommand("frpg skillTreeGUI global");
              break;
            case IRON_SHOVEL:
              p.performCommand("frpg skillTreeGUI digging");
              break;
            case IRON_AXE:
              p.performCommand("frpg skillTreeGUI woodcutting");
              break;
            case IRON_PICKAXE:
              p.performCommand("frpg skillTreeGUI mining");
              break;
            case IRON_HOE:
              p.performCommand("frpg skillTreeGUI farming");
              break;
            case FISHING_ROD:
              p.performCommand("frpg skillTreeGUI fishing");
              break;
            case BOW:
              p.performCommand("frpg skillTreeGUI archery");
              break;
            case BONE:
              p.performCommand("frpg skillTreeGUI beastMastery");
              break;
            case IRON_SWORD:
              p.performCommand("frpg skillTreeGUI swordsmanship");
              break;
            case IRON_CHESTPLATE:
              p.performCommand("frpg skillTreeGUI defense");
              break;
            case GOLDEN_AXE:
              p.performCommand("frpg skillTreeGUI axeMastery");
              break;
            case ANVIL:
              p.performCommand("frpg skillTreeGUI repair");
              break;
            case LEATHER_LEGGINGS:
              p.performCommand("frpg skillTreeGUI agility");
              break;
            case POTION:
              p.performCommand("frpg skillTreeGUI alchemy");
              break;
            case COAL:
              p.performCommand("frpg skillTreeGUI smelting");
              break;
            case ENCHANTING_TABLE:
              p.performCommand("frpg skillTreeGUI enchanting");
              break;
            case REDSTONE:
              p.performCommand("frpg configurationGUI");
              break;
            default:
              break;
          }
        }

        e.setCancelled(true); //So they cant take the items
      }

    }
  }
}