package mc.carlton.freerpg.party;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Consumer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

/** Saves and loads parties as one YAML file each in a folder. */
public final class PartyStorage {

  static final int VERSION = 1;

  private final File folder;

  public PartyStorage(File folder) {
    this.folder = folder;
  }

  private File fileOf(String partyName) {
    return new File(folder, partyName.toLowerCase(Locale.ROOT) + ".yml");
  }

  public void save(Party party) {
    YamlConfiguration yaml = new YamlConfiguration();
    yaml.set("version", VERSION);
    yaml.set("name", party.name());
    yaml.set("open", party.open());
    yaml.set("xpShare", party.xpShareEnabled());
    yaml.set("friendlyFire", party.friendlyFire());
    party.spawn().ifPresent(spawn -> {
      yaml.set("spawn.world", spawn.world());
      yaml.set("spawn.x", spawn.x());
      yaml.set("spawn.y", spawn.y());
      yaml.set("spawn.z", spawn.z());
      yaml.set("spawn.yaw", (double) spawn.yaw());
      yaml.set("spawn.pitch", (double) spawn.pitch());
    });
    List<String> order = new ArrayList<>();
    for (PartyMember member : party.members()) {
      String id = member.uuid().toString();
      order.add(id);
      yaml.set("members." + id + ".name", member.lastName());
      yaml.set("members." + id + ".role", member.role().name());
      yaml.set("members." + id + ".joinedAt", member.joinedAt());
      yaml.set("members." + id + ".lastSeen", member.lastSeen());
      yaml.set("members." + id + ".receiveSharedXp", member.receiveSharedXp());
      yaml.set("members." + id + ".acceptTeleportRequests", member.acceptTeleportRequests());
    }
    yaml.set("order", order);
    party.pendingNotices().forEach((uuid, notices) -> {
      if (!notices.isEmpty()) {
        yaml.set("pendingNotices." + uuid, new ArrayList<>(notices));
      }
    });
    try {
      Files.createDirectories(folder.toPath());
      File target = fileOf(party.name());
      File temp = new File(folder, target.getName() + ".tmp");
      yaml.save(temp);
      Files.move(temp.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      throw new IllegalStateException("Could not save party " + party.name(), e);
    }
  }

  public void delete(String partyName) {
    fileOf(partyName).delete();
  }

  /** Loads every readable party. Broken files are skipped and reported through {@code warn}. */
  public List<Party> loadAll(Consumer<String> warn) {
    List<Party> parties = new ArrayList<>();
    File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml"));
    if (files == null) {
      return parties;
    }
    for (File file : files) {
      try {
        Party party = load(file);
        if (party == null) {
          warn.accept("Skipped party file " + file.getName() + " (unsupported or empty)");
        } else {
          parties.add(party);
        }
      } catch (Exception e) {
        warn.accept("Skipped party file " + file.getName() + ": " + e.getMessage());
      }
    }
    return parties;
  }

  private Party load(File file) throws Exception {
    YamlConfiguration yaml = new YamlConfiguration();
    yaml.load(file);
    if (yaml.getInt("version", -1) != VERSION) {
      return null;
    }
    String name = yaml.getString("name");
    if (!PartyRules.isValidName(name)) {
      return null;
    }
    ConfigurationSection members = yaml.getConfigurationSection("members");
    if (members == null) {
      return null;
    }
    Party party = new Party(name);
    party.setOpen(yaml.getBoolean("open", false));
    party.setXpShareEnabled(yaml.getBoolean("xpShare", true));
    party.setFriendlyFire(yaml.getBoolean("friendlyFire", false));
    if (yaml.isConfigurationSection("spawn")) {
      party.setSpawn(new PartySpawn(yaml.getString("spawn.world", "world"),
          yaml.getDouble("spawn.x"), yaml.getDouble("spawn.y"), yaml.getDouble("spawn.z"),
          (float) yaml.getDouble("spawn.yaw"), (float) yaml.getDouble("spawn.pitch")));
    }
    List<String> order = yaml.getStringList("order");
    for (String key : members.getKeys(false)) {
      if (!order.contains(key)) {
        order.add(key);
      }
    }
    int owners = 0;
    for (String id : order) {
      ConfigurationSection section = members.getConfigurationSection(id);
      if (section == null) {
        continue;
      }
      PartyRole role = PartyRole.valueOf(section.getString("role", "MEMBER"));
      if (role == PartyRole.OWNER) {
        owners++;
      }
      PartyMember member = new PartyMember(UUID.fromString(id), section.getString("name"), role,
          section.getLong("joinedAt"));
      member.setLastSeen(section.getLong("lastSeen", member.joinedAt()));
      member.setReceiveSharedXp(section.getBoolean("receiveSharedXp", true));
      member.setAcceptTeleportRequests(section.getBoolean("acceptTeleportRequests", true));
      party.addMember(member);
    }
    if (party.size() == 0 || owners != 1) {
      return null;
    }
    ConfigurationSection notices = yaml.getConfigurationSection("pendingNotices");
    if (notices != null) {
      for (String id : notices.getKeys(false)) {
        UUID uuid = UUID.fromString(id);
        if (party.isMember(uuid)) {
          party.pendingNotices().put(uuid, new ArrayList<>(notices.getStringList(id)));
        }
      }
    }
    return party;
  }
}
