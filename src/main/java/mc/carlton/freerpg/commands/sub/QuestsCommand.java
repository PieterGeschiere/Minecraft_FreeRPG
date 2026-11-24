package mc.carlton.freerpg.commands.sub;

import java.util.List;
import org.bukkit.command.CommandSender;

/** /frpg quests (moved verbatim from FrpgCommands). */
public class QuestsCommand implements SubCommand {

  @Override
  public List<String> names() {
    return List.of("quests");
  }

  @Override
  public boolean execute(CommandSender sender, String[] args) {
    return mc.carlton.freerpg.quests.QuestAdminCommands.handle(sender, args);
  }
}
