package mc.carlton.freerpg.elites;

import java.io.File;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.Plugin;

/** Reads elites.yml. Every value has a built-in default, so an old or incomplete file works. */
public final class EliteConfig {

  private static final List<String> DEFAULT_EXCLUDED = List.of("WARDEN", "ENDER_DRAGON", "WITHER",
      "ELDER_GUARDIAN", "GIANT", "ILLUSIONER");

  private static final Map<String, List<String>> DEFAULT_GEAR = Map.of(
      "helmet", List.of("LEATHER_HELMET", "CHAINMAIL_HELMET", "IRON_HELMET", "GOLDEN_HELMET"),
      "chestplate", List.of("LEATHER_CHESTPLATE", "CHAINMAIL_CHESTPLATE", "IRON_CHESTPLATE",
          "GOLDEN_CHESTPLATE"),
      "leggings", List.of("LEATHER_LEGGINGS", "CHAINMAIL_LEGGINGS", "IRON_LEGGINGS",
          "GOLDEN_LEGGINGS"),
      "boots", List.of("LEATHER_BOOTS", "CHAINMAIL_BOOTS", "IRON_BOOTS", "GOLDEN_BOOTS"),
      "weapon", List.of("STONE_SWORD", "IRON_SWORD", "STONE_AXE", "IRON_AXE"));

  private static final int[] DEFAULT_WEIGHTS = {60, 30, 10};

  private static YamlConfiguration config = new YamlConfiguration();

  private EliteConfig() {
  }

  public static void load(Plugin plugin) {
    File file = new File(plugin.getDataFolder(), "elites.yml");
    if (!file.exists()) {
      plugin.saveResource("elites.yml", false);
    }
    loadFrom(YamlConfiguration.loadConfiguration(file));
  }

  /** Reloads elites.yml from disk (admin command). */
  public static void reload(Plugin plugin) {
    load(plugin);
  }

  /** Uses an already parsed configuration (also used by the tests). */
  public static void loadFrom(YamlConfiguration loaded) {
    config = loaded;
  }

  private static double clamp01(double value) {
    return Math.max(0.0, Math.min(1.0, value));
  }

  public static boolean enabled() {
    return config.getBoolean("enabled", true);
  }

  /** The chance (0 to 1) that a natural hostile spawn becomes an elite. */
  public static double chance() {
    return clamp01(config.getDouble("chance", 0.03));
  }

  /** Weights for an elite getting 1, 2 or 3 affixes. */
  public static int[] affixCountWeights() {
    List<Integer> list = config.getIntegerList("affixCountWeights");
    if (list.size() != 3) {
      return DEFAULT_WEIGHTS.clone();
    }
    int[] weights = new int[3];
    int sum = 0;
    for (int i = 0; i < 3; i++) {
      weights[i] = Math.max(0, list.get(i));
      sum += weights[i];
    }
    return sum > 0 ? weights : DEFAULT_WEIGHTS.clone();
  }

  public static double healthMultiplier() {
    return Math.max(1.0, config.getDouble("healthMultiplier", 3.0));
  }

  public static double damageMultiplier() {
    return Math.max(1.0, config.getDouble("damageMultiplier", 1.5));
  }

  /** How much bigger an elite looks than a normal mob (1.25 = a quarter bigger). */
  public static double sizeScale() {
    return Math.max(1.0, Math.min(2.0, config.getDouble("sizeScale", 1.25)));
  }

  /**
   * How many degrees a bigger-than-normal archer (an elite or a Titan) misses by; a negative value
   * switches the help off. Vanilla skeletons miss by about 10 degrees on easy, 6 on normal and 2 on hard.
   */
  public static double bigShooterSpread() {
    return config.getDouble("bigShooterAim", 2.0);
  }

  public static int chunkCap() {
    return Math.max(0, config.getInt("chunkCap", 20));
  }

  public static Set<String> disabledWorlds() {
    return new HashSet<>(config.getStringList("disabledWorlds"));
  }

  public static Set<EntityType> excludedTypes() {
    List<String> names = config.isList("excludedTypes") ? config.getStringList("excludedTypes")
        : DEFAULT_EXCLUDED;
    Set<EntityType> types = EnumSet.noneOf(EntityType.class);
    for (String name : names) {
      try {
        types.add(EntityType.valueOf(name.toUpperCase(Locale.ROOT)));
      } catch (IllegalArgumentException ignored) {
        // an unknown mob name in the file is skipped
      }
    }
    return types;
  }

  public static double expMultiplier() {
    return Math.max(1.0, config.getDouble("rewards.expMultiplier", 3.0));
  }

  /** A chunk pays out elite rewards at most once per this many seconds (0 = no limit). */
  public static int rewardCooldownSeconds() {
    return Math.max(0, config.getInt("rewards.chunkCooldownSeconds", 120));
  }

  /** One extra drop an elite can give. */
  public record DropEntry(Material material, int min, int max, double chance) {
  }

  /** The extra drops of an elite kill. Entries with an unknown material are skipped. */
  public static List<DropEntry> drops() {
    List<DropEntry> entries = new ArrayList<>();
    if (!config.isList("rewards.drops")) {
      entries.add(new DropEntry(Material.DIAMOND, 1, 2, 0.15));
      entries.add(new DropEntry(Material.EMERALD, 1, 3, 0.25));
      entries.add(new DropEntry(Material.GOLD_INGOT, 2, 5, 0.40));
      return entries;
    }
    for (Map<?, ?> raw : config.getMapList("rewards.drops")) {
      Material material = Material.matchMaterial(String.valueOf(raw.get("material")));
      // Material.isAir() needs a running server, so the three air blocks are compared directly
      if (material == null || material == Material.AIR || material == Material.CAVE_AIR
          || material == Material.VOID_AIR) {
        continue;
      }
      int min = number(raw.get("min"), 1).intValue();
      int max = number(raw.get("max"), min).intValue();
      double chance = clamp01(number(raw.get("chance"), 1.0).doubleValue());
      entries.add(new DropEntry(material, Math.max(1, min), Math.max(Math.max(1, min), max),
          chance));
    }
    return entries;
  }

  private static Number number(Object value, Number def) {
    return value instanceof Number n ? n : def;
  }

  /** The numbers of one affix, for example affixDouble("thorns", "reflect", 0.25). */
  public static double affixDouble(String affixId, String key, double def) {
    return config.getDouble("affixes." + affixId + "." + key, def);
  }

  // ------------------------------------------------------------------ Blood Moon

  /** Whether nights can turn into a Blood Moon by themselves (admins can always start one). */
  public static boolean bloodMoonEnabled() {
    return config.getBoolean("bloodMoon.enabled", true);
  }

  public static double bloodMoonHealthBonus() {
    return Math.max(0.0, config.getDouble("bloodMoon.healthBonus", 0.5));
  }

  public static double bloodMoonDamageBonus() {
    return Math.max(0.0, config.getDouble("bloodMoon.damageBonus", 0.25));
  }

  /** The chance (0 to 1) that a night turns into a Blood Moon. */
  public static double bloodMoonChance() {
    return clamp01(config.getDouble("bloodMoon.chance", 0.0625));
  }

  public static double bloodMoonMultiplier() {
    return Math.max(1.0, config.getDouble("bloodMoon.eliteChanceMultiplier", 4.0));
  }

  public static Set<String> bloodMoonWorlds() {
    List<String> worlds = config.isList("bloodMoon.worlds") ? config.getStringList("bloodMoon.worlds")
        : List.of("world");
    return new HashSet<>(worlds);
  }

  /** The time of day (ticks) at which the nightly roll happens. */
  public static int bloodMoonStartTick() {
    return Math.max(0, Math.min(23999, config.getInt("bloodMoon.startTick", 13000)));
  }

  /** The time of day (ticks) at which the Blood Moon ends. */
  public static int bloodMoonEndTick() {
    return Math.max(0, Math.min(23999, config.getInt("bloodMoon.endTick", 23000)));
  }

  public static boolean bloodMoonEffect(String key, boolean def) {
    return config.getBoolean("bloodMoon.effects." + key, def);
  }

  public static double gearChance(String slot) {
    return clamp01(config.getDouble("bloodMoon.gear." + slot + ".chance", 0.35));
  }

  public static List<Material> gearTable(String slot) {
    String path = "bloodMoon.gear." + slot + ".materials";
    List<String> names = config.isList(path) ? config.getStringList(path)
        : DEFAULT_GEAR.getOrDefault(slot, List.of());
    List<Material> materials = new ArrayList<>();
    for (String name : names) {
      Material material = Material.matchMaterial(name);
      if (material != null) {
        materials.add(material);
      }
    }
    return materials;
  }
}
