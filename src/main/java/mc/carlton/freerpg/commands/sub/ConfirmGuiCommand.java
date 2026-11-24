package mc.carlton.freerpg.commands.sub;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import mc.carlton.freerpg.FreeRPG;
import mc.carlton.freerpg.commands.CommandHelper;
import mc.carlton.freerpg.config.ConfigLoad;
import mc.carlton.freerpg.skills.SkillRegistry;
import mc.carlton.freerpg.utils.UtilityMethods;
import mc.carlton.freerpg.utils.game.LanguageSelector;
import mc.carlton.freerpg.utils.globalVariables.StringsAndOtherData;
import mc.carlton.freerpg.utils.gui.GuiItem;
import mc.carlton.freerpg.utils.gui.GuiWrapper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /frpg confirmGUI (moved verbatim from FrpgCommands). */
public class ConfirmGuiCommand implements SubCommand {

  @Override
  public List<String> names() {
    return List.of("confirmGUI", "confirmationGUI");
  }

  @Override
  public boolean execute(CommandSender sender, String[] args) {
    final String IMPROPER_ARGUMENTS_MESSSAGE = " /frpg confirmationGUI [::skillName::]";
    CommandHelper commandHelper = new CommandHelper(sender, args, 2, IMPROPER_ARGUMENTS_MESSSAGE);
    commandHelper.setPlayerOnlyCommand(true);
    commandHelper.setCheckInBed(true);
    commandHelper.setPermissionName("confirmGUI");
    if (!commandHelper.isProperCommand()) {
      return true; //Command Restricted or Improper
    }
    Player p = (Player) sender;
    LanguageSelector lang = new LanguageSelector(p);
    String[] labels_0 = SkillRegistry.labelArrayWithGlobal();
    String[] titles_0 = new String[labels_0.length];
    for (int i = 0; i < labels_0.length; i++) {
      titles_0[i] = lang.getString(labels_0[i]);
    }
    List<String> labels = Arrays.asList(labels_0);
    String skillName = UtilityMethods.convertStringToListCasing(labels, args[1]);
    if (!labels.contains(skillName)) {
      commandHelper.sendImproperArgumentsMessage();
      return true;
    }
    String skillTitle = titles_0[labels.indexOf(skillName)];
    GuiWrapper gui = new GuiWrapper(p, Bukkit.createInventory(p, 54, "Confirmation Window"));

    //Load souls data
    ConfigLoad loadConfig = new ConfigLoad();
    ArrayList<Integer> soulsInfo = loadConfig.getSoulsInfo();
    String refundCost = Integer.toString(soulsInfo.get(1));

    //Information
    GuiItem info = new GuiItem(Material.PAPER, 22, gui);
    info.setNameColor(ChatColor.YELLOW.toString() + ChatColor.BOLD.toString());
    info.setName(lang.getString("warning"));
    //The following is pretty messy, but it's all just to format a long strong
    String partOne =
        lang.getString("refundSkillTree0") + " " + refundCost + " " + lang.getString("souls")
            + " " + lang.getString("refundSkillTree1");
    String partTwo = lang.getString("refundSkillTree2") + " " + ChatColor.WHITE.toString() +
        skillTitle + ChatColor.RESET.toString() + ChatColor.ITALIC.toString()
        + ChatColor.GRAY.toString() + " " + lang.getString("skill") + "?";
    String refundWarningMessage = partOne + " " + partTwo;
    StringsAndOtherData stringsAndOtherData = new StringsAndOtherData();
    for (String line : stringsAndOtherData.getStringLines(refundWarningMessage)) {
      info.addSpecialLoreLine(ChatColor.ITALIC + ChatColor.GRAY.toString() + line);
    }
    gui.addItem(info);

    //Yes Button
    GuiItem yes = new GuiItem(Material.LIME_TERRACOTTA, 38, gui);
    yes.setNameColor(ChatColor.GREEN.toString() + ChatColor.BOLD.toString());
    yes.setName(lang.getString("yes0"));
    gui.addItem(yes);

    //No Button
    GuiItem no = new GuiItem(Material.RED_TERRACOTTA, 42, gui);
    no.setNameColor(ChatColor.RED.toString() + ChatColor.BOLD.toString());
    no.setName(lang.getString("no0"));
    gui.addItem(no);

    //Skill Item (Indicator)
    GuiItem skillIcon = new GuiItem(Material.IRON_SHOVEL, 4, gui);
    switch (skillName) {
      case "digging":
        skillIcon.setItemType(Material.IRON_SHOVEL);
        break;
      case "woodcutting":
        skillIcon.setItemType(Material.IRON_AXE);
        break;
      case "mining":
        skillIcon.setItemType(Material.IRON_PICKAXE);
        break;
      case "farming":
        skillIcon.setItemType(Material.IRON_HOE);
        break;
      case "fishing":
        skillIcon.setItemType(Material.FISHING_ROD);
        break;
      case "beastMastery":
        skillIcon.setItemType(Material.BONE);
        break;
      case "swordsmanship":
        skillIcon.setItemType(Material.IRON_SWORD);
        break;
      case "defense":
        skillIcon.setItemType(Material.IRON_CHESTPLATE);
        break;
      case "axeMastery":
        skillIcon.setItemType(Material.GOLDEN_AXE);
        break;
      case "repair":
        skillIcon.setItemType(Material.ANVIL);
        break;
      case "agility":
        skillIcon.setItemType(Material.LEATHER_LEGGINGS);
        break;
      case "alchemy":
        skillIcon.setItemType(Material.POTION);
        break;
      case "smelting":
        skillIcon.setItemType(Material.COAL);
        break;
      case "enchanting":
        skillIcon.setItemType(Material.ENCHANTING_TABLE);
        break;
      case "building":
        skillIcon.setItemType(Material.BRICKS);
        break;
      case "aeronautics":
        skillIcon.setItemType(Material.ELYTRA);
        break;
      default:
        break;
    }
    skillIcon.setNameColor(ChatColor.AQUA.toString() + ChatColor.BOLD.toString());
    skillIcon.setName(skillTitle);
    gui.addItem(skillIcon);

    //Sets all the items into the gui (This is redundant, but makes the code a bit clearer)
    gui.setGui();

    //Put the items in the inventory
    gui.displayGuiForPlayer();
    return true;
  }
}
