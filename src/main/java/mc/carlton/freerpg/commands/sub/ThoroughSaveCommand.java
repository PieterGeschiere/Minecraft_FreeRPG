package mc.carlton.freerpg.commands.sub;

import java.util.List;
import org.bukkit.command.CommandSender;
import mc.carlton.freerpg.FreeRPG;
import mc.carlton.freerpg.commands.CommandHelper;
import org.bukkit.plugin.Plugin;
import mc.carlton.freerpg.core.serverFileManagement.PeriodicSaving;
import org.bukkit.scheduler.BukkitRunnable;

/** /frpg thoroughSave (moved verbatim from FrpgCommands). */
public class ThoroughSaveCommand implements SubCommand {

  @Override
  public List<String> names() {
    return List.of("thoroughSave");
  }

  @Override
  public boolean execute(CommandSender sender, String[] args) {
    Plugin plugin = FreeRPG.getPlugin(FreeRPG.class);
    final String IMPROPER_ARGUMENTS_MESSSAGE = " /frpg save";
    CommandHelper commandHelper = new CommandHelper(sender, args, 0, IMPROPER_ARGUMENTS_MESSSAGE);
    commandHelper.setPermissionName("saveStats");
    if (!commandHelper.isProperCommand()) {
      return true; //Command Restricted or Improper
    }

    PeriodicSaving periodicSaving = new PeriodicSaving();
    new BukkitRunnable() {
      @Override
      public void run() {
        periodicSaving.saveAllStats(true);
      }
    }.runTaskAsynchronously(plugin);

    return true;
  }
}
