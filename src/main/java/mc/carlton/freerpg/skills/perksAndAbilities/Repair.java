package mc.carlton.freerpg.skills.perksAndAbilities;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import mc.carlton.freerpg.config.ConfigLoad;
import mc.carlton.freerpg.core.info.server.MinecraftVersion;
import mc.carlton.freerpg.utils.game.LanguageSelector;
import mc.carlton.freerpg.utils.globalVariables.ItemGroups;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

public class Repair extends Skill {

  Random rand = new Random(); //Random class Import
  private String skillName = "repair";
  private boolean runMethods;

  public Repair(Player p) {
    super(p);
    ConfigLoad configLoad = new ConfigLoad();
    this.runMethods = configLoad.getAllowedSkillsMap().get(skillName);
    expMap = configLoad.getExpMapForSkill(skillName);

  }

  public boolean repairItem() {
    if (!runMethods) {
      return false;
    }
    if (!p.hasPermission("freeRPG.canRepair")) {
      return false;
    }
    if (!isItemVanilla(itemInHand)) {
      ConfigLoad configLoad = new ConfigLoad();
      if (configLoad.isPreventUnsafeRepair()) {
        LanguageSelector lang = new LanguageSelector(p);
        p.sendMessage(ChatColor.RED + lang.getString("repairUnsafeEnchant"));
        return false;
      }
    }
    Material toolType = itemInHand.getType();
    boolean repaired = false;
    boolean foundItem = true;
    ItemGroups itemGroups = new ItemGroups();
    Map<Material, Material> repairItems = itemGroups.getRepairItems();
    if (repairItems.containsKey(toolType)) {
      Material mats = repairItems.get(itemInHand.getType());
      ItemMeta checkMeta = itemInHand.getItemMeta();
      if (checkMeta instanceof Damageable && ((Damageable) checkMeta).getDamage() == 0) {
        actionMessage.sendMessage(ChatColor.YELLOW + "This item is already fully repaired.");
        return false;
      }
      if (!p.getInventory().contains(mats)) {
        actionMessage.sendMessage(
            ChatColor.RED + "You need " + ChatColor.GOLD + readableName(mats) + ChatColor.RED
                + " in your inventory to repair " + ChatColor.GOLD
                + readableName(itemInHand.getType()) + ChatColor.RED + ".");
      }
      if (p.getInventory().contains(mats)) {
        Map<String, ArrayList<Number>> pStat = pStatClass.getPlayerData();
        int repairLevel = (int) pStat.get(skillName).get(4);
        int resourcefulLevel = (int) pStat.get(skillName).get(9);
        double keepMatsChance = resourcefulLevel * 0.1;
        double repairBonus = repairLevel * 0.002;
        double repairPercentage = 0;
        int expToGive = 0;
        int expRepairMultiplier = 0;
        repaired = true;
        double a = 1.0;
        MinecraftVersion minecraftVersion = new MinecraftVersion();
        double MCversion = minecraftVersion.getMinecraftVersionAsDouble();
        RepairProfile profile = getRepairProfile(toolType, MCversion);
        if (profile != null) {
          repairPercentage =
              (profile.baseFraction + repairBonus * profile.bonusFactor) / profile.divisor;
          a = profile.materialCost;
          expToGive += expMap.get(profile.baseExpKey);
          expRepairMultiplier = expMap.get(profile.multiplierKey);
        }
        ItemMeta itemInHandMeta = itemInHand.getItemMeta();
        if (repairPercentage * a < 0.2) {
          actionMessage.sendMessage(ChatColor.RED + lang.getString("repairFail0"));
          repaired = false;
        } else {
          if (itemInHandMeta instanceof Damageable) {
            int currentDamage = ((Damageable) itemInHandMeta).getDamage();
            if (currentDamage == 0) {
              return false;
            }
            int mats_index = p.getInventory().first(mats);
            int mats_amount = p.getInventory().getItem(mats_index).getAmount();
            if (keepMatsChance < rand.nextDouble()) {
              p.getInventory().getItem(mats_index).setAmount(mats_amount - 1);
            }
            int maxDamage = itemInHand.getType().getMaxDurability();
            int repairedDamage = (int) Math.round(maxDamage * repairPercentage);
            int expDamage = (int) a * Math.min(repairedDamage, currentDamage);
            ((Damageable) itemInHandMeta).setDamage(Math.max(0, currentDamage - repairedDamage));
            itemInHand.setItemMeta(itemInHandMeta);
            int enchantEXP = magicRepair();
            increaseStats.changeEXP(skillName,
                enchantEXP + expToGive + (expRepairMultiplier * expDamage));
            p.getWorld().playEffect(p.getLocation(), Effect.ANVIL_USE, 1);
          }
        }
      }
    } else if (toolType != Material.AIR) {
      actionMessage.sendMessage(ChatColor.RED + "This item cannot be repaired this way.");
    }
    return repaired;
  }

  /** Turns COPPER_INGOT into "Copper Ingot" for player messages. */
  private static String readableName(Material material) {
    StringBuilder name = new StringBuilder();
    for (String word : material.name().toLowerCase().split("_")) {
      if (name.length() > 0) {
        name.append(' ');
      }
      name.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
    }
    return name.toString();
  }

  public void salvaging() {
    if (!runMethods) {
      return;
    }
    if (!p.hasPermission("freeRPG.canSalvage")) {
      return;
    }
    if (!isItemVanilla(itemInHand)) {
      ConfigLoad configLoad = new ConfigLoad();
      if (configLoad.isPreventUnsafeSalvage()) {
        LanguageSelector lang = new LanguageSelector(p);
        p.sendMessage(ChatColor.RED + lang.getString("salvageUnsafeEnchant"));
        return;
      }
    }
    Map<String, ArrayList<Number>> pStat = pStatClass.getPlayerData();
    int salvageLevel = (int) pStat.get(skillName).get(7);
    ItemGroups itemGroups = new ItemGroups();
    Map<Material, Integer> repairItemsAmount = itemGroups.getRepairItemsAmount();
    if (repairItemsAmount.containsKey(itemInHand.getType())) {
      p.getWorld().playEffect(p.getLocation(), Effect.ANVIL_USE, 1);
      ItemMeta itemInHandMeta = itemInHand.getItemMeta();
      Material itemType = itemInHand.getType();
      int amount = repairItemsAmount.get(itemType);
      Map<Material, Material> repairItems = itemGroups.getRepairItems();
      Material type = repairItems.get(itemType);
      p.getInventory().getItemInMainHand().setAmount(0);
      double multiplier = 0.1 * salvageLevel + (1 - 0.1 * salvageLevel) * rand.nextDouble();
      double currentDamage = ((Damageable) itemInHandMeta).getDamage();
      double maxDamage = itemType.getMaxDurability();
      double percentDurability = (1 - (currentDamage / maxDamage));
      MinecraftVersion minecraftVersion = new MinecraftVersion();
      double mcVersion = minecraftVersion.getMinecraftVersionAsDouble();

      double amountToReturn_pre = amount * percentDurability * multiplier;
      double roundUpChance = (amountToReturn_pre - Math.floor(amountToReturn_pre));
      int amountToReturn = (int) Math.floor(amountToReturn_pre);
      if (roundUpChance
          > rand.nextDouble()) { //Round up if a random number is bigger than the excess
        amountToReturn += 1;
      }
      if (amountToReturn == 0) {
        if (amountToReturn_pre > rand.nextDouble()) {
          p.getInventory().addItem(new ItemStack(type, 1));
        } else {
          actionMessage.sendMessage(ChatColor.RED + lang.getString("repairFail1"));
        }
      } else {
        if (!type.equals(Material.NETHERITE_SCRAP)) {
          p.getInventory().addItem(new ItemStack(type, amountToReturn));
        } else {
          double multiplier1 = 0.1 * salvageLevel + (1 - 0.1 * salvageLevel) * rand.nextDouble();
          double multiplier2 = 0.1 * salvageLevel + (1 - 0.1 * salvageLevel) * rand.nextDouble();
          double amountToReturnGold_pre = 4 * percentDurability * multiplier1;
          int amountToReturnGold = (int) Math.round(amountToReturnGold_pre);
          double amountToReturnScrap_pre = 4 * percentDurability * multiplier2;
          int amountToReturnScrap = (int) Math.round(amountToReturnScrap_pre);
          p.getInventory().addItem(new ItemStack(Material.DIAMOND, amountToReturn));
          if (p.getInventory().firstEmpty() == -1) {
            World world = p.getWorld();
            world.dropItemNaturally(p.getLocation().add(0, 0.5, 0),
                new ItemStack(Material.GOLD_INGOT, amountToReturnGold));
            world.dropItemNaturally(p.getLocation().add(0, 0.5, 0),
                new ItemStack(Material.NETHERITE_SCRAP, amountToReturnScrap));
          } else {
            p.getInventory().addItem(new ItemStack(Material.GOLD_INGOT, amountToReturnGold));
            if (p.getInventory().firstEmpty() == -1) {
              World world = p.getWorld();
              world.dropItemNaturally(p.getLocation().add(0, 0.5, 0),
                  new ItemStack(Material.NETHERITE_SCRAP, amountToReturnScrap));
            } else {
              p.getInventory()
                  .addItem(new ItemStack(Material.NETHERITE_SCRAP, amountToReturnScrap));
            }
          }
        }
      }
      if (salvageLevel >= 5) {
        if (itemInHandMeta.hasEnchants()) {
          ItemStack enchantedBook = new ItemStack(Material.ENCHANTED_BOOK, 1);
          EnchantmentStorageMeta meta = ((EnchantmentStorageMeta) enchantedBook.getItemMeta());
          Map<Enchantment, Integer> enchants = itemInHandMeta.getEnchants();
          for (Enchantment enchant : enchants.keySet()) {
            meta.addStoredEnchant(enchant, enchants.get(enchant), true);
          }
          enchantedBook.setItemMeta(meta);
          if (p.getInventory().firstEmpty() == -1) {
            World world = p.getWorld();
            world.dropItemNaturally(p.getLocation().add(0, 0.5, 0), enchantedBook);
          } else {
            p.getInventory().addItem(enchantedBook);
          }
        }
      }
      if (type.equals(Material.STICK)) {
        increaseStats.changeEXP(skillName, expMap.get("salvageStick"));
      } else if (type.equals(Material.LEATHER)) {
        increaseStats.changeEXP(skillName, expMap.get("salvageLeather"));
      } else if (type.equals(Material.COBBLESTONE)) {
        increaseStats.changeEXP(skillName, expMap.get("salvageCobblestone"));
      } else if (type.equals(Material.IRON_INGOT) || type.equals(Material.COPPER_INGOT)) {
        increaseStats.changeEXP(skillName, expMap.get("salvageIron_Ingot"));
      } else if (type.equals(Material.GOLD_INGOT)) {
        increaseStats.changeEXP(skillName, expMap.get("salvageGold_Ingot"));
      } else if (type.equals(Material.STRING)) {
        increaseStats.changeEXP(skillName, expMap.get("salvageString"));
      } else if (type.equals(Material.PHANTOM_MEMBRANE)) {
        increaseStats.changeEXP(skillName, expMap.get("salvagePhantom_Membrane"));
      } else if (type.equals(Material.DIAMOND)) {
        increaseStats.changeEXP(skillName, expMap.get("salvageDiamond"));
      } else if (mcVersion >= 1.16) {
        if (type.equals(Material.NETHERITE_SCRAP)) {
          increaseStats.changeEXP(skillName, expMap.get("salvageNetherite_Ingot"));
        }
      }
    }
  }

  /** How much of an item one material repairs, and the exp it gives. */
  static final class RepairProfile {

    final double baseFraction;
    final double bonusFactor;
    final double divisor;
    final double materialCost;
    final String baseExpKey;
    final String multiplierKey;

    RepairProfile(double baseFraction, double bonusFactor, double divisor, double materialCost,
        String baseExpKey, String multiplierKey) {
      this.baseFraction = baseFraction;
      this.bonusFactor = bonusFactor;
      this.divisor = divisor;
      this.materialCost = materialCost;
      this.baseExpKey = baseExpKey;
      this.multiplierKey = multiplierKey;
    }
  }

  /** Materials per tool or armor piece of a tier: axe, hoe, pickaxe, shovel, sword, armor. */
  private static double partCost(String part, String tier) {
    switch (part) {
      case "AXE":
      case "PICKAXE":
        return 3.0;
      case "HOE":
      case "SWORD":
        return 2.0;
      case "SHOVEL":
        return 1.0;
      case "HELMET":
        return 5.0;
      case "CHESTPLATE":
        return 8.0;
      case "LEGGINGS":
        return tier.equals("CHAINMAIL") ? 11.0 : 7.0;
      case "BOOTS":
      case "NAUTILUS_ARMOR":
        return 4.0;
      default:
        return 0.0;
    }
  }

  /**
   * The repair values of an item, or null if the item cannot be repaired by this skill. Tool and
   * armor tiers share their values, other items have their own.
   */
  static RepairProfile getRepairProfile(Material item, double minecraftVersion) {
    if (copperIngotCost(item) > 0) {
      // Copper tools and armor repair like iron-tier gear
      double cost = copperIngotCost(item);
      return new RepairProfile(0.5, 1, cost, cost, "iron_baseEXP", "iron_EXPMultiplier");
    }
    switch (item) {
      case SHEARS:
        return new RepairProfile(0.5, 1, 2, 2, "shears_baseEXP", "shear_expMultiplier");
      case FISHING_ROD:
        return new RepairProfile(0.5, 1, 2, 2, "fishingRod_baseEXP", "fishingRod_expMultiplier");
      case CARROT_ON_A_STICK:
        return new RepairProfile(0.8, 1, 1, 1, "carrotOnAStick_baseEXP",
            "carrotOnAStick_expMultiplier");
      case FLINT_AND_STEEL:
        return new RepairProfile(0.8, 1, 1, 1, "flintAndSteel_baseEXP",
            "flintAndSteel_expMultiplier");
      case BOW:
        return new RepairProfile(0.8, 1, 3, 3, "bow_baseEXP", "bow_expMultiplier");
      case CROSSBOW:
        return new RepairProfile(0.8, 1, 3, 3, "crossbow_baseEXP", "crossbow_expMultiplier");
      case TRIDENT:
        return new RepairProfile(0.8, 1, 3, 1, "trident_baseEXP", "trident_expMultiplier");
      case ELYTRA:
        return new RepairProfile(0.8, 1, 3, 1, "elytra_baseEXP", "elytra_expMultiplier");
      case SHIELD:
        return new RepairProfile(0.8, 1, 3, 1, "shield_baseEXP", "shield_expMultiplier");
      case TURTLE_HELMET:
        return new RepairProfile(0.8, 1, 5, 5, "leather_baseEXP", "leather_EXPMultiplier");
      case WOLF_ARMOR:
        return new RepairProfile(0.8, 1, 4, 4, "leather_baseEXP", "leather_EXPMultiplier");
      default:
        break;
    }
    // Tools and armor are named TIER_PART, like IRON_PICKAXE or LEATHER_BOOTS
    String name = item.name();
    int underscore = name.indexOf('_');
    if (underscore < 0) {
      return null;
    }
    String tier = name.substring(0, underscore);
    String part = name.substring(underscore + 1);
    double cost = partCost(part, tier);
    if (cost == 0.0) {
      return null;
    }
    switch (tier) {
      case "WOODEN":
        return new RepairProfile(0.9, 1, cost, cost, "wooden_baseEXP", "wooden_EXPMultiplier");
      case "LEATHER":
        return cost >= 4.0 ? new RepairProfile(0.9, 1, cost, cost, "leather_baseEXP",
            "leather_EXPMultiplier") : null;
      case "STONE":
        return new RepairProfile(0.8, 1, cost, cost, "stone_baseEXP", "stone_EXPMultiplier");
      case "CHAINMAIL":
        return new RepairProfile(0.11, 1, cost, cost, "chainmail_baseEXP",
            "chainmail_EXPMultiplier");
      case "GOLDEN":
        return new RepairProfile(0.7, 1, cost, cost, "gold_baseEXP", "gold_EXPMultiplier");
      case "IRON":
        return new RepairProfile(0.5, 1, cost, cost, "iron_baseEXP", "iron_EXPMultiplier");
      case "DIAMOND":
        return new RepairProfile(0.0, 1, cost, cost, "diamond_baseEXP", "diamond_EXPMultiplier");
      case "NETHERITE":
        return minecraftVersion >= 1.16 ? new RepairProfile(0.0, 0.5, cost, 4.0,
            "netherite_baseEXP", "netherite_EXPMultiplier") : null;
      default:
        return null;
    }
  }

  /** Number of copper ingots in a copper tool/armor piece, or 0 if the item is not copper gear. */
  public static int copperIngotCost(Material material) {
    String name = material.name();
    if (!name.startsWith("COPPER_")) {
      return 0;
    }
    switch (name.substring("COPPER_".length())) {
      case "AXE":
      case "PICKAXE":
        return 3;
      case "HOE":
      case "SWORD":
      case "SPEAR":
        return 2;
      case "SHOVEL":
        return 1;
      case "HELMET":
        return 5;
      case "CHESTPLATE":
        return 8;
      case "LEGGINGS":
        return 7;
      case "BOOTS":
      case "NAUTILUS_ARMOR":
        return 4;
      default:
        return 0;
    }
  }

  public int magicRepair() {
    if (!runMethods) {
      return 0;
    }
    if (itemInHand.getEnchantments().size() == 0) {
      return 0;
    }
    Map<String, ArrayList<Number>> pStat = pStatClass.getPlayerData();
    double sucessChance = 0;
    if ((int) pStat.get(skillName).get(13) > 0) {
      sucessChance = 1;
    }
    if (sucessChance < rand.nextDouble()) {
      actionMessage.sendMessage(ChatColor.RED + lang.getString("repairFail2"));
      Map<Enchantment, Integer> enchantments = itemInHand.getEnchantments();
      for (Enchantment enchantment : enchantments.keySet()) {
        int level = enchantments.get(enchantment);
        itemInHand.removeEnchantment(enchantment);
        if (level > 1) {
          itemInHand.addEnchantment(enchantment, level - 1);
        }
      }
    } else {
      return 1000;
    }
    return 0;
  }

  public boolean isItemVanilla(ItemStack itemStack) {
    ItemMeta itemMeta = itemStack.getItemMeta();
    Map<Enchantment, Integer> itemEnchants = itemMeta.getEnchants();
    try {//Try to create a safe item with those enchants, if it fails, we know the item is not vanilla
      new ItemStack(itemStack.getType(), itemStack.getAmount()).addEnchantments(itemEnchants);
    } catch (IllegalArgumentException e) {
      return false;
    }
    return true;
  }
}
