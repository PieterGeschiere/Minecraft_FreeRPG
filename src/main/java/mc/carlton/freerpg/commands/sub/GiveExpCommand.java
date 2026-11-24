package mc.carlton.freerpg.commands.sub;

import java.util.Arrays;
import java.util.List;
import mc.carlton.freerpg.FreeRPG;
import mc.carlton.freerpg.commands.CommandHelper;
import mc.carlton.freerpg.config.ConfigLoad;
import mc.carlton.freerpg.core.info.player.ChangeStats;
import mc.carlton.freerpg.skills.SkillRegistry;
import mc.carlton.freerpg.utils.UtilityMethods;
import mc.carlton.freerpg.utils.game.LanguageSelector;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/** /frpg giveEXP (moved verbatim from FrpgCommands). */
public class GiveExpCommand implements SubCommand {

  @Override
  public List<String> names() {
    return List.of("giveEXP", "expGive");
  }

  @Override
  public boolean execute(CommandSender sender, String[] args) {
    Plugin plugin = FreeRPG.getPlugin(FreeRPG.class);
    final String IMPROPER_ARGUMENTS_MESSSAGE = " /frpg giveEXP [::playerName::] [::skillName::] [::exp::]";
    CommandHelper commandHelper = new CommandHelper(sender, args, 3, 4,
        IMPROPER_ARGUMENTS_MESSSAGE);
    commandHelper.setPermissionName("giveEXP");
    if (!commandHelper.isProperCommand()) {
      return true; //Command Restricted or Improper
    }
    Player target;
    String skillNameInput;
    String expInput;
    if (args.length == 3) {
      if (sender instanceof Player) {
        target = (Player) sender;
        skillNameInput = args[1];
        expInput = args[2];
      } else {
        commandHelper.sendImproperArgumentsMessage();
        return true; //Improper Arguments
      }
    } else {
      String playerName = args[1];
      skillNameInput = args[2];
      expInput = args[3];

      //Target Online Check
      target = plugin.getServer().getPlayer(playerName);
      boolean targetOnline = CommandSupport.isTargetOnline(target, sender);
      if (!targetOnline) {
        return true; //Target not online
      }
    }

    int exp = CommandSupport.getArgumentAsInteger(expInput);
    if (exp == Integer.MAX_VALUE) {
      commandHelper.sendImproperArgumentsMessage();
      return true; //Improper argument (exp not integer)
    }

    String[] labels_0 = SkillRegistry.labelArray();
    List<String> labels_arr = Arrays.asList(labels_0);
    String skillName = UtilityMethods.convertStringToListCasing(labels_arr, skillNameInput);
    if (!labels_arr.contains(skillName)) {
      commandHelper.sendImproperArgumentsMessage();
      return true; //Improper argument (skillName not valid)
    }

    if (exp < 0) {
      if (sender instanceof Player) {
        Player p = (Player) sender;
        LanguageSelector lang = new LanguageSelector(p);
        p.sendMessage(ChatColor.RED + lang.getString("onlyIncrease"));
      } else {
        sender.sendMessage(
            "Please only increase exp with this command, otherwise, use /frpg statReset then /frpg giveEXP");
      }
      return true; //Improper argument (exp change not positive)
    }

    if (!new ConfigLoad().getAllowedSkillsMap().getOrDefault(skillName, true)) {
      sender.sendMessage(ChatColor.RED + "That skill is disabled (Archery is retired).");
      return true;
    }
    ChangeStats increaseStats = new ChangeStats(target);
    increaseStats.set_isCommand(true);
    increaseStats.changeEXP(skillName, exp);
    increaseStats.setTotalLevel();
    increaseStats.setTotalExperience();
    return true;
  }
}
