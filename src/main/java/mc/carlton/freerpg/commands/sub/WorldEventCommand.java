package mc.carlton.freerpg.commands.sub;

import java.util.List;
import mc.carlton.freerpg.worldevents.WorldEventCommands;
import org.bukkit.command.CommandSender;

/** /frpg event start | stop | status | list. */
public class WorldEventCommand implements SubCommand {

  @Override
  public List<String> names() {
    return List.of("event");
  }

  @Override
  public boolean execute(CommandSender sender, String[] args) {
    return WorldEventCommands.handle(sender, args);
  }
}
