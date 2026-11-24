package mc.carlton.freerpg.commands.sub;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import mc.carlton.freerpg.commands.CommandHelper;
import mc.carlton.freerpg.commands.sub.menu.GlobalSkillTreeMenu;
import mc.carlton.freerpg.commands.sub.menu.MainSkillTreeMenu;
import mc.carlton.freerpg.commands.sub.menu.PassiveSkillTreeMenu;
import mc.carlton.freerpg.config.ConfigLoad;
import mc.carlton.freerpg.core.info.player.PlayerStats;
import mc.carlton.freerpg.skills.SkillRegistry;
import mc.carlton.freerpg.utils.UtilityMethods;
import mc.carlton.freerpg.utils.game.LanguageSelector;
import mc.carlton.freerpg.utils.globalVariables.StringsAndOtherData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

/** /frpg skillTree (moved from FrpgCommands; the three menus live in commands/sub/menu). */
public class SkillTreeCommand implements SubCommand {

  public enum MenuKind {
    MAIN, PASSIVE, GLOBAL
  }

  /** Which menu a skill label opens, or null for an unknown label (exact, case-sensitive match). */
  public static MenuKind menuKindFor(String skillName) {
    if (SkillRegistry.isMain(skillName)) {
      return MenuKind.MAIN;
    }
    if (SkillRegistry.isPassive(skillName)) {
      return MenuKind.PASSIVE;
    }
    if ("global".equals(skillName)) {
      return MenuKind.GLOBAL;
    }
    return null;
  }

  @Override
  public List<String> names() {
    return List.of("skillTree", "skillTreeGUI");
  }

  private void setTranslatedSkillTreeInformation(Player p, String skillName,
      Map<String, String[]> perksMap, Map<String, String[]> descriptionsMap,
      Map<String, String[]> passivePerksMap, Map<String, String[]> passiveDescriptionsMap) {
    LanguageSelector lang = new LanguageSelector(p);
    String[] newTitles = perksMap.get(skillName);
    String[] newDescs = descriptionsMap.get(skillName);
    String[] newPassiveTitles = passivePerksMap.get(skillName);
    String[] newPassiveDescs = passiveDescriptionsMap.get(skillName);
    for (int i = 0; i < newTitles.length; i++) {
      newTitles[i] = lang.getString(skillName + "PerkTitle" + i);
    }
    for (int i = 0; i < newDescs.length; i++) {
      newDescs[i] = lang.getString(skillName + "PerkDesc" + i);
    }
    for (int i = 0; i < newPassiveTitles.length; i++) {
      newPassiveTitles[i] = lang.getString(skillName + "PassiveTitle" + i);
    }
    for (int i = 0; i < newPassiveDescs.length; i++) {
      newPassiveDescs[i] = lang.getString(skillName + "PassiveDesc" + i);
    }
    perksMap.put(skillName, newTitles);
    descriptionsMap.put(skillName, newDescs);
    passivePerksMap.put(skillName, newPassiveTitles);
    passiveDescriptionsMap.put(skillName, newPassiveDescs);
  }

  @Override
  public boolean execute(CommandSender sender, String[] args) {
    final String IMPROPER_ARGUMENTS_MESSSAGE = " /frpg skillTree [::skillName::]";
    CommandHelper commandHelper = new CommandHelper(sender, args, 2, IMPROPER_ARGUMENTS_MESSSAGE);
    commandHelper.setPlayerOnlyCommand(true);
    commandHelper.setCheckInBed(true);
    commandHelper.setPermissionName("skillsGUI");
    if (!commandHelper.isProperCommand()) {
      return true; //Command Restricted or Improper
    }

    Player p = (Player) sender;
    LanguageSelector lang = new LanguageSelector(p);
    PlayerStats pStatClass = new PlayerStats(p);
    Map<String, ArrayList<Number>> pStatAll = pStatClass.getPlayerData();
    ConfigLoad configLoad = new ConfigLoad();
    StringsAndOtherData stringsAndOtherData = new StringsAndOtherData();

    String[] titles_0 = SkillRegistry.titlesWithGlobal().toArray(new String[0]);
    List<String> labels_arr = SkillRegistry.labelsWithGlobal();
    String skillName = UtilityMethods.convertStringToListCasing(labels_arr, args[1]);

    if (!labels_arr.contains(skillName)) {
      commandHelper.sendImproperArgumentsMessage();
      return true;
    }
    String skillTitle = titles_0[labels_arr.indexOf(skillName)];

    if (!configLoad.getAllowedSkillsMap()
        .get(skillName)) { //Special condition to return (skill disabled)
      p.sendMessage(ChatColor.RED + lang.getString("disabledSkill"));
      return true;
    }

    ArrayList<Integer> soulsInfo = configLoad.getSoulsInfo();
    String refundCost = Integer.toString(soulsInfo.get(1));

    Map<String, String[]> perksMap = stringsAndOtherData.getPerksMap();
    Map<String, String[]> descriptionsMap = stringsAndOtherData.getDescriptionsMap();
    Map<String, String[]> passivePerksMap = stringsAndOtherData.getPassivePerksMap();
    Map<String, String[]> passiveDescriptionsMap = stringsAndOtherData.getPassiveDescriptionsMap();
    setTranslatedSkillTreeInformation(p, skillName, perksMap, descriptionsMap, passivePerksMap,
        passiveDescriptionsMap);

    //Create Gui
    //GuiWrapper gui = new GuiWrapper(p,Bukkit.createInventory(p, 54,skillTitle));
    Inventory gui = Bukkit.createInventory(p, 54, skillTitle);

    // The menu is chosen from the case-corrected skill name, so "Smelting" or "Global" work too.
    if (menuKindFor(skillName) == MenuKind.MAIN) { //One of the 10 "main" skills
      new MainSkillTreeMenu().open(p, skillName, gui, pStatAll, perksMap, descriptionsMap,
          passivePerksMap, passiveDescriptionsMap, lang, configLoad, stringsAndOtherData,
          refundCost);
    } else if (menuKindFor(skillName) == MenuKind.PASSIVE) {
      new PassiveSkillTreeMenu().open(p, skillName, gui, pStatAll, perksMap, descriptionsMap,
          passivePerksMap, passiveDescriptionsMap, lang, configLoad, stringsAndOtherData,
          refundCost);
    } else if (menuKindFor(skillName) == MenuKind.GLOBAL) {
      new GlobalSkillTreeMenu().open(p, skillName, gui, pStatAll, perksMap, descriptionsMap,
          passivePerksMap, passiveDescriptionsMap, lang, configLoad, stringsAndOtherData,
          refundCost);
    } else {
      System.out.println("You need to be a player to cast this command");
    }
    return true;
  }
}
