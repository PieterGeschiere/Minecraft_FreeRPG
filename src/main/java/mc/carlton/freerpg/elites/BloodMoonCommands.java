package mc.carlton.freerpg.elites;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /frpg bloodmoon start | stop | status [world] */
public final class BloodMoonCommands {

  private BloodMoonCommands() {
  }

  /** Handles {@code /frpg bloodmoon ...}; args[0] is "bloodmoon". */
  public static boolean handle(CommandSender sender, String[] args) {
    Player player = sender instanceof Player p ? p : null;
    if (!sender.hasPermission("freerpg.elites.admin")) {
      sender.sendMessage(EliteMessages.prefixed(player, "eliteNoPermission"));
      return true;
    }
    if (args.length < 2) {
      sender.sendMessage(EliteMessages.prefixed(player, "bloodMoonUsage"));
      return true;
    }
    String action = args[1].toLowerCase(Locale.ROOT);
    if (!List.of("start", "stop", "status").contains(action)) {
      sender.sendMessage(EliteMessages.prefixed(player, "bloodMoonUsage"));
      return true;
    }
    World world;
    if (args.length >= 3) {
      world = Bukkit.getWorld(args[2]);
      if (world == null) {
        sender.sendMessage(EliteMessages.prefixed(player, "bloodMoonUnknownWorld", "world",
            args[2]));
        return true;
      }
    } else {
      world = player != null ? player.getWorld() : Bukkit.getWorlds().get(0);
    }
    switch (action) {
      case "start" -> {
        BloodMoonManager.start(world);
        sender.sendMessage(EliteMessages.prefixed(player, "bloodMoonStarted", "world",
            world.getName()));
      }
      case "stop" -> {
        BloodMoonManager.stop(world);
        sender.sendMessage(EliteMessages.prefixed(player, "bloodMoonStopped", "world",
            world.getName()));
      }
      default -> sender.sendMessage(EliteMessages.prefixed(player,
          BloodMoonManager.isActive(world) ? "bloodMoonStatusActive" : "bloodMoonStatusInactive",
          "world", world.getName()));
    }
    return true;
  }

  /** World names for tab completion. */
  public static List<String> worldNames() {
    List<String> names = new ArrayList<>();
    Bukkit.getWorlds().forEach(world -> names.add(world.getName()));
    return names;
  }
}
