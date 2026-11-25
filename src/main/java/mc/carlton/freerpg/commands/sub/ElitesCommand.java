package mc.carlton.freerpg.commands.sub;

import java.util.List;
import org.bukkit.command.CommandSender;

/** /frpg elites reload | spawn. */
public class ElitesCommand implements SubCommand {

  @Override
  public List<String> names() {
    return List.of("elites");
  }

  @Override
  public boolean execute(CommandSender sender, String[] args) {
    return mc.carlton.freerpg.elites.EliteAdminCommands.handle(sender, args);
  }
}
