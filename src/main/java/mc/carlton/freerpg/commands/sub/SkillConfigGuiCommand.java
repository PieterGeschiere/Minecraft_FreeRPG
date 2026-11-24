package mc.carlton.freerpg.commands.sub;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import mc.carlton.freerpg.FreeRPG;
import mc.carlton.freerpg.commands.CommandHelper;
import mc.carlton.freerpg.config.ConfigLoad;
import mc.carlton.freerpg.core.info.player.PlayerStats;
import mc.carlton.freerpg.skills.SkillRegistry;
import mc.carlton.freerpg.utils.UtilityMethods;
import mc.carlton.freerpg.utils.game.LanguageSelector;
import mc.carlton.freerpg.utils.gui.GuiItem;
import mc.carlton.freerpg.utils.gui.GuiWrapper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /frpg skillConfigGUI (moved verbatim from FrpgCommands). */
public class SkillConfigGuiCommand implements SubCommand {

  @Override
  public List<String> names() {
    return List.of("skillConfigGUI", "skillConfigurationGUI", "skillConfig");
  }

  @Override
  public boolean execute(CommandSender sender, String[] args) {
    final String IMPROPER_ARGUMENTS_MESSSAGE = " /frpg skillConfigGUI [::skillName::]";
    CommandHelper commandHelper = new CommandHelper(sender, args, 2, IMPROPER_ARGUMENTS_MESSSAGE);
    commandHelper.setPlayerOnlyCommand(true);
    commandHelper.setCheckInBed(true);
    commandHelper.setPermissionName("skillConfigGUI");
    if (!commandHelper.isProperCommand()) {
      return true; //Command Restricted or Improper
    }
    Player p = (Player) sender;
    LanguageSelector lang = new LanguageSelector(p);
    PlayerStats pStatClass = new PlayerStats(p);
    ConfigLoad configLoad = new ConfigLoad();

    String[] titles_0 = SkillRegistry.titleArray();
    String[] labels_0 = SkillRegistry.labelArray();
    List<String> passiveLabels = new ArrayList<>();
    for (String label : SkillRegistry.labels()) {
      if (SkillRegistry.isPassive(label)) {
        passiveLabels.add(label);
      }
    }
    List<String> labels = Arrays.asList(labels_0);
    String skillName = UtilityMethods.convertStringToListCasing(labels, args[1]);
    if (!labels.contains(skillName)) {
      commandHelper.sendImproperArgumentsMessage();
      return true;
    }

    String skillTitle = titles_0[labels.indexOf(skillName)];
    GuiWrapper gui = new GuiWrapper(p,
        Bukkit.createInventory(p, 54, skillTitle + " Configuration"));

    CommandSupport.addToggleButton(p, gui, "::showEXPBar::", Material.EXPERIENCE_BOTTLE, 10,
        pStatClass.isPlayerSkillExpBarOn(skillName)); //EXP bar show
    if (!passiveLabels.contains(skillName)) {
      CommandSupport.addToggleButton(p, gui, "::triggerAbilities::", Material.WOODEN_PICKAXE, 11,
          pStatClass.isPlayerSkillAbilityOn(skillName)); //Trigger Abilities
    }

    if (!configLoad.getAllowedSkillsMap()
        .get(skillName)) { //Special condition to return (skill disabled)
      p.sendMessage(ChatColor.RED + lang.getString("disabledSkill"));
      return true;
    }

    //Back button
    GuiItem backButton = new GuiItem(Material.ARROW, 45, gui);
    backButton.setName(lang.getString("diggingPassiveTitle1"));
    backButton.setDescription(lang.getString("diggingPassiveDesc1"));
    gui.addItem(backButton);

    GuiItem skillIcon = new GuiItem(Material.WOODEN_PICKAXE, 4, gui);
    skillIcon.setNameColor(ChatColor.AQUA.toString() + ChatColor.BOLD.toString());
    skillIcon.setName(lang.getString(skillName));

    switch (skillName) {
      case "digging":
        skillIcon.setItemType(Material.IRON_SHOVEL);
        //flintFinder
        togglePerkSetGuiItem("flintToggle", p, gui);

        //Mega Dig
        togglePerkSetGuiItem("megaDigToggle", p, gui);
        break;
      case "woodcutting":
        skillIcon.setItemType(Material.IRON_AXE);

        //leafBlower
        togglePerkSetGuiItem("leafBlowerToggle", p, gui);
        break;
      case "mining":
        skillIcon.setItemType(Material.IRON_PICKAXE);

        //veinMiner
        togglePerkSetGuiItem("veinMinerToggle", p, gui);

        break;
      case "farming":
        skillIcon.setItemType(Material.IRON_HOE);
        break;
      case "fishing":
        skillIcon.setItemType(Material.FISHING_ROD);

        //grappling Hook
        togglePerkSetGuiItem("grappleToggle", p, gui);

        //hot Rod
        togglePerkSetGuiItem("hotRodToggle", p, gui);
        break;
      case "archery":
        skillIcon.setItemType(Material.BOW);
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

        //Holy Axe
        togglePerkSetGuiItem("holyAxeToggle", p, gui);
        break;
      case "repair":
        skillIcon.setItemType(Material.ANVIL);
        break;
      case "agility":
        skillIcon.setItemType(Material.LEATHER_LEGGINGS);

        //gracefulFeet
        togglePerkSetGuiItem("speedToggle", p, gui);

        break;
      case "alchemy":
        skillIcon.setItemType(Material.POTION);

        //potionMaster
        togglePerkSetGuiItem("potionToggle", p, gui);
        break;
      case "smelting":
        skillIcon.setItemType(Material.COAL);

        //flamePick
        togglePerkSetGuiItem("flamePickToggle", p, gui);
        break;
      case "enchanting":
        skillIcon.setItemType(Material.ENCHANTING_TABLE);
        break;
      case "building":
        skillIcon.setItemType(Material.BRICKS);
        break;
      default:
        break;

    }
    gui.addItem(skillIcon);

    //Sets all the items into the gui (This is redundant, but makes the code a bit clearer)
    gui.setGui();

    //Put the items in the inventory
    gui.displayGuiForPlayer();
    return true;
  }

  private static void togglePerkSetGuiItem(String id, Player p, GuiWrapper gui) {
    String langID;
    int globalIndex;
    int guiIndex = 28;
    boolean enchanted = false;
    Material icon;
    switch (id) {
      case "flintToggle":
        langID = "diggingPerkTitle4";
        globalIndex = 12;
        icon = Material.FLINT;
        break;
      case "flamePickToggle":
        langID = "smeltingPerkTitle2";
        globalIndex = 13;
        icon = Material.BLAZE_POWDER;
        break;
      case "speedToggle":
        langID = "agilityPerkTitle2";
        globalIndex = 14;
        icon = Material.LEATHER_BOOTS;
        break;
      case "potionToggle":
        langID = "alchemyPerkTitle2";
        globalIndex = 15;
        icon = Material.POTION;
        break;
      case "grappleToggle":
        langID = "fishingPerkTitle4";
        globalIndex = 16;
        icon = Material.LEAD;
        break;
      case "hotRodToggle":
        langID = "fishingPerkTitle5";
        globalIndex = 17;
        guiIndex = 29;
        icon = Material.FISHING_ROD;
        break;
      case "veinMinerToggle":
        langID = "miningPerkTitle4";
        globalIndex = 18;
        icon = Material.DIAMOND_PICKAXE;
        break;
      case "megaDigToggle":
        langID = "diggingPerkTitle6";
        globalIndex = 19;
        guiIndex = 29;
        icon = Material.DIAMOND_SHOVEL;
        break;
      case "leafBlowerToggle":
        langID = "woodcuttingPerkTitle5";
        globalIndex = 26;
        icon = Material.OAK_LEAVES;
        break;
      case "holyAxeToggle":
        langID = "axeMasteryPerkTitle1";
        globalIndex = 27;
        icon = Material.DIAMOND_AXE;
        enchanted = true;
        break;
      default:
        return;
    }

    String parsableName = "::" + langID + "::" + " " + "::toggle::";
    CommandSupport.addToggleButton(p, gui, parsableName, icon, guiIndex, globalIndex);
    if (enchanted) {
      gui.getItem(guiIndex).addEnchantmentGlow();
    }
  }
}
