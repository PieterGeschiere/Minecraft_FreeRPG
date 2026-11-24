package mc.carlton.freerpg.commands.sub;

import java.util.List;
import mc.carlton.freerpg.FreeRPG;
import mc.carlton.freerpg.commands.CommandHelper;
import mc.carlton.freerpg.core.info.player.ChangeStats;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/** /frpg addMultiplier (moved verbatim from FrpgCommands). */
public class AddMultiplierCommand implements SubCommand {

  @Override
  public List<String> names() {
    return List.of("addMultiplier", "changeMultiplier");
  }

  @Override
  public boolean execute(CommandSender sender, String[] args) {
    Plugin plugin = FreeRPG.getPlugin(FreeRPG.class);
    final String IMPROPER_ARGUMENTS_MESSSAGE = " /frpg addMultiplier [::playerName::] [::expIncrease::]";
    CommandHelper commandHelper = new CommandHelper(sender, args, 2, 3,
        IMPROPER_ARGUMENTS_MESSSAGE);
    commandHelper.setPermissionName("setMultiplier");
    if (!commandHelper.isProperCommand()) {
      return true; //Command Restricted or Improper
    }
    Player target;
    String multiplierInput;
    if (args.length == 2) {
      if (sender instanceof Player) {
        target = (Player) sender;
        multiplierInput = args[1];
      } else {
        commandHelper.sendImproperArgumentsMessage();
        return true; //Improper Arguments
      }
    } else {
      String playerName = args[1];
      multiplierInput = args[2];

      //Target Online Check
      target = plugin.getServer().getPlayer(playerName);
      boolean targetOnline = CommandSupport.isTargetOnline(target, sender);
      if (!targetOnline) {
        return true; //Target not online
      }
    }

    double multiplier = CommandSupport.getArgumentAsDouble(multiplierInput);
    if (multiplier == Double.MAX_VALUE) {
      commandHelper.sendImproperArgumentsMessage();
      return true; //Improper argument (amount not integer)
    }

    ChangeStats setMultiplier = new ChangeStats(target);
    setMultiplier.changeStat("global", 23, multiplier);
    return true;
  }
}
