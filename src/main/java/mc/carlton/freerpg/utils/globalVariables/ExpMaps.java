package mc.carlton.freerpg.utils.globalVariables;

import java.util.HashMap;
import java.util.Map;
import mc.carlton.freerpg.config.ConfigLoad;
import mc.carlton.freerpg.core.info.server.MinecraftVersion;
import org.bukkit.Material;

public class ExpMaps {

  static Map<Material, Integer> diggingEXP = new HashMap<>();
  static Map<Material, Integer> woodcuttingEXP = new HashMap<>();
  static Map<Material, Integer> miningEXP = new HashMap<>();
  static Map<Material, Integer> farmingEXP = new HashMap<>();
  static Map<Material, Object[]> flamePickEXP = new HashMap<>();
  private final MinecraftVersion minecraftVersion = new MinecraftVersion();
  private final double mcVersion = minecraftVersion.getMinecraftVersionAsDouble();

  /**
   * Initializes all experience
   */
  public void initializeAllExpMaps() {
    initializeDiggingEXP();
    initializeWoodcuttingEXP();
    initializeMiningEXP();
    initializeFarmingEXP();
    initializeFlamePickEXP();
    initializeNewContentEXP();
  }

  /**
   * Adds experience for blocks introduced after 1.18. Materials are looked up by name so that a
   * material missing in some server version is simply skipped. Config keys that are missing (old
   * config files) fall back to the given default.
   */
  private static void putExp(Map<Material, Integer> target, Map<String, Integer> expMap,
      String key, int defaultExp, String... names) {
    int exp = expMap.getOrDefault(key, defaultExp);
    for (String name : names) {
      Material material = Material.matchMaterial(name);
      if (material != null) {
        target.put(material, exp);
      }
    }
  }

  /** Like putExp, but skips names that are items instead of blocks on this version. */
  private static void putBlockExp(Map<Material, Integer> target, Map<String, Integer> expMap,
      String key, int defaultExp, String... names) {
    int exp = expMap.getOrDefault(key, defaultExp);
    for (String name : names) {
      Material material = Material.matchMaterial(name);
      if (material != null && material.isBlock()) {
        target.put(material, exp);
      }
    }
  }

  private static String[] copperFamily(boolean cut) {
    String[] bases = cut
        ? new String[]{"CUT_COPPER", "EXPOSED_CUT_COPPER", "WEATHERED_CUT_COPPER",
        "OXIDIZED_CUT_COPPER"}
        : new String[]{"COPPER_BLOCK", "EXPOSED_COPPER", "WEATHERED_COPPER", "OXIDIZED_COPPER"};
    String[] all = new String[bases.length * 2];
    for (int i = 0; i < bases.length; i++) {
      all[i] = bases[i];
      all[i + bases.length] = "WAXED_" + bases[i];
    }
    return all;
  }

  public void initializeNewContentEXP() {
    ConfigLoad configLoad = new ConfigLoad();
    Map<String, Integer> mining = configLoad.getExpMapForSkill("mining");
    putExp(miningEXP, mining, "breakRaw_Gold_Block", 4500, "RAW_GOLD_BLOCK");
    putExp(miningEXP, mining, "breakCoal_Block", 1800, "COAL_BLOCK");
    putExp(miningEXP, mining, "breakIron_Block", 3150, "IRON_BLOCK");
    putExp(miningEXP, mining, "breakGold_Block", 4500, "GOLD_BLOCK");
    putExp(miningEXP, mining, "breakLapis_Block", 6300, "LAPIS_BLOCK");
    putExp(miningEXP, mining, "breakRedstone_Block", 4500, "REDSTONE_BLOCK");
    putExp(miningEXP, mining, "breakDiamond_Block", 9000, "DIAMOND_BLOCK");
    putExp(miningEXP, mining, "breakEmerald_Block", 11250, "EMERALD_BLOCK");
    putExp(miningEXP, mining, "breakNetherite_Block", 15000, "NETHERITE_BLOCK");
    putExp(miningEXP, mining, "breakCopper_Block", 3000, copperFamily(false));
    putExp(miningEXP, mining, "breakCut_Copper", 75, copperFamily(true));
    putExp(miningEXP, mining, "breakBudding_Amethyst", 250, "BUDDING_AMETHYST");
    putExp(miningEXP, mining, "breakSmall_Amethyst_Bud", 300, "SMALL_AMETHYST_BUD");
    putExp(miningEXP, mining, "breakMedium_Amethyst_Bud", 400, "MEDIUM_AMETHYST_BUD");
    putExp(miningEXP, mining, "breakLarge_Amethyst_Bud", 600, "LARGE_AMETHYST_BUD");
    putExp(miningEXP, mining, "breakCobbled_Deepslate", 30, "COBBLED_DEEPSLATE");
    putExp(miningEXP, mining, "breakPolished_Deepslate", 25, "POLISHED_DEEPSLATE",
        "DEEPSLATE_BRICKS", "DEEPSLATE_TILES", "CHISELED_DEEPSLATE");
    putExp(miningEXP, mining, "breakTuff_Variants", 25, "POLISHED_TUFF", "TUFF_BRICKS",
        "CHISELED_TUFF", "CHISELED_TUFF_BRICKS");
    putExp(miningEXP, mining, "breakPolished_Basalt", 25, "SMOOTH_BASALT", "POLISHED_BASALT");
    putExp(miningEXP, mining, "breakMud_Bricks", 25, "MUD_BRICKS");
    putExp(miningEXP, mining, "breakPacked_Mud", 25, "PACKED_MUD");
    putExp(miningEXP, mining, "breakResin", 25, "RESIN_BLOCK", "RESIN_BRICKS");
    // Cinnabar and sulfur stone (only the names that are blocks on this version count)
    putBlockExp(miningEXP, mining, "breakCinnabar", 25, "CINNABAR", "POLISHED_CINNABAR",
        "CINNABAR_BRICKS", "CHISELED_CINNABAR");
    putBlockExp(miningEXP, mining, "breakSulfur", 25, "SULFUR", "POLISHED_SULFUR",
        "SULFUR_BRICKS", "CHISELED_SULFUR", "SULFUR_SPIKE", "POTENT_SULFUR");

    Map<String, Integer> digging = configLoad.getExpMapForSkill("digging");
    putExp(diggingEXP, digging, "breakMud", 22, "MUD");
    putExp(diggingEXP, digging, "breakMuddy_Mangrove_Roots", 22, "MUDDY_MANGROVE_ROOTS");

    Map<String, Integer> wood = configLoad.getExpMapForSkill("woodcutting");
    String[][] woodTypes = {{"breakMangrove_Log", "MANGROVE"}, {"breakCherry_Log", "CHERRY"},
        {"breakPale_Oak_Log", "PALE_OAK"}, {"breakPoplar_Log", "POPLAR"}};
    for (String[] type : woodTypes) {
      putExp(woodcuttingEXP, wood, type[0], 100, type[1] + "_LOG", "STRIPPED_" + type[1] + "_LOG",
          type[1] + "_WOOD", "STRIPPED_" + type[1] + "_WOOD");
      putExp(woodcuttingEXP, wood, "breakNew_Planks", 25, type[1] + "_PLANKS");
      putExp(woodcuttingEXP, wood, "breakNew_Leaves", 13, type[1] + "_LEAVES");
    }
    // Poplar leaves come in three colors, so there is no POPLAR_LEAVES block
    putExp(woodcuttingEXP, wood, "breakNew_Leaves", 13, "ORANGE_POPLAR_LEAVES",
        "RED_POPLAR_LEAVES", "YELLOW_POPLAR_LEAVES");
    // Giant mushrooms and the nether trees (their wart blocks are the "leaves" of the huge fungi)
    putExp(woodcuttingEXP, wood, "breakMushroom_Stem", 125, "MUSHROOM_STEM");
    putExp(woodcuttingEXP, wood, "breakWart_Block", 13, "NETHER_WART_BLOCK", "WARPED_WART_BLOCK",
        "SHROOMLIGHT");
    putExp(woodcuttingEXP, wood, "breakBamboo_Block", 100, "BAMBOO_BLOCK",
        "STRIPPED_BAMBOO_BLOCK");
    putExp(woodcuttingEXP, wood, "breakNew_Planks", 25, "BAMBOO_PLANKS", "BAMBOO_MOSAIC");
    putExp(woodcuttingEXP, wood, "breakMangrove_Roots", 20, "MANGROVE_ROOTS",
        "MUDDY_MANGROVE_ROOTS");
    putExp(woodcuttingEXP, wood, "breakHyphae", 125, "CRIMSON_HYPHAE", "WARPED_HYPHAE",
        "STRIPPED_CRIMSON_HYPHAE", "STRIPPED_WARPED_HYPHAE");

    Map<String, Integer> farming = configLoad.getExpMapForSkill("farming");
    putExp(farmingEXP, farming, "breakPitcher_Crop", 50, "PITCHER_CROP", "TORCHFLOWER_CROP",
        "PITCHER_PLANT");
    putExp(farmingEXP, farming, "breakNew_Plants", 25, "CACTUS_FLOWER", "FIREFLY_BUSH", "BUSH",
        "WILDFLOWERS", "GOLDEN_DANDELION");
    putExp(farmingEXP, farming, "breakLeaf_Litter", 5, "LEAF_LITTER");
    putExp(farmingEXP, farming, "breakHoney_Block", 25, "HONEY_BLOCK");
    putExp(farmingEXP, farming, "breakHoneycomb_Block", 25, "HONEYCOMB_BLOCK");
    putExp(farmingEXP, farming, "breakBee_Nest", 75, "BEEHIVE", "BEE_NEST");
    putExp(farmingEXP, farming, "breakPale_Moss", 15, "PALE_MOSS_BLOCK", "PALE_HANGING_MOSS");
    putExp(farmingEXP, farming, "breakPale_Moss_Carpet", 5, "PALE_MOSS_CARPET");
  }

  public void initializeDiggingEXP() {
    ConfigLoad configLoad = new ConfigLoad();
    Map<String, Integer> expMap = configLoad.getExpMapForSkill("digging");
    diggingEXP.put(Material.CLAY, expMap.get("breakClay"));
    diggingEXP.put(Material.FARMLAND, expMap.get("breakFarmland"));
    diggingEXP.put(Material.GRASS_BLOCK, expMap.get("breakGrassBlock"));
    diggingEXP.put(Material.DIRT_PATH, expMap.get("breakGrassPath"));
    diggingEXP.put(Material.GRAVEL, expMap.get("breakGravel"));
    diggingEXP.put(Material.MYCELIUM, expMap.get("breakMycelium"));
    diggingEXP.put(Material.PODZOL, expMap.get("breakPodzol"));
    diggingEXP.put(Material.COARSE_DIRT, expMap.get("breakCoarse_Dirt"));
    diggingEXP.put(Material.DIRT, expMap.get("breakDirt"));
    diggingEXP.put(Material.RED_SAND, expMap.get("breakRed_Sand"));
    diggingEXP.put(Material.SAND, expMap.get("breakSand"));
    diggingEXP.put(Material.SOUL_SAND, expMap.get("breakSoulSand"));
    diggingEXP.put(Material.SNOW_BLOCK, expMap.get("breakSnowBlock"));
    diggingEXP.put(Material.SNOW, expMap.get("breakSnow"));
    diggingEXP.put(Material.WHITE_CONCRETE_POWDER, expMap.get("breakConcretePowder"));
    diggingEXP.put(Material.ORANGE_CONCRETE_POWDER, expMap.get("breakConcretePowder"));
    diggingEXP.put(Material.MAGENTA_CONCRETE_POWDER, expMap.get("breakConcretePowder"));
    diggingEXP.put(Material.LIGHT_BLUE_CONCRETE_POWDER, expMap.get("breakConcretePowder"));
    diggingEXP.put(Material.YELLOW_CONCRETE_POWDER, expMap.get("breakConcretePowder"));
    diggingEXP.put(Material.LIME_CONCRETE_POWDER, expMap.get("breakConcretePowder"));
    diggingEXP.put(Material.PINK_CONCRETE_POWDER, expMap.get("breakConcretePowder"));
    diggingEXP.put(Material.GRAY_CONCRETE_POWDER, expMap.get("breakConcretePowder"));
    diggingEXP.put(Material.LIGHT_GRAY_CONCRETE_POWDER, expMap.get("breakConcretePowder"));
    diggingEXP.put(Material.CYAN_CONCRETE_POWDER, expMap.get("breakConcretePowder"));
    diggingEXP.put(Material.PURPLE_CONCRETE_POWDER, expMap.get("breakConcretePowder"));
    diggingEXP.put(Material.BLUE_CONCRETE_POWDER, expMap.get("breakConcretePowder"));
    diggingEXP.put(Material.BROWN_CONCRETE_POWDER, expMap.get("breakConcretePowder"));
    diggingEXP.put(Material.GREEN_CONCRETE_POWDER, expMap.get("breakConcretePowder"));
    diggingEXP.put(Material.RED_CONCRETE_POWDER, expMap.get("breakConcretePowder"));
    diggingEXP.put(Material.BLACK_CONCRETE_POWDER, expMap.get("breakConcretePowder"));
    if (mcVersion >= 1.16) {
      diggingEXP.put(Material.SOUL_SOIL, expMap.get("breakSoulSoil"));
    }
    if (mcVersion >= 1.17) {
      diggingEXP.put(Material.ROOTED_DIRT, expMap.get("breakRooted_Dirt"));
    }
  }

  public void initializeWoodcuttingEXP() {
    ConfigLoad configLoad = new ConfigLoad();
    Map<String, Integer> expMap = configLoad.getExpMapForSkill("woodcutting");
    woodcuttingEXP.put(Material.ACACIA_LOG, expMap.get("breakAcacia_Log"));
    woodcuttingEXP.put(Material.BIRCH_LOG, expMap.get("breakBirch_Log"));
    woodcuttingEXP.put(Material.DARK_OAK_LOG, expMap.get("breakDark_Oak_Log"));
    woodcuttingEXP.put(Material.OAK_LOG, expMap.get("breakOak_Log"));
    woodcuttingEXP.put(Material.SPRUCE_LOG, expMap.get("breakSpruce_Log"));
    woodcuttingEXP.put(Material.JUNGLE_LOG, expMap.get("breakJungle_Log"));
    woodcuttingEXP.put(Material.STRIPPED_ACACIA_LOG, expMap.get("breakAcacia_Log"));
    woodcuttingEXP.put(Material.STRIPPED_BIRCH_LOG, expMap.get("breakBirch_Log"));
    woodcuttingEXP.put(Material.STRIPPED_DARK_OAK_LOG, expMap.get("breakDark_Oak_Log"));
    woodcuttingEXP.put(Material.STRIPPED_OAK_LOG, expMap.get("breakOak_Log"));
    woodcuttingEXP.put(Material.STRIPPED_SPRUCE_LOG, expMap.get("breakSpruce_Log"));
    woodcuttingEXP.put(Material.STRIPPED_JUNGLE_LOG, expMap.get("breakJungle_Log"));
    woodcuttingEXP.put(Material.ACACIA_WOOD, expMap.get("breakAcacia_Log"));
    woodcuttingEXP.put(Material.BIRCH_WOOD, expMap.get("breakBirch_Log"));
    woodcuttingEXP.put(Material.DARK_OAK_WOOD, expMap.get("breakDark_Oak_Log"));
    woodcuttingEXP.put(Material.OAK_WOOD, expMap.get("breakOak_Log"));
    woodcuttingEXP.put(Material.SPRUCE_WOOD, expMap.get("breakSpruce_Log"));
    woodcuttingEXP.put(Material.JUNGLE_WOOD, expMap.get("breakJungle_Log"));
    woodcuttingEXP.put(Material.STRIPPED_ACACIA_WOOD, expMap.get("breakAcacia_Log"));
    woodcuttingEXP.put(Material.STRIPPED_BIRCH_WOOD, expMap.get("breakBirch_Log"));
    woodcuttingEXP.put(Material.STRIPPED_DARK_OAK_WOOD, expMap.get("breakDark_Oak_Log"));
    woodcuttingEXP.put(Material.STRIPPED_OAK_WOOD, expMap.get("breakOak_Log"));
    woodcuttingEXP.put(Material.STRIPPED_SPRUCE_WOOD, expMap.get("breakSpruce_Log"));
    woodcuttingEXP.put(Material.STRIPPED_JUNGLE_WOOD, expMap.get("breakJungle_Log"));
    woodcuttingEXP.put(Material.ACACIA_PLANKS, expMap.get("breakAcacia_Plank"));
    woodcuttingEXP.put(Material.BIRCH_PLANKS, expMap.get("breakBirch_Plank"));
    woodcuttingEXP.put(Material.DARK_OAK_PLANKS, expMap.get("breakDark_Oak_Plank"));
    woodcuttingEXP.put(Material.OAK_PLANKS, expMap.get("breakOak_Plank"));
    woodcuttingEXP.put(Material.SPRUCE_PLANKS, expMap.get("breakSpruce_Plank"));
    woodcuttingEXP.put(Material.JUNGLE_PLANKS, expMap.get("breakJungle_Plank"));
    woodcuttingEXP.put(Material.ACACIA_LEAVES, expMap.get("breakAcacia_Leaves"));
    woodcuttingEXP.put(Material.BIRCH_LEAVES, expMap.get("breakBirch_Leaves"));
    woodcuttingEXP.put(Material.DARK_OAK_LEAVES, expMap.get("breakDark_Oak_Leaves"));
    woodcuttingEXP.put(Material.OAK_LEAVES, expMap.get("breakOak_Leaves"));
    woodcuttingEXP.put(Material.SPRUCE_LEAVES, expMap.get("breakSpruce_Leaves"));
    woodcuttingEXP.put(Material.JUNGLE_LEAVES, expMap.get("breakJungle_Leaves"));
    woodcuttingEXP.put(Material.BROWN_MUSHROOM_BLOCK, expMap.get("breakBrown_Mushroom_Block"));
    woodcuttingEXP.put(Material.RED_MUSHROOM_BLOCK, expMap.get("breakRed_Mushroom_Block"));
    //1.16 Blocks, EXP subject to change
    if (mcVersion >= 1.16) {
      woodcuttingEXP.put(Material.CRIMSON_STEM, expMap.get("breakCrimson_Stem"));
      woodcuttingEXP.put(Material.WARPED_STEM, expMap.get("breakWarped_Stem"));
      woodcuttingEXP.put(Material.STRIPPED_CRIMSON_STEM, expMap.get("breakCrimson_Stem"));
      woodcuttingEXP.put(Material.STRIPPED_WARPED_STEM, expMap.get("breakWarped_Stem"));
      woodcuttingEXP.put(Material.WARPED_PLANKS, expMap.get("breakWarped_Planks"));
      woodcuttingEXP.put(Material.CRIMSON_PLANKS, expMap.get("breakCrimson_Planks"));
    }
    if (mcVersion >= 1.17) {
      woodcuttingEXP.put(Material.FLOWERING_AZALEA_LEAVES,
          expMap.get("breakFlowering_Azalea_Leaves"));
    }
  }

  public void initializeMiningEXP() {
    ConfigLoad configLoad = new ConfigLoad();
    Map<String, Integer> expMap = configLoad.getExpMapForSkill("mining");
    miningEXP.put(Material.ICE, expMap.get("breakIce"));
    miningEXP.put(Material.BLUE_ICE, expMap.get("breakBlue_Ice"));
    miningEXP.put(Material.PACKED_ICE, expMap.get("breakPacked_Ice"));
    miningEXP.put(Material.FROSTED_ICE, expMap.get("breakFrosted_Ice"));
    miningEXP.put(Material.ANDESITE, expMap.get("breakAndesite"));
    miningEXP.put(Material.COAL_ORE, expMap.get("breakCoal_Ore"));
    miningEXP.put(Material.DIORITE, expMap.get("breakDiorite"));
    miningEXP.put(Material.END_STONE, expMap.get("breakEnd_Stone"));
    miningEXP.put(Material.GRANITE, expMap.get("breakGranite"));
    miningEXP.put(Material.NETHERRACK, expMap.get("breakNetherrack"));
    miningEXP.put(Material.NETHER_QUARTZ_ORE, expMap.get("breakNether_Quartz_Ore"));
    miningEXP.put(Material.MOSSY_COBBLESTONE, expMap.get("breakMossy_Cobblestone"));
    miningEXP.put(Material.SANDSTONE, expMap.get("breakSandstone"));
    miningEXP.put(Material.RED_SANDSTONE, expMap.get("breakRed_Sandstone"));
    miningEXP.put(Material.SPAWNER, expMap.get("breakSpawner"));
    miningEXP.put(Material.STONE, expMap.get("breakStone"));
    miningEXP.put(Material.TERRACOTTA, expMap.get("breakTerracotta"));
    miningEXP.put(Material.RED_TERRACOTTA, expMap.get("breakTerracotta"));
    miningEXP.put(Material.ORANGE_TERRACOTTA, expMap.get("breakTerracotta"));
    miningEXP.put(Material.YELLOW_TERRACOTTA, expMap.get("breakTerracotta"));
    miningEXP.put(Material.BROWN_TERRACOTTA, expMap.get("breakTerracotta"));
    miningEXP.put(Material.WHITE_TERRACOTTA, expMap.get("breakTerracotta"));
    miningEXP.put(Material.LIGHT_GRAY_TERRACOTTA, expMap.get("breakTerracotta"));
    miningEXP.put(Material.IRON_ORE, expMap.get("breakIron_Ore"));
    miningEXP.put(Material.REDSTONE_ORE, expMap.get("breakRedstone_Ore"));
    miningEXP.put(Material.LAPIS_ORE, expMap.get("breakLapis_Ore"));
    miningEXP.put(Material.DIAMOND_ORE, expMap.get("breakDiamond_Ore"));
    miningEXP.put(Material.GOLD_ORE, expMap.get("breakGold_Ore"));
    miningEXP.put(Material.EMERALD_ORE, expMap.get("breakEmerald_Ore"));
    miningEXP.put(Material.OBSIDIAN, expMap.get("breakObsidian"));
    miningEXP.put(Material.GLOWSTONE, 0);
    if (mcVersion >= 1.16) {
      miningEXP.put(Material.ANCIENT_DEBRIS, expMap.get("breakAncient_Debris"));
      miningEXP.put(Material.NETHER_GOLD_ORE, expMap.get("breakNether_Gold_Ore"));
      miningEXP.put(Material.BASALT, expMap.get("breakBasalt"));
      miningEXP.put(Material.BLACKSTONE, expMap.get("breakBlackstone"));
      miningEXP.put(Material.CRYING_OBSIDIAN, expMap.get("breakCrying_Obsidian"));
      miningEXP.put(Material.CRIMSON_NYLIUM, expMap.get("breakCrimson_Nylium"));
      miningEXP.put(Material.WARPED_NYLIUM, expMap.get("breakWarped_Nylium"));
      miningEXP.put(Material.GILDED_BLACKSTONE, expMap.get("breakGilded_Blackstone"));
    }
    if (mcVersion >= 1.17) {
      miningEXP.put(Material.AMETHYST_CLUSTER, expMap.get("breakAmethyst_Cluster"));
      miningEXP.put(Material.AMETHYST_BLOCK, expMap.get("breakAmethyst_Block"));
      miningEXP.put(Material.RAW_COPPER_BLOCK, expMap.get("breakRaw_Copper_Block"));
      miningEXP.put(Material.RAW_IRON_BLOCK, expMap.get("breakRaw_Iron_Block"));
      miningEXP.put(Material.CALCITE, expMap.get("breakCalcite"));
      miningEXP.put(Material.DEEPSLATE, expMap.get("breakDeepslate"));
      miningEXP.put(Material.COPPER_ORE, expMap.get("breakCopper_Ore"));
      miningEXP.put(Material.DEEPSLATE_COAL_ORE, expMap.get("breakDeepslate_Coal_Ore"));
      miningEXP.put(Material.DEEPSLATE_COPPER_ORE, expMap.get("breakDeepslate_Copper_Ore"));
      miningEXP.put(Material.DEEPSLATE_DIAMOND_ORE, expMap.get("breakDeepslate_Diamond_Ore"));
      miningEXP.put(Material.DEEPSLATE_EMERALD_ORE, expMap.get("breakDeepslate_Emerald_Ore"));
      miningEXP.put(Material.DEEPSLATE_GOLD_ORE, expMap.get("breakDeepslate_Gold_Ore"));
      miningEXP.put(Material.DEEPSLATE_IRON_ORE, expMap.get("breakDeepslate_Iron_Ore"));
      miningEXP.put(Material.DEEPSLATE_LAPIS_ORE, expMap.get("breakDeepslate_Lapis_Ore"));
      miningEXP.put(Material.DEEPSLATE_REDSTONE_ORE, expMap.get("breakDeepslate_Redstone_Ore"));
      miningEXP.put(Material.DRIPSTONE_BLOCK, expMap.get("breakDripstone_Block"));
      miningEXP.put(Material.POINTED_DRIPSTONE, expMap.get("breakPointed_Dripstone"));
      miningEXP.put(Material.TUFF, expMap.get("breakTuff"));
    }
  }

  public void initializeFarmingEXP() {
    ConfigLoad configLoad = new ConfigLoad();
    Map<String, Integer> expMap = configLoad.getExpMapForSkill("farming");
    farmingEXP.put(Material.WHEAT, expMap.get("breakWheat"));
    farmingEXP.put(Material.BEETROOTS, expMap.get("breakBeatRoots"));
    farmingEXP.put(Material.CARROTS, expMap.get("breakCarrots"));
    farmingEXP.put(Material.POTATOES, expMap.get("breakPotatoes"));
    farmingEXP.put(Material.MELON, expMap.get("breakMelon"));
    farmingEXP.put(Material.PUMPKIN, expMap.get("breakPumpkin"));
    farmingEXP.put(Material.BAMBOO, expMap.get("breakBamboo"));
    farmingEXP.put(Material.COCOA, expMap.get("breakCocoa"));
    farmingEXP.put(Material.SUGAR_CANE, expMap.get("breakSugarCane"));
    farmingEXP.put(Material.CACTUS, expMap.get("breakCactus"));
    farmingEXP.put(Material.RED_MUSHROOM, expMap.get("breakRed_Mushroom"));
    farmingEXP.put(Material.BROWN_MUSHROOM, expMap.get("breakBrown_Mushroom"));
    farmingEXP.put(Material.SWEET_BERRIES, expMap.get("breakSweetBerries"));
    farmingEXP.put(Material.KELP, expMap.get("breakKelp"));
    farmingEXP.put(Material.SEA_PICKLE, expMap.get("breakSea_Pickle"));
    farmingEXP.put(Material.NETHER_WART, expMap.get("breakNether_Wart"));
    farmingEXP.put(Material.CHORUS_PLANT, expMap.get("breakChorusPlant"));
    if (mcVersion >= 1.17) {
      farmingEXP.put(Material.AZALEA, expMap.get("breakAzalea"));
      farmingEXP.put(Material.FLOWERING_AZALEA, expMap.get("breakFlowering_Aealea"));
      farmingEXP.put(Material.CAVE_VINES, expMap.get("breakCave_Vines"));
      farmingEXP.put(Material.CAVE_VINES_PLANT, expMap.get("breakCave_Vines"));
      farmingEXP.put(Material.SMALL_DRIPLEAF, expMap.get("breakSmall_Dripleaf"));
      farmingEXP.put(Material.GLOW_LICHEN, expMap.get("breakGlow_Lichen"));
      farmingEXP.put(Material.HANGING_ROOTS, expMap.get("breakHanging_Roots"));
      farmingEXP.put(Material.MOSS_BLOCK, expMap.get("breakMoos_block"));
      farmingEXP.put(Material.SPORE_BLOSSOM, expMap.get("breakSpore_Blossom"));
      // Not sure if this is a great idea
      farmingEXP.put(Material.BIG_DRIPLEAF, expMap.get("breakBig_Dripleaf"));
      farmingEXP.put(Material.BIG_DRIPLEAF_STEM, expMap.get("breakBig_Dripleaf"));
    }
  }

  public void initializeFlamePickEXP() {
    ConfigLoad configLoad = new ConfigLoad();
    Map<String, Map<String, Integer>> expMap = configLoad.getExpMap();
    flamePickEXP.put(Material.IRON_ORE,
        new Object[]{"mining", expMap.get("mining").get("breakIron_Ore")});
    flamePickEXP.put(Material.GOLD_ORE,
        new Object[]{"mining", expMap.get("mining").get("breakGold_Ore")});
    flamePickEXP.put(Material.COBBLESTONE, new Object[]{"mining", 0});
    flamePickEXP.put(Material.STONE,
        new Object[]{"mining", expMap.get("mining").get("breakStone")});
    flamePickEXP.put(Material.SANDSTONE,
        new Object[]{"mining", expMap.get("mining").get("breakSandstone")});
    flamePickEXP.put(Material.RED_SANDSTONE,
        new Object[]{"mining", expMap.get("mining").get("breakRed_Sandstone")});
    flamePickEXP.put(Material.SAND,
        new Object[]{"digging", expMap.get("digging").get("breakSand")});
    flamePickEXP.put(Material.QUARTZ_BLOCK, new Object[]{"mining", 0});
    flamePickEXP.put(Material.NETHERRACK,
        new Object[]{"mining", expMap.get("mining").get("breakNetherrack")});
    flamePickEXP.put(Material.CLAY,
        new Object[]{"digging", expMap.get("digging").get("breakClay")});
    flamePickEXP.put(Material.STONE_BRICKS, new Object[]{"mining", 0});
    flamePickEXP.put(Material.WET_SPONGE, new Object[]{"mining", 0});
    flamePickEXP.put(Material.CACTUS,
        new Object[]{"farming", expMap.get("farming").get("breakCactus")});
    flamePickEXP.put(Material.JUNGLE_LOG,
        new Object[]{"woodcutting", expMap.get("woodcutting").get("breakJungle_Log")});
    flamePickEXP.put(Material.SPRUCE_LOG,
        new Object[]{"woodcutting", expMap.get("woodcutting").get("breakSpruce_Log")});
    flamePickEXP.put(Material.OAK_LOG,
        new Object[]{"woodcutting", expMap.get("woodcutting").get("breakOak_Log")});
    flamePickEXP.put(Material.DARK_OAK_LOG,
        new Object[]{"woodcutting", expMap.get("woodcutting").get("breakDark_Oak_Log")});
    flamePickEXP.put(Material.BIRCH_LOG,
        new Object[]{"woodcutting", expMap.get("woodcutting").get("breakBirch_Log")});
    flamePickEXP.put(Material.ACACIA_LOG,
        new Object[]{"woodcutting", expMap.get("woodcutting").get("breakAcacia_Log")});
    flamePickEXP.put(Material.STRIPPED_JUNGLE_LOG,
        new Object[]{"woodcutting", expMap.get("woodcutting").get("breakJungle_Log")});
    flamePickEXP.put(Material.STRIPPED_SPRUCE_LOG,
        new Object[]{"woodcutting", expMap.get("woodcutting").get("breakSpruce_Log")});
    flamePickEXP.put(Material.STRIPPED_OAK_LOG,
        new Object[]{"woodcutting", expMap.get("woodcutting").get("breakOak_Log")});
    flamePickEXP.put(Material.STRIPPED_DARK_OAK_LOG,
        new Object[]{"woodcutting", expMap.get("woodcutting").get("breakDark_Oak_Log")});
    flamePickEXP.put(Material.STRIPPED_BIRCH_LOG,
        new Object[]{"woodcutting", expMap.get("woodcutting").get("breakBirch_Log")});
    flamePickEXP.put(Material.STRIPPED_ACACIA_LOG,
        new Object[]{"woodcutting", expMap.get("woodcutting").get("breakAcacia_Log")});
    flamePickEXP.put(Material.JUNGLE_WOOD,
        new Object[]{"woodcutting", expMap.get("woodcutting").get("breakJungle_Log")});
    flamePickEXP.put(Material.SPRUCE_WOOD,
        new Object[]{"woodcutting", expMap.get("woodcutting").get("breakSpruce_Log")});
    flamePickEXP.put(Material.OAK_WOOD,
        new Object[]{"woodcutting", expMap.get("woodcutting").get("breakOak_Log")});
    flamePickEXP.put(Material.DARK_OAK_WOOD,
        new Object[]{"woodcutting", expMap.get("woodcutting").get("breakDark_Oak_Log")});
    flamePickEXP.put(Material.BIRCH_WOOD,
        new Object[]{"woodcutting", expMap.get("woodcutting").get("breakBirch_Log")});
    flamePickEXP.put(Material.ACACIA_WOOD,
        new Object[]{"woodcutting", expMap.get("woodcutting").get("breakAcacia_Log")});
    flamePickEXP.put(Material.STRIPPED_JUNGLE_WOOD,
        new Object[]{"woodcutting", expMap.get("woodcutting").get("breakJungle_Log")});
    flamePickEXP.put(Material.STRIPPED_SPRUCE_WOOD,
        new Object[]{"woodcutting", expMap.get("woodcutting").get("breakSpruce_Log")});
    flamePickEXP.put(Material.STRIPPED_OAK_WOOD,
        new Object[]{"woodcutting", expMap.get("woodcutting").get("breakOak_Log")});
    flamePickEXP.put(Material.STRIPPED_DARK_OAK_WOOD,
        new Object[]{"woodcutting", expMap.get("woodcutting").get("breakDark_Oak_Log")});
    flamePickEXP.put(Material.STRIPPED_BIRCH_WOOD,
        new Object[]{"woodcutting", expMap.get("woodcutting").get("breakBirch_Log")});
    flamePickEXP.put(Material.STRIPPED_ACACIA_WOOD,
        new Object[]{"woodcutting", expMap.get("woodcutting").get("breakAcacia_Log")});
    // Copper ore and the deepslate versions of the ores Flame Pick smelts (1.17 and later)
    Object[][] deepOres = {{"COPPER_ORE", "breakCopper_Ore", 350},
        {"DEEPSLATE_COPPER_ORE", "breakDeepslate_Copper_Ore", 350},
        {"DEEPSLATE_IRON_ORE", "breakDeepslate_Iron_Ore", 700},
        {"DEEPSLATE_GOLD_ORE", "breakDeepslate_Gold_Ore", 1000}};
    for (Object[] ore : deepOres) {
      Material material = Material.matchMaterial((String) ore[0]);
      if (material != null) {
        int exp = expMap.get("mining").getOrDefault((String) ore[1], (Integer) ore[2]);
        flamePickEXP.put(material, new Object[]{"mining", exp});
      }
    }
    if (mcVersion >= 1.16) {
      flamePickEXP.put(Material.NETHER_GOLD_ORE,
          new Object[]{"mining", expMap.get("mining").get("breakNether_Gold_Ore")});
      flamePickEXP.put(Material.ANCIENT_DEBRIS,
          new Object[]{"mining", expMap.get("mining").get("breakAncient_Debris")});
    }
  }


  public Map<Material, Integer> getDiggingEXP() {
    return diggingEXP;
  }

  public Map<Material, Integer> getWoodcuttingEXP() {
    return woodcuttingEXP;
  }

  public Map<Material, Integer> getMiningEXP() {
    return miningEXP;
  }

  public Map<Material, Integer> getFarmingEXP() {
    return farmingEXP;
  }

  public Map<Material, Object[]> getFlamePickEXP() {
    return flamePickEXP;
  }
}
