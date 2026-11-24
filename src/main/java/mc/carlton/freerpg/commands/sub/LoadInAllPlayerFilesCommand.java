package mc.carlton.freerpg.commands.sub;

import java.util.List;
import org.bukkit.command.CommandSender;
import mc.carlton.freerpg.FreeRPG;
import mc.carlton.freerpg.commands.CommandHelper;
import mc.carlton.freerpg.core.info.player.OfflinePlayerStatLoadIn;

/** /frpg loadInAllPlayerFiles (moved verbatim from FrpgCommands). */
public class LoadInAllPlayerFilesCommand implements SubCommand {

  @Override
  public List<String> names() {
    return List.of("loadInAllPlayerFiles", "loadInPlayerFiles");
  }

  @Override
  public boolean execute(CommandSender sender, String[] args) {
    final String IMPROPER_ARGUMENTS_MESSSAGE = " /frpg save";
    CommandHelper commandHelper = new CommandHelper(sender, args, 0, IMPROPER_ARGUMENTS_MESSSAGE);
    commandHelper.setPermissionName("saveStats");
    if (!commandHelper.isProperCommand()) {
      return true; //Command Restricted or Improper
    }
    OfflinePlayerStatLoadIn offlinePlayerStatLoadIn = new OfflinePlayerStatLoadIn();
    offlinePlayerStatLoadIn.loadInAllOfflinePlayers();

    return true;
  }
}
