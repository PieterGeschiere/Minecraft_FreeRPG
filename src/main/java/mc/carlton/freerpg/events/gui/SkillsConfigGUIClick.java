package mc.carlton.freerpg.events.gui;

import mc.carlton.freerpg.skills.SkillRegistry;
import java.util.Arrays;
import java.util.List;
import mc.carlton.freerpg.core.info.player.PlayerStats;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;

public class SkillsConfigGUIClick implements Listener {

  @EventHandler
  public void clickEvent(InventoryClickEvent e) {
    if (e.getClickedInventory() == null) { //Clicked outside the inventory
      return;
    }
    String[] titles_0 = SkillRegistry.titleArray();
    String[] labels_0 = SkillRegistry.labelArray();
    String[] passiveLabels_0 = SkillRegistry.passiveLabels().toArray(new String[0]);
    List<String> labels = Arrays.asList(labels_0);
    List<String> skillTitles = Arrays.asList(titles_0);
    List<String> passiveLabels = Arrays.asList(passiveLabels_0);
    for (String skillTitle : skillTitles) {
      if (e.getView().getTitle().equalsIgnoreCase(skillTitle + " Configuration")) {
        String skillName = labels.get(skillTitles.indexOf(skillTitle));
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
        PlayerStats pStatClass = new PlayerStats(p);
        if (e.getCurrentItem() != null) {
          if (e.getCurrentItem().getType().equals(Material.ARROW)) {
            p.performCommand("frpg skillTreeGUI " + skillName);
          } else if (e.getRawSlot() == 19) {//Toggle EXPBar for skill
            pStatClass.togglePlayerSkillExpBar(skillName);
            p.performCommand("frpg skillConfigGUI " + skillName);
          } else if (e.getRawSlot() == 20 && !passiveLabels.contains(
              skillName)) { //Toggle Ability for skill
            pStatClass.togglePlayerSkillAbility(skillName);
            p.performCommand("frpg skillConfigGUI " + skillName);
          } else {
            switch (skillName) {
              case "digging":
                if (e.getSlot() == 37) {
                  p.performCommand("frpg flintToggle");
                  p.performCommand("frpg skillConfigGUI " + skillName);
                } else if (e.getSlot() == 38) {
                  p.performCommand("frpg megaDigToggle");
                  p.performCommand("frpg skillConfigGUI " + skillName);
                }
                break;
              case "woodcutting":
                if (e.getSlot() == 37) {
                  p.performCommand("frpg leafBlowerToggle");
                  p.performCommand("frpg skillConfigGUI " + skillName);
                }
                break;
              case "mining":
                if (e.getSlot() == 37) {
                  p.performCommand("frpg veinMinerToggle");
                  p.performCommand("frpg skillConfigGUI " + skillName);
                }
                break;
              case "fishing":
                if (e.getSlot() == 37) {
                  p.performCommand("frpg grappleToggle");
                  p.performCommand("frpg skillConfigGUI " + skillName);
                } else if (e.getSlot() == 38) {
                  p.performCommand("frpg hotRodToggle");
                  p.performCommand("frpg skillConfigGUI " + skillName);
                }
                break;
              case "axeMastery":
                if (e.getSlot() == 37) {
                  p.performCommand("frpg holyAxeToggle");
                  p.performCommand("frpg skillConfigGUI " + skillName);
                }
                break;
              case "agility":
                if (e.getSlot() == 37) {
                  p.performCommand("frpg speedToggle");
                  p.performCommand("frpg skillConfigGUI " + skillName);
                }
                break;
              case "alchemy":
                if (e.getSlot() == 37) {
                  p.performCommand("frpg potionToggle");
                  p.performCommand("frpg skillConfigGUI " + skillName);
                }
                break;
              case "smelting":
                if (e.getSlot() == 37) {
                  p.performCommand("frpg flamePickToggle");
                  p.performCommand("frpg skillConfigGUI " + skillName);
                }
                break;
              default:
                break;
            }
          }

          e.setCancelled(true); //So they cant take the items
        }
        break;
      }
    }
  }
}

