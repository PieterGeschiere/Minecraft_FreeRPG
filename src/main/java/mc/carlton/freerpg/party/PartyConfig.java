package mc.carlton.freerpg.party;

import java.io.File;
import java.util.List;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

/** Reads party.yml. Every value has a built-in default, so an old or incomplete file works. */
public final class PartyConfig {

  private static YamlConfiguration config = new YamlConfiguration();

  private PartyConfig() {
  }

  public static void load(Plugin plugin) {
    File file = new File(plugin.getDataFolder(), "party.yml");
    if (!file.exists()) {
      plugin.saveResource("party.yml", false);
    }
    config = YamlConfiguration.loadConfiguration(file);
  }

  /** Reloads party.yml from disk (admin command). */
  public static void reload(Plugin plugin) {
    load(plugin);
  }

  public static int maxSize() {
    return Math.max(2, config.getInt("maxSize", 5));
  }

  public static int inviteExpirySeconds() {
    return config.getInt("inviteExpirySeconds", 60);
  }

  public static boolean xpShareEnabled() {
    return config.getBoolean("xpShare.enabled", true);
  }

  public static double xpSharePercent() {
    return config.getDouble("xpShare.percent", 25.0);
  }

  public static double xpShareRadius() {
    return config.getDouble("xpShare.radius", 30.0);
  }

  public static int afkSeconds() {
    return config.getInt("xpShare.afkSeconds", 120);
  }

  public static boolean friendlyFireDefault() {
    return config.getBoolean("friendlyFire.default", false);
  }

  public static boolean spawnEnabled() {
    return config.getBoolean("spawn.enabled", true);
  }

  public static int tpaExpirySeconds() {
    return config.getInt("tpa.expirySeconds", 60);
  }

  public static int tpaCooldownSeconds() {
    return config.getInt("tpa.cooldownSeconds", 30);
  }

  public static int tpaWarmupSeconds() {
    return Math.max(1, config.getInt("tpa.warmupSeconds", 5));
  }

  public static boolean tpaAllowCrossWorld() {
    return config.getBoolean("tpa.allowCrossWorld", true);
  }

  public static List<String> tpaBlockedWorlds() {
    return config.getStringList("tpa.blockedWorlds");
  }

  public static int combatSeconds() {
    return config.getInt("tpa.combatSeconds", 10);
  }

  public static boolean effectsEnabled() {
    return config.getBoolean("tpa.effects.enabled", true);
  }

  /** Sounds for invites, requests, joins, leaves and promotions. */
  public static boolean notificationSounds() {
    return config.getBoolean("notificationSounds", true);
  }

  public static String effectParticle() {
    return config.getString("tpa.effects.particle", "PORTAL");
  }

  public static String effectBurstParticle() {
    return config.getString("tpa.effects.burstParticle", "REVERSE_PORTAL");
  }

  public static String effectSound() {
    return config.getString("tpa.effects.sound", "ENTITY_ENDERMAN_TELEPORT");
  }

  public static boolean displayTablist() {
    return config.getBoolean("display.tablist", true);
  }

  public static boolean displayNameplate() {
    return config.getBoolean("display.nameplate", true);
  }

  public static int ownerTransferDays() {
    return config.getInt("inactivity.ownerTransferDays", 30);
  }

  public static int deleteDays() {
    return config.getInt("inactivity.deleteDays", 60);
  }

  // ------------------------------------------------------------------ buffs

  public static boolean buffXpEnabled() {
    return config.getBoolean("buffs.xp.enabled", true);
  }

  /** Extra experience per nearby party member, as a fraction (0.03 = 3%). */
  public static double buffXpPerMember() {
    return Math.max(0.0, config.getDouble("buffs.xp.perMember", 0.03));
  }

  /** The most the buff can add in total, as a fraction (0.15 = 15%). */
  public static double buffXpMax() {
    return Math.max(0.0, config.getDouble("buffs.xp.max", 0.15));
  }

  /** How close (in blocks) a member has to be to count. */
  public static double buffRadius() {
    return Math.max(1.0, config.getDouble("buffs.xp.radius", 40.0));
  }
}
