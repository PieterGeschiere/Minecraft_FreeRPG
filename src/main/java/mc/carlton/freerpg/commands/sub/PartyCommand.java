package mc.carlton.freerpg.commands.sub;

import java.util.List;
import org.bukkit.command.CommandSender;

/** /frpg party (moved verbatim from FrpgCommands). */
public class PartyCommand implements SubCommand {

  @Override
  public List<String> names() {
    return List.of("party");
  }

  @Override
  public boolean execute(CommandSender sender, String[] args) {
    return mc.carlton.freerpg.party.PartyAdminCommands.handle(sender, args);
  }
}
