package mc.carlton.freerpg.commands.sub;

import java.util.List;
import org.bukkit.command.CommandSender;

/** /frpg bloodmoon start | stop | status. */
public class BloodMoonCommand implements SubCommand {

  @Override
  public List<String> names() {
    return List.of("bloodmoon");
  }

  @Override
  public boolean execute(CommandSender sender, String[] args) {
    return mc.carlton.freerpg.elites.BloodMoonCommands.handle(sender, args);
  }
}
