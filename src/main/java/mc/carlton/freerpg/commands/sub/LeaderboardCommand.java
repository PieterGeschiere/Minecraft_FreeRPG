package mc.carlton.freerpg.commands.sub;

import java.util.ArrayList;
import java.util.List;
import mc.carlton.freerpg.commands.CommandHelper;
import mc.carlton.freerpg.config.ConfigLoad;
import mc.carlton.freerpg.core.info.player.Leaderboards;
import mc.carlton.freerpg.core.info.player.PlayerLeaderboardStat;
import mc.carlton.freerpg.skills.SkillRegistry;
import mc.carlton.freerpg.utils.UtilityMethods;
import mc.carlton.freerpg.utils.game.LanguageSelector;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /frpg top (moved verbatim from FrpgCommands). */
public class LeaderboardCommand implements SubCommand {

  @Override
  public List<String> names() {
    return List.of("leaderboard", "statLeaders", "top");
  }

  @Override
  public boolean execute(CommandSender sender, String[] args) {
    final String IMPROPER_ARGUMENTS_MESSSAGE = " /frpg top [::skillName::] [::page::]";
    CommandHelper commandHelper = new CommandHelper(sender, args, 1, 3,
        IMPROPER_ARGUMENTS_MESSSAGE);
    commandHelper.setPermissionName("leaderboard");
    if (!commandHelper.isProperCommand()) {
      return true; //Command Restricted or Improper
    }
    // "/frpg top" and "/frpg top <page>" show the global leaderboard (total level)
    if (args.length == 1) {
      createLeaderboard(sender, 1, "global");
      return true;
    }
    if (args.length == 2 && CommandSupport.getArgumentAsInteger(args[1]) != Integer.MAX_VALUE) {
      createLeaderboard(sender, CommandSupport.getArgumentAsInteger(args[1]), "global");
      return true;
    }
    int page = 1;
    if (args.length == 3) {
      page = CommandSupport.getArgumentAsInteger(args[2]);
      if (page == Integer.MAX_VALUE) {
        commandHelper.sendImproperArgumentsMessage();
        return true; //Improper input
      }
    }
    List<String> labels_arr = leaderboardLabels();
    String skillName = UtilityMethods.convertStringToListCasing(labels_arr, args[1]);
    if (!labels_arr.contains(skillName)) {
      commandHelper.sendImproperArgumentsMessage();
      return true; //Improper input
    }
    if (!new ConfigLoad().getAllowedSkillsMap().getOrDefault(skillName, true)) {
      sender.sendMessage(ChatColor.RED + "That skill is disabled.");
      return true;
    }
    createLeaderboard(sender, page, skillName);
    return true;
  }

  /** The 15 skills, global, then playTime (same order as the old literal array). */
  private static List<String> leaderboardLabels() {
    List<String> labels = new ArrayList<>(SkillRegistry.labelsWithGlobal());
    labels.add("playTime");
    return labels;
  }

  private void createLeaderboard(CommandSender sender, int page, String skillName) {
    List<String> labels_arr = leaderboardLabels();

    Leaderboards getStats = new Leaderboards();
    if (!getStats.isLeaderboardsLoaded()) {
      if (sender instanceof Player) {
        Player p = (Player) sender;
        p.sendMessage(ChatColor.RED + "Leaderboard is not yet loaded, try again soon");
      } else {
        sender.sendMessage("Leaderboard is not yet loaded, try again soon");
      }
    }
    //Sorts and Updates leaderboard (if dynamic updates are on)
    getStats.sortLeaderBoard(skillName);

    ArrayList<PlayerLeaderboardStat> sortedStats = getStats.getLeaderboard(skillName);
    int totalPlayers = sortedStats.size();
    int totalPages = (int) Math.ceil(totalPlayers / 10.0);
    if (page < 1 || page > totalPages) {
      page = 1;
    }
    if (sender instanceof Player) {
      Player p = (Player) sender;
      LanguageSelector lang = new LanguageSelector(p);
      List<String> titleKeys = new ArrayList<>(SkillRegistry.labelsWithGlobal());
      titleKeys.add("totalPlayTime");
      String skillTitle = lang.getString(titleKeys.get(labels_arr.indexOf(skillName)));
      p.sendMessage(
          ChatColor.AQUA + "------| " + ChatColor.WHITE + ChatColor.BOLD.toString() + skillTitle
              + " " + lang.getString("leaderboard") + "" +
              ChatColor.RESET + ChatColor.WHITE.toString() + " " + lang.getString("page") + " ["
              + Integer.toString(page) + "/" + Integer.toString(totalPages) + "]" +
              ChatColor.AQUA.toString() + " |-----");
      for (int i = 10 * (page - 1); i < (int) Math.min(10 * page, totalPlayers); i++) {
        PlayerLeaderboardStat info = sortedStats.get(i);
        if (skillName.equalsIgnoreCase("global")) {
          p.sendMessage(
              ChatColor.WHITE + Integer.toString(i + 1) + ". " + ChatColor.AQUA + info.get_pName()
                  + ChatColor.WHITE.toString() + " - " + ChatColor.WHITE + String.format("%,d",
                  info.get_sortedStat()) + ChatColor.WHITE + " (" + ChatColor.GRAY + String.format(
                  "%,d", info.get_stat2()) + ChatColor.WHITE + ")");
        } else if (skillName.equalsIgnoreCase("playTime")) {
          p.sendMessage(
              ChatColor.WHITE + Integer.toString(i + 1) + ". " + ChatColor.AQUA + info.get_pName()
                  + ChatColor.WHITE.toString() + " - " + ChatColor.WHITE
                  + info.get_playTimeString());
        } else {
          p.sendMessage(
              ChatColor.WHITE + Integer.toString(i + 1) + ". " + ChatColor.AQUA + info.get_pName()
                  + ChatColor.WHITE.toString() + " - " + ChatColor.WHITE + String.format("%,d",
                  info.get_stat2()) + ChatColor.WHITE + " (" + ChatColor.GRAY + String.format("%,d",
                  info.get_sortedStat()) + ChatColor.WHITE + ")");
        }
      }
    } else {
      String[] titles_0 = SkillRegistry.titlesWithGlobal().toArray(new String[0]);
      String skillTitle = skillName.equalsIgnoreCase("playTime") ? "Play Time"
          : titles_0[labels_arr.indexOf(skillName)];
      sender.sendMessage(
          "------| " + skillTitle + " Leaderboard" + " Page [" + Integer.toString(page) + "/"
              + Integer.toString(totalPages) + "]" + " |-----");
      for (int i = 10 * (page - 1); i < (int) Math.min(10 * page, totalPlayers); i++) {
        PlayerLeaderboardStat info = sortedStats.get(i);
        if (skillName.equalsIgnoreCase("global")) {
          sender.sendMessage(
              Integer.toString(i + 1) + ". " + info.get_pName() + " - " + Integer.toString(
                  (int) info.get_sortedStat()) + " (" + info.get_stat2() + ")");
        } else if (skillName.equalsIgnoreCase("playTime")) {
          sender.sendMessage(Integer.toString(i + 1) + ". " + info.get_pName() + " - "
              + info.get_playTimeString());
        } else {
          sender.sendMessage(
              Integer.toString(i + 1) + ". " + info.get_pName() + " - " + Integer.toString(
                  (int) info.get_stat2()) + " (" + info.get_sortedStat() + ")");
        }

      }
    }
  }
}
