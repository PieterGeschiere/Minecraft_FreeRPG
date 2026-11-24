package mc.carlton.freerpg.commands.sub;

import java.util.List;
import org.bukkit.command.CommandSender;

/** One /frpg sub-command (with its aliases). */
public interface SubCommand {

  /** The name and aliases this command answers to (matched case-insensitively). */
  List<String> names();

  boolean execute(CommandSender sender, String[] args);
}
