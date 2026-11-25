package mc.carlton.freerpg.worldevents;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.bukkit.Material;

/** Built-in defaults of the settings that are lists, and readers that apply them. */
final class EventDefaults {

  static final List<String> HARVEST_SKILLS = List.of("farming", "fishing", "woodcutting");
  static final List<String> GOLD_RUSH_SKILLS = List.of("mining");

  static final List<String> HARVEST_BLOCKS = List.of("WHEAT", "CARROTS", "POTATOES", "BEETROOTS",
      "NETHER_WART", "MELON", "PUMPKIN", "SWEET_BERRY_BUSH", "COCOA", "OAK_LOG", "BIRCH_LOG",
      "SPRUCE_LOG", "JUNGLE_LOG", "ACACIA_LOG", "DARK_OAK_LOG", "CHERRY_LOG", "MANGROVE_LOG");

  static final List<String> GOLD_RUSH_BLOCKS = List.of("COAL_ORE", "DEEPSLATE_COAL_ORE",
      "IRON_ORE", "DEEPSLATE_IRON_ORE", "COPPER_ORE", "DEEPSLATE_COPPER_ORE", "GOLD_ORE",
      "DEEPSLATE_GOLD_ORE", "REDSTONE_ORE", "DEEPSLATE_REDSTONE_ORE", "LAPIS_ORE",
      "DEEPSLATE_LAPIS_ORE", "DIAMOND_ORE", "DEEPSLATE_DIAMOND_ORE", "EMERALD_ORE",
      "DEEPSLATE_EMERALD_ORE", "NETHER_GOLD_ORE", "NETHER_QUARTZ_ORE", "ANCIENT_DEBRIS");

  static final List<String> NOISE_MOBS = List.of("ZOMBIE", "SKELETON", "SPIDER", "CAVE_SPIDER",
      "CREEPER");

  private EventDefaults() {
  }

  /** A list setting, or the default when the setting is missing. */
  static List<String> list(WorldEventType type, String key, List<String> defaults) {
    return WorldEventConfig.isSet(type, key) ? WorldEventConfig.list(type, key) : defaults;
  }

  /** The skills that get the event's exp multiplier. */
  static Set<String> skills(WorldEventType type) {
    List<String> defaults = type == WorldEventType.GOLD_RUSH ? GOLD_RUSH_SKILLS : HARVEST_SKILLS;
    return Set.copyOf(list(type, "skills", defaults));
  }

  /** The blocks whose drops can come twice. Names that are not materials are ignored. */
  static Set<Material> dropBlocks(WorldEventType type) {
    List<String> defaults = type == WorldEventType.GOLD_RUSH ? GOLD_RUSH_BLOCKS : HARVEST_BLOCKS;
    return materials(list(type, "dropBlocks", defaults));
  }

  static Set<Material> materials(List<String> names) {
    Set<Material> materials = EnumSet.noneOf(Material.class);
    for (String name : names) {
      Material material = Material.matchMaterial(name);
      if (material != null) {
        materials.add(material);
      }
    }
    return materials;
  }

  /** The chance (0 to 1) that a drop comes twice. */
  static double dropChance(WorldEventType type) {
    return Math.max(0.0, Math.min(1.0, WorldEventConfig.number(type, "dropChance", 0.25)));
  }

  static double xpMultiplier(WorldEventType type) {
    return Math.max(1.0, WorldEventConfig.number(type, "xpMultiplier", 1.5));
  }
}
