package mc.carlton.freerpg.weaponskills;

import java.io.File;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import mc.carlton.freerpg.FreeRPG;
import mc.carlton.freerpg.core.info.player.ChangeStats;
import mc.carlton.freerpg.utils.game.ActionBarMessages;
import org.apache.logging.log4j.Level;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Holds the weapon skill data of online players in memory and saves it to
 * plugins/FreeRPG/weaponSkills/{uuid}.yml. This is separate from the main FreeRPG player data.
 */
public final class WeaponSkillManager {

  private static final Map<UUID, Map<WeaponSkill, WeaponSkillData>> players =
      new ConcurrentHashMap<>();
  private static File folder;

  private WeaponSkillManager() {
  }

  public static void initialize(Plugin plugin) {
    folder = new File(plugin.getDataFolder(), "weaponSkills");
    if (!folder.exists() && !folder.mkdirs()) {
      FreeRPG.log(Level.ERROR, "Could not create the weaponSkills folder");
    }
  }

  private static File fileOf(UUID uuid) {
    return new File(folder, uuid + ".yml");
  }

  /** Returns the data of the player, loading it from the file the first time. */
  public static WeaponSkillData get(Player p, WeaponSkill skill) {
    Map<WeaponSkill, WeaponSkillData> skills = players.computeIfAbsent(p.getUniqueId(),
        uuid -> load(p));
    return skills.computeIfAbsent(skill, s -> new WeaponSkillData());
  }

  private static Map<WeaponSkill, WeaponSkillData> load(Player p) {
    Map<WeaponSkill, WeaponSkillData> skills = new EnumMap<>(WeaponSkill.class);
    File file = fileOf(p.getUniqueId());
    YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
    ChangeStats changeStats = new ChangeStats(p);
    for (WeaponSkill skill : WeaponSkill.values()) {
      WeaponSkillData data = new WeaponSkillData();
      long experience = yaml.getLong(skill.getId() + ".experience", 0L);
      data.setExperience(experience);
      data.setLevel(changeStats.getLevelfromEXP((int) Math.min(experience, Integer.MAX_VALUE)));
      ConfigurationSection passives = yaml.getConfigurationSection(skill.getId() + ".passives");
      if (passives != null) {
        for (String key : passives.getKeys(false)) {
          data.setPassive(key, passives.getInt(key));
        }
      }
      ConfigurationSection skillLevels = yaml.getConfigurationSection(skill.getId() + ".skills");
      if (skillLevels != null) {
        for (String key : skillLevels.getKeys(false)) {
          data.setSkill(key, skillLevels.getInt(key));
        }
      }
      data.setAbilityEnabled(yaml.getBoolean(skill.getId() + ".abilityEnabled", true));
      skills.put(skill, data);
    }
    return skills;
  }

  public static void save(UUID uuid) {
    Map<WeaponSkill, WeaponSkillData> skills = players.get(uuid);
    if (skills == null || folder == null) {
      return;
    }
    YamlConfiguration yaml = new YamlConfiguration();
    for (Map.Entry<WeaponSkill, WeaponSkillData> entry : skills.entrySet()) {
      yaml.set(entry.getKey().getId() + ".experience", entry.getValue().getExperience());
      yaml.set(entry.getKey().getId() + ".level", entry.getValue().getLevel());
      for (Map.Entry<String, Integer> passive : entry.getValue().getPassives().entrySet()) {
        yaml.set(entry.getKey().getId() + ".passives." + passive.getKey(), passive.getValue());
      }
      for (Map.Entry<String, Integer> skillLevel : entry.getValue().getSkills().entrySet()) {
        yaml.set(entry.getKey().getId() + ".skills." + skillLevel.getKey(), skillLevel.getValue());
      }
      yaml.set(entry.getKey().getId() + ".abilityEnabled", entry.getValue().isAbilityEnabled());
    }
    try {
      yaml.save(fileOf(uuid));
    } catch (IOException e) {
      FreeRPG.log(Level.ERROR, "Could not save weapon skills of " + uuid + ": " + e.getMessage());
    }
  }

  public static void saveAll() {
    for (UUID uuid : players.keySet()) {
      save(uuid);
    }
  }

  /** Saves the player's data and removes it from memory (when the player leaves). */
  public static void unload(UUID uuid) {
    save(uuid);
    players.remove(uuid);
  }

  /** Experience needed in total to reach the given level. */
  public static int getExpForLevel(Player p, int level) {
    return new ChangeStats(p).getEXPfromLevel(level);
  }

  /**
   * Returns {rank, total players} of a player in a weapon skill by experience, counting every
   * saved player file and the player's current (maybe unsaved) experience.
   */
  public static int[] getRank(Player p, WeaponSkill skill) {
    long mine = get(p, skill).getExperience();
    int higher = 0;
    int total = 0;
    boolean foundSelf = false;
    File[] files = folder == null ? null : folder.listFiles((dir, name) -> name.endsWith(".yml"));
    if (files != null) {
      for (File file : files) {
        String name = file.getName().substring(0, file.getName().length() - 4);
        long exp;
        if (name.equals(p.getUniqueId().toString())) {
          foundSelf = true;
          continue;
        }
        try {
          UUID other = UUID.fromString(name);
          Map<WeaponSkill, WeaponSkillData> loaded = players.get(other);
          exp = loaded != null && loaded.get(skill) != null ? loaded.get(skill).getExperience()
              : YamlConfiguration.loadConfiguration(file).getLong(skill.getId() + ".experience", 0L);
        } catch (IllegalArgumentException e) {
          continue;
        }
        total++;
        if (exp > mine) {
          higher++;
        }
      }
    }
    total++; // the player itself
    return new int[]{higher + 1, total};
  }

  /** Passive tokens earned so far (from the level) minus the ones already invested. */
  public static int getAvailablePassiveTokens(WeaponSkillData data) {
    double perToken = Math.max(0.01, WeaponSkillConfig.getDouble("tokens.levelsPerPassiveToken", 1));
    int earned = (int) Math.floor(data.getLevel() / perToken);
    return Math.max(0, earned - data.getTotalPassiveLevels());
  }

  /** Skill tokens earned so far (from the level) minus the skills already unlocked. */
  public static int getAvailableSkillTokens(WeaponSkillData data) {
    double perToken = Math.max(1, WeaponSkillConfig.getDouble("tokens.levelsPerSkillToken", 100));
    int earned = (int) Math.floor(data.getLevel() / perToken);
    return Math.max(0, earned - data.getTotalSkillLevels());
  }

  /**
   * Gives experience to a weapon skill. The amount is multiplied by the player's combat multiplier,
   * so personal multipliers and global perks apply like for other combat skills.
   */
  public static void addExp(Player p, WeaponSkill skill, double rawExp) {
    if (rawExp <= 0 || !WeaponSkillConfig.isEnabled()) {
      return;
    }
    ChangeStats changeStats = new ChangeStats(p);
    long exp = Math.round(rawExp * changeStats.getSkillMultiplier("swordsmanship"));
    if (exp <= 0) {
      return;
    }
    WeaponSkillData data = get(p, skill);
    data.setExperience(Math.min(Integer.MAX_VALUE, data.getExperience() + exp));
    int oldLevel = data.getLevel();
    int newLevel = changeStats.getLevelfromEXP((int) data.getExperience());
    data.setLevel(newLevel);
    new ActionBarMessages(p).sendMessage(
        skill.getColor() + "+" + exp + " " + skill.getDisplayName() + ChatColor.GRAY + " (Lv "
            + newLevel + ")");
    if (newLevel > oldLevel) {
      p.sendMessage(skill.getColor() + "" + ChatColor.BOLD + skill.getDisplayName()
          + ChatColor.RESET + ChatColor.GREEN + " reached level " + newLevel + "!");
      p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
      announceTokens(p, skill, oldLevel, newLevel, data);
    }
  }

  /** Tells the player when a level-up gave a new skill token. */
  private static void announceTokens(Player p, WeaponSkill skill, int oldLevel, int newLevel,
      WeaponSkillData data) {
    double perSkillToken = Math.max(1, WeaponSkillConfig.getDouble("tokens.levelsPerSkillToken",
        100));
    if (Math.floor(newLevel / perSkillToken) > Math.floor(oldLevel / perSkillToken)) {
      p.sendMessage(ChatColor.GOLD + "You earned a skill token for " + skill.getDisplayName()
          + "! Spend it in the skill tree (/frpg weapons).");
    }
  }
}
