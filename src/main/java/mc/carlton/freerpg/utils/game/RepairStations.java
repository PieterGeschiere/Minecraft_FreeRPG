package mc.carlton.freerpg.utils.game;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import mc.carlton.freerpg.FreeRPG;
import org.apache.logging.log4j.Level;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

/**
 * Craftable repair and salvage stations. Only blocks placed from the crafted station items act as
 * a station, so ordinary iron and gold blocks can be used for building. Placed stations are stored
 * by location in stations.yml. When "stations.enabled" is false in advancedConfig.yml, the old
 * behaviour (every iron block repairs, every gold block salvages) is used.
 */
public class RepairStations implements Listener {

  public static final String REPAIR = "repair";
  public static final String SALVAGE = "salvage";

  private static final Map<String, String> stations = new HashMap<>();
  private static boolean enabled = true;
  private static File dataFile;
  private static NamespacedKey itemKey;
  private static NamespacedKey repairRecipeKey;
  private static NamespacedKey salvageRecipeKey;

  /** Reads the config, loads saved stations and registers the recipes. */
  public static void initialize(Plugin plugin) {
    itemKey = new NamespacedKey(plugin, "station_type");
    repairRecipeKey = new NamespacedKey(plugin, "repair_station");
    salvageRecipeKey = new NamespacedKey(plugin, "salvage_station");

    File advancedFile = new File(plugin.getDataFolder(), "advancedConfig.yml");
    YamlConfiguration advanced = YamlConfiguration.loadConfiguration(advancedFile);
    enabled = advanced.getBoolean("stations.enabled", true);

    dataFile = new File(plugin.getDataFolder(), "stations.yml");
    stations.clear();
    if (dataFile.exists()) {
      for (String entry : YamlConfiguration.loadConfiguration(dataFile)
          .getStringList("stations")) {
        int split = entry.lastIndexOf('|');
        if (split > 0) {
          stations.put(entry.substring(0, split), entry.substring(split + 1));
        }
      }
    }

    Bukkit.removeRecipe(repairRecipeKey);
    Bukkit.removeRecipe(salvageRecipeKey);
    if (!enabled) {
      return;
    }
    addRecipe(repairRecipeKey, REPAIR,
        material(advanced.getString("stations.repairRecipeCenter"), Material.IRON_BLOCK),
        material(advanced.getString("stations.repairRecipeSurround"), Material.IRON_INGOT));
    addRecipe(salvageRecipeKey, SALVAGE,
        material(advanced.getString("stations.salvageRecipeCenter"), Material.GOLD_BLOCK),
        material(advanced.getString("stations.salvageRecipeSurround"), Material.GOLD_INGOT));
    for (Player player : Bukkit.getOnlinePlayers()) {
      discoverRecipes(player);
    }
  }

  private static Material material(String name, Material fallback) {
    if (name == null) {
      return fallback;
    }
    Material material = Material.matchMaterial(name);
    return material == null ? fallback : material;
  }

  private static void addRecipe(NamespacedKey key, String type, Material center,
      Material surround) {
    ShapedRecipe recipe = new ShapedRecipe(key, createItem(type));
    recipe.shape("SSS", "SCS", "SSS");
    recipe.setIngredient('S', surround);
    recipe.setIngredient('C', center);
    Bukkit.addRecipe(recipe);
  }

  private static void discoverRecipes(Player player) {
    player.discoverRecipe(repairRecipeKey);
    player.discoverRecipe(salvageRecipeKey);
  }

  public static boolean isEnabled() {
    return enabled;
  }

  /** Creates the craftable station item of the given type. */
  public static ItemStack createItem(String type) {
    boolean repair = REPAIR.equals(type);
    ItemStack item = new ItemStack(repair ? Material.IRON_BLOCK : Material.GOLD_BLOCK, 1);
    ItemMeta meta = item.getItemMeta();
    meta.setDisplayName(
        (repair ? ChatColor.AQUA + "Repair Station" : ChatColor.GOLD + "Salvage Station"));
    List<String> lore = new ArrayList<>();
    lore.add(ChatColor.GRAY + (repair
        ? "Place it, then sneak + right-click it"
        : "Place it, then sneak + right-click it"));
    lore.add(ChatColor.GRAY + (repair
        ? "while holding a damaged item to repair it."
        : "while holding an item to salvage it."));
    meta.setLore(lore);
    meta.getPersistentDataContainer().set(itemKey, PersistentDataType.STRING, type);
    item.setItemMeta(meta);
    return item;
  }

  private static String itemType(ItemStack item) {
    if (item == null || !item.hasItemMeta()) {
      return null;
    }
    return item.getItemMeta().getPersistentDataContainer()
        .get(itemKey, PersistentDataType.STRING);
  }

  private static String key(Location location) {
    return location.getWorld().getName() + ";" + location.getBlockX() + ";"
        + location.getBlockY() + ";" + location.getBlockZ();
  }

  /** Returns "repair", "salvage" or null if the block is not a station. */
  public static String getStationType(Block block) {
    return stations.get(key(block.getLocation()));
  }

  /** True if the block works as a repair station (any iron block when stations are disabled). */
  public static boolean isRepairStation(Block block) {
    if (!enabled) {
      return block.getType() == Material.IRON_BLOCK;
    }
    return REPAIR.equals(getStationType(block));
  }

  /** True if the block works as a salvage station (any gold block when stations are disabled). */
  public static boolean isSalvageStation(Block block) {
    if (!enabled) {
      return block.getType() == Material.GOLD_BLOCK;
    }
    return SALVAGE.equals(getStationType(block));
  }

  private static void save() {
    YamlConfiguration yaml = new YamlConfiguration();
    List<String> entries = new ArrayList<>();
    for (Map.Entry<String, String> entry : stations.entrySet()) {
      entries.add(entry.getKey() + "|" + entry.getValue());
    }
    yaml.set("stations", entries);
    try {
      yaml.save(dataFile);
    } catch (IOException e) {
      FreeRPG.log(Level.ERROR, "Could not save stations.yml: " + e.getMessage());
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  void onPlace(BlockPlaceEvent e) {
    if (!enabled) {
      return;
    }
    String type = itemType(e.getItemInHand());
    if (type != null) {
      stations.put(key(e.getBlockPlaced().getLocation()), type);
      save();
    }
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  void onBreak(BlockBreakEvent e) {
    if (!enabled) {
      return;
    }
    Block block = e.getBlock();
    String type = getStationType(block);
    if (type == null) {
      return;
    }
    stations.remove(key(block.getLocation()));
    save();
    e.setDropItems(false);
    if (e.getPlayer().getGameMode() != GameMode.CREATIVE) {
      block.getWorld().dropItemNaturally(block.getLocation().add(0.5, 0.5, 0.5),
          createItem(type));
    }
  }

  // Stations are protected from explosions so they are never lost
  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  void onEntityExplode(EntityExplodeEvent e) {
    removeStationsFrom(e.blockList());
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  void onBlockExplode(BlockExplodeEvent e) {
    removeStationsFrom(e.blockList());
  }

  private void removeStationsFrom(List<Block> blocks) {
    if (!enabled) {
      return;
    }
    Iterator<Block> iterator = blocks.iterator();
    while (iterator.hasNext()) {
      if (getStationType(iterator.next()) != null) {
        iterator.remove();
      }
    }
  }

  // Pistons cannot move stations, which would leave the saved location behind
  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  void onPistonExtend(BlockPistonExtendEvent e) {
    if (enabled && containsStation(e.getBlocks())) {
      e.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  void onPistonRetract(BlockPistonRetractEvent e) {
    if (enabled && containsStation(e.getBlocks())) {
      e.setCancelled(true);
    }
  }

  private boolean containsStation(List<Block> blocks) {
    for (Block block : blocks) {
      if (getStationType(block) != null) {
        return true;
      }
    }
    return false;
  }

  @EventHandler
  void onJoin(PlayerJoinEvent e) {
    if (enabled) {
      discoverRecipes(e.getPlayer());
    }
  }
}
