package mc.carlton.freerpg.commands.sub;

import java.util.List;
import org.bukkit.command.CommandSender;
import mc.carlton.freerpg.FreeRPG;
import mc.carlton.freerpg.commands.CommandHelper;
import java.io.File;
import org.bukkit.Bukkit;

/** /frpg deleteFakePlayers (moved verbatim from FrpgCommands). */
public class DeleteFakePlayersCommand implements SubCommand {

  @Override
  public List<String> names() {
    return List.of("deleteFakePlayers", "deleteFakeProfiles", "removeFakePlayers", "removeFakeProfiles");
  }

  @Override
  public boolean execute(CommandSender sender, String[] args) {
    final String IMPROPER_ARGUMENTS_MESSSAGE = " /frpg deleteFakePlayers";
    CommandHelper commandHelper = new CommandHelper(sender, args, 0, IMPROPER_ARGUMENTS_MESSSAGE);
    commandHelper.setPermissionName("createFakePlayers");
    if (!commandHelper.isProperCommand()) {
      return true; //Command Restricted or Improper
    }

    File userdata = new File(
        Bukkit.getServer().getPluginManager().getPlugin("FreeRPG").getDataFolder(),
        File.separator + "PlayerDatabase");
    File[] allUsers = userdata.listFiles();
    if (allUsers == null) {
      return true;
    }
    for (File f : allUsers) {
      if (f.getName().regionMatches(true, 0, "badf", 0, 4)) {
        f.delete();
      }
    }

    return true;
  }
}
