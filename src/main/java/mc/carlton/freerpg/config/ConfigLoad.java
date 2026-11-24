package mc.carlton.freerpg.config;

import mc.carlton.freerpg.skills.SkillRegistry;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import mc.carlton.freerpg.FreeRPG;
import mc.carlton.freerpg.customContainers.OldCustomPotion;
import mc.carlton.freerpg.customContainers.collections.OldCustomRecipe;
import mc.carlton.freerpg.skills.aeronautics.AeronauticsConfig;
import mc.carlton.freerpg.skills.building.BuildingConfig;
import mc.carlton.freerpg.utils.globalVariables.EffectTypeCompat;
import org.apache.logging.log4j.Level;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

public class ConfigLoad {

  static int playerStatFilesLoadedInOnStartup;
  static boolean forceLanguage;
  static double basePlayerHP;
  static boolean saveRunTimeData;
  static boolean verboseRunTimeData;
  static int saveStatsTimer;
  static String defaultLanguage;
  static boolean allowExplosions;
  static boolean allowBuild;
  static boolean allowPvP;
  static boolean allowHurtAnimals;
  static int furnaceDeleteTimer;
  static boolean getEXPFromEnchantingBottles;
  static boolean trackFewerBlocks;
  static boolean flamePickGiveXP;
  static boolean shiftRightClickInvestAll;
  static int veinMinerMaxBreakSize;
  static int superBaitCooldown;
  static boolean leaderboardDyanmicUpdate;
  static int leaderboardUpdateTimer;
  static boolean preventUnsafeRepair;
  static boolean preventUnsafeSalvage;
  static boolean earnExperiencePastMaxLevel;
  static double agilityMinSprintTimeForExperience;
  static int maxLevelForBeginnerMessage;
  static String beginnerLevelUpMessage;
  static boolean advancedKeepinventory;
  static Map<String, Double> specialMultiplier = new HashMap<>();
  static Map<String, Integer> maxLevels = new HashMap<>();
  static ArrayList<Integer> soulsInfo = new ArrayList<>();
  static ArrayList<Double> multipliers = new ArrayList<>();
  static ArrayList<Double> tokensInfo = new ArrayList<>();
  static ArrayList<Double> levelingInfo = new ArrayList<>();
  static ArrayList<Object> diggingInfo = new ArrayList<>();
  static ArrayList<Object> woodcuttingInfo = new ArrayList<>();
  static ArrayList<Object> fishingInfo = new ArrayList<>();
  static ArrayList<Object> fishingInfoBaseChances = new ArrayList<>();
  static ArrayList<Object> fishingInfoHotRod = new ArrayList<>();
  static ArrayList<Object> fishingInfoEnchants = new ArrayList<>();
  static Map<String, Boolean> allowedSkillsMap = new HashMap<>();
  static Map<String, Boolean> allowedSkillGainEXPMap = new HashMap<>();
  static Map<String, OldCustomPotion> alchemyInfo = new HashMap<>();
  static Map<String, Map<String, Integer>> expMap = new HashMap<>();
  static Map<String, OldCustomRecipe> craftingRecipes = new HashMap<>();
  static HashSet<Material> veinMinerBlocks = new HashSet<>();
  static ArrayList<Integer> timberBreakLimits = new ArrayList<>();
  static Map<String, Integer> abilityCooldowns = new HashMap<>();
  static Map<String, Double> spawnerEXPMultipliers = new HashMap<>();
  static Map<String, Double> mobFarmEXPMultipliers = new HashMap<>();
  static Map<String, Double> durabilityModifiers = new HashMap<>();
  static double[] buildingWeatherproofChances = BuildingConfig.DEFAULT_WEATHERPROOF.clone();
  static double[] buildingFortifiedChances = BuildingConfig.DEFAULT_FORTIFIED.clone();
  static double buildingDemolitionSpeedBonus = BuildingConfig.DEFAULT_DEMOLITION_SPEED_BONUS;
  static double buildingDemolitionDurabilityMultiplier =
      BuildingConfig.DEFAULT_DEMOLITION_DURABILITY_MULTIPLIER;
  static double buildingReachPerPoint = BuildingConfig.DEFAULT_REACH_PER_POINT;
  static double buildingReachCap = BuildingConfig.DEFAULT_REACH_CAP;
  static long buildingXpGraceMillis = BuildingConfig.DEFAULT_XP_GRACE_SECONDS * 1000L;
  static int buildingXpPerMaterialPerMinute = BuildingConfig.DEFAULT_XP_PER_MATERIAL_PER_MINUTE;
  static int buildingTrackerMaxBlocks = BuildingConfig.DEFAULT_TRACKER_MAX_BLOCKS;
  static double[] aeronauticsSoftLandingReductions =
      AeronauticsConfig.DEFAULT_SOFT_LANDING.clone();
  static int[] aeronauticsLongBurnPower = AeronauticsConfig.DEFAULT_LONG_BURN.clone();
  static int aeronauticsTailwindPower = AeronauticsConfig.DEFAULT_TAILWIND_POWER;
  static double aeronauticsEfficiencyPerPoint = AeronauticsConfig.DEFAULT_EFFICIENCY_PER_POINT;
  static double aeronauticsEfficiencyCap = AeronauticsConfig.DEFAULT_EFFICIENCY_CAP;
  static double aeronauticsStepBlocks = AeronauticsConfig.DEFAULT_STEP_BLOCKS;
  static int aeronauticsMaxStepsPerMinute = AeronauticsConfig.DEFAULT_MAX_STEPS_PER_MINUTE;
  Plugin plugin = FreeRPG.getPlugin(FreeRPG.class);

  public void initializeConfig() {
    setConfigData();
  }


  public void setConfigData() {
    File f = new File(plugin.getDataFolder(), "config.yml");
    f.setReadable(true, false);
    f.setWritable(true, false);
    FileConfiguration config = YamlConfiguration.loadConfiguration(f);
    File f1 = new File(plugin.getDataFolder(), "advancedConfig.yml");
    f.setReadable(true, false);
    f.setWritable(true, false);
    FileConfiguration advancedConfig = YamlConfiguration.loadConfiguration(f1);

    //Useful Label Groups:
    String[] labels = SkillRegistry.labelArray();
    String[] combatLabels = SkillRegistry.fighterLabels().toArray(new String[0]);

    //General Config and Config that has no real category
    defaultLanguage = config.getString("general.defaultLanguage");
    //basePlayerHP = config.getDouble("general.playerBaseHP");
    forceLanguage = config.getBoolean("general.forceLanguage");
    playerStatFilesLoadedInOnStartup = advancedConfig.getInt(
        "general.playerStatFilesLoadedInOnStartup");
    saveRunTimeData = advancedConfig.getBoolean("general.saveRunTimeData");
    verboseRunTimeData = advancedConfig.getBoolean("general.verboseRunTimeData");
    saveStatsTimer = advancedConfig.getInt("general.saveStatsTimer");
    allowExplosions = advancedConfig.getBoolean("general.allowCustomExplosions");
    allowBuild = advancedConfig.getBoolean("general.allowBuild");
    allowPvP = advancedConfig.getBoolean("general.allowPvP");
    allowHurtAnimals = advancedConfig.getBoolean("general.allowHurtAnimals");
    furnaceDeleteTimer = advancedConfig.getInt("smelting.removePlayerFurnacesTimer");
    trackFewerBlocks = advancedConfig.getBoolean("general.trackFewerBlocks");
    leaderboardDyanmicUpdate = advancedConfig.getBoolean("general.leaderboardDynamicUpdate");
    leaderboardUpdateTimer = advancedConfig.getInt("general.leaderboardUpdateTimer");
    getEXPFromEnchantingBottles = advancedConfig.getBoolean(
        "enchanting.gainEXPfromEnchantingBottles");
    flamePickGiveXP = advancedConfig.getBoolean("smelting.flamePickGiveMinecraftXP");
    preventUnsafeRepair = advancedConfig.getBoolean(
        "repair.preventRepairOfItemsWithUnsafeEnchantments");
    preventUnsafeSalvage = advancedConfig.getBoolean(
        "repair.preventSalvageOfItemsWithUnsafeEnchantments");
    maxLevelForBeginnerMessage = advancedConfig.getInt("leveling.beginnerLevelUpMessageMaxLevel");
    beginnerLevelUpMessage = advancedConfig.getString("leveling.customBegginerLevelUpMessage");
    advancedKeepinventory = advancedConfig.getBoolean("general.keepInventoryOn");
    List<String> veinMinerBlockStrings = advancedConfig.getStringList("mining.veinMinerBlocks");
    for (String matString : veinMinerBlockStrings) {
      if (Material.matchMaterial(matString) != null) {
        veinMinerBlocks.add(Material.matchMaterial(matString));
      }
    }
    veinMinerMaxBreakSize = advancedConfig.getInt("mining.veinMinerMaximumBlocksBroken");
    timberBreakLimits.add(advancedConfig.getInt("woodcutting.timberMaxBreakInitial"));
    timberBreakLimits.add(advancedConfig.getInt("woodcutting.timberMaxBreakUpgraded"));
    shiftRightClickInvestAll = advancedConfig.getBoolean(
        "tokens.passiveShiftClickAndRightClickInvestAll");
    superBaitCooldown = advancedConfig.getInt("fishing.superBaitCooldown");
    earnExperiencePastMaxLevel = config.getBoolean("leveling.earnExperiencePastMaxLevel");
    agilityMinSprintTimeForExperience = advancedConfig.getDouble("agility.sprintTimeThreshold");

    maxLevels.put("global", Integer.valueOf(config.getString("leveling.maxLevel")));
    allowedSkillsMap.put("global", config.getBoolean("global.skillAllowed"));
    for (String label : labels) {
      maxLevels.put(label, maxLevel(config, label));
      allowedSkillsMap.put(label, skillAllowed(config, label));
      abilityCooldowns.put(label, config.getInt(label + ".abilityCooldown"));
      allowedSkillGainEXPMap.put(label, expDropsEnabled(advancedConfig, label));
    }
    abilityCooldowns.put("robCooldown", advancedConfig.getInt("fishing.robCooldown"));

    durabilityModifiers.put("megaDig",
        advancedConfig.getDouble("digging.megaDigDurabilityModifier"));
    durabilityModifiers.put("timber",
        advancedConfig.getDouble("woodcutting.timberDurabilityModifier"));
    durabilityModifiers.put("leafBlower",
        advancedConfig.getDouble("woodcutting.leafBlowerDurabilityModifier"));
    durabilityModifiers.put("veinMiner",
        advancedConfig.getDouble("mining.veinMinerDurabilityModifier"));
    durabilityModifiers.put("flamePick",
        advancedConfig.getDouble("smelting.smeltingDurabilityModifier"));

    //Config that is skill dependant (not used in all skills)
    for (String label : combatLabels) {
      spawnerEXPMultipliers.put(label,
          advancedConfig.getDouble(label + ".spawnerMobsEXPMultiplier"));
      mobFarmEXPMultipliers.put(label, advancedConfig.getDouble(label + ".mobFarmEXPMultiplier"));
    }

    soulsInfo.add(Integer.valueOf(advancedConfig.getString("souls.startingSouls")));
    soulsInfo.add(Integer.valueOf(advancedConfig.getString("souls.refundCost")));

    multipliers.add(Double.valueOf(config.getString("global.EXP_Multiplier")));
    for (String label : labels) {
      multipliers.add(expMultiplier(config, label));
    }

    tokensInfo.add(
        Double.valueOf(advancedConfig.getString("tokens.levelsPerAutomaticPassiveUpgrade")));
    tokensInfo.add(Double.valueOf(advancedConfig.getString("tokens.levelsPerPassiveToken")));
    tokensInfo.add(Double.valueOf(advancedConfig.getString("tokens.levelsPerSkillToken")));
    tokensInfo.add(Double.valueOf(advancedConfig.getString("tokens.levelsPerGlobalToken")));
    tokensInfo.add(Double.valueOf(advancedConfig.getString("tokens.startingPassiveTokens")));
    tokensInfo.add(Double.valueOf(advancedConfig.getString("tokens.startingSkillTokens")));
    tokensInfo.add(Double.valueOf(advancedConfig.getString("tokens.startingGlobalTokens")));
    tokensInfo.add(
        Double.valueOf(advancedConfig.getString("tokens.skillTokenToPassiveTokenConversion")));
    tokensInfo.add(Double.valueOf(advancedConfig.getString("tokens.globalTokenToEXPbuff")));
    tokensInfo.add(Double.valueOf(advancedConfig.getString("tokens.passiveRightClickInvestment")));
    tokensInfo.add(Double.valueOf(advancedConfig.getString("tokens.passiveShiftClickInvestment")));

    levelingInfo.add(Double.valueOf(config.getString("leveling.maxLevel")));
    levelingInfo.add(Double.valueOf(advancedConfig.getString("leveling.exponentialGrowthFactor")));
    levelingInfo.add(
        Double.valueOf(advancedConfig.getString("leveling.exponentialReferenceLevel")));
    levelingInfo.add(Double.valueOf(advancedConfig.getString("leveling.exponentialReferenceEXP")));
    levelingInfo.add(Double.valueOf(advancedConfig.getString("leveling.levelBeginLinear")));
    levelingInfo.add(Double.valueOf(advancedConfig.getString("leveling.LinearEXPperLevel")));

    for (int i = 1; i <= 9; i++) {
      diggingInfo.add(
          Material.matchMaterial(advancedConfig.getString("digging.drops.drop" + i + "Name")));
      diggingInfo.add(
          Integer.valueOf(advancedConfig.getString("digging.drops.drop" + i + "Amount")));
    }
    for (int i = 10; i <= 15; i++) {
      diggingInfo.add(
          Material.matchMaterial(advancedConfig.getString("digging.drops.drop" + i + "Name")));
      diggingInfo.add(
          Integer.valueOf(advancedConfig.getString("digging.drops.drop" + i + "Amount")));
      diggingInfo.add(
          Double.valueOf(advancedConfig.getString("digging.drops.drop" + i + "BaseChance")));
    }

    for (int i = 1; i <= 5; i++) {
      woodcuttingInfo.add(Material.matchMaterial(
          advancedConfig.getString("woodcutting.drops.leavesDrop" + i + "Name")));
      woodcuttingInfo.add(
          Integer.valueOf(advancedConfig.getString("woodcutting.drops.leavesDrop" + i + "Amount")));
      woodcuttingInfo.add(
          Double.valueOf(advancedConfig.getString("woodcutting.drops.leavesDrop" + i + "Chance")));
    }

    fishingInfoBaseChances.add(
        Double.valueOf(advancedConfig.getString("fishing.drops.tier1_baseChance")));
    fishingInfoBaseChances.add(
        Double.valueOf(advancedConfig.getString("fishing.drops.tier2_baseChance")));
    fishingInfoBaseChances.add(
        Double.valueOf(advancedConfig.getString("fishing.drops.tier3_baseChance")));
    fishingInfoBaseChances.add(
        Double.valueOf(advancedConfig.getString("fishing.drops.tier4_baseChance")));
    fishingInfoBaseChances.add(
        Double.valueOf(advancedConfig.getString("fishing.drops.tier5_baseChance")));

    fishingInfoEnchants.add(
        Integer.valueOf(advancedConfig.getString("fishing.drops.tier1_enchantedArmor")));
    fishingInfoEnchants.add(
        Integer.valueOf(advancedConfig.getString("fishing.drops.tier2_enchantedArmor")));
    fishingInfoEnchants.add(
        Integer.valueOf(advancedConfig.getString("fishing.drops.tier3_enchantedArmor")));
    fishingInfoEnchants.add(
        Integer.valueOf(advancedConfig.getString("fishing.drops.tier4_enchantedArmor")));
    fishingInfoEnchants.add(
        Integer.valueOf(advancedConfig.getString("fishing.drops.tier5_enchantedArmor")));

    for (int i = 1; i <= 5; i++) {
      for (int j = 1; j <= 4; j++) {
        fishingInfo.add(Material.matchMaterial(
            advancedConfig.getString("fishing.drops.tier" + i + "_drop" + j + "Name")));
        fishingInfo.add(Integer.valueOf(
            advancedConfig.getString("fishing.drops.tier" + i + "_drop" + j + "Amount")));
        fishingInfo.add(Integer.valueOf(
            advancedConfig.getString("fishing.drops.tier" + i + "_drop" + j + "Random")));
      }
    }

    fishingInfoHotRod.add(
        Material.matchMaterial(advancedConfig.getString("fishing.drops.tier1_drop1Name_HotRod")));
    fishingInfoHotRod.add(
        Integer.valueOf(advancedConfig.getString("fishing.drops.tier1_drop1Amount_HotRod")));
    fishingInfoHotRod.add(
        Integer.valueOf(advancedConfig.getString("fishing.drops.tier1_drop1Random_HotRod")));
    fishingInfoHotRod.add(
        Material.matchMaterial(advancedConfig.getString("fishing.drops.tier1_drop2Name_HotRod")));
    fishingInfoHotRod.add(
        Integer.valueOf(advancedConfig.getString("fishing.drops.tier1_drop2Amount_HotRod")));
    fishingInfoHotRod.add(
        Integer.valueOf(advancedConfig.getString("fishing.drops.tier1_drop2Random_HotRod")));
    fishingInfoHotRod.add(
        Material.matchMaterial(advancedConfig.getString("fishing.drops.tier2_drop1Name_HotRod")));
    fishingInfoHotRod.add(
        Integer.valueOf(advancedConfig.getString("fishing.drops.tier2_drop1Amount_HotRod")));
    fishingInfoHotRod.add(
        Integer.valueOf(advancedConfig.getString("fishing.drops.tier2_drop1Random_HotRod")));
    fishingInfoHotRod.add(
        Material.matchMaterial(advancedConfig.getString("fishing.drops.tier2_drop3Name_HotRod")));
    fishingInfoHotRod.add(
        Integer.valueOf(advancedConfig.getString("fishing.drops.tier2_drop3Amount_HotRod")));
    fishingInfoHotRod.add(
        Integer.valueOf(advancedConfig.getString("fishing.drops.tier2_drop3Random_HotRod")));
    fishingInfoHotRod.add(
        Material.matchMaterial(advancedConfig.getString("fishing.drops.tier2_drop4Name_HotRod")));
    fishingInfoHotRod.add(
        Integer.valueOf(advancedConfig.getString("fishing.drops.tier2_drop4Amount_HotRod")));
    fishingInfoHotRod.add(
        Integer.valueOf(advancedConfig.getString("fishing.drops.tier2_drop4Random_HotRod")));

    //Alchemy Info
    for (int i = 1; i <= 5; i++) {
      OldCustomPotion customPotion = new OldCustomPotion();
      customPotion.setPotionEffectType(EffectTypeCompat.parse(
          advancedConfig.getString("alchemy.customPotions.potionType" + i)));
      customPotion.setIngredient(Material.matchMaterial(
          advancedConfig.getString("alchemy.customPotions.potionIngredient" + i)));
      customPotion.setPotionDuration(
          advancedConfig.getInt("alchemy.customPotions.potionDuration" + i));
      customPotion.setColor(
          getColorFromString(advancedConfig.getString("alchemy.customPotions.potionColor" + i)));
      customPotion.setPotionName();
      alchemyInfo.put("customPotion" + i, customPotion);
    }

    //EXP Info
    for (String label : labels) {
      expMap.put(label, readExpDrops(advancedConfig, label));
    }

    //Building (perkConfig.yml is read straight from the data folder, or the bundled copy)
    FileConfiguration perkConfig = loadPerkConfig();
    buildingWeatherproofChances = BuildingConfig.weatherproofChances(perkConfig);
    buildingFortifiedChances = BuildingConfig.fortifiedChances(perkConfig);
    buildingDemolitionSpeedBonus = BuildingConfig.demolitionSpeedBonus(perkConfig);
    buildingDemolitionDurabilityMultiplier = BuildingConfig.demolitionDurabilityMultiplier(
        perkConfig);
    buildingReachPerPoint = BuildingConfig.reachPerPoint(perkConfig);
    buildingReachCap = BuildingConfig.reachCap(advancedConfig);
    buildingXpGraceMillis = BuildingConfig.xpGraceMillis(advancedConfig);
    buildingXpPerMaterialPerMinute = BuildingConfig.xpPerMaterialPerMinute(advancedConfig);
    buildingTrackerMaxBlocks = BuildingConfig.trackerMaxBlocks(advancedConfig);
    //Aeronautics (same files as Building, same fallbacks when a server's config has no section)
    aeronauticsSoftLandingReductions = AeronauticsConfig.softLandingReductions(perkConfig);
    aeronauticsLongBurnPower = AeronauticsConfig.longBurnPower(perkConfig);
    aeronauticsTailwindPower = AeronauticsConfig.tailwindPower(perkConfig);
    aeronauticsEfficiencyPerPoint = AeronauticsConfig.efficiencyPerPoint(perkConfig);
    aeronauticsEfficiencyCap = AeronauticsConfig.efficiencyCap(advancedConfig);
    aeronauticsStepBlocks = AeronauticsConfig.stepBlocks(advancedConfig);
    aeronauticsMaxStepsPerMinute = AeronauticsConfig.maxStepsPerMinute(advancedConfig);

    //Special Multipliers
    specialMultiplier.put("megaDigEXPMultiplier",
        advancedConfig.getDouble("digging.megaDigEXPMultiplier"));
    specialMultiplier.put("superBaitEXPMultiplier",
        advancedConfig.getDouble("fishing.superBaitEXPMultiplier"));
    specialMultiplier.put("timberEXPMultiplier",
        advancedConfig.getDouble("woodcutting.timberEXPMultiplier"));
    specialMultiplier.put("blockingEXPMultiplier",
        advancedConfig.getDouble("defense.blockingEXPMultiplier"));

    //Crafting arrays
    for (int i = 1; i <= 5; i++) {
      OldCustomRecipe oldCustomRecipe = new OldCustomRecipe();
      oldCustomRecipe.setOutput(
          Material.matchMaterial(advancedConfig.getString("farming.crafting.recipeOutput" + i)));
      oldCustomRecipe.setOutputAmount(
          advancedConfig.getInt("farming.crafting.recipeOutputAmount" + i));
      List<String> recipeStrings = advancedConfig.getStringList("farming.crafting.recipe" + i);
      ArrayList<Material> recipeMaterials = new ArrayList<>();
      for (String item : recipeStrings) {
        Material recipeMaterial = Material.matchMaterial(item);
        if (recipeMaterial == null) {
          FreeRPG.log(Level.WARN, "[FreeRPG] WARNING: Unrecognized Material in " +
              "farming.crafting.recipe" + i + "(Some materials will be treated as air");
        }
        recipeMaterials.add(recipeMaterial);
      }
      oldCustomRecipe.setRecipe(recipeMaterials);
      oldCustomRecipe.setYAML_ID("farming.crafting.recipe" + i);
      craftingRecipes.put("farming" + i, oldCustomRecipe);
    }
    for (int i = 1; i <= 10; i++) {
      OldCustomRecipe oldCustomRecipe = new OldCustomRecipe();
      oldCustomRecipe.setOutput(
          Material.matchMaterial(advancedConfig.getString("enchanting.crafting.recipeOutput" + i)));
      oldCustomRecipe.setOutputAmount(
          advancedConfig.getInt("enchanting.crafting.recipeOutputAmount" + i));
      String enchantType = advancedConfig.getString("enchanting.crafting.recipeEnchant" + i);
      if (!enchantType.equalsIgnoreCase("none")) {
        oldCustomRecipe.setEnchantment(
            Registry.ENCHANTMENT.get(NamespacedKey.minecraft(enchantType)));
        oldCustomRecipe.setEnchantmentLevel(
            advancedConfig.getInt("enchanting.crafting.recipeEnchantLevel" + i));
        oldCustomRecipe.setXPcraftCost(
            advancedConfig.getInt("enchanting.crafting.XPcostToCraft" + i));
      }
      List<String> recipeStrings = advancedConfig.getStringList("enchanting.crafting.recipe" + i);
      ArrayList<Material> recipeMaterials = new ArrayList<>();
      for (String item : recipeStrings) {
        Material recipeMaterial = Material.matchMaterial(item);
        if (recipeMaterial == null) {
          FreeRPG.log(Level.WARN, "[FreeRPG] WARNING: Unrecognized Material in " +
              "enchanting.crafting.recipe" + i + "(Some materials will be treated as air");
        }
        recipeMaterials.add(recipeMaterial);
      }
      oldCustomRecipe.setRecipe(recipeMaterials);
      oldCustomRecipe.setYAML_ID("enchanting.crafting.recipe" + i);
      craftingRecipes.put("enchanting" + i, oldCustomRecipe);
    }
    for (int i = 1; i <= 5; i++) {
      OldCustomRecipe oldCustomRecipe = new OldCustomRecipe();
      String materialString = advancedConfig.getString("alchemy.crafting.recipeOutput" + i);
      oldCustomRecipe.setOutput(Material.matchMaterial(materialString));
      oldCustomRecipe.setOutputAmount(
          advancedConfig.getInt("alchemy.crafting.recipeOutputAmount" + i));
      if (materialString.equalsIgnoreCase("POTION")) {
        oldCustomRecipe.setPotionType(
            mc.carlton.freerpg.utils.globalVariables.PotionTypeCompat.parse(advancedConfig.getString("alchemy.crafting.recipePotionType" + i)));
        oldCustomRecipe.setExtended(
            advancedConfig.getBoolean("alchemy.crafting.recipePotionExtended" + i));
        oldCustomRecipe.setUpgraded(
            advancedConfig.getBoolean("alchemy.crafting.recipePotionUpgraded" + i));
      }
      List<String> recipeStrings = advancedConfig.getStringList("alchemy.crafting.recipe" + i);
      ArrayList<Material> recipeMaterials = new ArrayList<>();
      for (String item : recipeStrings) {
        Material recipeMaterial = Material.matchMaterial(item);
        if (recipeMaterial == null) {
          FreeRPG.log(Level.WARN, "[FreeRPG] WARNING: Unrecognized Material in " +
              "alchemy.crafting.recipe" + i + "(Some materials will be treated as air");
        }
        recipeMaterials.add(recipeMaterial);
      }
      oldCustomRecipe.setRecipe(recipeMaterials);
      oldCustomRecipe.setYAML_ID("alchemy.crafting.recipe" + i);
      craftingRecipes.put("alchemy" + i, oldCustomRecipe);
    }

  }

  public Color getColorFromString(String colorString) {
    colorString = colorString.substring(1, colorString.length() - 1);
    List<String> RGB = Arrays.asList(colorString.trim().split(","));
    int red = 0;
    int green = 0;
    int blue = 0;
    if (RGB.size() == 3) {
      red = Integer.parseInt(RGB.get(0));
      green = Integer.parseInt(RGB.get(1));
      blue = Integer.parseInt(RGB.get(2));
    }
    return Color.fromRGB(red, green, blue);
  }

  public int getSaveStatsTimer() {
    return saveStatsTimer;
  }

  public String getDefaultLanguage() {
    return defaultLanguage;
  }

  public void setDefaultLanguage(String languageCode) {
    defaultLanguage = languageCode;
  }

  public boolean isAllowExplosions() {
    return allowExplosions;
  }

  public boolean isAllowBuild() {
    return allowBuild;
  }

  public boolean isAllowPvP() {
    return allowPvP;
  }

  public boolean isAllowHurtAnimals() {
    return allowHurtAnimals;
  }

  public Map<String, Integer> getMaxLevels() {
    return maxLevels;
  }

  public ArrayList<Integer> getSoulsInfo() {
    return soulsInfo;
  }

  public ArrayList<Double> getMultipliers() {
    return multipliers;
  }

  public ArrayList<Double> getTokensInfo() {
    return tokensInfo;
  }

  public ArrayList<Double> getLevelingInfo() {
    return levelingInfo;
  }

  public ArrayList<Object> getDiggingInfo() {
    return diggingInfo;
  }

  public ArrayList<Object> getWoodcuttingInfo() {
    return woodcuttingInfo;
  }

  public ArrayList<Object> getFishingInfo1() {
    return fishingInfo;
  }

  public ArrayList<Object> getFishingInfo2() {
    return fishingInfoBaseChances;
  }

  public ArrayList<Object> getFishingInfo3() {
    return fishingInfoHotRod;
  }

  public ArrayList<Object> getFishingInfo4() {
    return fishingInfoEnchants;
  }

  public boolean isGetEXPFromEnchantingBottles() {
    return getEXPFromEnchantingBottles;
  }

  public boolean isSaveRunTimeData() {
    return saveRunTimeData;
  }

  public boolean isVerboseRunTimeData() {
    return verboseRunTimeData;
  }

  public int getFurnaceDeleteTimer() {
    return furnaceDeleteTimer;
  }

  public Map<String, Boolean> getAllowedSkillsMap() {
    return allowedSkillsMap;
  }

  public Map<String, Boolean> getAllowedSkillGainEXPMap() {
    return allowedSkillGainEXPMap;
  }

  public Map<String, OldCustomPotion> getAlchemyInfo() {
    return alchemyInfo;
  }

  public Map<String, Map<String, Integer>> getExpMap() {
    return expMap;
  }

  public Map<String, Integer> getExpMapForSkill(String skillName) {
    if (expMap.containsKey(skillName)) {
      return expMap.get(skillName);
    }
    return null;
  }

  /** perkConfig.yml from the data folder when the server has one, otherwise the bundled copy. */
  private FileConfiguration loadPerkConfig() {
    File file = new File(plugin.getDataFolder(), "perkConfig.yml");
    if (file.isFile()) {
      return YamlConfiguration.loadConfiguration(file);
    }
    try (java.io.InputStream stream = plugin.getResource("perkConfig.yml")) {
      if (stream != null) {
        return YamlConfiguration.loadConfiguration(
            new java.io.InputStreamReader(stream, java.nio.charset.StandardCharsets.UTF_8));
      }
    } catch (java.io.IOException e) {
      FreeRPG.log(Level.ERROR, e.getMessage());
    }
    return new YamlConfiguration();
  }

  // A skill missing from an older config file counts as enabled, with no level limit and a x1
  // experience multiplier, instead of failing to load.
  public static boolean skillAllowed(FileConfiguration config, String label) {
    return config.getBoolean(label + ".skillAllowed", true);
  }

  public static int maxLevel(FileConfiguration config, String label) {
    return Integer.parseInt(config.getString(label + ".maxLevel", "-1"));
  }

  public static double expMultiplier(FileConfiguration config, String label) {
    return config.getDouble(label + ".EXP_Multiplier", 1.0);
  }

  public static boolean expDropsEnabled(FileConfiguration advancedConfig, String label) {
    return advancedConfig.getBoolean(label + ".expDrops.enableEXPDrops", true);
  }

  /** The exp values of a skill (every key under expDrops except the on/off switch). */
  public static Map<String, Integer> readExpDrops(FileConfiguration advancedConfig, String label) {
    Map<String, Integer> skillExpMap = new HashMap<>();
    if (label.equals("building")) {
      skillExpMap.put("basicBlock", BuildingConfig.DEFAULT_BASIC_BLOCK_EXP);
      skillExpMap.put("decorativeBlock", BuildingConfig.DEFAULT_DECORATIVE_BLOCK_EXP);
    }
    if (label.equals("aeronautics")) {
      skillExpMap.put("glideStep", AeronauticsConfig.DEFAULT_GLIDE_STEP_EXP);
    }
    ConfigurationSection skillExpDrops = advancedConfig.getConfigurationSection(
        label + ".expDrops");
    if (skillExpDrops != null) {
      for (String id : skillExpDrops.getKeys(false)) {
        if (!id.equalsIgnoreCase("enableEXPDrops")) {
          skillExpMap.put(id, advancedConfig.getInt(label + ".expDrops." + id));
        }
      }
    }
    return skillExpMap;
  }

  public double[] getBuildingWeatherproofChances() {
    return buildingWeatherproofChances.clone();
  }

  public double[] getBuildingFortifiedChances() {
    return buildingFortifiedChances.clone();
  }

  public double getBuildingDemolitionSpeedBonus() {
    return buildingDemolitionSpeedBonus;
  }

  public double getBuildingDemolitionDurabilityMultiplier() {
    return buildingDemolitionDurabilityMultiplier;
  }

  public double getBuildingReachPerPoint() {
    return buildingReachPerPoint;
  }

  public double getBuildingReachCap() {
    return buildingReachCap;
  }

  public long getBuildingXpGraceMillis() {
    return buildingXpGraceMillis;
  }

  public int getBuildingXpPerMaterialPerMinute() {
    return buildingXpPerMaterialPerMinute;
  }

  public int getBuildingTrackerMaxBlocks() {
    return buildingTrackerMaxBlocks;
  }

  public double[] getAeronauticsSoftLandingReductions() {
    return aeronauticsSoftLandingReductions.clone();
  }

  public int[] getAeronauticsLongBurnPower() {
    return aeronauticsLongBurnPower.clone();
  }

  public int getAeronauticsTailwindPower() {
    return aeronauticsTailwindPower;
  }

  public double getAeronauticsEfficiencyPerPoint() {
    return aeronauticsEfficiencyPerPoint;
  }

  public double getAeronauticsEfficiencyCap() {
    return aeronauticsEfficiencyCap;
  }

  public double getAeronauticsStepBlocks() {
    return aeronauticsStepBlocks;
  }

  public int getAeronauticsMaxStepsPerMinute() {
    return aeronauticsMaxStepsPerMinute;
  }

  public Map<String, Double> getSpecialMultiplier() {
    return specialMultiplier;
  }

  public Map<String, OldCustomRecipe> getCraftingRecipes() {
    return craftingRecipes;
  }

  public boolean isTrackFewerBlocks() {
    return trackFewerBlocks;
  }

  public boolean isFlamePickGiveXP() {
    return flamePickGiveXP;
  }

  public HashSet<Material> getVeinMinerBlocks() {
    return veinMinerBlocks;
  }

  public int getVeinMinerMaxBreakSize() {
    return veinMinerMaxBreakSize;
  }

  public ArrayList<Integer> getTimberBreakLimits() {
    return timberBreakLimits;
  }

  public Map<String, Integer> getAbilityCooldowns() {
    return abilityCooldowns;
  }

  public boolean isShiftRightClickInvestAll() {
    return shiftRightClickInvestAll;
  }

  public Map<String, Double> getMobFarmEXPMultipliers() {
    return mobFarmEXPMultipliers;
  }

  public Map<String, Double> getSpawnerEXPMultipliers() {
    return spawnerEXPMultipliers;
  }

  public int getPlayerStatFilesLoadedInOnStartup() {
    return playerStatFilesLoadedInOnStartup;
  }

  public double getBasePlayerHP() {
    return basePlayerHP;
  }

  public int getSuperBaitCooldown() {
    return superBaitCooldown;
  }

  public int getLeaderboardUpdateTimer() {
    return leaderboardUpdateTimer;
  }

  public boolean isLeaderboardDyanmicUpdate() {
    return leaderboardDyanmicUpdate;
  }

  public boolean isPreventUnsafeRepair() {
    return preventUnsafeRepair;
  }

  public boolean isPreventUnsafeSalvage() {
    return preventUnsafeSalvage;
  }

  public boolean isEarnExperiencePastMaxLevel() {
    return earnExperiencePastMaxLevel;
  }

  public double getAgilityMinSprintTimeForExperience() {
    return agilityMinSprintTimeForExperience;
  }

  public Map<String, Double> getDurabilityModifiers() {
    return durabilityModifiers;
  }

  public int getMaxLevelForBeginnerMessage() {
    return maxLevelForBeginnerMessage;
  }

  public String getBeginnerLevelUpMessage() {
    return beginnerLevelUpMessage;
  }

  public boolean isKeepinventory() {
    return advancedKeepinventory;
  }

  public boolean isForceLanguage() {
    return forceLanguage;
  }
}
