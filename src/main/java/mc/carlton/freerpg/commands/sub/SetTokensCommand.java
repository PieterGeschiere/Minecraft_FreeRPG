package mc.carlton.freerpg.commands.sub;

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

/** /frpg setTokens (moved verbatim from FrpgCommands). */
public class SetTokensCommand implements SubCommand {

  @Override
  public List<String> names() {
    return List.of("setTokens", "tokensSet");
  }

  @Override
  public boolean execute(CommandSender sender, String[] args) {
    Plugin plugin = FreeRPG.getPlugin(FreeRPG.class);
    final String IMPROPER_ARGUMENTS_MESSSAGE = " /frpg setTokens [::playerName::] global [::amount::] or /frpg setTokens [::playerName::] [::skillName::] skill/passive [::amount::]";
    CommandHelper commandHelper = new CommandHelper(sender, args, 3, 5,
        IMPROPER_ARGUMENTS_MESSSAGE);
    commandHelper.setPermissionName("setTokens");
    if (!commandHelper.isProperCommand()) {
      return true; //Command Restricted or Improper
    }
    Player target;
    String skillNameInput;
    String tokenType;
    String amountInput;
    if (args.length == 3) { // Two inputs: tokenType and amount
      if (sender instanceof Player) {
        target = (Player) sender;
        tokenType = args[1];
        amountInput = args[2];
        skillNameInput = "global";
      } else {
        commandHelper.sendImproperArgumentsMessage();
        return true; //Improper Arguments
      }
    } else if (args.length == 4) {
      if (args[2].equalsIgnoreCase("global")) { //Three inputs: PlayerName, token type, amount
        String playerName = args[1];
        tokenType = args[2];
        skillNameInput = "global";
        amountInput = args[3];

        //Target Online Check
        target = plugin.getServer().getPlayer(playerName);
        boolean targetOnline = CommandSupport.isTargetOnline(target, sender);
        if (!targetOnline) {
          return true; //Target not online
        }
      } else { //Three inputs: Skillname, tokenType, amount
        if (!(sender instanceof Player)) {
          commandHelper.sendImproperArgumentsMessage();
          return true; //Improper Arguments (a console has no "self" to change)
        }
        target = (Player) sender;
        skillNameInput = args[1];
        tokenType = args[2];
        amountInput = args[3];
      }
    } else { //Four inputs: Playername, skillName, tokenType, amount
      String playerName = args[1];
      skillNameInput = args[2];
      tokenType = args[3];
      amountInput = args[4];

      //Target Online Check
      target = plugin.getServer().getPlayer(playerName);
      boolean targetOnline = CommandSupport.isTargetOnline(target, sender);
      if (!targetOnline) {
        return true; //Target not online
      }
    }

    String[] labels_0 = SkillRegistry.labelArrayWithGlobal();
    List<String> labels_arr = Arrays.asList(labels_0);
    String skillName = UtilityMethods.convertStringToListCasing(labels_arr, skillNameInput);
    if (!labels_arr.contains(skillName)) {
      commandHelper.sendImproperArgumentsMessage();
      return true; //Improper argument (skillName not valid)
    }

    int amount = CommandSupport.getArgumentAsInteger(amountInput);
    if (amount == Integer.MAX_VALUE) {
      commandHelper.sendImproperArgumentsMessage();
      return true; //Improper argument (amount not integer)
    }

    if (!(tokenType.equalsIgnoreCase("global") || tokenType.equalsIgnoreCase("skill")
        || tokenType.equalsIgnoreCase("passive"))) {
      commandHelper.sendImproperArgumentsMessage();
      return true; //Improper arguemnt (token type not global, skill, or passive)
    }

    ChangeStats increaseStats = new ChangeStats(target);
    if (tokenType.equalsIgnoreCase("global")) {
      increaseStats.setStat("global", 1, amount);
    } else if (tokenType.equalsIgnoreCase("passive")) {
      increaseStats.setStat(skillName, 2, amount);
    } else if (tokenType.equalsIgnoreCase("skill")) {
      increaseStats.setStat(skillName, 3, amount);
    }
    return true;
  }
}
