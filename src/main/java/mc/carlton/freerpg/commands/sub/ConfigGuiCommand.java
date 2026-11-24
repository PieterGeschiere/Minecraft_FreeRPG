package mc.carlton.freerpg.commands.sub;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import mc.carlton.freerpg.FreeRPG;
import mc.carlton.freerpg.commands.CommandHelper;
import mc.carlton.freerpg.core.info.player.PlayerStats;
import mc.carlton.freerpg.utils.game.LanguageSelector;
import mc.carlton.freerpg.utils.globalVariables.StringsAndOtherData;
import mc.carlton.freerpg.utils.gui.GuiItem;
import mc.carlton.freerpg.utils.gui.GuiWrapper;
import org.apache.logging.log4j.Level;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /frpg configGUI (moved verbatim from FrpgCommands). */
public class ConfigGuiCommand implements SubCommand {

  @Override
  public List<String> names() {
    return List.of("configGUI", "configurationGUI");
  }

  @Override
  public boolean execute(CommandSender sender, String[] args) {
    final String IMPROPER_ARGUMENTS_MESSSAGE = " /frpg configGUI";
    CommandHelper commandHelper = new CommandHelper(sender, args, 1, IMPROPER_ARGUMENTS_MESSSAGE);
    commandHelper.setPlayerOnlyCommand(true);
    commandHelper.setCheckInBed(true);
    commandHelper.setPermissionName("configGUI");
    if (!commandHelper.isProperCommand()) {
      return true; //Command Restricted or Improper
    }
    Player p = (Player) sender;
    LanguageSelector lang = new LanguageSelector(p);
    PlayerStats pStatClass = new PlayerStats(p);
    Map<String, ArrayList<Number>> pStat = pStatClass.getPlayerData();

    //GuiWrapper
    GuiWrapper gui = new GuiWrapper(p, "Configuration Window", 54);

    //Back button
    GuiItem backButton = new GuiItem(Material.ARROW, 45, gui);
    backButton.setName(lang.getString("diggingPassiveTitle1"));
    backButton.setDescription(lang.getString("diggingPassiveDesc1"));
    gui.addItem(backButton);

    //Simple Toggle Buttons
    CommandSupport.addToggleButton(p, gui, "::levelUpNotif::", Material.OAK_SIGN, 1,
        21); //Level up Notifications
    CommandSupport.addToggleButton(p, gui, "::abilityPreparationNotif::", Material.OAK_SIGN, 2,
        22); //Ability Prep Notifications
    CommandSupport.addToggleButton(p, gui, "::triggerAbilities::", Material.WOODEN_PICKAXE, 3,
        24); //Ability Trigger
    CommandSupport.addToggleButton(p, gui, "::showEXPBar::", Material.EXPERIENCE_BOTTLE, 4, 25); //EXP bar show

    //Ability Duration Bar
    GuiItem durationBar = new GuiItem(Material.CLOCK, 5, gui);
    durationBar.setName(lang.getString("numberOfAbilityTimersDisplayed"));
    gui.addItem(durationBar);

    GuiItem durationBarToggle = new GuiItem(Material.LIME_DYE, 14, gui);
    int numberOfBars = (int) pStat.get("global").get(28);
    if (numberOfBars > 0) {
      durationBarToggle.setNameColor(ChatColor.BOLD + ChatColor.GREEN.toString());
      durationBarToggle.setName(String.valueOf(numberOfBars));
    } else {
      durationBarToggle.setNameColor(ChatColor.BOLD + ChatColor.RED.toString());
      durationBarToggle.setName(String.valueOf(0));
      durationBarToggle.setItemType(Material.GRAY_DYE);
    }
    durationBarToggle.setItemAmount(Math.max(1, numberOfBars));
    gui.addItem(durationBarToggle);

    //Sets all the items into the gui (This is redundant, but makes the code a bit clearer)
    gui.setGui();

    //LANGUAGES (sets all the language items into the gui)
    StringsAndOtherData stringsAndOtherData = new StringsAndOtherData();
    stringsAndOtherData.setLanguageItems(p, gui.getGui());

    //Put the items in the inventory
    gui.displayGuiForPlayer();
    return true;
  }
}
