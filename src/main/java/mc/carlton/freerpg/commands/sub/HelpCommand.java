package mc.carlton.freerpg.commands.sub;

import java.util.List;
import mc.carlton.freerpg.utils.game.LanguageSelector;
import org.bukkit.command.CommandSender;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import mc.carlton.freerpg.FreeRPG;
import mc.carlton.freerpg.commands.CommandHelper;

/** /frpg help (moved verbatim from FrpgCommands). */
public class HelpCommand implements SubCommand {

  @Override
  public List<String> names() {
    return List.of("help", "?");
  }

  @Override
  public boolean execute(CommandSender sender, String[] args) {
    final String IMPROPER_ARGUMENTS_MESSSAGE = " /frpg help [::page::]";
    CommandHelper commandHelper = new CommandHelper(sender, args, 1, 2,
        IMPROPER_ARGUMENTS_MESSSAGE);
    commandHelper.setPermissionName("help");
    if (!commandHelper.isProperCommand()) {
      return true; //Command Restricted or Improper
    }
    if (!(sender instanceof Player)) {
      sendConsoleHelp(sender);
      return true;
    }
    int page = 1;
    if (args.length == 2) {
      page = CommandSupport.getArgumentAsInteger(args[1]);
      if (page == Integer.MAX_VALUE) {
        commandHelper.sendImproperArgumentsMessage();
        return true; //Improper input
      }
    }
    messagePlayerHelpScreen(sender, page); //Sends player the corresponding help screen
    return true;
  }

  /** Plain-text overview for the console and command blocks (no per-player language). */
  private void sendConsoleHelp(CommandSender sender) {
    sender.sendMessage("FreeRPG admin commands (tab-complete /frpg for the full list):");
    sender.sendMessage("  /frpg giveEXP <player> <skill> <amount>");
    sender.sendMessage("  /frpg setLevel <player> <skill> <level>");
    sender.sendMessage("  /frpg setTokens <player> <skill|global> [skill|passive] <amount>");
    sender.sendMessage("  /frpg setSouls <player> <amount>");
    sender.sendMessage("  /frpg setMultiplier <player> <amount>");
    sender.sendMessage("  /frpg statReset <player> [skill]");
    sender.sendMessage("  /frpg statLookup <player>");
    sender.sendMessage("  /frpg statLeaders [skill] [page]");
    sender.sendMessage("  /frpg resetCooldown <player> <skill>");
    sender.sendMessage("  /frpg saveStats [player]");
    sender.sendMessage("  /frpg reloadWeapons, /frpg info");
  }

  private void messagePlayerHelpScreen(CommandSender sender, int page) {
    if (sender instanceof Player) {
      Player p = (Player) sender;
      LanguageSelector lang = new LanguageSelector(p);
      final int TOTAL_PAGES = 4;
      if (page > TOTAL_PAGES || page < 1) {
        page = 1; //Page input out of bounds
      }
      p.sendMessage(
          ChatColor.RED + "------| " + ChatColor.GREEN + ChatColor.BOLD.toString() + " Help" +
              ChatColor.RESET + ChatColor.GREEN.toString() + " " + lang.getString("page") + " ["
              + Integer.toString(page) + "/" + Integer.toString(TOTAL_PAGES) + "]" +
              ChatColor.RED.toString() + " |-----");
      switch (page) {
        case 1:
          p.sendMessage(
              ChatColor.GOLD + "/frpg" + ChatColor.RESET + ChatColor.GRAY.toString() + " - " +
                  ChatColor.RESET + ChatColor.WHITE + lang.getString("commandDesc0"));
          p.sendMessage(ChatColor.GOLD + "/frpg skillTreeGUI [" + lang.getString("skillName") + "]"
              + ChatColor.RESET + ChatColor.GRAY.toString() + " - " +
              ChatColor.RESET + ChatColor.WHITE + lang.getString("commandDesc1"));
          p.sendMessage(ChatColor.GOLD + "/frpg configurationGUI" + ChatColor.RESET
              + ChatColor.GRAY.toString() + " - " +
              ChatColor.RESET + ChatColor.WHITE + lang.getString("commandDesc2"));
          p.sendMessage(ChatColor.GOLD + "/frpg weapons" + ChatColor.RESET
              + ChatColor.GRAY.toString() + " - " + ChatColor.RESET + ChatColor.WHITE
              + "Opens the weapon skills menu (Swordsmanship, Axe, Mace, Trident, Spear, Bow, Crossbow)");
          p.sendMessage(ChatColor.GOLD + "/frpg giveEXP [" + lang.getString("playerName") + "] ["
              + lang.getString("skillName") + "] [amount]" + ChatColor.RESET
              + ChatColor.GRAY.toString() + " - " +
              ChatColor.RESET + ChatColor.WHITE + lang.getString("commandDesc3"));
          p.sendMessage(ChatColor.GOLD + "/frpg setLevel [" + lang.getString("playerName") + "] ["
              + lang.getString("skillName") + "] [" + lang.getString("level") + "]"
              + ChatColor.RESET + ChatColor.GRAY.toString() + " - " +
              ChatColor.RESET + ChatColor.WHITE + lang.getString("commandDesc4"));
          p.sendMessage(ChatColor.GOLD + "/frpg statReset [" + lang.getString("playerName") + "] ["
              + lang.getString("skillName") + "]" + ChatColor.RESET + ChatColor.GRAY.toString()
              + " - " +
              ChatColor.RESET + ChatColor.WHITE + lang.getString("commandDesc5"));
          p.sendMessage(ChatColor.GOLD + "/frpg statLeaders [" + lang.getString("skillName") + "] ["
              + lang.getString("page") + "]" + ChatColor.RESET + ChatColor.GRAY.toString() + " - " +
              ChatColor.RESET + ChatColor.WHITE + lang.getString("commandDesc6"));
          p.sendMessage(
              ChatColor.GOLD + "/frpg info" + ChatColor.RESET + ChatColor.GRAY.toString() + " - " +
                  ChatColor.RESET + ChatColor.WHITE + lang.getString("commandDesc7"));
          break;
        case 2:
          p.sendMessage(ChatColor.GOLD + "/frpg flintToggle [" + lang.getString("onOrOff") + "]"
              + ChatColor.RESET + ChatColor.GRAY.toString() + " - " +
              ChatColor.RESET + ChatColor.WHITE + lang.getString("manuallyToggles") + " "
              + lang.getString("diggingPerkTitle4"));
          p.sendMessage(ChatColor.GOLD + "/frpg speedToggle [" + lang.getString("onOrOff") + "]"
              + ChatColor.RESET + ChatColor.GRAY.toString() + " - " +
              ChatColor.RESET + ChatColor.WHITE + lang.getString("manuallyToggles") + " "
              + lang.getString("agilityPerkTitle2"));
          p.sendMessage(ChatColor.GOLD + "/frpg potionToggle [" + lang.getString("onOrOff") + "]"
              + ChatColor.RESET + ChatColor.GRAY.toString() + " - " +
              ChatColor.RESET + ChatColor.WHITE + lang.getString("manuallyToggles") + " "
              + lang.getString("alchemyPerkTitle2"));
          p.sendMessage(ChatColor.GOLD + "/frpg flamePickToggle [" + lang.getString("onOrOff") + "]"
              + ChatColor.RESET + ChatColor.GRAY.toString() + " - " +
              ChatColor.RESET + ChatColor.WHITE + lang.getString("manuallyToggles") + " "
              + lang.getString("smeltingPerkTitle2"));
          p.sendMessage(ChatColor.GOLD + "/frpg grappleToggle [" + lang.getString("onOrOff") + "]"
              + ChatColor.RESET + ChatColor.GRAY.toString() + " - " +
              ChatColor.RESET + ChatColor.WHITE + lang.getString("manuallyToggles") + " "
              + lang.getString("fishingPerkTitle4"));
          p.sendMessage(ChatColor.GOLD + "/frpg hotRodToggle [" + lang.getString("onOrOff") + "]"
              + ChatColor.RESET + ChatColor.GRAY.toString() + " - " +
              ChatColor.RESET + ChatColor.WHITE + lang.getString("manuallyToggles") + " "
              + lang.getString("fishingPerkTitle5"));
          p.sendMessage(ChatColor.GOLD + "/frpg veinMinerToggle [" + lang.getString("onOrOff") + "]"
              + ChatColor.RESET + ChatColor.GRAY.toString() + " - " +
              ChatColor.RESET + ChatColor.WHITE + lang.getString("manuallyToggles") + " "
              + lang.getString("miningPerkTitle4"));
          p.sendMessage(ChatColor.GOLD + "/frpg megaDigToggle [" + lang.getString("onOrOff") + "]"
              + ChatColor.RESET + ChatColor.GRAY.toString() + " - " +
              ChatColor.RESET + ChatColor.WHITE + lang.getString("manuallyToggles") + " "
              + lang.getString("diggingPerkTitle6"));
          break;
        case 3:
          p.sendMessage(
              ChatColor.GOLD + "/frpg leafBlowerToggle [" + lang.getString("onOrOff") + "]"
                  + ChatColor.RESET + ChatColor.GRAY.toString() + " - " +
                  ChatColor.RESET + ChatColor.WHITE + lang.getString("manuallyToggles") + " "
                  + lang.getString("woodcuttingPerkTitle5"));
          p.sendMessage(ChatColor.GOLD + "/frpg holyAxeToggle [" + lang.getString("onOrOff") + "]"
              + ChatColor.RESET + ChatColor.GRAY.toString() + " - " +
              ChatColor.RESET + ChatColor.WHITE + lang.getString("manuallyToggles") + " "
              + lang.getString("axeMasteryPerkTitle1"));
          p.sendMessage(ChatColor.GOLD + "/frpg enchantItem [" + lang.getString("level") + "]"
              + ChatColor.RESET + ChatColor.GRAY.toString() + " - " +
              ChatColor.RESET + ChatColor.WHITE + lang.getString("commandDesc8"));
          p.sendMessage(
              ChatColor.GOLD + "/frpg setSouls" + " [" + lang.getString("playerName") + "]" + " ["
                  + lang.getString("amount") + "]" + ChatColor.RESET + ChatColor.GRAY.toString()
                  + " - " +
                  ChatColor.RESET + ChatColor.WHITE + lang.getString("commandDesc9"));
          p.sendMessage(
              ChatColor.GOLD + "/frpg setTokens" + " [" + lang.getString("playerName") + "]" + " ["
                  + lang.getString("skillName") + "]" + " [skill/passive]" + " [" + lang.getString(
                  "amount") + "]" + ChatColor.RESET + ChatColor.GRAY.toString() + " - " +
                  ChatColor.RESET + ChatColor.WHITE + lang.getString("commandDesc10"));
          p.sendMessage(
              ChatColor.GOLD + "/frpg setTokens" + " [" + lang.getString("playerName") + "]"
                  + " global" + " [" + lang.getString("amount") + "]" + ChatColor.RESET
                  + ChatColor.GRAY.toString() + " - " +
                  ChatColor.RESET + ChatColor.WHITE + lang.getString("commandDesc11"));
          p.sendMessage(ChatColor.GOLD + "/frpg saveStats [" + lang.getString("playerName") + "]"
              + ChatColor.RESET + ChatColor.GRAY.toString() + " - " +
              ChatColor.RESET + ChatColor.WHITE + lang.getString("commandDesc12"));
          p.sendMessage(
              ChatColor.GOLD + "/frpg setMultiplier [" + lang.getString("playerName") + "] " + "["
                  + lang.getString("amount") + "]" + ChatColor.RESET + ChatColor.GRAY.toString()
                  + " - " +
                  ChatColor.RESET + ChatColor.WHITE + lang.getString("commandDesc13"));
          break;
        case 4:
          p.sendMessage(
              ChatColor.GOLD + "/frpg resetCooldown [" + lang.getString("playerName") + "] ["
                  + lang.getString("skillName") + "]" + ChatColor.RESET + ChatColor.GRAY.toString()
                  + " - " +
                  ChatColor.RESET + ChatColor.WHITE + lang.getString("commandDesc14"));
          p.sendMessage(ChatColor.GOLD + "/frpg statLookup [" + lang.getString("playerName") + "]"
              + ChatColor.RESET + ChatColor.GRAY.toString() + " - " +
              ChatColor.RESET + ChatColor.WHITE + lang.getString("commandDesc15"));
          p.sendMessage(
              ChatColor.GOLD + "/frpg changeMultiplier [" + lang.getString("playerName") + "] "
                  + "[" + lang.getString("expIncrease") + "]" + ChatColor.RESET
                  + ChatColor.GRAY.toString() + " - " +
                  ChatColor.RESET + ChatColor.WHITE + lang.getString("commandDesc16"));
        default:
          break;
      }
    }
  }
}
