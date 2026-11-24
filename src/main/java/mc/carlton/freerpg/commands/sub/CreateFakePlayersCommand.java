package mc.carlton.freerpg.commands.sub;

import java.util.List;
import org.bukkit.command.CommandSender;
import mc.carlton.freerpg.FreeRPG;
import mc.carlton.freerpg.commands.CommandHelper;
import java.util.Random;
import java.util.UUID;
import mc.carlton.freerpg.core.serverFileManagement.PlayerStatsFilePreparation;

/** /frpg createFakePlayers (moved verbatim from FrpgCommands). */
public class CreateFakePlayersCommand implements SubCommand {

  @Override
  public List<String> names() {
    return List.of("createFakePlayers", "createFakeProfiles");
  }

  @Override
  public boolean execute(CommandSender sender, String[] args) {
    final String IMPROPER_ARGUMENTS_MESSSAGE = " /frpg createFakePlayers [#]";
    CommandHelper commandHelper = new CommandHelper(sender, args, 2, IMPROPER_ARGUMENTS_MESSSAGE);
    commandHelper.setPermissionName("createFakePlayers");
    if (!commandHelper.isProperCommand()) {
      return true; //Command Restricted or Improper
    }

    int numPlayers = CommandSupport.getArgumentAsInteger(args[1]);
    if (numPlayers == Integer.MAX_VALUE) {
      commandHelper.sendImproperArgumentsMessage();
      return true; //Improper input
    }

    PlayerStatsFilePreparation playerStatsFilePreparation = new PlayerStatsFilePreparation();
    Random rand = new Random();
    for (int i = 0; i < numPlayers; i++) {
      String fakeName = "FakePlayer" + rand.nextInt(100000);
      UUID fakeUUID = UUID.fromString("badf" + UUID.randomUUID().toString()
          .substring(4)); //"badf" hexadecimal identifier for "bad files"
      playerStatsFilePreparation.preparePlayerFile(fakeName, fakeUUID, false);
    }

    return true;
  }
}
