package mc.carlton.freerpg.party;

import java.util.Locale;
import java.util.UUID;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** /frpg party disband|kick|reload|info, for administrators (permission freerpg.party.admin). */
public final class PartyAdminCommands {

  private PartyAdminCommands() {
  }

  private static void say(CommandSender sender, String key, String... kv) {
    Player player = sender instanceof Player ? (Player) sender : null;
    sender.sendMessage(PartyMessages.prefixed(player, key, kv));
  }

  /** @param args the arguments of /frpg, starting with "party" */
  public static boolean handle(CommandSender sender, String[] args) {
    if (!sender.hasPermission("freerpg.party.admin")) {
      say(sender, "partyNoPermission");
      return true;
    }
    if (args.length < 2) {
      say(sender, "partyAdminUsage");
      return true;
    }
    PartyManager manager = Parties.manager();
    switch (args[1].toLowerCase(Locale.ROOT)) {
      case "reload":
        Parties.reload();
        say(sender, "partyAdminReloaded");
        return true;
      case "disband":
        if (args.length < 3) {
          say(sender, "partyAdminUsage");
        } else if (manager.adminDisband(args[2]) == PartyResult.OK) {
          say(sender, "partyAdminDisbanded", "party", args[2]);
        } else {
          say(sender, "partyNotFound", "name", args[2]);
        }
        return true;
      case "kick": {
        if (args.length < 3) {
          say(sender, "partyAdminUsage");
          return true;
        }
        UUID target = findPlayer(args[2]);
        if (target != null && manager.adminKick(target) == PartyResult.OK) {
          say(sender, "partyAdminKicked", "player", args[2]);
        } else {
          say(sender, "partyAdminNoSuchMember", "player", args[2]);
        }
        return true;
      }
      case "info": {
        Party party = args.length < 3 ? null : manager.byName(args[2]).orElse(null);
        if (party == null) {
          say(sender, args.length < 3 ? "partyAdminUsage" : "partyNotFound", "name",
              args.length < 3 ? "" : args[2]);
          return true;
        }
        Player viewer = sender instanceof Player ? (Player) sender : null;
        sender.sendMessage(PartyMessages.text(viewer, "partyListHeader", "party", party.name(),
            "size", String.valueOf(party.size()), "max", String.valueOf(manager.maxSize()),
            "state", party.open() ? "@partyWordOpen" : "@partyWordClosed"));
        for (PartyMember member : party.members()) {
          sender.sendMessage(PartyMessages.text(viewer, "partyListLine", "player",
              String.valueOf(member.lastName()), "role", "@" + PartyCommands.roleKey(member.role()),
              "online", Parties.isOnline(member.uuid()) ? "@partyWordOnline" : "@partyWordOffline"));
        }
        return true;
      }
      default:
        say(sender, "partyAdminUsage");
        return true;
    }
  }

  /** A player in any party by last known name. */
  private static UUID findPlayer(String name) {
    for (Party party : Parties.manager().all()) {
      for (PartyMember member : party.members()) {
        if (member.lastName() != null && member.lastName().equalsIgnoreCase(name)) {
          return member.uuid();
        }
      }
    }
    return null;
  }
}
