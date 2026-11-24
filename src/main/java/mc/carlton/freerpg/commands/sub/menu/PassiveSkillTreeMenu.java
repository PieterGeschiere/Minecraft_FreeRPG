package mc.carlton.freerpg.commands.sub.menu;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import mc.carlton.freerpg.config.ConfigLoad;
import mc.carlton.freerpg.skills.building.BuildingMenu;
import mc.carlton.freerpg.utils.globalVariables.ItemGroups;
import mc.carlton.freerpg.utils.UtilityMethods;
import mc.carlton.freerpg.utils.game.LanguageSelector;
import mc.carlton.freerpg.utils.globalVariables.StringsAndOtherData;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/** Skill tree menu of the 5 passive skills (moved verbatim from FrpgCommands). */
public class PassiveSkillTreeMenu implements SkillTreeMenu {

  @Override
  public void open(Player p, String skillName, Inventory gui,
      Map<String, ArrayList<Number>> pStatAll, Map<String, String[]> perksMap,
      Map<String, String[]> descriptionsMap, Map<String, String[]> passivePerksMap,
      Map<String, String[]> passiveDescriptionsMap, LanguageSelector lang, ConfigLoad configLoad,
      StringsAndOtherData stringsAndOtherData, String refundCost) {

        //Skills
        ArrayList<Number> pStats = pStatAll.get(skillName);
        int tokens_S = (Integer) pStats.get(3);
        Number passive1 = pStats.get(4);
        int skill_1a_level = (Integer) pStats.get(7);
        int skill_2a_level = (Integer) pStats.get(9);
        int skill_M_level = (Integer) pStats.get(13);

        ItemStack skill_1a = new ItemStack(Material.PINK_TERRACOTTA);
        ItemStack skill_2a = new ItemStack(Material.RED_TERRACOTTA);
        ItemStack skill_M = new ItemStack(Material.RED_TERRACOTTA);

        if (skill_1a_level == 0) {
          skill_1a.setType(Material.PINK_TERRACOTTA);
        } else if (skill_1a_level > 0 && skill_1a_level < 5) {
          skill_1a.setType(Material.YELLOW_TERRACOTTA);
        } else {
          skill_1a.setType(Material.GREEN_TERRACOTTA);
        }

        if (skill_2a_level == 0) {
          if (skill_1a_level >= 2) {
            skill_2a.setType(Material.PINK_TERRACOTTA);
          }
        } else if (skill_2a_level > 0 && skill_2a_level < 5) {
          skill_2a.setType(Material.YELLOW_TERRACOTTA);
        } else {
          skill_2a.setType(Material.GREEN_TERRACOTTA);
        }

        if (skill_M_level == 0) {
          if (skill_1a_level + skill_2a_level >= 10) {
            skill_M.setType(Material.PINK_TERRACOTTA);
          }
        } else {
          skill_M.setType(Material.GREEN_TERRACOTTA);
        }

        ItemStack[] menu_items = {skill_1a, skill_2a, skill_M};
        String[] labels = perksMap.get(skillName);
        String[] lores_line2 = descriptionsMap.get(skillName);
        String desc = "";
        int special_index = 0;
        switch (skillName) {
          case "alchemy":
            special_index = 1;
            switch (skill_2a_level) {
              case 0:
                desc = lang.getString("alchemyPerkDesc1_0") + " ";
                desc += stringsAndOtherData.getPotionEffectTypeString(1, p);
                lores_line2[special_index] = desc;
                break;
              case 1:
                desc = lang.getString("alchemyPerkDesc1_0") + " ";
                desc += stringsAndOtherData.getPotionEffectTypeString(2, p);
                lores_line2[special_index] = desc;
                break;
              case 2:
                desc = lang.getString("alchemyPerkDesc1_0") + " ";
                desc += stringsAndOtherData.getPotionEffectTypeString(3, p);
                lores_line2[special_index] = desc;
                break;
              case 3:
                desc = lang.getString("alchemyPerkDesc1_0") + " ";
                desc += stringsAndOtherData.getPotionEffectTypeString(4, p);
                lores_line2[special_index] = desc;
                break;
              case 4:
                desc = lang.getString("alchemyPerkDesc1_0") + " ";
                desc += stringsAndOtherData.getPotionEffectTypeString(5, p);
                lores_line2[special_index] = desc;
                break;
              default:
                break;
            }
            switch (skill_1a_level) {
              case 0:
                desc = lang.getString("alchemyPerkDesc0_0") + " ";
                desc += stringsAndOtherData.getPotionTypeString(1, p);
                lores_line2[0] = desc;
                break;
              case 1:
                desc = lang.getString("alchemyPerkDesc0_0") + " ";
                desc += stringsAndOtherData.getPotionTypeString(2, p);
                lores_line2[0] = desc;
                break;
              case 2:
                desc = lang.getString("alchemyPerkDesc0_0") + " ";
                desc += stringsAndOtherData.getPotionTypeString(3, p);
                lores_line2[0] = desc;
                break;
              case 3:
                desc = lang.getString("alchemyPerkDesc0_0") + " ";
                desc += stringsAndOtherData.getPotionTypeString(4, p);
                lores_line2[0] = desc;
                break;
              case 4:
                desc = lang.getString("alchemyPerkDesc0_0") + " ";
                desc += stringsAndOtherData.getPotionTypeString(5, p);
                lores_line2[0] = desc;
                break;
              default:
                break;
            }
            break;
          case "enchanting":
            special_index = 1;
            desc = lang.getString("enchantingPerkDesc1_0") + " ";
            StringsAndOtherData stringsAndOtherData1 = new StringsAndOtherData();
            switch (skill_2a_level) {
              case 0:
                desc += stringsAndOtherData1.getEnchantmentPerkDescString(1, p);
                lores_line2[special_index] = desc;
                break;
              case 1:
                desc += stringsAndOtherData1.getEnchantmentPerkDescString(2, p);
                lores_line2[special_index] = desc;
                break;
              case 2:
                desc += stringsAndOtherData1.getEnchantmentPerkDescString(3, p);
                lores_line2[special_index] = desc;
                break;
              case 3:
                desc += stringsAndOtherData1.getEnchantmentPerkDescString(4, p);
                lores_line2[special_index] = desc;
                break;
              case 4:
                desc += stringsAndOtherData1.getEnchantmentPerkDescString(5, p);
                lores_line2[special_index] = desc;
                break;
              default:
                break;
            }
            break;
          case "repair":
            special_index = 0;
            switch (skill_1a_level) {
              case 0:
              case 1:
              case 2:
              case 3:
                desc += lang.getString("repairPerkDesc0_1");
                lores_line2[special_index] = desc;
                break;
              case 4:
                desc += lang.getString("repairPerkDesc0_2");
                lores_line2[special_index] = desc;
                break;
              default:
                break;
            }
            break;
        }
        String[] lores_line1 = {"Level: 0/5", "Level: 0/5", "Level: 0/1"};
        String level = pStats.get(7).toString();
        lores_line1[0] =
            ChatColor.GRAY + lang.getString("level") + " " + ChatColor.GREEN + level + "/5";
        level = pStats.get(9).toString();
        lores_line1[1] =
            ChatColor.GRAY + lang.getString("level") + " " + ChatColor.GREEN + level + "/5";
        level = pStats.get(13).toString();
        lores_line1[2] =
            ChatColor.GRAY + lang.getString("level") + " " + ChatColor.DARK_PURPLE + level + "/1";

        Integer[] indices = {20, 23, 26};
        //Set skills
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

        //Tokens, passives, and back button
        ItemStack skill_token = new ItemStack(Material.GOLD_NUGGET);
        ItemStack passive_1 = new ItemStack(Material.RED_DYE, 1);
        ItemStack back_button = new ItemStack(Material.ARROW);

        ItemStack[] menu_items_2 = {back_button, skill_token, passive_1};
        String[] labels_2 = passivePerksMap.get(skillName);
        String[] lores_line1_2 = {"", "Total: 0", "Chance: 0 %"};
        String[] lores_line2_2 = passiveDescriptionsMap.get(skillName);

        //Total skill tokens
        lores_line1_2[1] =
            ChatColor.BLUE + lang.getString("total") + ": " + ChatColor.GOLD + String.valueOf(
                tokens_S);
        if (tokens_S > 1 && tokens_S < 64) {
          skill_token.setAmount(tokens_S);
        } else if (tokens_S >= 64) {
          skill_token.setAmount(64);
        }

        double passive = passive1.doubleValue();
        switch (skillName) {
          case "repair":
            //passive = Math.round(passive*1000)/1000.0d;
            lores_line1_2[2] =
                ChatColor.GRAY + lang.getString("level") + ": " + ChatColor.AQUA + String.valueOf(
                    passive);
            break;
          case "agility":
            passive = passive * 0.05;
            passive = Math.round(passive * 1000) / 1000.0d;
            lores_line1_2[2] = ChatColor.GRAY + lang.getString("likelihood") + ": " + ChatColor.AQUA
                + String.valueOf(passive) + "%";
            break;
          case "enchanting":
            passive = passive * 0.2;
            passive = Math.round(passive * 1000) / 1000.0d;
            lores_line1_2[2] =
                ChatColor.GRAY + lang.getString("xpBoost") + ": " + ChatColor.AQUA + "+"
                    + String.valueOf(passive) + "%";
            break;
          case "smelting":
            passive = passive * 0.2;
            passive = Math.round(passive * 1000) / 1000.0d;
            lores_line1_2[2] =
                ChatColor.GRAY + lang.getString("speedBoost") + ": " + ChatColor.AQUA + "+"
                    + String.valueOf(passive) + "%";
            break;
          case "building":
            lores_line1_2[2] =
                ChatColor.GRAY + lang.getString("buildingReachLabel") + ": " + ChatColor.AQUA
                    + BuildingMenu.reachLore(passive1.intValue(),
                    configLoad.getBuildingReachPerPoint(), configLoad.getBuildingReachCap())
                    + " " + lang.getString("buildingReachUnit");
            break;
          case "alchemy":
            passive = passive * 0.1;
            passive = Math.round(passive * 1000) / 1000.0d;
            lores_line1_2[2] =
                ChatColor.GRAY + lang.getString("timeExtension") + ": " + ChatColor.AQUA + "+"
                    + String.valueOf(passive) + "%";
            break;
          default:
            break;
        }

        Integer[] indices_2 = {45, 0, 18};
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
          ;
          meta.setLore(lore);
          menu_items_2[i].setItemMeta(meta);
          gui.setItem(indices_2[i], menu_items_2[i]);
        }

        //Crafting
        if (skillName.equalsIgnoreCase("enchanting")) {
          Integer[] indices_crafting = {39, 40, 41, 42, 43, 48, 49, 50, 51, 52};
          ItemStack[] crafting = {new ItemStack(Material.CRAFTING_TABLE),
              new ItemStack(Material.CRAFTING_TABLE),
              new ItemStack(Material.CRAFTING_TABLE), new ItemStack(Material.CRAFTING_TABLE),
              new ItemStack(Material.CRAFTING_TABLE), new ItemStack(Material.CRAFTING_TABLE),
              new ItemStack(Material.CRAFTING_TABLE), new ItemStack(Material.CRAFTING_TABLE),
              new ItemStack(Material.CRAFTING_TABLE), new ItemStack(Material.CRAFTING_TABLE)};
          String[] craftingNames = stringsAndOtherData.getEnchantingCraftingNames(p);
          int bookSmartLevel = (int) pStats.get(9);
          for (int i = 0; i < craftingNames.length; i++) {
            ArrayList<String> lore = new ArrayList<>();
            ItemMeta craftingMeta = crafting[i].getItemMeta();
            if (bookSmartLevel >= Math.ceil((double) (i + 1) / 2.0)) {
              lore.add(ChatColor.GREEN + ChatColor.ITALIC.toString() + lang.getString("unlocked"));
            } else {
              lore.add(ChatColor.RED + ChatColor.ITALIC.toString() + lang.getString("locked"));
            }
            craftingMeta.setDisplayName(ChatColor.BOLD + craftingNames[i]);
            craftingMeta.setLore(lore);
            crafting[i].setItemMeta(craftingMeta);
            gui.setItem(indices_crafting[i], crafting[i]);
          }
        } else if (skillName.equalsIgnoreCase("alchemy")) {
          Integer[] indices_crafting = {48, 49, 50, 51, 52};
          ItemStack[] crafting = {new ItemStack(Material.CRAFTING_TABLE),
              new ItemStack(Material.CRAFTING_TABLE),
              new ItemStack(Material.CRAFTING_TABLE), new ItemStack(Material.CRAFTING_TABLE),
              new ItemStack(Material.CRAFTING_TABLE)};
          String[] craftingNames = {stringsAndOtherData.getPotionTypeString(1, p),
              stringsAndOtherData.getPotionTypeString(2, p),
              stringsAndOtherData.getPotionTypeString(3, p),
              stringsAndOtherData.getPotionTypeString(4, p),
              stringsAndOtherData.getPotionTypeString(5, p)};
          int alchemicalSummoningLevel = (int) pStats.get(7);
          for (int i = 0; i < craftingNames.length; i++) {
            ArrayList<String> lore = new ArrayList<>();
            ItemMeta craftingMeta = crafting[i].getItemMeta();
            if (alchemicalSummoningLevel > i) {
              lore.add(ChatColor.GREEN + ChatColor.ITALIC.toString() + lang.getString("unlocked"));
            } else {
              lore.add(ChatColor.RED + ChatColor.ITALIC.toString() + lang.getString("locked"));
            }
            craftingMeta.setDisplayName(ChatColor.BOLD + craftingNames[i]);
            craftingMeta.setLore(lore);
            crafting[i].setItemMeta(craftingMeta);
            gui.setItem(indices_crafting[i], crafting[i]);
          }
          Integer[] indices_brewing = {39, 40, 41, 42, 43};
          ItemStack[] brewing = {new ItemStack(Material.IRON_BARS),
              new ItemStack(Material.IRON_BARS),
              new ItemStack(Material.IRON_BARS), new ItemStack(Material.IRON_BARS),
              new ItemStack(Material.IRON_BARS)};
          ItemGroups itemGroups = new ItemGroups();
          List<Material> brewingUnlocked = itemGroups.getNewIngredients();
          int ancientKnowledgeLevel = (int) pStats.get(9);
          for (int i = 0; i < brewing.length; i++) {
            String lvl = String.valueOf(i + 1);
            ArrayList<String> lore = new ArrayList<>();
            ItemStack brewingItem = brewing[i];
            ItemMeta brewingMeta = brewingItem.getItemMeta();
            brewingMeta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
            brewingMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            brewingMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            String brewingName =
                lang.getString("alchemyPerkTitle1") + " " + lang.getString("lvl") + " " + lvl + " "
                    + lang.getString("ingredient");
            if (ancientKnowledgeLevel > i) {
              brewingItem.setType(brewingUnlocked.get(i));
              lore.add(ChatColor.GRAY + ChatColor.ITALIC.toString() + lang.getString("usedToBrew"));
              lore.add(ChatColor.GRAY + stringsAndOtherData.getPotionEffectTypeString(i + 1, p));
            } else {
              lore.add(ChatColor.RED + ChatColor.ITALIC.toString() + lang.getString("locked"));
            }
            brewingMeta.setDisplayName(ChatColor.WHITE + ChatColor.BOLD.toString() + brewingName);
            brewingMeta.setLore(lore);
            brewingItem.setItemMeta(brewingMeta);
            gui.setItem(indices_brewing[i], brewingItem);

          }
        }

        //Souls (refunding)
        ItemStack soul = new ItemStack(Material.COMPOSTER);
        ItemMeta soulMeta = soul.getItemMeta();
        ArrayList<String> soulLore = new ArrayList<>();
        soulMeta.setDisplayName(ChatColor.BOLD + lang.getString("refundSkillTitle"));
        if ((int) pStatAll.get("global").get(9) < 1) {
          soulLore.add(ChatColor.RED + ChatColor.ITALIC.toString() + lang.getString("locked"));
        } else {
          int souls = (int) pStatAll.get("global").get(20);
          String soulsString = lang.getString("souls");
          String soulsCapitilized = UtilityMethods.capitalizeString(soulsString);
          soulLore.add(
              soulsCapitilized + ": " + ChatColor.AQUA + ChatColor.ITALIC.toString() + souls + "/"
                  + refundCost);
          soulLore.add(
              ChatColor.GRAY + ChatColor.ITALIC.toString() + lang.getString("refundSkillTreeDesc"));
          soulLore.add(ChatColor.GRAY + ChatColor.ITALIC.toString() + "(-" + refundCost + " "
              + lang.getString("souls") + ")");
        }
        soulMeta.setLore(soulLore);
        soul.setItemMeta(soulMeta);
        gui.setItem(47, soul);

        ItemStack configItem = new ItemStack(Material.REDSTONE);
        ItemMeta configItemMeta = configItem.getItemMeta();
        ArrayList<String> configItemLore = new ArrayList<>();
        configItemMeta.setDisplayName(ChatColor.BOLD + lang.getString("configuration"));
        configItemLore.addAll(
            stringsAndOtherData.getStringLines(lang.getString("skillConfigDesc")));
        configItemMeta.setLore(configItemLore);
        configItem.setItemMeta(configItemMeta);
        gui.setItem(53, configItem);

        //Connectors
        ItemStack connector = new ItemStack(Material.GLASS_PANE);
        ItemMeta connectorMeta = connector.getItemMeta();
        connectorMeta.setDisplayName(ChatColor.WHITE.toString());
        connector.setItemMeta(connectorMeta);
        Integer[] indices_3 = {21, 22, 24, 25};
        for (int i = 0; i < indices_3.length; i++) {
          gui.setItem(indices_3[i], connector);
        }
        //Put the items in the inventory
        p.openInventory(gui);
  }
}
