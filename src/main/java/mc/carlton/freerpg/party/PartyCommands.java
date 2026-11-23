package mc.carlton.freerpg.party;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import mc.carlton.freerpg.core.info.server.WorldGuardChecks;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

/** The /party command. */
public final class PartyCommands implements CommandExecutor, TabCompleter {

  private static final List<String> SUBCOMMANDS = List.of("help", "create", "invite", "accept",
      "deny", "join", "leave", "list", "kick", "promote", "demote", "transfer", "disband", "open",
      "closed", "xpshare", "friendlyfire", "setspawn", "spawn", "tpa", "tpaccept", "tpdeny",
      "tptoggle", "receivexp");

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (!(sender instanceof Player p)) {
      sender.sendMessage(PartyMessages.text(null, "partyPlayersOnly"));
      return true;
    }
    if (!p.hasPermission("freerpg.party.use")) {
      p.sendMessage(PartyMessages.prefixed(p, "partyNoPermission"));
      return true;
    }
    if (args.length == 0) {
      PartyMenu.open(p);
      return true;
    }
    PartyManager manager = Parties.manager();
    UUID id = p.getUniqueId();
    long now = System.currentTimeMillis();
    String sub = args[0].toLowerCase(Locale.ROOT);
    switch (sub) {
      case "help":
        for (String line : PartyMessages.text(p, "partyHelp").split("\n")) {
          p.sendMessage(line);
        }
        return true;
      case "create":
        return create(p, args);
      case "invite":
        return invite(p, args);
      case "accept": {
        InviteManager.Invite invite = Parties.invites()
            .pending(id, args.length > 1 ? args[1] : null, now).orElse(null);
        if (invite == null) {
          PartyMessages.send(p, "partyNoInvite");
          return true;
        }
        PartyResult result = manager.join(id, p.getName(), invite.party(), true, now);
        if (result == PartyResult.OK) {
          Parties.invites().clear(id);
        } else {
          report(p, result, invite.party(), false);
        }
        return true;
      }
      case "deny":
        if (Parties.invites().deny(id, args.length > 1 ? args[1] : null, now)) {
          PartyMessages.send(p, "partyInviteDeclined");
        } else {
          PartyMessages.send(p, "partyNoInvite");
        }
        return true;
      case "join": {
        if (args.length < 2) {
          return usage(p, "/party join <party|owner>");
        }
        boolean hasInvite = Parties.invites().pending(id, args[1], now).isPresent();
        PartyResult result = manager.join(id, p.getName(), args[1], hasInvite, now);
        if (result == PartyResult.OK) {
          Parties.invites().clear(id);
        } else {
          report(p, result, args[1], false);
        }
        return true;
      }
      case "leave":
        report(p, manager.leave(id, now), "", false);
        return true;
      case "list":
        return list(p);
      case "kick":
      case "promote":
      case "demote":
      case "transfer":
        return memberAction(p, sub, args);
      case "disband":
        report(p, manager.disband(id), "", true);
        return true;
      case "open":
      case "closed":
        report(p, manager.setOpen(id, sub.equals("open")), "", false);
        return true;
      case "xpshare": {
        Party party = manager.partyOf(id).orElse(null);
        if (party == null) {
          PartyMessages.send(p, "partyNotIn");
          return true;
        }
        report(p, manager.setXpShare(id, toggle(args, party.xpShareEnabled())), "", false);
        return true;
      }
      case "friendlyfire": {
        Party party = manager.partyOf(id).orElse(null);
        if (party == null) {
          PartyMessages.send(p, "partyNotIn");
          return true;
        }
        report(p, manager.setFriendlyFire(id, toggle(args, party.friendlyFire())), "", false);
        return true;
      }
      case "setspawn":
        return setSpawn(p);
      case "spawn":
        return spawn(p);
      case "tpa":
        return tpa(p, args);
      case "tpaccept":
        return tpaccept(p);
      case "tpdeny":
        return tpdeny(p);
      case "tptoggle":
      case "receivexp":
        return personalToggle(p, sub, args);
      default:
        PartyMessages.send(p, "partyUnknown");
        return true;
    }
  }

  // ---------------------------------------------------------------- helpers

  private static boolean usage(Player p, String usage) {
    PartyMessages.send(p, "partyUsageLine", "usage", usage);
    return true;
  }

  /** "on"/"off" as the second argument, otherwise the opposite of the current value. */
  private static boolean toggle(String[] args, boolean current) {
    if (args.length > 1) {
      if (args[1].equalsIgnoreCase("on")) {
        return true;
      }
      if (args[1].equalsIgnoreCase("off")) {
        return false;
      }
    }
    return !current;
  }

  /** Tells the player what a failed result means. OK says nothing (notices do that). */
  static void report(Player p, PartyResult result, String name, boolean ownerOnly) {
    switch (result) {
      case OK:
        return;
      case NO_PERMISSION:
        PartyMessages.send(p, ownerOnly ? "partyOnlyOwner" : "partyNoRights");
        return;
      case NOT_IN_PARTY:
        PartyMessages.send(p, "partyNotIn");
        return;
      case ALREADY_IN_PARTY:
        PartyMessages.send(p, "partyAlreadyIn");
        return;
      case NAME_INVALID:
        PartyMessages.send(p, "partyNameInvalid");
        return;
      case NAME_TAKEN:
        PartyMessages.send(p, "partyNameTaken");
        return;
      case NOT_FOUND:
        PartyMessages.send(p, "partyNotFound", "name", name);
        return;
      case PARTY_FULL:
        PartyMessages.send(p, "partyFull");
        return;
      case PARTY_CLOSED:
        PartyMessages.send(p, "partyClosedParty");
        return;
      case TARGET_NOT_MEMBER:
        PartyMessages.send(p, "partyTargetNotMember", "player", name);
        return;
      case TARGET_PROTECTED:
        PartyMessages.send(p, "partyTargetProtected", "player", name);
        return;
      case SELF_TARGET:
        PartyMessages.send(p, "partySelf");
        return;
      default:
        return;
    }
  }

  /** A member of the party by name (they may be offline). */
  private static UUID findMember(Party party, String name) {
    for (PartyMember member : party.members()) {
      if (member.lastName() != null && member.lastName().equalsIgnoreCase(name)) {
        return member.uuid();
      }
    }
    return null;
  }

  // ----------------------------------------------------------- subcommands

  private boolean create(Player p, String[] args) {
    if (!p.hasPermission("freerpg.party.create")) {
      p.sendMessage(PartyMessages.prefixed(p, "partyNoPermission"));
      return true;
    }
    if (args.length < 2) {
      return usage(p, "/party create <name>");
    }
    PartyResult result = Parties.manager().create(p.getUniqueId(), p.getName(), args[1],
        System.currentTimeMillis());
    if (result != PartyResult.OK) {
      report(p, result, args[1], false);
      return true;
    }
    Party party = Parties.manager().partyOf(p.getUniqueId()).get();
    party.setFriendlyFire(PartyConfig.friendlyFireDefault());
    Parties.save(party);
    PartyMessages.send(p, "partyCreated", "party", party.name());
    return true;
  }

  private boolean invite(Player p, String[] args) {
    if (args.length < 2) {
      return usage(p, "/party invite <player>");
    }
    Party party = Parties.manager().partyOf(p.getUniqueId()).orElse(null);
    if (party == null) {
      PartyMessages.send(p, "partyNotIn");
      return true;
    }
    if (!PartyRules.can(PartyAction.INVITE, party.member(p.getUniqueId()).role())) {
      PartyMessages.send(p, "partyNoRights");
      return true;
    }
    Player target = Bukkit.getPlayerExact(args[1]);
    if (target == null) {
      PartyMessages.send(p, "partyNotOnline", "player", args[1]);
      return true;
    }
    if (target.equals(p)) {
      PartyMessages.send(p, "partySelf");
      return true;
    }
    if (Parties.manager().partyOf(target.getUniqueId()).isPresent()) {
      PartyMessages.send(p, "partyTargetInParty", "player", target.getName());
      return true;
    }
    Parties.invites().invite(p.getUniqueId(), target.getUniqueId(), party.name(),
        System.currentTimeMillis());
    PartyMessages.send(p, "partyInviteSent", "player", target.getName(), "party", party.name());
    PartyChat.sendInvite(target, p.getName(), party.name());
    return true;
  }

  private boolean list(Player p) {
    Party party = Parties.manager().partyOf(p.getUniqueId()).orElse(null);
    if (party == null) {
      PartyMessages.send(p, "partyNotIn");
      return true;
    }
    p.sendMessage(PartyMessages.text(p, "partyListHeader", "party", party.name(), "size",
        String.valueOf(party.size()), "max", String.valueOf(Parties.manager().maxSize()), "state",
        party.open() ? "@partyWordOpen" : "@partyWordClosed"));
    for (PartyMember member : party.members()) {
      p.sendMessage(PartyMessages.text(p, "partyListLine", "player",
          String.valueOf(member.lastName()), "role", "@" + roleKey(member.role()), "online",
          Parties.isOnline(member.uuid()) ? "@partyWordOnline" : "@partyWordOffline"));
    }
    return true;
  }

  static String roleKey(PartyRole role) {
    switch (role) {
      case OWNER:
        return "partyRoleOwner";
      case OFFICER:
        return "partyRoleOfficer";
      default:
        return "partyRoleMember";
    }
  }

  private boolean memberAction(Player p, String sub, String[] args) {
    if (args.length < 2) {
      return usage(p, "/party " + sub + " <player>");
    }
    Party party = Parties.manager().partyOf(p.getUniqueId()).orElse(null);
    if (party == null) {
      PartyMessages.send(p, "partyNotIn");
      return true;
    }
    UUID target = findMember(party, args[1]);
    if (target == null) {
      PartyMessages.send(p, "partyTargetNotMember", "player", args[1]);
      return true;
    }
    UUID id = p.getUniqueId();
    PartyResult result;
    switch (sub) {
      case "kick":
        result = Parties.manager().kick(id, target);
        break;
      case "promote":
        result = Parties.manager().promote(id, target);
        break;
      case "demote":
        result = Parties.manager().demote(id, target);
        break;
      default:
        result = Parties.manager().transfer(id, target);
        break;
    }
    report(p, result, args[1], !sub.equals("kick"));
    return true;
  }

  private boolean personalToggle(Player p, String sub, String[] args) {
    Party party = Parties.manager().partyOf(p.getUniqueId()).orElse(null);
    if (party == null) {
      PartyMessages.send(p, "partyNotIn");
      return true;
    }
    PartyMember member = party.member(p.getUniqueId());
    if (sub.equals("tptoggle")) {
      member.setAcceptTeleportRequests(toggle(args, member.acceptTeleportRequests()));
      PartyMessages.send(p, "partyTpRequests", "state",
          member.acceptTeleportRequests() ? "@partyWordOn" : "@partyWordOff");
    } else {
      member.setReceiveSharedXp(toggle(args, member.receiveSharedXp()));
      PartyMessages.send(p, "partyReceiveXp", "state",
          member.receiveSharedXp() ? "@partyWordOn" : "@partyWordOff");
    }
    Parties.save(party);
    return true;
  }

  // ------------------------------------------------------- spawn and tpa

  private boolean setSpawn(Player p) {
    if (!PartyConfig.spawnEnabled()) {
      PartyMessages.send(p, "partyTpSpawnDisabled");
      return true;
    }
    Location l = p.getLocation();
    if (PartyConfig.tpaBlockedWorlds().contains(l.getWorld().getName())
        || !new WorldGuardChecks().canEnter(p, l)) {
      PartyMessages.send(p, "partySpawnRefused");
      return true;
    }
    PartyResult result = Parties.manager().setSpawn(p.getUniqueId(),
        new PartySpawn(l.getWorld().getName(), l.getX(), l.getY(), l.getZ(), l.getYaw(),
            l.getPitch()));
    report(p, result, "", false);
    return true;
  }

  private boolean spawn(Player p) {
    Party party = Parties.manager().partyOf(p.getUniqueId()).orElse(null);
    if (party == null) {
      PartyMessages.send(p, "partyNotIn");
      return true;
    }
    if (!PartyConfig.spawnEnabled()) {
      PartyMessages.send(p, "partyTpSpawnDisabled");
      return true;
    }
    PartySpawn spawn = party.spawn().orElse(null);
    if (spawn == null) {
      PartyMessages.send(p, "partyTpNoSpawn");
      return true;
    }
    World world = Bukkit.getWorld(spawn.world());
    if (world == null) {
      PartyMessages.send(p, "partyTpSpawnWorldMissing");
      return true;
    }
    TeleportService.start(p, null,
        () -> new Location(world, spawn.x(), spawn.y(), spawn.z(), spawn.yaw(), spawn.pitch()),
        null);
    return true;
  }

  private boolean tpa(Player p, String[] args) {
    if (args.length < 2) {
      return usage(p, "/party tpa <member>");
    }
    Party party = Parties.manager().partyOf(p.getUniqueId()).orElse(null);
    if (party == null) {
      PartyMessages.send(p, "partyNotIn");
      return true;
    }
    Player target = Bukkit.getPlayerExact(args[1]);
    if (target == null) {
      PartyMessages.send(p, "partyNotOnline", "player", args[1]);
      return true;
    }
    if (target.equals(p)) {
      PartyMessages.send(p, "partyTpaSelf");
      return true;
    }
    PartyMember targetMember = party.member(target.getUniqueId());
    if (targetMember == null) {
      PartyMessages.send(p, "partyTpaNotMember", "player", target.getName());
      return true;
    }
    if (!targetMember.acceptTeleportRequests()) {
      PartyMessages.send(p, "partyTpaOff", "player", target.getName());
      return true;
    }
    if (target.getGameMode() == GameMode.SPECTATOR || !p.canSee(target)) {
      PartyMessages.send(p, "partyTpaUnavailable", "player", target.getName());
      return true;
    }
    String[] reason = TeleportService.blockReason(p, target.getLocation());
    if (reason != null) {
      String[] rest = new String[reason.length - 1];
      System.arraycopy(reason, 1, rest, 0, rest.length);
      PartyMessages.send(p, reason[0], rest);
      return true;
    }
    Parties.teleports().request(p.getUniqueId(), target.getUniqueId(), System.currentTimeMillis());
    PartyMessages.send(p, "partyTpaSent", "player", target.getName());
    PartyChat.sendTpaRequest(target, p.getName());
    return true;
  }

  private boolean tpaccept(Player target) {
    TeleportRequestManager.Request request = Parties.teleports()
        .accept(target.getUniqueId(), System.currentTimeMillis()).orElse(null);
    if (request == null) {
      PartyMessages.send(target, "partyTpaNone");
      return true;
    }
    Player requester = Bukkit.getPlayer(request.from());
    Party party = Parties.manager().partyOf(target.getUniqueId()).orElse(null);
    if (requester == null || party == null || !party.isMember(request.from())) {
      PartyMessages.send(target, "partyTpaUnavailable", "player", "?");
      return true;
    }
    UUID targetId = target.getUniqueId();
    TeleportService.start(requester, targetId, () -> {
      Player current = Bukkit.getPlayer(targetId);
      return current == null ? null : current.getLocation();
    }, null);
    return true;
  }

  private boolean tpdeny(Player target) {
    TeleportRequestManager.Request request = Parties.teleports()
        .deny(target.getUniqueId(), System.currentTimeMillis()).orElse(null);
    if (request == null) {
      PartyMessages.send(target, "partyTpaNone");
      return true;
    }
    PartyMessages.send(target, "partyTpaDeniedYou");
    PartyMessages.send(Bukkit.getPlayer(request.from()), "partyTpaDeniedBy", "player",
        target.getName());
    return true;
  }

  // ---------------------------------------------------------- tab complete

  @Override
  public List<String> onTabComplete(CommandSender sender, Command command, String alias,
      String[] args) {
    List<String> out = new ArrayList<>();
    if (!(sender instanceof Player p)) {
      return out;
    }
    if (args.length == 1) {
      for (String sub : SUBCOMMANDS) {
        if (sub.startsWith(args[0].toLowerCase(Locale.ROOT))) {
          out.add(sub);
        }
      }
      return out;
    }
    if (args.length != 2) {
      return out;
    }
    String prefix = args[1].toLowerCase(Locale.ROOT);
    switch (args[0].toLowerCase(Locale.ROOT)) {
      case "invite":
        for (Player online : Bukkit.getOnlinePlayers()) {
          add(out, online.getName(), prefix);
        }
        break;
      case "kick":
      case "promote":
      case "demote":
      case "transfer":
      case "tpa":
        Parties.manager().partyOf(p.getUniqueId()).ifPresent(party -> {
          for (PartyMember member : party.members()) {
            add(out, String.valueOf(member.lastName()), prefix);
          }
        });
        break;
      case "join":
        for (Party party : Parties.manager().all()) {
          if (party.open()) {
            add(out, party.name(), prefix);
            add(out, String.valueOf(party.owner().lastName()), prefix);
          }
        }
        break;
      case "xpshare":
      case "friendlyfire":
      case "tptoggle":
      case "receivexp":
        add(out, "on", prefix);
        add(out, "off", prefix);
        break;
      default:
        break;
    }
    return out;
  }

  private static void add(List<String> out, String value, String lowerPrefix) {
    if (value.toLowerCase(Locale.ROOT).startsWith(lowerPrefix)) {
      out.add(value);
    }
  }
}
