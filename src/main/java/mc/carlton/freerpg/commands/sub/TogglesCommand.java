package mc.carlton.freerpg.commands.sub;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import mc.carlton.freerpg.commands.CommandHelper;
import mc.carlton.freerpg.core.info.player.PlayerStats;
import mc.carlton.freerpg.skills.perksAndAbilities.Agility;
import mc.carlton.freerpg.utils.game.LanguageSelector;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /frpg toggle* perk toggles (moved verbatim from FrpgCommands). One instance per toggle. */
public class TogglesCommand implements SubCommand {

  private final String id;
  private final List<String> names;

  private TogglesCommand(String id, String commandName) {
    this.id = id;
    this.names = List.of(commandName, id);
  }

  public static List<SubCommand> all() {
    return List.of(
        new TogglesCommand("flamePickToggle", "toggleFlamePick"),
        new TogglesCommand("flintToggle", "toggleFlint"),
        new TogglesCommand("grappleToggle", "toggleGrapple"),
        new TogglesCommand("hotRodToggle", "toggleHotRod"),
        new TogglesCommand("megaDigToggle", "toggleMegaDig"),
        new TogglesCommand("potionToggle", "togglePotion"),
        new TogglesCommand("speedToggle", "toggleSpeed"),
        new TogglesCommand("veinMinerToggle", "toggleVeinMiner"),
        new TogglesCommand("leafBlowerToggle", "toggleLeafBlower"),
        new TogglesCommand("holyAxeToggle", "toggleHolyAxe"));
  }

  @Override
  public List<String> names() {
    return names;
  }

  @Override
  public boolean execute(CommandSender sender, String[] args) {
    return togglePerk(id, sender, args);
  }

  private boolean togglePerk(String id, CommandSender sender, String[] args) {
    String permission;
    String langID;
    String skillName;
    int skillIndex;
    int globalIndex;
    switch (id) {
      case "flintToggle":
        permission = "toggleFlint";
        langID = "diggingPerkTitle4";
        skillName = "digging";
        skillIndex = 11;
        globalIndex = 12;
        break;
      case "flamePickToggle":
        permission = "toggleFlamePick";
        langID = "smeltingPerkTitle2";
        skillName = "smelting";
        skillIndex = 13;
        globalIndex = 13;
        break;
      case "speedToggle":
        permission = "toggleSpeed";
        langID = "agilityPerkTitle2";
        skillName = "agility";
        skillIndex = 13;
        globalIndex = 14;
        break;
      case "potionToggle":
        permission = "togglePotion";
        langID = "alchemyPerkTitle2";
        skillName = "alchemy";
        skillIndex = 13;
        globalIndex = 15;
        break;
      case "grappleToggle":
        permission = "toggleGrapple";
        langID = "fishingPerkTitle4";
        skillName = "fishing";
        skillIndex = 11;
        globalIndex = 16;
        break;
      case "hotRodToggle":
        permission = "toggleHotRod";
        langID = "fishingPerkTitle5";
        skillName = "fishing";
        skillIndex = 12;
        globalIndex = 17;
        break;
      case "veinMinerToggle":
        permission = "toggleVeinMiner";
        langID = "miningPerkTitle4";
        skillName = "mining";
        skillIndex = 11;
        globalIndex = 18;
        break;
      case "megaDigToggle":
        permission = "toggleMegaDig";
        langID = "diggingPerkTitle6";
        skillName = "digging";
        skillIndex = 13;
        globalIndex = 19;
        break;
      case "leafBlowerToggle":
        permission = "toggleLeafBlower";
        langID = "woodcuttingPerkTitle5";
        skillName = "woodcutting";
        skillIndex = 12;
        globalIndex = 26;
        break;
      case "holyAxeToggle":
        permission = "toggleHolyAxe";
        langID = "axeMasteryPerkTitle1";
        skillName = "axeMastery";
        skillIndex = 8;
        globalIndex = 27;
        break;
      default:
        return true;
    }

    final String IMPROPER_ARGUMENTS_MESSSAGE = " /frpg " + id;
    CommandHelper commandHelper = new CommandHelper(sender, args, 1, 2,
        IMPROPER_ARGUMENTS_MESSSAGE);
    commandHelper.setPlayerOnlyCommand(true);
    commandHelper.setPermissionName(permission);
    if (!commandHelper.isProperCommand()) {
      return true; //Command Restricted or Improper
    }
    Player p = (Player) sender;

    LanguageSelector lang = new LanguageSelector(p);
    PlayerStats pStatClass = new PlayerStats(p);
    Agility agility = new Agility(p);
    Map<UUID, Map<String, ArrayList<Number>>> statAll = pStatClass.getData();
    Map<String, ArrayList<Number>> pStat = pStatClass.getPlayerData();
    int level = (int) pStat.get(skillName).get(skillIndex);

    if (level <= 0) {
      p.sendMessage(
          ChatColor.RED + lang.getString("unlockToggle") + " " + ChatColor.BOLD + lang.getString(
              langID));
      return true;
    }
    if (args.length == 1) {
      int toggle = (int) pStat.get("global").get(globalIndex);
      if (toggle > 0) {
        p.sendMessage(ChatColor.RED + lang.getString(langID) + ": " + lang.getString("off0"));
        pStat.get("global").set(globalIndex, 0);
        if (id.equalsIgnoreCase("speedToggle")) {
          agility.gracefulFeetEnd();
        }
      } else {
        p.sendMessage(ChatColor.GREEN + lang.getString(langID) + ": " + lang.getString("on0"));
        pStat.get("global").set(globalIndex, 1);
        if (id.equalsIgnoreCase("speedToggle")) {
          agility.gracefulFeetStart();
        }
      }
      statAll.put(p.getUniqueId(), pStat);
      pStatClass.setData(statAll);
    } else if (args.length == 2) {
      if (args[1].equalsIgnoreCase("off")) {
        p.sendMessage(ChatColor.RED + lang.getString(langID) + ": " + lang.getString("off0"));
        pStat.get("global").set(globalIndex, 0);
        statAll.put(p.getUniqueId(), pStat);
        pStatClass.setData(statAll);
        if (id.equalsIgnoreCase("speedToggle")) {
          agility.gracefulFeetEnd();
        }
      } else if (args[1].equalsIgnoreCase("on")) {
        p.sendMessage(ChatColor.GREEN + lang.getString(langID) + ": " + lang.getString("on0"));
        pStat.get("global").set(globalIndex, 1);
        statAll.put(p.getUniqueId(), pStat);
        pStatClass.setData(statAll);
        if (id.equalsIgnoreCase("speedToggle")) {
          agility.gracefulFeetStart();
        }
      } else {
        commandHelper.sendImproperArgumentsMessage();
      }
    }
    return true;
  }
}
