package mc.carlton.freerpg.worldevents;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /frpg event start &lt;type&gt; [world] | stop | status | list */
public final class WorldEventCommands {

  private WorldEventCommands() {
  }

  /** The text to answer {@code start} with. */
  static String startReplyKey(WorldEventManager.StartResult result) {
    return switch (result) {
      case STARTED -> "worldEventStarted";
      case ALREADY_ACTIVE -> "worldEventAlreadyActive";
      case CANNOT_START -> "worldEventCannotStart";
      case DISABLED -> "worldEventDisabled";
      case NO_WORLD -> "worldEventUnknownWorld";
    };
  }

  /** The names of the types, for the command and tab completion. */
  public static List<String> typeNames() {
    List<String> names = new ArrayList<>();
    for (WorldEventType type : WorldEventType.values()) {
      names.add(type.commandName());
    }
    return names;
  }

  /** World names for tab completion. */
  public static List<String> worldNames() {
    List<String> names = new ArrayList<>();
    Bukkit.getWorlds().forEach(world -> names.add(world.getName()));
    return names;
  }

  /** Handles {@code /frpg event ...}; args[0] is "event". */
  public static boolean handle(CommandSender sender, String[] args) {
    Player player = sender instanceof Player p ? p : null;
    if (!sender.hasPermission("freerpg.events.admin")) {
      sender.sendMessage(WorldEventMessages.prefixed(player, "worldEventNoPermission"));
      return true;
    }
    if (args.length < 2) {
      sender.sendMessage(WorldEventMessages.prefixed(player, "worldEventUsage"));
      return true;
    }
    switch (args[1].toLowerCase(Locale.ROOT)) {
      case "start" -> start(sender, player, args);
      case "stop" -> {
        Optional<ActiveEvent> running = WorldEventManager.current();
        if (running.isPresent() && WorldEventManager.stop(true)) {
          sender.sendMessage(WorldEventMessages.prefixed(player, "worldEventStopped", "type",
              running.get().type().commandName()));
        } else {
          sender.sendMessage(WorldEventMessages.prefixed(player, "worldEventNone"));
        }
      }
      case "status" -> status(sender, player);
      case "list" -> list(sender, player);
      default -> sender.sendMessage(WorldEventMessages.prefixed(player, "worldEventUsage"));
    }
    return true;
  }

  private static void start(CommandSender sender, Player player, String[] args) {
    if (args.length < 3) {
      sender.sendMessage(WorldEventMessages.prefixed(player, "worldEventUsage"));
      return;
    }
    Optional<WorldEventType> type = WorldEventType.fromCommandName(args[2]);
    if (type.isEmpty()) {
      sender.sendMessage(WorldEventMessages.prefixed(player, "worldEventUnknownType", "type",
          args[2]));
      return;
    }
    World world;
    if (args.length >= 4) {
      world = Bukkit.getWorld(args[3]);
      if (world == null) {
        sender.sendMessage(WorldEventMessages.prefixed(player, "worldEventUnknownWorld", "world",
            args[3]));
        return;
      }
    } else {
      world = player != null ? player.getWorld() : Bukkit.getWorlds().get(0);
    }
    WorldEventManager.StartResult result = WorldEventManager.start(type.get(), world, true);
    sender.sendMessage(WorldEventMessages.prefixed(player, startReplyKey(result), "type",
        type.get().commandName(), "world", world.getName()));
  }

  private static void status(CommandSender sender, Player player) {
    Optional<ActiveEvent> running = WorldEventManager.current();
    if (running.isEmpty()) {
      sender.sendMessage(WorldEventMessages.prefixed(player, "worldEventNone"));
      return;
    }
    ActiveEvent event = running.get();
    sender.sendMessage(WorldEventMessages.prefixed(player, "worldEventStatus", "type",
        event.type().commandName(), "world",
        event.world() == null ? "*" : event.world().getName(), "time",
        EventVisuals.formatTime(WorldEventManager.ticksLeft(event))));
  }

  private static void list(CommandSender sender, Player player) {
    for (WorldEventType type : WorldEventType.values()) {
      sender.sendMessage(WorldEventMessages.prefixed(player, "worldEventListEntry", "type",
          type.commandName(), "state", WorldEventConfig.enabled(type) ? "on" : "off", "chance",
          String.format(Locale.ROOT, "%.1f", WorldEventConfig.chance(type) * 100)));
    }
  }
}
