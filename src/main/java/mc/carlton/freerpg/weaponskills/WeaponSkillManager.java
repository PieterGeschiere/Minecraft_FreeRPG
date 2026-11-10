package mc.carlton.freerpg.weaponskills;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import mc.carlton.freerpg.FreeRPG;
import mc.carlton.freerpg.config.ConfigLoad;
import mc.carlton.freerpg.core.info.player.ChangeStats;
import mc.carlton.freerpg.core.info.player.Leaderboards;
import mc.carlton.freerpg.core.info.player.PlayerStats;
import mc.carlton.freerpg.utils.game.ActionBarMessages;
import mc.carlton.freerpg.utils.game.BossBarStorage;
import mc.carlton.freerpg.utils.game.LanguageSelector;
import org.apache.logging.log4j.Level;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BossBar;
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

  static File getFolder() {
    return folder;
  }

  /** The data of an online (loaded) player, or null. */
  static Map<WeaponSkill, WeaponSkillData> getLoaded(UUID uuid) {
    return players.get(uuid);
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
      data.setExpBarEnabled(yaml.getBoolean(skill.getId() + ".expBarEnabled", true));
      data.setAbilityCooldownUntil(yaml.getLong(skill.getId() + ".cooldownUntil", 0L));
      data.setBonusSkillTokens(trainingBonus(p));
      skills.put(skill, data);
    }
    return skills;
  }

  /** The Training global perk gives one extra skill token to every fighting skill. */
  private static int trainingBonus(Player p) {
    try {
      Number owned = new mc.carlton.freerpg.core.info.player.PlayerStats(p).getPlayerData()
          .get("global").get(7);
      return owned.intValue() > 0 ? 1 : 0;
    } catch (RuntimeException e) {
      return 0;
    }
  }

  /** Re-reads the global perks, call after the player buys one. */
  public static void refreshPerkBonuses(Player p) {
    for (WeaponSkill skill : WeaponSkill.values()) {
      get(p, skill).setBonusSkillTokens(trainingBonus(p));
    }
  }

  public static void save(UUID uuid) {
    Map<WeaponSkill, WeaponSkillData> skills = players.get(uuid);
    if (skills == null || folder == null) {
      return;
    }
    YamlConfiguration yaml = new YamlConfiguration();
    Player online = org.bukkit.Bukkit.getPlayer(uuid);
    if (online != null) {
      yaml.set("name", online.getName());
    } else if (fileOf(uuid).exists()) {
      yaml.set("name", YamlConfiguration.loadConfiguration(fileOf(uuid)).getString("name"));
    }
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
      yaml.set(entry.getKey().getId() + ".expBarEnabled", entry.getValue().isExpBarEnabled());
      yaml.set(entry.getKey().getId() + ".cooldownUntil",
          entry.getValue().getAbilityCooldownUntil());
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
    File[] files = folder == null ? null : folder.listFiles((dir, name) -> name.endsWith(".yml"));
    if (files != null) {
      for (File file : files) {
        String name = file.getName().substring(0, file.getName().length() - 4);
        long exp;
        if (name.equals(p.getUniqueId().toString())) {
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
    int earned = (int) Math.floor(data.getLevel() / perToken) + data.getBonusSkillTokens();
    return Math.max(0, earned - data.getTotalSkillLevels());
  }

  /** Total of the levels of all weapon skills, which counts towards the global level. */
  public static int getTotalLevel(Player p) {
    int total = 0;
    for (WeaponSkill skill : WeaponSkill.values()) {
      total += get(p, skill).getLevel();
    }
    return total;
  }

  /** Total experience of all weapon skills, which counts towards the global experience. */
  public static int getTotalExperience(Player p) {
    long total = 0;
    for (WeaponSkill skill : WeaponSkill.values()) {
      total += get(p, skill).getExperience();
    }
    return (int) Math.min(Integer.MAX_VALUE, total);
  }

  /**
   * Gives experience to a weapon skill the way the main skills do: permission, creative mode,
   * enable switch, multipliers and maximum level are checked, the global level and experience grow,
   * and the exp bar and level-up messages are shown.
   */
  public static void addExp(Player p, WeaponSkill skill, double rawExp) {
    String key = skill.getConfigKey();
    if (rawExp <= 0 || !WeaponSkillConfig.isEnabled()
        || !WeaponSkillConfig.getBoolean(key + ".skillAllowed", true)
        || !p.hasPermission("freeRPG." + skill.getId() + "EXP")
        || p.getGameMode() == GameMode.CREATIVE) {
      return;
    }
    ChangeStats changeStats = new ChangeStats(p);
    double multiplier = changeStats.getSkillMultiplier("swordsmanship")
        * WeaponSkillConfig.getDouble(key + ".EXP_Multiplier", 1.0);
    int exp = (int) Math.round(rawExp * multiplier);
    if (exp <= 0) {
      return;
    }
    int maxLevel = WeaponSkillConfig.getInt(key + ".maxLevel", -1);
    if (maxLevel < 0) {
      maxLevel = Integer.MAX_VALUE;
    }
    WeaponSkillData data = get(p, skill);
    int oldLevel = data.getLevel();
    if (oldLevel >= maxLevel) {
      return;
    }
    long newExperience = Math.min(Integer.MAX_VALUE, data.getExperience() + exp);
    int newLevel = Math.min(maxLevel, changeStats.getLevelfromEXP((int) newExperience));
    data.setExperience(newExperience);
    data.setLevel(newLevel);

    ArrayList<Number> global = new PlayerStats(p).getPlayerData().get("global");
    global.set(29, global.get(29).intValue() + exp);
    showExpBar(p, changeStats, skill, (int) newExperience, oldLevel, newLevel, exp,
        newLevel >= maxLevel);
    if (newLevel > oldLevel) {
      levelUp(p, changeStats, skill, global, oldLevel, newLevel);
    }
    if (new ConfigLoad().isLeaderboardDyanmicUpdate()) {
      new Leaderboards().updatePlayerStats(p, "global", global.get(0).intValue(),
          global.get(29).intValue());
    }
  }

  /** The exp bar (boss bar) of the main skills, with the same toggle and look. */
  private static void showExpBar(Player p, ChangeStats changeStats, WeaponSkill skill, int exp,
      int oldLevel, int newLevel, int expChange, boolean maxed) {
    ArrayList<Number> global = new PlayerStats(p).getPlayerData().get("global");
    if (global.get(25).intValue() < 1 || !get(p, skill).isExpBarEnabled()) {
      return;
    }
    LanguageSelector lang = new LanguageSelector(p);
    String title = skill.getDisplayName();
    BossBar expBar = new BossBarStorage().getPlayerExpBar(p);
    if (newLevel > oldLevel) {
      expBar.setProgress(1.0);
      expBar.setVisible(true);
      changeStats.flashEXPBarText(
          title.toUpperCase() + " " + lang.getString("level").toUpperCase() + " " + newLevel + "!",
          ChatColor.GOLD, ChatColor.DARK_PURPLE, expBar);
      changeStats.removeEXPBar(expBar);
      return;
    }
    int levelForBar = maxed ? changeStats.getLevelfromEXP(exp) : oldLevel;
    int lastLevelExp = changeStats.getEXPfromLevel(levelForBar);
    int nextLevelExp = changeStats.getEXPfromLevel(levelForBar + 1);
    double progress = Math.max(exp - lastLevelExp, 0);
    double needed = Math.max(1, nextLevelExp - lastLevelExp);
    expBar.setProgress(Math.min(progress / needed, 1.0));
    if (!maxed) {
      expBar.setColor(BarColor.GREEN);
      expBar.setTitle(ChatColor.GRAY + title + ChatColor.GOLD + " " + lang.getString("lvl") + " "
          + newLevel + ChatColor.YELLOW + " (+" + expChange + " " + lang.getString("exp") + ")");
    } else {
      expBar.setColor(BarColor.BLUE);
      expBar.setTitle(ChatColor.LIGHT_PURPLE + "(" + lang.getString("virtual") + ") "
          + ChatColor.GRAY + title + ChatColor.GOLD + " " + lang.getString("lvl") + " "
          + levelForBar + ChatColor.YELLOW + " (+" + expChange + " " + lang.getString("exp")
          + ")");
    }
    expBar.setVisible(true);
    changeStats.removeEXPBar(expBar);
  }

  /**
   * The level-up of a main skill: the global level grows by the same amount, global tokens are
   * given, and the same banner, titles and sound are shown (when the player has them turned on).
   */
  private static void levelUp(Player p, ChangeStats changeStats, WeaponSkill skill,
      ArrayList<Number> global, int oldLevel, int newLevel) {
    ConfigLoad configLoad = new ConfigLoad();
    ArrayList<Double> tokensInfo = configLoad.getTokensInfo();
    LanguageSelector lang = new LanguageSelector(p);
    int levelChange = newLevel - oldLevel;

    // Global level and global tokens
    double levelsPerGlobal = tokensInfo.get(3);
    int oldGlobalLevel = global.get(0).intValue();
    int globalLevel = oldGlobalLevel + levelChange;
    int newTokensG = (int) Math.floor(globalLevel / levelsPerGlobal)
        - (int) Math.floor(oldGlobalLevel / levelsPerGlobal);
    boolean gainedGlobalToken = false;
    int extraGlobalTokens = -100;
    if (newTokensG > 0) {
      gainedGlobalToken = true;
      extraGlobalTokens = changeStats.areGlobalPerksMaxed(newTokensG);
      if (extraGlobalTokens >= 0) {
        newTokensG -= extraGlobalTokens;
        changeStats.changeStat("global", 23, extraGlobalTokens * tokensInfo.get(8));
      }
    }
    global.set(0, globalLevel);
    global.set(1, global.get(1).intValue() + newTokensG);

    // Tokens of the skill itself
    double perPassive = Math.max(0.01, WeaponSkillConfig.getDouble("tokens.levelsPerPassiveToken",
        1));
    double perSkill = Math.max(1, WeaponSkillConfig.getDouble("tokens.levelsPerSkillToken", 100));
    int newTokensP = (int) Math.floor(newLevel / perPassive) - (int) Math.floor(
        oldLevel / perPassive);
    int newTokensS = (int) Math.floor(newLevel / perSkill) - (int) Math.floor(oldLevel / perSkill);

    if (global.get(21).intValue() < 1) {
      return; // level-up messages are turned off
    }
    String skillTitle = skill.getDisplayName();
    if (gainedGlobalToken) {
      AbilityEffects.levelUp(p, skill);
      if (newTokensG > 0 && extraGlobalTokens <= 0) {
        p.sendTitle(ChatColor.DARK_PURPLE + lang.getString("globalPassiveTitle0") + " +"
                + newTokensG,
            ChatColor.YELLOW + lang.getString("try0") + " /frpg skillTree global", 5, 40, 20);
      } else {
        double multiplierIncrease = extraGlobalTokens * tokensInfo.get(8);
        p.sendTitle(ChatColor.DARK_PURPLE + lang.getString("expIncrease") + " +" + (
                multiplierIncrease * 100) + "%",
            ChatColor.RED + lang.getString("globalPassiveTitle0") + " +" + newTokensG, 5, 40,
            20);
      }
    }
    if (newTokensS > 0 || oldLevel < 3) {
      AbilityEffects.levelUp(p, skill);
      String bars = "------------------------------------------------";
      String levelUpMessage = skillTitle + " " + lang.getString("level") + " " + newLevel + "!";
      int spaces = bars.length() - levelUpMessage.length();
      if (spaces % 2 != 0) {
        bars += "-";
        spaces -= 1;
      }
      StringBuilder padding = new StringBuilder();
      for (int i = 0; i < spaces / 2.0; i++) {
        padding.append(' ');
      }
      p.sendMessage(bars);
      p.sendMessage(ChatColor.GREEN + ChatColor.BOLD.toString() + padding + levelUpMessage);
      if (newTokensP > 0) {
        p.sendMessage(ChatColor.LIGHT_PURPLE + "+" + newTokensP + " "
            + lang.getString("diggingPassiveTitle0"));
      }
      if (newTokensS > 0) {
        p.sendMessage(ChatColor.GOLD + "+" + newTokensS + " "
            + lang.getString("diggingPassiveTitle2"));
      }
      if (newTokensG > 0) {
        p.sendMessage(ChatColor.DARK_PURPLE + "+" + newTokensG + " "
            + lang.getString("globalPassiveTitle0"));
      }
      if (oldLevel < configLoad.getMaxLevelForBeginnerMessage()) {
        p.sendMessage("");
        p.sendMessage(ChatColor.ITALIC + lang.getString("passiveImprove"));
        p.sendMessage(ChatColor.ITALIC + lang.getString("try0") + " /frpg weapons");
      }
      p.sendMessage(bars);
    } else {
      new ActionBarMessages(p).sendMessage(
          ChatColor.YELLOW + skillTitle + " " + lang.getString("increasedBy") + " " + levelChange
              + ". (" + newLevel + ")");
    }
  }

  /** Administration: adds exact experience (no multipliers, no messages). */
  public static void giveExp(Player p, WeaponSkill skill, long exp) {
    WeaponSkillData data = get(p, skill);
    data.setExperience(Math.max(0, Math.min(Integer.MAX_VALUE, data.getExperience() + exp)));
    data.setLevel(new ChangeStats(p).getLevelfromEXP((int) data.getExperience()));
    enforceTokenLimits(data);
    syncGlobalTotals(p);
  }

  /** Administration: sets the level, with the experience the level needs. */
  public static void setLevel(Player p, WeaponSkill skill, int level) {
    WeaponSkillData data = get(p, skill);
    data.setExperience(new ChangeStats(p).getEXPfromLevel(Math.max(0, level)));
    data.setLevel(Math.max(0, level));
    enforceTokenLimits(data);
    syncGlobalTotals(p);
  }

  /** Administration: removes all experience and the whole skill tree. */
  public static void reset(Player p, WeaponSkill skill) {
    WeaponSkillData data = get(p, skill);
    data.setExperience(0);
    data.setLevel(0);
    data.resetTree();
    syncGlobalTotals(p);
  }

  /** Administration: ends the ability and removes its cooldown. */
  public static void resetCooldown(Player p, WeaponSkill skill) {
    WeaponSkillData data = get(p, skill);
    data.setAbilityActiveUntil(0);
    data.setAbilityCooldownUntil(0);
    data.setAbilityCharges(0);
  }

  /** Recalculates the global level and experience, which include the weapon skills. */
  private static void syncGlobalTotals(Player p) {
    ChangeStats changeStats = new ChangeStats(p);
    changeStats.setTotalLevel();
    changeStats.setTotalExperience();
  }

  /** If the level dropped below what the invested tokens need, the skill tree is reset. */
  private static void enforceTokenLimits(WeaponSkillData data) {
    double perPassive = Math.max(0.01, WeaponSkillConfig.getDouble("tokens.levelsPerPassiveToken",
        1));
    double perSkill = Math.max(1, WeaponSkillConfig.getDouble("tokens.levelsPerSkillToken", 100));
    if (data.getTotalPassiveLevels() > Math.floor(data.getLevel() / perPassive)
        || data.getTotalSkillLevels() > Math.floor(data.getLevel() / perSkill)) {
      data.resetTree();
    }
  }
}
