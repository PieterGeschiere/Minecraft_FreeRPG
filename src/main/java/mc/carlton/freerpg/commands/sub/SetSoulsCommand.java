package mc.carlton.freerpg.commands.sub;

import java.util.List;
import mc.carlton.freerpg.FreeRPG;
import mc.carlton.freerpg.commands.CommandHelper;
import mc.carlton.freerpg.core.info.player.ChangeStats;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/** /frpg setSouls (moved verbatim from FrpgCommands). */
public class SetSoulsCommand implements SubCommand {

  @Override
  public List<String> names() {
    return List.of("setSouls", "soulsSet");
  }

  @Override
  public boolean execute(CommandSender sender, String[] args) {
    Plugin plugin = FreeRPG.getPlugin(FreeRPG.class);
    final String IMPROPER_ARGUMENTS_MESSSAGE = " /frpg setSouls [::playerName::] [::amount::]";
    CommandHelper commandHelper = new CommandHelper(sender, args, 2, 3,
        IMPROPER_ARGUMENTS_MESSSAGE);
    commandHelper.setPermissionName("setSouls");
    if (!commandHelper.isProperCommand()) {
      return true; //Command Restricted or Improper
    }
    Player target;
    String soulsInput;
    if (args.length == 2) {
      if (sender instanceof Player) {
        target = (Player) sender;
        soulsInput = args[1];
      } else {
        commandHelper.sendImproperArgumentsMessage();
        return true; //Improper Arguments
      }
    } else {
      String playerName = args[1];
      soulsInput = args[2];

      //Target Online Check
      target = plugin.getServer().getPlayer(playerName);
      boolean targetOnline = CommandSupport.isTargetOnline(target, sender);
      if (!targetOnline) {
        return true; //Target not online
      }
    }

    int souls = CommandSupport.getArgumentAsInteger(soulsInput);
    if (souls == Integer.MAX_VALUE) {
      commandHelper.sendImproperArgumentsMessage();
      return true; //Improper argument (souls not integer)
    }

    ChangeStats increaseStats = new ChangeStats(target);
    increaseStats.setStat("global", 20, souls);
    return true;
  }
}
