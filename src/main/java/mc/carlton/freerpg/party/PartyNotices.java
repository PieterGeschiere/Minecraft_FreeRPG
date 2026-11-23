package mc.carlton.freerpg.party;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Tells party members what happened: roster, role and setting changes. Notices that matter
 * personally (kicked, promoted, new owner, disbanded) are kept for offline players and shown on
 * their next login.
 */
public final class PartyNotices {

  private static final Map<UUID, List<String>> pending = new HashMap<>();
  private static File file;

  private PartyNotices() {
  }

  public static void load(Plugin plugin) {
    file = new File(plugin.getDataFolder(), "partyNotices.yml");
    pending.clear();
    if (!file.exists()) {
      return;
    }
    YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
    for (String key : yaml.getKeys(false)) {
      try {
        pending.put(UUID.fromString(key), new ArrayList<>(yaml.getStringList(key)));
      } catch (IllegalArgumentException ignored) {
        // not a player id, skip
      }
    }
  }

  public static void save() {
    if (file == null) {
      return;
    }
    YamlConfiguration yaml = new YamlConfiguration();
    pending.forEach((uuid, list) -> {
      if (!list.isEmpty()) {
        yaml.set(uuid.toString(), list);
      }
    });
    try {
      yaml.save(file);
    } catch (IOException e) {
      Bukkit.getLogger().warning("Could not save partyNotices.yml: " + e.getMessage());
    }
  }

  /** Shows (and forgets) the notices a player missed. */
  public static void deliverPending(Player p) {
    List<String> list = pending.remove(p.getUniqueId());
    if (list == null) {
      return;
    }
    for (String line : list) {
      p.sendMessage(line);
    }
    save();
  }

  public static void handle(PartyEvent e) {
    Party party = e.party();
    String partyName = party.name();
    String actor = nameOf(e.actor(), party);
    String target = nameOf(e.target(), party);
    switch (e.type()) {
      case MEMBER_JOINED:
        if (e.actor() != null && e.actor().equals(e.target()) && party.size() == 1) {
          return; // the party was just created, the command answers itself
        }
        toMembers(party, e.target(), "partyNoticeJoined", "player", target);
        tell(e.target(), false, "partyJoined", "party", partyName);
        break;
      case MEMBER_LEFT:
        toMembers(party, null, "partyNoticeLeft", "player", target);
        tell(e.target(), false, "partyLeft", "party", partyName);
        break;
      case MEMBER_KICKED:
        toMembers(party, null, "partyNoticeKicked", "player", target, "actor", actor);
        tell(e.target(), true, "partyYouKicked", "party", partyName);
        break;
      case PROMOTED:
        toMembers(party, e.target(), "partyNoticePromoted", "player", target);
        tell(e.target(), true, "partyYouPromoted", "party", partyName);
        break;
      case DEMOTED:
        toMembers(party, e.target(), "partyNoticeDemoted", "player", target);
        tell(e.target(), true, "partyYouDemoted", "party", partyName);
        break;
      case OWNER_CHANGED:
        toMembers(party, e.target(), "partyNoticeOwner", "player", target);
        tell(e.target(), true, "partyYouOwner", "party", partyName);
        break;
      case OPEN_CHANGED:
        toMembers(party, null, "partyNoticeOpen", "actor", actor, "state",
            party.open() ? "@partyWordOpen" : "@partyWordClosed");
        break;
      case XP_SHARE_CHANGED:
        toMembers(party, null, "partyNoticeXpShare", "actor", actor, "state",
            party.xpShareEnabled() ? "@partyWordOn" : "@partyWordOff");
        break;
      case FRIENDLY_FIRE_CHANGED:
        toMembers(party, null, "partyNoticeFriendlyFire", "actor", actor, "state",
            party.friendlyFire() ? "@partyWordOn" : "@partyWordOff");
        break;
      case SPAWN_SET:
      case SPAWN_REMOVED:
        toMembers(party, null, "partyNoticeSpawn", "actor", actor);
        break;
      case DISBANDED:
        for (PartyMember member : party.members()) {
          tell(member.uuid(), true, "partyNoticeDisbanded", "party", partyName);
        }
        break;
      default:
        break;
    }
  }

  private static String nameOf(UUID uuid, Party party) {
    if (uuid == null) {
      return "the server";
    }
    PartyMember member = party.member(uuid);
    if (member != null && member.lastName() != null) {
      return member.lastName();
    }
    Player online = Bukkit.getPlayer(uuid);
    if (online != null) {
      return online.getName();
    }
    String name = Bukkit.getOfflinePlayer(uuid).getName();
    return name == null ? "someone" : name;
  }

  private static void toMembers(Party party, UUID exclude, String key, String... kv) {
    for (PartyMember member : party.members()) {
      if (member.uuid().equals(exclude)) {
        continue;
      }
      Player online = Bukkit.getPlayer(member.uuid());
      if (online != null) {
        PartyMessages.send(online, key, kv);
      }
    }
  }

  /** Tells one player; when they are offline and it matters, keeps it for their next login. */
  private static void tell(UUID uuid, boolean keepIfOffline, String key, String... kv) {
    if (uuid == null) {
      return;
    }
    Player online = Bukkit.getPlayer(uuid);
    if (online != null) {
      PartyMessages.send(online, key, kv);
    } else if (keepIfOffline) {
      pending.computeIfAbsent(uuid, k -> new ArrayList<>()).add(PartyMessages.plain(key, kv));
      save();
    }
  }
}
