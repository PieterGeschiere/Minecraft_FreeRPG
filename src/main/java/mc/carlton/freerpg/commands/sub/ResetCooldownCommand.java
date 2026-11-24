package mc.carlton.freerpg.commands.sub;

import java.util.List;
import mc.carlton.freerpg.FreeRPG;
import mc.carlton.freerpg.commands.CommandHelper;
import mc.carlton.freerpg.core.info.player.AbilityTimers;
import mc.carlton.freerpg.skills.SkillRegistry;
import mc.carlton.freerpg.utils.UtilityMethods;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/** /frpg resetCooldown (moved verbatim from FrpgCommands). */
public class ResetCooldownCommand implements SubCommand {

  @Override
  public List<String> names() {
    return List.of("resetCooldown", "cooldownReset");
  }

  @Override
  public boolean execute(CommandSender sender, String[] args) {
    Plugin plugin = FreeRPG.getPlugin(FreeRPG.class);
    final String IMPROPER_ARGUMENTS_MESSSAGE = " /frpg resetCooldown [::playerName::] [::skillName::]";
    CommandHelper commandHelper = new CommandHelper(sender, args, 2, 3,
        IMPROPER_ARGUMENTS_MESSSAGE);
    commandHelper.setPermissionName("resetCooldown");
    if (!commandHelper.isProperCommand()) {
      return true; //Command Restricted or Improper
    }
    //Arguments --> Variables
    Player target;
    String skillNameInput;
    if (args.length == 2) {
      if (sender instanceof Player) {
        target = (Player) sender;
        skillNameInput = args[1];
      } else {
        commandHelper.sendImproperArgumentsMessage();
        return true; //Improper Arguments
      }
    } else {
      String playerName = args[1];
      skillNameInput = args[2];

      //Target Online Check
      target = plugin.getServer().getPlayer(playerName);
      boolean targetOnline = CommandSupport.isTargetOnline(target, sender);
      if (!targetOnline) {
        return true; //Target not online
      }
    }

    //Skill name match check
    List<String> labels = SkillRegistry.mainLabels();
    String skillName = UtilityMethods.convertStringToListCasing(labels, skillNameInput);
    if (!labels.contains(skillName)) {
      commandHelper.sendImproperArgumentsMessage();
      return true;
    }

    AbilityTimers abilityTimers = new AbilityTimers(target);
    abilityTimers.setPlayerCooldownTime(skillName, 0);
    return true;
  }
}
