package mc.carlton.freerpg.quests;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * Wheel spins that were won (for example at a world event), not earned with the daily quests.
 * They are not tied to a day: a player keeps them until he uses them. Saved on every change.
 */
public final class BonusSpins {

  private final File file;
  private final Map<UUID, Integer> spins = new HashMap<>();

  public BonusSpins(File file) {
    this.file = file;
    load();
  }

  private void load() {
    if (!file.exists()) {
      return;
    }
    try {
      YamlConfiguration yaml = new YamlConfiguration();
      yaml.load(file);
      ConfigurationSection section = yaml.getConfigurationSection("spins");
      if (section == null) {
        return;
      }
      for (String key : section.getKeys(false)) {
        int count = section.getInt(key, 0);
        if (count > 0) {
          spins.put(UUID.fromString(key), count);
        }
      }
    } catch (Exception e) {
      spins.clear(); // an unreadable file means no spins
    }
  }

  public synchronized int count(UUID player) {
    return spins.getOrDefault(player, 0);
  }

  /** Gives spins. Zero and negative amounts are ignored. */
  public synchronized void add(UUID player, int amount) {
    if (amount <= 0) {
      return;
    }
    spins.merge(player, amount, Integer::sum);
    save();
  }

  /** Uses one spin. False when the player has none. */
  public synchronized boolean consume(UUID player) {
    int count = spins.getOrDefault(player, 0);
    if (count <= 0) {
      return false;
    }
    if (count == 1) {
      spins.remove(player);
    } else {
      spins.put(player, count - 1);
    }
    save();
    return true;
  }

  private void save() {
    YamlConfiguration yaml = new YamlConfiguration();
    for (Map.Entry<UUID, Integer> entry : spins.entrySet()) {
      yaml.set("spins." + entry.getKey(), entry.getValue());
    }
    try {
      File parent = file.getAbsoluteFile().getParentFile();
      Files.createDirectories(parent.toPath());
      File temp = new File(parent, file.getName() + ".tmp");
      yaml.save(temp);
      Files.move(temp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      throw new IllegalStateException("Could not save bonus spins", e);
    }
  }
}
