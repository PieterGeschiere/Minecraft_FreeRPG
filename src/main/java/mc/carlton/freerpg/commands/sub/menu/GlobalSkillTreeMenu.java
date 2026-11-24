package mc.carlton.freerpg.commands.sub.menu;

import java.util.ArrayList;
import java.util.Map;
import mc.carlton.freerpg.config.ConfigLoad;
import mc.carlton.freerpg.utils.game.LanguageSelector;
import mc.carlton.freerpg.utils.globalVariables.StringsAndOtherData;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/** Skill tree menu of the global skill (moved verbatim from FrpgCommands). */
public class GlobalSkillTreeMenu implements SkillTreeMenu {

  @Override
  public void open(Player p, String skillName, Inventory gui,
      Map<String, ArrayList<Number>> pStatAll, Map<String, String[]> perksMap,
      Map<String, String[]> descriptionsMap, Map<String, String[]> passivePerksMap,
      Map<String, String[]> passiveDescriptionsMap, LanguageSelector lang, ConfigLoad configLoad,
      StringsAndOtherData stringsAndOtherData, String refundCost) {
        ArrayList<Number> pStats = pStatAll.get(skillName);

        int tokens_G = (Integer) pStats.get(1);
        int skill_1a_level = (Integer) pStats.get(2);
        int skill_1b_level = (Integer) pStats.get(3);
        int skill_1c_level = (Integer) pStats.get(4);
        int skill_2a_level = (Integer) pStats.get(5);
        int skill_2b_level = (Integer) pStats.get(6);
        int skill_2c_level = (Integer) pStats.get(7);
        int skill_3a_level = (Integer) pStats.get(8);
        int skill_3b_level = (Integer) pStats.get(9);
        int skill_3c_level = (Integer) pStats.get(10);
        int skill_M_level = (Integer) pStats.get(11);

        ItemStack skill_1a = new ItemStack(Material.PINK_TERRACOTTA);
        ItemStack skill_2a = new ItemStack(Material.PINK_TERRACOTTA);
        ItemStack skill_3a = new ItemStack(Material.PINK_TERRACOTTA);
        ItemStack skill_1b = new ItemStack(Material.RED_TERRACOTTA);
        ItemStack skill_2b = new ItemStack(Material.RED_TERRACOTTA);
        ItemStack skill_3b = new ItemStack(Material.RED_TERRACOTTA);
        ItemStack skill_1c = new ItemStack(Material.RED_TERRACOTTA);
        ItemStack skill_2c = new ItemStack(Material.RED_TERRACOTTA);
        ItemStack skill_3c = new ItemStack(Material.RED_TERRACOTTA);
        ItemStack skill_M = new ItemStack(Material.RED_TERRACOTTA);

        if (skill_1a_level == 0) {
          skill_1a.setType(Material.PINK_TERRACOTTA);
        } else {
          skill_1a.setType(Material.GREEN_TERRACOTTA);
        }

        if (skill_1b_level == 0) {
          skill_1b.setType(Material.PINK_TERRACOTTA);
        } else {
          skill_1b.setType(Material.GREEN_TERRACOTTA);
        }

        if (skill_1c_level == 0) {
          skill_1c.setType(Material.PINK_TERRACOTTA);
        } else {
          skill_1c.setType(Material.GREEN_TERRACOTTA);
        }

        if (skill_2a_level == 0) {
          if (skill_1a_level == 1) {
            skill_2a.setType(Material.PINK_TERRACOTTA);
          } else {
            skill_2a.setType(Material.RED_TERRACOTTA);
          }
        } else {
          skill_2a.setType(Material.GREEN_TERRACOTTA);
        }

        if (skill_2b_level == 0) {
          if (skill_1b_level == 1) {
            skill_2b.setType(Material.PINK_TERRACOTTA);
          } else {
            skill_2b.setType(Material.RED_TERRACOTTA);
          }
        } else {
          skill_2b.setType(Material.GREEN_TERRACOTTA);
        }

        if (skill_2c_level == 0) {
          if (skill_1c_level == 1) {
            skill_2c.setType(Material.PINK_TERRACOTTA);
          } else {
            skill_2c.setType(Material.RED_TERRACOTTA);
          }
        } else {
          skill_2c.setType(Material.GREEN_TERRACOTTA);
        }

        if (skill_3a_level == 0) {
          if (skill_3b_level == 1) {
            skill_3a.setType(Material.PINK_TERRACOTTA);
          } else {
            skill_3a.setType(Material.RED_TERRACOTTA);
          }
        } else {
          skill_3a.setType(Material.GREEN_TERRACOTTA);
        }

        if (skill_3c_level == 0) {
          if (skill_3b_level == 1) {
            skill_3c.setType(Material.PINK_TERRACOTTA);
          } else {
            skill_3c.setType(Material.RED_TERRACOTTA);
          }
        } else {
          skill_3c.setType(Material.GREEN_TERRACOTTA);
        }

        if (skill_3b_level == 0) {
          if (skill_2a_level == 1 && skill_2b_level == 1 && skill_2c_level == 1) {
            skill_3b.setType(Material.PINK_TERRACOTTA);
          } else {
            skill_3b.setType(Material.RED_TERRACOTTA);
          }
        } else {
          skill_3b.setType(Material.GREEN_TERRACOTTA);
        }

        if (skill_M_level == 0) {
          if (skill_3a_level == 1 && skill_3b_level == 1 && skill_3c_level == 1) {
            skill_M.setType(Material.PINK_TERRACOTTA);
          } else {
            skill_M.setType(Material.RED_TERRACOTTA);
          }
        } else {
          skill_M.setType(Material.GREEN_TERRACOTTA);
        }

        ItemStack[] menu_items = {skill_1a, skill_1b, skill_1c, skill_2a, skill_2b, skill_2c,
            skill_3a, skill_3b, skill_3c, skill_M};
        String[] labels = perksMap.get(skillName);
        String[] lores_line2 = descriptionsMap.get(skillName);
        String[] lores_line1 = {"1", "2", "3", "4", "5", "6", "7", "8", "9",
            "10"}; //Data lines 7-13
        for (int i = 0; i < labels.length; i++) {
          String level = pStats.get(2 + i).toString();
          lores_line1[i] =
              ChatColor.GRAY + lang.getString("level") + " " + ChatColor.BLUE + level + "/1";
          if (i == 9) {
            lores_line1[i] =
                ChatColor.GRAY + lang.getString("level") + " " + ChatColor.DARK_PURPLE + level
                    + "/1";
          }
        }

        Integer[] indices = {1, 19, 37, 3, 21, 39, 6, 24, 42, 26};
        //
        for (int i = 0; i < labels.length; i++) {
          ItemMeta meta = menu_items[i].getItemMeta();
          meta.setDisplayName(ChatColor.BOLD + labels[i]);
          ArrayList<String> lore = new ArrayList<>();
          lore.add(lores_line1[i]);
          String longString = lores_line2[i];
          ArrayList<String> splitDescs = stringsAndOtherData.getStringLines(longString);
          for (int j = 0; j < splitDescs.size(); j++) {
            lore.add(ChatColor.GRAY + ChatColor.ITALIC.toString() + splitDescs.get(j));
          }
          meta.setLore(lore);
          menu_items[i].setItemMeta(meta);
          gui.setItem(indices[i], menu_items[i]);
        }
        ItemStack g_token = new ItemStack(Material.DIAMOND);
        ItemStack back_button = new ItemStack(Material.ARROW);
        ItemStack[] menu_items_2 = {g_token, back_button};
        String[] labels_2 = passivePerksMap.get(skillName);
        String[] lores_line1_2 = {"Total: 0", ""};
        String[] lores_line2_2 = passiveDescriptionsMap.get(skillName);

        //Total global Tokens
        lores_line1_2[0] =
            ChatColor.BLUE + lang.getString("total") + ": " + ChatColor.GOLD + String.valueOf(
                tokens_G);
        if (tokens_G > 1 && tokens_G < 64) {
          g_token.setAmount(tokens_G);
        } else if (tokens_G >= 64) {
          g_token.setAmount(64);
        }
        Integer[] indices_2 = {0, 45};
        for (int i = 0; i < labels_2.length; i++) {
          ItemMeta meta = menu_items_2[i].getItemMeta();
          meta.setDisplayName(ChatColor.BOLD + labels_2[i]);
          ArrayList<String> lore = new ArrayList<>();
          lore.add(lores_line1_2[i]);
          String longString = lores_line2_2[i];
          ArrayList<String> splitDescs = stringsAndOtherData.getStringLines(longString);
          for (int j = 0; j < splitDescs.size(); j++) {
            lore.add(ChatColor.GRAY + ChatColor.ITALIC.toString() + splitDescs.get(j));
          }
          meta.setLore(lore);
          menu_items_2[i].setItemMeta(meta);
          gui.setItem(indices_2[i], menu_items_2[i]);
        }
        //Connectors
        ItemStack connector = new ItemStack(Material.GLASS_PANE);
        ItemMeta connectorMeta = connector.getItemMeta();
        connectorMeta.setDisplayName(ChatColor.WHITE.toString());
        connector.setItemMeta(connectorMeta);
        Integer[] indices_3 = {2, 4, 7, 8, 13, 15, 17, 20, 22, 23, 25, 31, 33, 35, 38, 40, 43, 44};
        for (int i = 0; i < indices_3.length; i++) {
          gui.setItem(indices_3[i], connector);
        }
        //Put the items in the inventory
        p.openInventory(gui);
  }
}
