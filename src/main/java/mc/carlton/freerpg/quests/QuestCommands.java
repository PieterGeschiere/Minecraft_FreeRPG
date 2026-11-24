package mc.carlton.freerpg.quests;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** The /quests command. */
public final class QuestCommands implements CommandExecutor {

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (!(sender instanceof Player p)) {
      sender.sendMessage(QuestMessages.text(null, "questPlayersOnly"));
      return true;
    }
    if (!p.hasPermission("freerpg.quests.use")) {
      QuestMessages.send(p, "questNoPermission");
      return true;
    }
    QuestMenu.open(p);
    return true;
  }
}
