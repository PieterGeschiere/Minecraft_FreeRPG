package mc.carlton.freerpg.quests;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

/** Saves and loads one player's quests as one YAML file named after their UUID. */
public final class QuestStorage {

  static final int VERSION = 1;

  private final File folder;

  public QuestStorage(File folder) {
    this.folder = folder;
  }

  private File fileOf(UUID uuid) {
    return new File(folder, uuid + ".yml");
  }

  public void save(PlayerQuests data) {
    YamlConfiguration yaml = new YamlConfiguration();
    yaml.set("version", VERSION);
    yaml.set("day", data.dayKey());
    yaml.set("spin", data.spin().name());
    int index = 0;
    for (ActiveQuest quest : data.quests()) {
      String path = "quests." + index++;
      yaml.set(path + ".id", quest.id());
      yaml.set(path + ".type", quest.type().name());
      yaml.set(path + ".filter", quest.filter());
      yaml.set(path + ".target", quest.target());
      yaml.set(path + ".progress", quest.progress());
      yaml.set(path + ".name", quest.name());
    }
    try {
      Files.createDirectories(folder.toPath());
      File target = fileOf(data.uuid());
      File temp = new File(folder, target.getName() + ".tmp");
      yaml.save(temp);
      Files.move(temp.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      throw new IllegalStateException("Could not save quests of " + data.uuid(), e);
    }
  }

  /** Loads a player's quests. A missing, unreadable, unsupported or malformed file is empty. */
  public Optional<PlayerQuests> load(UUID uuid) {
    File file = fileOf(uuid);
    if (!file.exists()) {
      return Optional.empty();
    }
    try {
      YamlConfiguration yaml = new YamlConfiguration();
      yaml.load(file);
      if (yaml.getInt("version", -1) != VERSION) {
        return Optional.empty();
      }
      String day = yaml.getString("day");
      if (day == null) {
        return Optional.empty();
      }
      PlayerQuests data = new PlayerQuests(uuid, day);
      try {
        data.setSpin(SpinState.valueOf(yaml.getString("spin", "NONE")));
      } catch (IllegalArgumentException e) {
        data.setSpin(SpinState.NONE);
      }
      ConfigurationSection quests = yaml.getConfigurationSection("quests");
      if (quests != null) {
        for (String key : quests.getKeys(false)) {
          ConfigurationSection section = quests.getConfigurationSection(key);
          if (section == null) {
            return Optional.empty();
          }
          data.quests().add(new ActiveQuest(section.getString("id", key),
              QuestType.valueOf(section.getString("type", "")), section.getString("filter", "ANY"),
              section.getInt("target", 1), section.getInt("progress", 0),
              section.getString("name", key)));
        }
      }
      return Optional.of(data);
    } catch (Exception e) {
      return Optional.empty();
    }
  }
}
