package mc.carlton.freerpg.commands.sub.menu;

import java.util.ArrayList;
import java.util.Map;
import mc.carlton.freerpg.config.ConfigLoad;
import mc.carlton.freerpg.customContainers.collections.OldCustomRecipe;
import mc.carlton.freerpg.utils.UtilityMethods;
import mc.carlton.freerpg.utils.game.LanguageSelector;
import mc.carlton.freerpg.utils.globalVariables.StringsAndOtherData;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/** Skill tree menu of the 10 main skills (moved verbatim from FrpgCommands). */
public class MainSkillTreeMenu implements SkillTreeMenu {

  @Override
  public void open(Player p, String skillName, Inventory gui,
      Map<String, ArrayList<Number>> pStatAll, Map<String, String[]> perksMap,
      Map<String, String[]> descriptionsMap, Map<String, String[]> passivePerksMap,
      Map<String, String[]> passiveDescriptionsMap, LanguageSelector lang, ConfigLoad configLoad,
      StringsAndOtherData stringsAndOtherData, String refundCost) {
        //Sets colors of the skill icons to indicate whether the perk is locked/unlocked/in progress/completed
        //setSkillTreeProgressMainSkill(p,skillName,gui);

        ArrayList<Number> pStats = pStatAll.get(skillName);
        int skill_1a_level = (Integer) pStats.get(7);
        int skill_1b_level = (Integer) pStats.get(8);
        int skill_2a_level = (Integer) pStats.get(9);
        int skill_2b_level = (Integer) pStats.get(10);
        int skill_3a_level = (Integer) pStats.get(11);
        int skill_3b_level = (Integer) pStats.get(12);
        int skill_M_level = (Integer) pStats.get(13);

        int tokens_P = (Integer) pStats.get(2);
        int tokens_S = (Integer) pStats.get(3);
        Number passive1 = pStats.get(4);
        Number passive2 = pStats.get(5);
        Number passive3 = pStats.get(6);

        ItemStack skill_1a = new ItemStack(Material.PINK_TERRACOTTA);
        ItemStack skill_2a = new ItemStack(Material.RED_TERRACOTTA);
        ItemStack skill_3a = new ItemStack(Material.RED_TERRACOTTA);
        ItemStack skill_1b = new ItemStack(Material.PINK_TERRACOTTA);
        ItemStack skill_2b = new ItemStack(Material.RED_TERRACOTTA);
        ItemStack skill_3b = new ItemStack(Material.RED_TERRACOTTA);
        ItemStack skill_M = new ItemStack(Material.RED_TERRACOTTA);

        if (skill_1a_level == 0) {
          skill_1a.setType(Material.PINK_TERRACOTTA);
        } else if (skill_1a_level > 0 && skill_1a_level < 5) {
          skill_1a.setType(Material.YELLOW_TERRACOTTA);
        } else {
          skill_1a.setType(Material.GREEN_TERRACOTTA);
        }

        if (skill_1b_level == 0) {
          skill_1b.setType(Material.PINK_TERRACOTTA);
        } else if (skill_1b_level > 0 && skill_1b_level < 5) {
          skill_1b.setType(Material.YELLOW_TERRACOTTA);
        } else {
          skill_1b.setType(Material.GREEN_TERRACOTTA);
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

        if (skill_2b_level == 0) {
          if (skill_1b_level >= 2) {
            skill_2b.setType(Material.PINK_TERRACOTTA);
          }
        } else if (skill_2b_level > 0 && skill_2b_level < 5) {
          skill_2b.setType(Material.YELLOW_TERRACOTTA);
        } else {
          skill_2b.setType(Material.GREEN_TERRACOTTA);
        }

        if (skill_3a_level == 0) {
          if (skill_2a_level >= 2) {
            skill_3a.setType(Material.PINK_TERRACOTTA);
          }
        } else {
          skill_3a.setType(Material.GREEN_TERRACOTTA);
        }

        if (skill_3b_level == 0) {
          if (skill_2b_level >= 2) {
            skill_3b.setType(Material.PINK_TERRACOTTA);
          }
        } else {
          skill_3b.setType(Material.GREEN_TERRACOTTA);
        }

        if (skill_M_level == 0) {
          if (skill_1a_level + skill_1b_level + skill_2a_level + skill_2b_level + skill_3a_level
              + skill_3b_level >= 10) {
            skill_M.setType(Material.PINK_TERRACOTTA);
          }
        } else {
          skill_M.setType(Material.GREEN_TERRACOTTA);
        }

        //Beginning state of the menu
        ItemStack[] menu_items = {skill_1a, skill_1b, skill_2a, skill_2b, skill_3a, skill_3b,
            skill_M};
        String[] labels = perksMap.get(skillName);
        String[] lores_line2 = descriptionsMap.get(skillName);

        //Initialize some varibales for use
        String desc = "";
        Map<String, OldCustomRecipe> customRecipeMap = configLoad.getCraftingRecipes();

        //Make changes to some skills independent of level
        if (skillName.equalsIgnoreCase("woodcutting")) {
          ArrayList<Integer> timberBreakLimits = configLoad.getTimberBreakLimits();
          String timberBreakLimitInitial = String.valueOf(timberBreakLimits.get(0));
          String timberBreakLimitUpgraded = String.valueOf(timberBreakLimits.get(1));
          String newLore0 = stringsAndOtherData.replaceIfPresent(lores_line2[4], "64",
              timberBreakLimitInitial);
          String newLore1 = stringsAndOtherData.replaceIfPresent(newLore0, "128",
              timberBreakLimitUpgraded);
          lores_line2[4] = newLore1;
        }

        int special_index = 0;
        switch (skillName) {
          case "mining":
            special_index = 0;
            switch (skill_1a_level) {
              case 0:
                desc = lang.getString("miningPerkDesc0_1");
                lores_line2[special_index] = desc;
                break;
              case 1:
                desc = lang.getString("miningPerkDesc0_2");
                lores_line2[special_index] = desc;
                break;
              case 2:
                desc = lang.getString("miningPerkDesc0_3");
                lores_line2[special_index] = desc;
                break;
              case 3:
                desc = lang.getString("miningPerkDesc0_4");
                lores_line2[special_index] = desc;
                break;
              case 4:
                desc = lang.getString("miningPerkDesc0_5");
                lores_line2[special_index] = desc;
                break;
              default:
                break;
            }
            break;
          case "woodcutting":
            special_index = 3;
            desc = lang.getString("woodcuttingPerkDesc3_1") + " ";
            ArrayList<Object> woodcuttingInfo = configLoad.getWoodcuttingInfo();
            switch (skill_2b_level) {
              case 0:
                Material mat0 = (Material) woodcuttingInfo.get(0);
                String itemName0 = "";
                if (mat0 != null) {
                  itemName0 = mat0.toString().replaceAll("_", " ").toLowerCase();
                } else {
                  itemName0 = "feather";
                }
                desc += itemName0;
                lores_line2[special_index] = desc;
                break;
              case 1:
                Material mat1 = (Material) woodcuttingInfo.get(3);
                String itemName1 = "";
                if (mat1 != null) {
                  itemName1 = mat1.toString().replaceAll("_", " ").toLowerCase();
                } else {
                  itemName1 = "gold nugget";
                }
                desc += itemName1;
                lores_line2[special_index] = desc;
                break;
              case 2:
                Material mat2 = (Material) woodcuttingInfo.get(6);
                String itemName2 = "";
                if (mat2 != null) {
                  itemName2 = mat2.toString().replaceAll("_", " ").toLowerCase();
                } else {
                  itemName2 = "golden apple";
                }
                desc += itemName2;
                lores_line2[special_index] = desc;
                break;
              case 3:
                Material mat3 = (Material) woodcuttingInfo.get(9);
                String itemName3 = "";
                if (mat3 != null) {
                  itemName3 = mat3.toString().replaceAll("_", " ").toLowerCase();
                } else {
                  itemName3 = "experience bottle";
                }
                desc += itemName3;
                lores_line2[special_index] = desc;
                break;
              case 4:
                Material mat4 = (Material) woodcuttingInfo.get(12);
                String itemName4 = "";
                if (mat4 != null) {
                  itemName4 = mat4.toString().replaceAll("_", " ").toLowerCase();
                } else {
                  itemName4 = "enchanted golden apple";
                }
                desc += itemName4;
                lores_line2[special_index] = desc;
                break;
              default:
                break;
            }
            break;
          case "fishing":
            special_index = 1;
            desc = lang.getString("fishingPerkDesc1_1") + " ";
            switch (skill_1b_level) {
              case 0:
                desc += "I (" + lang.getString("common") + ")";
                lores_line2[special_index] = desc;
                break;
              case 1:
                desc += "II (" + lang.getString("uncommon") + ")";
                lores_line2[special_index] = desc;
                break;
              case 2:
                desc += "III (" + lang.getString("rare") + ")";
                lores_line2[special_index] = desc;
                break;
              case 3:
                desc += "IV (" + lang.getString("veryRare") + ")";
                lores_line2[special_index] = desc;
                break;
              case 4:
                desc += "V (" + lang.getString("legendary") + ")";
                lores_line2[special_index] = desc;
                break;
              default:
                break;
            }
            break;
          case "farming":
            special_index = 1;
            desc = lang.getString("farmingPerkDesc1_1") + " ";
            switch (skill_1b_level) {
              case 0:
                Material output1 = customRecipeMap.get("farming" + 1).getOutput();
                if (output1.equals(Material.COW_SPAWN_EGG)) {
                  desc += lang.getString("cowSpawnEgg");
                } else {
                  desc += stringsAndOtherData.cleanUpTitleString(output1.toString());
                }
                lores_line2[special_index] = desc;
                break;
              case 1:
                Material output2 = customRecipeMap.get("farming" + 2).getOutput();
                if (output2.equals(Material.BEE_SPAWN_EGG)) {
                  desc += lang.getString("beeSpawnEgg");
                } else {
                  desc += stringsAndOtherData.cleanUpTitleString(output2.toString());
                }
                lores_line2[special_index] = desc;
                break;
              case 2:
                Material output3 = customRecipeMap.get("farming" + 3).getOutput();
                if (output3.equals(Material.MOOSHROOM_SPAWN_EGG)) {
                  desc += lang.getString("mooshroomSpawnEgg");
                } else {
                  desc += stringsAndOtherData.cleanUpTitleString(output3.toString());
                }
                lores_line2[special_index] = desc;
                break;
              case 3:
                Material output4 = customRecipeMap.get("farming" + 4).getOutput();
                if (output4.equals(Material.HORSE_SPAWN_EGG)) {
                  desc += lang.getString("horseSpawnEgg");
                } else {
                  desc += stringsAndOtherData.cleanUpTitleString(output4.toString());
                }
                lores_line2[special_index] = desc;
                break;
              case 4:
                Material output5 = customRecipeMap.get("farming" + 5).getOutput();
                if (output5.equals(Material.SLIME_SPAWN_EGG)) {
                  desc += lang.getString("slimeSpawnEgg");
                } else {
                  desc += stringsAndOtherData.cleanUpTitleString(output5.toString());
                }
                lores_line2[special_index] = desc;
                break;
              default:
                break;
            }
            break;
          case "digging":
            special_index = 0;
            ArrayList<Object> diggingInfo = configLoad.getDiggingInfo();
            switch (skill_1a_level) {
              case 0:
                desc = lang.getString("diggingPerkDesc0_1") + " ";
                Material mat0 = (Material) diggingInfo.get(10);
                String itemName0 = "";
                if (mat0 != null) {
                  itemName0 = mat0.toString().replaceAll("_", " ").toLowerCase();
                } else {
                  itemName0 = "gold ingot";
                }
                desc += itemName0;
                lores_line2[special_index] = desc;
                break;
              case 1:
                desc = lang.getString("diggingPerkDesc0_1") + " ";
                Material mat1 = (Material) diggingInfo.get(12);
                String itemName1 = "";
                if (mat1 != null) {
                  itemName1 = mat1.toString().replaceAll("_", " ").toLowerCase();
                } else {
                  itemName1 = "name tag";
                }
                desc += itemName1;
                lores_line2[special_index] = desc;
                break;
              case 2:
                desc = lang.getString("diggingPerkDesc0_1") + " ";
                Material mat2 = (Material) diggingInfo.get(14);
                String itemName2 = "";
                if (mat2 != null) {
                  itemName2 = mat2.toString().replaceAll("_", " ").toLowerCase();
                } else {
                  itemName2 = "music discs";
                }
                desc += itemName2;
                lores_line2[special_index] = desc;
                break;
              case 3:
                desc = lang.getString("diggingPerkDesc0_1") + " ";
                Material mat3 = (Material) diggingInfo.get(16);
                String itemName3 = "";
                if (mat3 != null) {
                  itemName3 = mat3.toString().replaceAll("_", " ").toLowerCase();
                } else {
                  itemName3 = "horse armor";
                }
                desc += itemName3;
                lores_line2[special_index] = desc;
                break;
              case 4:
                desc = lang.getString("diggingPerkDesc0_1") + " ";
                Material mat4 = (Material) diggingInfo.get(18);
                String itemName4 = "";
                if (mat4 != null) {
                  itemName4 = mat4.toString().replaceAll("_", " ").toLowerCase();
                } else {
                  itemName4 = "diamond";
                }
                desc += itemName4;
                lores_line2[special_index] = desc;
                break;
              default:
                break;
            }
            switch (skill_2a_level) {
              case 0:
                desc = lang.getString("diggingPerkDesc0_1") + " ";
                Material mat5 = (Material) diggingInfo.get(21);
                String itemName5 = "";
                if (mat5 != null) {
                  itemName5 = mat5.toString().replaceAll("_", " ").toLowerCase();
                } else {
                  itemName5 = "emerald";
                }
                desc += itemName5;
                lores_line2[2] = desc;
                break;
              case 1:
                desc = lang.getString("diggingPerkDesc0_1") + " ";
                Material mat6 = (Material) diggingInfo.get(24);
                String itemName6 = "";
                if (mat6 != null) {
                  itemName6 = mat6.toString().replaceAll("_", " ").toLowerCase();
                } else {
                  itemName6 = "enchanted book";
                }
                desc += itemName6;
                lores_line2[2] = desc;
                break;
              case 2:
                desc = lang.getString("diggingPerkDesc0_1") + " ";
                Material mat7 = (Material) diggingInfo.get(27);
                String itemName7 = "";
                if (mat7 != null) {
                  itemName7 = mat7.toString().replaceAll("_", " ").toLowerCase();
                } else {
                  itemName7 = "dragon breath";
                }
                desc += itemName7;
                lores_line2[2] = desc;
                break;
              case 3:
                desc = lang.getString("diggingPerkDesc0_1") + " ";
                Material mat8 = (Material) diggingInfo.get(30);
                String itemName8 = "";
                if (mat8 != null) {
                  itemName8 = mat8.toString().replaceAll("_", " ").toLowerCase();
                } else {
                  itemName8 = "totem of undying";
                }
                desc += itemName8;
                lores_line2[2] = desc;
                break;
              case 4:
                desc = lang.getString("diggingPerkDesc0_1") + " ";
                Material mat9 = (Material) diggingInfo.get(30);
                String itemName9 = "";
                if (mat9 != null) {
                  itemName9 = mat9.toString().replaceAll("_", " ").toLowerCase();
                } else {
                  itemName9 = "nether star";
                }
                desc += itemName9;
                lores_line2[2] = desc;
                break;
              default:
                break;
            }
            break;
          case "defense":
            special_index = 0;
            switch (skill_1a_level) {
              case 0:
              case 2:
              case 1:
                desc = lang.getString("defensePerkDesc0_1");
                lores_line2[special_index] = desc;
                break;
              case 3:
              case 4:
                desc = lang.getString("defensePerkDesc0_2");
                lores_line2[special_index] = desc;
                break;
              default:
                break;
            }
            break;
          default:
            break;
        }

        String[] lores_line1 = {"Level: 0/5", "Level: 0/5", "Level: 0/5", "Level: 0/5",
            "Level: 0/1", "Level: 0/1", "Level: 0/1"}; //Data lines 7-13
        for (int i = 0; i < 4; i++) {
          String level = pStats.get(7 + i).toString();
          lores_line1[i] =
              ChatColor.GRAY + lang.getString("level") + " " + ChatColor.GREEN + level + "/5";
        }
        for (int i = 0; i < 3; i++) {
          String level = pStats.get(11 + i).toString();
          if (i != 2) {
            lores_line1[i + 4] =
                ChatColor.GRAY + lang.getString("level") + " " + ChatColor.BLUE + level + "/1";
          } else {
            lores_line1[i + 4] =
                ChatColor.GRAY + lang.getString("level") + " " + ChatColor.DARK_PURPLE + level
                    + "/1";
          }
        }

        Integer[] indices = {11, 29, 13, 31, 7, 43, 26};
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
        //Tokens, passives, and back button
        ItemStack passive_token = new ItemStack(Material.IRON_NUGGET);
        ItemStack skill_token = new ItemStack(Material.GOLD_NUGGET);
        ItemStack passive_1 = new ItemStack(Material.RED_DYE, 1);
        ItemStack passive_2 = new ItemStack(Material.GREEN_DYE, 2);
        ItemStack passive_3 = new ItemStack(Material.BLUE_DYE, 3);
        ItemStack back_button = new ItemStack(Material.ARROW);

        ItemStack[] menu_items_2 = {passive_token, back_button, skill_token, passive_1, passive_2,
            passive_3};
        String[] labels_2 = passivePerksMap.get(skillName);
        String[] lores_line1_2 = {"Total: 0", "", "Total: 0", "Duration: 1 s", "Percentage: 0%",
            "Percentage: 0%"};
        String[] lores_line2_2 = passiveDescriptionsMap.get(skillName);

        //Total Passive Tokens
        lores_line1_2[0] =
            ChatColor.BLUE + lang.getString("total") + ": " + ChatColor.GOLD + String.valueOf(
                tokens_P);
        if (tokens_P > 1 && tokens_P < 64) {
          passive_token.setAmount(tokens_P);
        } else if (tokens_P >= 64) {
          passive_token.setAmount(64);
        }

        //Total skill tokens
        lores_line1_2[2] =
            ChatColor.BLUE + lang.getString("total") + ": " + ChatColor.GOLD + String.valueOf(
                tokens_S);
        if (tokens_S > 1 && tokens_S < 64) {
          skill_token.setAmount(tokens_S);
        } else if (tokens_S >= 64) {
          skill_token.setAmount(64);
        }

        double duration = passive1.doubleValue();
        double chance1 = passive2.doubleValue();
        double chance2 = passive3.doubleValue();
        //Passive 1
        duration = duration * 0.02 + 2;
        duration = Math.round(duration * 1000) / 1000.0d;
        lores_line1_2[3] =
            ChatColor.GRAY + lang.getString("duration") + ": " + ChatColor.AQUA + String.valueOf(
                duration) + " s";
        switch (skillName) {
          case "digging":
            //Passive2
            chance1 = 1 + chance1 * 0.01;
            chance1 = Math.round(chance1 * 1000) / 1000.0d;
            lores_line1_2[4] = ChatColor.GRAY + lang.getString("likelihood") + ": " + ChatColor.AQUA
                + String.valueOf(chance1) + "%";

            //Passive 3
            break;
          case "woodcutting":
          case "archery":
            //Passive 2
            chance1 = chance1 * 0.05;
            chance1 = Math.round(chance1 * 1000) / 1000.0d;
            lores_line1_2[4] = ChatColor.GRAY + lang.getString("likelihood") + ": " + ChatColor.AQUA
                + String.valueOf(chance1) + "%";

            //Passive 3
            break;

          //Passive 3
          case "mining":
            //Passive 2
            chance1 = chance1 * 0.05;
            chance1 = Math.round(chance1 * 1000) / 1000.0d;
            lores_line1_2[4] = ChatColor.GRAY + lang.getString("likelihood") + ": " + ChatColor.AQUA
                + String.valueOf(chance1) + "%";

            //Passive 3
            chance2 = chance2 * 0.01;
            chance2 = Math.round(chance2 * 1000) / 1000.0d;
            lores_line1_2[5] = ChatColor.GRAY + lang.getString("likelihood") + ": " + ChatColor.AQUA
                + String.valueOf(chance2) + "%";
            break;
          case "farming":
            //Passive 2
            chance1 = chance1 * 0.05;
            chance1 = Math.round(chance1 * 1000) / 1000.0d;
            lores_line1_2[4] = ChatColor.GRAY + lang.getString("likelihood") + ": " + ChatColor.AQUA
                + String.valueOf(chance1) + "%";

            //Passive 3
            chance2 = chance2 * 0.05;
            chance2 = Math.round(chance2 * 1000) / 1000.0d;
            lores_line1_2[5] = ChatColor.GRAY + lang.getString("likelihood") + ": " + ChatColor.AQUA
                + String.valueOf(chance2) + "%";
            break;
          case "fishing":
            //Passive 1
            duration = duration / 2.0;
            duration = Math.round(duration * 1000) / 1000.0d;
            lores_line1_2[3] = ChatColor.GRAY + lang.getString("duration") + ": " + ChatColor.AQUA
                + String.valueOf(duration) + " s";

            //Passive 2
            chance1 = chance1 * 0.05;
            chance1 = Math.round(chance1 * 1000) / 1000.0d;
            lores_line1_2[4] = ChatColor.GRAY + lang.getString("likelihood") + ": " + ChatColor.AQUA
                + String.valueOf(chance1) + "%";

            //Passive 3
            chance2 = 10 - chance2 * 0.005;
            chance2 = Math.round(chance2 * 1000) / 1000.0d;
            lores_line1_2[5] = ChatColor.GRAY + lang.getString("junkChance") + ": " + ChatColor.AQUA
                + String.valueOf(chance2) + "%";
            break;

          //Passive 3
          case "beastMastery":
            //Passive 2
            chance1 = chance1 * 0.025;
            chance1 = Math.round(chance1 * 1000) / 1000.0d;
            lores_line1_2[4] = ChatColor.GRAY + lang.getString("likelihood") + ": " + ChatColor.AQUA
                + String.valueOf(chance1) + "%";

            //Passive 3
            break;
          case "swordsmanship":
            //Passive 2
            chance1 = chance1 * 0.02;
            chance1 = Math.round(chance1 * 1000) / 1000.0d;
            lores_line1_2[4] = ChatColor.GRAY + lang.getString("likelihood") + ": " + ChatColor.AQUA
                + String.valueOf(chance1) + "%";

            //Passive 3
            break;
          case "defense":
            //Passive 2
            chance1 = 1 + chance1 * 0.01;
            chance1 = Math.round(chance1 * 1000) / 1000.0d;
            lores_line1_2[4] = ChatColor.GRAY + lang.getString("likelihood") + ": " + ChatColor.AQUA
                + String.valueOf(chance1) + "%";

            //Passive 3
            chance2 = chance2 * 0.05;
            chance2 = Math.round(chance2 * 1000) / 1000.0d;
            lores_line1_2[5] = ChatColor.GRAY + lang.getString("likelihood") + ": " + ChatColor.AQUA
                + String.valueOf(chance2) + "%";

            break;

          case "axeMastery":
            //Passive 2
            chance1 = chance1 * 0.01;
            chance1 = Math.round(chance1 * 1000) / 1000.0d;
            lores_line1_2[4] = ChatColor.GRAY + lang.getString("likelihood") + ": " + ChatColor.AQUA
                + String.valueOf(chance1) + "%";

            //Passive 3
            break;

          //Passive 3
          default:
            break;
        }

        Integer[] indices_2 = {0, 45, 9, 18, 27, 36};
        for (int i = 0; i < labels_2.length; i++) {
          ItemMeta meta = menu_items_2[i].getItemMeta();
          meta.setDisplayName(ChatColor.BOLD + labels_2[i]);
          ArrayList<String> lore = new ArrayList<>();
          String longString = lores_line2_2[i];
          ArrayList<String> splitDescs = stringsAndOtherData.getStringLines(longString);

          if (i == 3) {
            splitDescs.add("");
            splitDescs.add(ChatColor.UNDERLINE + lang.getString("abilityDescription"));
            ArrayList<String> appendingDesc = stringsAndOtherData.getStringLines(
                lang.getString("abilityDescription_" + skillName));
            splitDescs.addAll(appendingDesc);
          }
          lore.add(lores_line1_2[i]);
          for (int j = 0; j < splitDescs.size(); j++) {
            lore.add(ChatColor.GRAY + ChatColor.ITALIC.toString() + splitDescs.get(j));
          }
          meta.setLore(lore);
          menu_items_2[i].setItemMeta(meta);
          gui.setItem(indices_2[i], menu_items_2[i]);
        }

        if (skillName.equalsIgnoreCase("farming")) {
          Integer[] indices_crafting = {48, 49, 50, 51, 52};
          ItemStack[] crafting = {new ItemStack(Material.CRAFTING_TABLE),
              new ItemStack(Material.CRAFTING_TABLE),
              new ItemStack(Material.CRAFTING_TABLE), new ItemStack(Material.CRAFTING_TABLE),
              new ItemStack(Material.CRAFTING_TABLE)};
          String[] craftingNames = {lang.getString("cowEgg"), lang.getString("beeEgg"),
              lang.getString("mooshroomEgg"), lang.getString("horseEgg"),
              lang.getString("slimeEgg")};
          Material[] defaultMaterials = {Material.COW_SPAWN_EGG, Material.BEE_SPAWN_EGG,
              Material.MOOSHROOM_SPAWN_EGG, Material.HORSE_SPAWN_EGG, Material.SLIME_SPAWN_EGG};
          for (int i = 0; i < craftingNames.length; i++) {
            int stringIndex = i + 1;
            Material output = customRecipeMap.get("farming" + stringIndex).getOutput();
            if (!output.equals(defaultMaterials[i])) {
              craftingNames[i] = stringsAndOtherData.cleanUpTitleString(output.toString());
            }
          }
          int animalFarmLevel = (int) pStats.get(8);
          for (int i = 0; i < craftingNames.length; i++) {
            ArrayList<String> lore = new ArrayList<>();
            ItemMeta craftingMeta = crafting[i].getItemMeta();
            if (animalFarmLevel >= i + 1) {
              lore.add(ChatColor.GREEN + ChatColor.ITALIC.toString() + lang.getString("unlocked"));
            } else {
              lore.add(ChatColor.RED + ChatColor.ITALIC.toString() + lang.getString("locked"));
            }
            craftingMeta.setDisplayName(ChatColor.BOLD + craftingNames[i]);
            craftingMeta.setLore(lore);
            crafting[i].setItemMeta(craftingMeta);
            gui.setItem(indices_crafting[i], crafting[i]);
          }
        } else if (skillName.equalsIgnoreCase("archery")) {
          Integer[] indices_crafting = {48, 49, 50, 51, 52};
          ItemStack[] crafting = {new ItemStack(Material.CRAFTING_TABLE)};
          String[] craftingNames = {lang.getString("tippedArrows")};
          int dragonlessArrowsLevel = (int) pStats.get(11);
          for (int i = 0; i < craftingNames.length; i++) {
            ArrayList<String> lore = new ArrayList<>();
            ItemMeta craftingMeta = crafting[i].getItemMeta();
            if (dragonlessArrowsLevel >= 1) {
              lore.add(ChatColor.GREEN + ChatColor.ITALIC.toString() + lang.getString("unlocked"));
            } else {
              lore.add(ChatColor.RED + ChatColor.ITALIC.toString() + lang.getString("locked"));
            }
            craftingMeta.setDisplayName(ChatColor.BOLD + craftingNames[i]);
            craftingMeta.setLore(lore);
            crafting[i].setItemMeta(craftingMeta);
            gui.setItem(indices_crafting[i], crafting[i]);
          }
        }

        //Soul Bucket (Refunding)
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

        //Configuration Menu:
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
        Integer[] indices_3 = {6, 12, 14, 24, 25, 30, 32, 42};
        for (int i = 0; i < indices_3.length; i++) {
          gui.setItem(indices_3[i], connector);
        }
        //Put the items in the inventory
        p.openInventory(gui);
  }
}
