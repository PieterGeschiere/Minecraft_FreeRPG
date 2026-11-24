package mc.carlton.freerpg.commands.sub;

import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import mc.carlton.freerpg.FreeRPG;
import mc.carlton.freerpg.commands.CommandHelper;
import mc.carlton.freerpg.utils.game.LanguageSelector;

/** /frpg info (moved verbatim from FrpgCommands). */
public class InfoCommand implements SubCommand {

  @Override
  public List<String> names() {
    return List.of("info", "use");
  }

  @Override
  public boolean execute(CommandSender sender, String[] args) {
    final String IMPROPER_ARGUMENTS_MESSSAGE = " /frpg info";
    CommandHelper commandHelper = new CommandHelper(sender, args, 0, IMPROPER_ARGUMENTS_MESSSAGE);
    commandHelper.setPlayerOnlyCommand(true);
    commandHelper.setPermissionName("info");
    if (!commandHelper.isProperCommand()) {
      return true; //Command Restricted or Improper
    }
    Player p = (Player) sender;
    LanguageSelector lang = new LanguageSelector(p);
    p.sendMessage(
        lang.getString("informationURL") + ": " + ChatColor.AQUA + ChatColor.UNDERLINE.toString()
            + "shorturl.at/ptCDX" +
            ChatColor.RESET + ChatColor.GOLD.toString() + ChatColor.BOLD.toString() + "<-- "
            + lang.getString("click"));
    return true;
  }
}
