package mc.carlton.freerpg.commands.sub;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import mc.carlton.freerpg.FreeRPG;
import mc.carlton.freerpg.commands.CommandHelper;
import mc.carlton.freerpg.core.info.player.ChangeStats;
import mc.carlton.freerpg.skills.SkillRegistry;
import mc.carlton.freerpg.utils.UtilityMethods;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/** /frpg statReset (moved verbatim from FrpgCommands). */
public class StatResetCommand implements SubCommand {

  @Override
  public List<String> names() {
    return List.of("statReset", "resetStat");
  }

  @Override
  public boolean execute(CommandSender sender, String[] args) {
    Plugin plugin = FreeRPG.getPlugin(FreeRPG.class);
    final String IMPROPER_ARGUMENTS_MESSSAGE = " /frpg statReset [::playerName::] [::skillName::]";
    CommandHelper commandHelper = new CommandHelper(sender, args, 2, 3,
        IMPROPER_ARGUMENTS_MESSSAGE);
    commandHelper.setPermissionName("statReset");
    if (!commandHelper.isProperCommand()) {
      return true; //Command Restricted or Improper
    }
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

    String[] labels_0 = SkillRegistry.labelArrayWithGlobal();
    List<String> labels_arr = new ArrayList<>(Arrays.asList(labels_0));
    labels_arr.add("all");
    String skillName = UtilityMethods.convertStringToListCasing(labels_arr, skillNameInput);
    if (!labels_arr.contains(skillName)) {
      commandHelper.sendImproperArgumentsMessage();
      return true; //Improper argument (skillName not valid)
    }
    ChangeStats changeStats = new ChangeStats(target);
    if (!skillName.equalsIgnoreCase("all")) {
      changeStats.resetStat(skillName);
    } else {
      for (String skillNameLabel : labels_arr) {
        if (!skillNameLabel.equalsIgnoreCase("all")) {
          changeStats.resetStat(skillNameLabel);
        }
      }
      for (mc.carlton.freerpg.weaponskills.WeaponSkill weaponSkill
          : mc.carlton.freerpg.weaponskills.WeaponSkill.values()) {
        mc.carlton.freerpg.weaponskills.WeaponSkillManager.reset(target, weaponSkill);
      }
    }
    return true;
  }
}
