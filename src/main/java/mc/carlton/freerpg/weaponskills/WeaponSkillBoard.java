package mc.carlton.freerpg.weaponskills;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

/** The leaderboard of a weapon skill, built from the saved player files and online players. */
public final class WeaponSkillBoard {

  /** One player on a leaderboard. */
  public static final class Entry {

    private final UUID uuid;
    private final String name;
    private final int level;
    private final long experience;

    Entry(UUID uuid, String name, int level, long experience) {
      this.uuid = uuid;
      this.name = name;
      this.level = level;
      this.experience = experience;
    }

    public UUID getUuid() {
      return uuid;
    }

    public String getName() {
      return name;
    }

    public int getLevel() {
      return level;
    }

    public long getExperience() {
      return experience;
    }
  }

  private WeaponSkillBoard() {
  }

  /** All players with their level and experience in the skill, best first. */
  public static List<Entry> getSorted(WeaponSkill skill) {
    List<Entry> entries = new ArrayList<>();
    File folder = WeaponSkillManager.getFolder();
    File[] files = folder == null ? null : folder.listFiles((dir, name) -> name.endsWith(".yml"));
    if (files != null) {
      for (File file : files) {
        String fileName = file.getName().substring(0, file.getName().length() - 4);
        UUID uuid;
        try {
          uuid = UUID.fromString(fileName);
        } catch (IllegalArgumentException e) {
          continue;
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        long experience = yaml.getLong(skill.getId() + ".experience", 0L);
        int level = yaml.getInt(skill.getId() + ".level", 0);
        String name = yaml.getString("name");
        Map<WeaponSkill, WeaponSkillData> loaded = WeaponSkillManager.getLoaded(uuid);
        if (loaded != null && loaded.get(skill) != null) { // online players: current values
          experience = loaded.get(skill).getExperience();
          level = loaded.get(skill).getLevel();
          if (Bukkit.getPlayer(uuid) != null) {
            name = Bukkit.getPlayer(uuid).getName();
          }
        }
        if (name == null) {
          name = Bukkit.getOfflinePlayer(uuid).getName();
        }
        if (name == null) {
          name = uuid.toString().substring(0, 8);
        }
        entries.add(new Entry(uuid, name, level, experience));
      }
    }
    entries.sort(Comparator.comparingLong(Entry::getExperience).reversed());
    return entries;
  }

  /** 1-based rank of the player with this name in the skill, or -1 if he is not on the board. */
  public static int getRank(WeaponSkill skill, String playerName) {
    List<Entry> sorted = getSorted(skill);
    for (int i = 0; i < sorted.size(); i++) {
      if (sorted.get(i).getName().equalsIgnoreCase(playerName)) {
        return i + 1;
      }
    }
    return -1;
  }
}
