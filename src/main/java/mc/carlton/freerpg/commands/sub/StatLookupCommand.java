package mc.carlton.freerpg.commands.sub;

import java.util.List;
import mc.carlton.freerpg.commands.CommandHelper;
import mc.carlton.freerpg.core.info.player.Leaderboards;
import mc.carlton.freerpg.core.info.player.PlayerLeaderboardStat;
import mc.carlton.freerpg.utils.UtilityMethods;
import mc.carlton.freerpg.utils.game.LanguageSelector;
import mc.carlton.freerpg.utils.globalVariables.StringsAndOtherData;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /frpg stats (moved verbatim from FrpgCommands). */
public class StatLookupCommand implements SubCommand {

  @Override
  public List<String> names() {
    return List.of("statLookup", "lookupStats", "stats");
  }

  @Override
  public boolean execute(CommandSender sender, String[] args) {
    final String IMPROPER_ARGUMENTS_MESSSAGE = " /frpg stats [::playerName::]";
    CommandHelper commandHelper = new CommandHelper(sender, args, 1, 2,
        IMPROPER_ARGUMENTS_MESSSAGE);
    commandHelper.setPermissionName("statLookup");
    if (!commandHelper.isProperCommand()) {
      return true; //Command Restricted or Improper
    }
    if (args.length == 1) {
      if (sender instanceof Player) {
        Player p = (Player) sender;
        p.performCommand("frpg statLookup " + p.getName());
      } else {
        commandHelper.sendImproperArgumentsMessage();
        return true; //Improper Arguments
      }
    } else if (args.length == 2) {
      String playerName = args[1];
      Leaderboards leaderboards = new Leaderboards();
      if (!leaderboards.isPlayerOnLeaderboards(playerName)) {
        if (sender instanceof Player) {
          Player p = (Player) sender;
          LanguageSelector lang = new LanguageSelector(p);
          p.sendMessage(ChatColor.RED + lang.getString("playerNotInLeaderboard"));
        } else {
          sender.sendMessage("That player is not on any leaderboards");
        }
        return true; //Player not on leaderboards
      }
      createStatLookup(sender, playerName);
      mc.carlton.freerpg.weaponskills.WeaponSkillCommands.sendStatLines(sender, playerName);
    }
    return true;
  }

  private void createStatLookup(CommandSender sender, String playerName) {
    Leaderboards leaderboards = new Leaderboards();
    if (sender instanceof Player) {
      Player p = (Player) sender;
      LanguageSelector lang = new LanguageSelector(p);
      List<String> leaderboardNames = leaderboards.getLeaderboardNames();
      p.sendMessage(
          ChatColor.AQUA + "--------| " + ChatColor.WHITE + ChatColor.BOLD.toString() + playerName
              + ChatColor.RESET + ChatColor.WHITE.toString() + " " + lang.getString("stats") +
              ChatColor.AQUA.toString() + " |-------");
      StringsAndOtherData stringsAndOtherData = new StringsAndOtherData();
      for (String leaderboardName : leaderboardNames) {
        int rank = leaderboards.getLeaderboardPosition(playerName, leaderboardName);
        PlayerLeaderboardStat playerStat = leaderboards.getPlayerStat(playerName, leaderboardName);
        String displayedStat;
        String displayTitle;
        String rankString = UtilityMethods.intToRankingString(rank);
        if (rankString.equals("1st")) {
          rankString = ChatColor.YELLOW + rankString;
        } else if (rankString.equals("2nd")) {
          rankString = ChatColor.WHITE + rankString;
        } else if (rankString.equals("3rd")) {
          rankString = ChatColor.GOLD + rankString;
        } else {
          rankString = ChatColor.GRAY + rankString;
        }
        if (leaderboardName.equalsIgnoreCase("playTime")) {
          displayedStat = playerStat.get_playTimeString();
          displayTitle = "Play Time";
        } else if (leaderboardName.equalsIgnoreCase("global")) {
          displayedStat = String.format("%,d", playerStat.get_sortedStat());
          displayTitle = "Global Level";
        } else {
          displayedStat = String.format("%,d", playerStat.get_stat2());
          displayTitle = stringsAndOtherData.camelCaseToTitle(leaderboardName);
        }
        p.sendMessage(ChatColor.AQUA + ChatColor.BOLD.toString() + displayTitle + ChatColor.RESET
            + ChatColor.GRAY.toString() + " - " +
            ChatColor.WHITE + displayedStat + " (" + rankString + ChatColor.WHITE + ")");
      }

    } else {
      List<String> leaderboardNames = leaderboards.getLeaderboardNames();
      sender.sendMessage("--------| " + playerName + " stats |-------");
      StringsAndOtherData stringsAndOtherData = new StringsAndOtherData();
      for (String leaderboardName : leaderboardNames) {
        int rank = leaderboards.getLeaderboardPosition(playerName, leaderboardName);
        PlayerLeaderboardStat playerStat = leaderboards.getPlayerStat(playerName, leaderboardName);
        String displayedStat;
        String displayTitle;
        String rankString = UtilityMethods.intToRankingString(rank);
        if (leaderboardName.equalsIgnoreCase("playTime")) {
          displayedStat = playerStat.get_playTimeString();
          displayTitle = "Play Time";
        } else if (leaderboardName.equalsIgnoreCase("global")) {
          displayedStat = String.format("%,d", playerStat.get_sortedStat());
          displayTitle = "Global Level";
        } else {
          displayedStat = String.format("%,d", playerStat.get_stat2());
          displayTitle = stringsAndOtherData.camelCaseToTitle(leaderboardName);
        }
        sender.sendMessage(displayTitle + " - " + displayedStat + " (" + rankString + ")");
      }
    }
  }
}
