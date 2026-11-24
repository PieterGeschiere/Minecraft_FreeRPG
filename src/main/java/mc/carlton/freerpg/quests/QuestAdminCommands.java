package mc.carlton.freerpg.quests;

import java.util.Locale;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /frpg quests reload | reset &lt;player&gt; */
public final class QuestAdminCommands {

  private QuestAdminCommands() {
  }

  /** Handles {@code /frpg quests ...}; args[0] is "quests". */
  public static boolean handle(CommandSender sender, String[] args) {
    if (!sender.hasPermission("freerpg.quests.admin")) {
      sender.sendMessage(QuestMessages.prefixed(null, "questNoPermission"));
      return true;
    }
    if (args.length < 2) {
      sender.sendMessage(QuestMessages.prefixed(null, "questAdminUsage"));
      return true;
    }
    switch (args[1].toLowerCase(Locale.ROOT)) {
      case "reload" -> {
        Quests.reload();
        sender.sendMessage(QuestMessages.prefixed(null, "questReloaded"));
      }
      case "reset" -> {
        if (args.length < 3) {
          sender.sendMessage(QuestMessages.prefixed(null, "questAdminUsage"));
          return true;
        }
        Player target = Bukkit.getPlayerExact(args[2]);
        if (target == null) {
          sender.sendMessage(QuestMessages.prefixed(null, "questUnknownPlayer", "player",
              args[2]));
          return true;
        }
        // /frpg quests reset <player> [daily|weekly|seasonal|all], daily when no period is given
        String which = args.length > 3 ? args[3].toLowerCase(Locale.ROOT) : "daily";
        QuestPeriod period = QuestPeriod.parse(which);
        if (period == null && !which.equals("all")) {
          sender.sendMessage(QuestMessages.prefixed(null, "questAdminUsage"));
          return true;
        }
        for (QuestPeriod each : QuestPeriod.values()) {
          if (period == null || period == each) {
            Quests.manager(each).resetPlayer(target.getUniqueId());
          }
        }
        sender.sendMessage(QuestMessages.prefixed(null, "questResetDone", "player",
            target.getName()));
      }
      default -> sender.sendMessage(QuestMessages.prefixed(null, "questAdminUsage"));
    }
    return true;
  }
}
