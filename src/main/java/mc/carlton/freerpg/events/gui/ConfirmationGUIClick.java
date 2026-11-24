package mc.carlton.freerpg.events.gui;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import mc.carlton.freerpg.config.ConfigLoad;
import mc.carlton.freerpg.core.info.player.ChangeStats;
import mc.carlton.freerpg.core.info.player.PlayerStats;
import mc.carlton.freerpg.skills.perksAndAbilities.Global;
import mc.carlton.freerpg.utils.game.LanguageSelector;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

public class ConfirmationGUIClick implements Listener {

  @EventHandler
  public void clickEvent(InventoryClickEvent e) {
    if (e.getClickedInventory() == null) { //Clicked outside the inventory
      return;
    }
    if (e.getView().getTitle().equalsIgnoreCase("Confirmation Window")) {
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
      Inventory inv = e.getClickedInventory();
      if (e.getCurrentItem() != null) {
        String skillName = "";
        Material indicator = inv.getItem(4).getType();
        switch (indicator) {
          case IRON_SHOVEL:
            skillName = "digging";
            break;
          case IRON_AXE:
            skillName = "woodcutting";
            break;
          case IRON_PICKAXE:
            skillName = "mining";
            break;
          case IRON_HOE:
            skillName = "farming";
            break;
          case FISHING_ROD:
            skillName = "fishing";
            break;
          case BONE:
            skillName = "beastMastery";
            break;
          case IRON_SWORD:
            skillName = "swordsmanship";
            break;
          case IRON_CHESTPLATE:
            skillName = "defense";
            break;
          case GOLDEN_AXE:
            skillName = "axeMastery";
            break;
          case ANVIL:
            skillName = "repair";
            break;
          case LEATHER_LEGGINGS:
            skillName = "agility";
            break;
          case POTION:
            skillName = "alchemy";
            break;
          case COAL:
            skillName = "smelting";
            break;
          case ENCHANTING_TABLE:
            skillName = "enchanting";
            break;
          case BRICKS:
            skillName = "building";
            break;
          case ELYTRA:
            skillName = "aeronautics";
            break;
          default:
            break;
        }
        if (e.getCurrentItem().getType() == Material.LIME_TERRACOTTA) {
          UUID uuid = p.getUniqueId();
          ChangeStats refundStat = new ChangeStats(p);
          PlayerStats pStatClass = new PlayerStats(p);
          Map<UUID, Map<String, ArrayList<Number>>> statAll = pStatClass.getData();
          Map<String, ArrayList<Number>> pStatAll = statAll.get(uuid);
          ConfigLoad loadConfig = new ConfigLoad();
          ArrayList<Integer> soulsInfo = loadConfig.getSoulsInfo();
          int refundCost = (int) soulsInfo.get(1);
          int souls = (int) pStatAll.get("global").get(20);
          if (!loadConfig.getAllowedSkillsMap().getOrDefault(skillName, true)) {
            p.closeInventory(); // a disabled (or retired) skill cannot be refunded
            return;
          }
          if (souls >= refundCost) {

            //Refund the skill tree
            refundStat.refundSkillTree(skillName);

            //Remove the souls
            Global globalClass = new Global(p);
            globalClass.loseSouls(refundCost);
          } else {
            LanguageSelector langManager = new LanguageSelector(p);
            String refundCostString = Integer.toString(refundCost);
            p.sendMessage(
                ChatColor.RED + refundCostString + " " + langManager.getString("requiredSouls"));
          }

          p.performCommand("frpg skillTreeGUI " + skillName);
        } else if (e.getCurrentItem().getType() == Material.RED_TERRACOTTA
            || e.getCurrentItem().getType() == Material.ARROW) {
          p.performCommand("frpg skillTreeGUI " + skillName);
        }
      }

      e.setCancelled(true); //So they cant take the items
    }
  }
}

