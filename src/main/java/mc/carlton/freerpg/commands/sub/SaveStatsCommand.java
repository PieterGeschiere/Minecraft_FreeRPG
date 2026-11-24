package mc.carlton.freerpg.commands.sub;

import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import mc.carlton.freerpg.FreeRPG;
import mc.carlton.freerpg.commands.CommandHelper;
import org.bukkit.plugin.Plugin;
import java.io.IOException;
import mc.carlton.freerpg.core.info.player.PlayerStatsLoadIn;
import mc.carlton.freerpg.core.serverFileManagement.PeriodicSaving;
import org.apache.logging.log4j.Level;

/** /frpg saveStats (moved verbatim from FrpgCommands). */
public class SaveStatsCommand implements SubCommand {

  @Override
  public List<String> names() {
    return List.of("saveStats", "statSave");
  }

  @Override
  public boolean execute(CommandSender sender, String[] args) {
    Plugin plugin = FreeRPG.getPlugin(FreeRPG.class);
    final String IMPROPER_ARGUMENTS_MESSSAGE = " /frpg saveStats";
    CommandHelper commandHelper = new CommandHelper(sender, args, 1, 2,
        IMPROPER_ARGUMENTS_MESSSAGE);
    commandHelper.setPermissionName("saveStats");
    if (!commandHelper.isProperCommand()) {
      return true; //Command Restricted or Improper
    }
    if (args.length == 1) {
      PeriodicSaving saveAll = new PeriodicSaving();
      saveAll.saveAllStats(false);
    } else if (args.length == 2) {
      String playerName = args[1];
      Player target = plugin.getServer().getPlayer(playerName);
      if (!CommandSupport.isTargetOnline(target, sender)) {
        return true; //Player not online
      }
      PlayerStatsLoadIn saveStats = new PlayerStatsLoadIn(target);
      try {
        saveStats.setPlayerStatsMap();
      } catch (IOException e) {
        FreeRPG.log(Level.ERROR, e.getMessage());
      }
    }
    return true;
  }
}
