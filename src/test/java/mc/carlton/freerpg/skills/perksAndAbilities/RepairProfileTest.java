package mc.carlton.freerpg.skills.perksAndAbilities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import mc.carlton.freerpg.skills.perksAndAbilities.Repair.RepairProfile;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;

/** The repair values per item, which used to be a long if-else chain. */
class RepairProfileTest {

  private static final double EPS = 1e-9;
  private static final double LATEST = 26.03;

  private static void check(Material item, double base, double bonus, double divisor, double cost,
      String expKey, String multiplierKey) {
    RepairProfile p = Repair.getRepairProfile(item, LATEST);
    assertNotNull(p, item.name());
    assertEquals(base, p.baseFraction, EPS, item + " base");
    assertEquals(bonus, p.bonusFactor, EPS, item + " bonus");
    assertEquals(divisor, p.divisor, EPS, item + " divisor");
    assertEquals(cost, p.materialCost, EPS, item + " cost");
    assertEquals(expKey, p.baseExpKey, item.name());
    assertEquals(multiplierKey, p.multiplierKey, item.name());
  }

  @Test
  void toolsAndArmorOfEveryTier() {
    check(Material.WOODEN_SHOVEL, 0.9, 1, 1, 1, "wooden_baseEXP", "wooden_EXPMultiplier");
    check(Material.LEATHER_BOOTS, 0.9, 1, 4, 4, "leather_baseEXP", "leather_EXPMultiplier");
    check(Material.STONE_HOE, 0.8, 1, 2, 2, "stone_baseEXP", "stone_EXPMultiplier");
    check(Material.CHAINMAIL_HELMET, 0.11, 1, 5, 5, "chainmail_baseEXP",
        "chainmail_EXPMultiplier");
    check(Material.GOLDEN_CHESTPLATE, 0.7, 1, 8, 8, "gold_baseEXP", "gold_EXPMultiplier");
    check(Material.IRON_PICKAXE, 0.5, 1, 3, 3, "iron_baseEXP", "iron_EXPMultiplier");
    check(Material.IRON_LEGGINGS, 0.5, 1, 7, 7, "iron_baseEXP", "iron_EXPMultiplier");
    check(Material.DIAMOND_SWORD, 0.0, 1, 2, 2, "diamond_baseEXP", "diamond_EXPMultiplier");
  }

  @Test
  void chainmailLeggingsNeedMoreThanOtherLeggings() {
    check(Material.CHAINMAIL_LEGGINGS, 0.11, 1, 11, 11, "chainmail_baseEXP",
        "chainmail_EXPMultiplier");
  }

  @Test
  void netheriteRepairsFourMaterialsWorthAndHalvesTheBonus() {
    check(Material.NETHERITE_SHOVEL, 0.0, 0.5, 1, 4, "netherite_baseEXP",
        "netherite_EXPMultiplier");
    check(Material.NETHERITE_CHESTPLATE, 0.0, 0.5, 8, 4, "netherite_baseEXP",
        "netherite_EXPMultiplier");
  }

  @Test
  void netheriteDoesNotExistBeforeMinecraft116() {
    assertNull(Repair.getRepairProfile(Material.NETHERITE_AXE, 1.15));
  }

  @Test
  void otherItemsHaveTheirOwnValues() {
    check(Material.SHEARS, 0.5, 1, 2, 2, "shears_baseEXP", "shear_expMultiplier");
    check(Material.BOW, 0.8, 1, 3, 3, "bow_baseEXP", "bow_expMultiplier");
    check(Material.CROSSBOW, 0.8, 1, 3, 3, "crossbow_baseEXP", "crossbow_expMultiplier");
    check(Material.TRIDENT, 0.8, 1, 3, 1, "trident_baseEXP", "trident_expMultiplier");
    check(Material.SHIELD, 0.8, 1, 3, 1, "shield_baseEXP", "shield_expMultiplier");
  }

  @Test
  void wolfAndNautilusArmorCanBeRepaired() {
    check(Material.WOLF_ARMOR, 0.8, 1, 4, 4, "leather_baseEXP", "leather_EXPMultiplier");
    check(Material.IRON_NAUTILUS_ARMOR, 0.5, 1, 4, 4, "iron_baseEXP", "iron_EXPMultiplier");
    check(Material.GOLDEN_NAUTILUS_ARMOR, 0.7, 1, 4, 4, "gold_baseEXP", "gold_EXPMultiplier");
    check(Material.DIAMOND_NAUTILUS_ARMOR, 0.0, 1, 4, 4, "diamond_baseEXP",
        "diamond_EXPMultiplier");
    check(Material.NETHERITE_NAUTILUS_ARMOR, 0.0, 0.5, 4, 4, "netherite_baseEXP",
        "netherite_EXPMultiplier");
    check(Material.COPPER_NAUTILUS_ARMOR, 0.5, 1, 4, 4, "iron_baseEXP", "iron_EXPMultiplier");
  }

  @Test
  void itemsThatCannotBeRepairedHaveNoProfile() {
    assertNull(Repair.getRepairProfile(Material.IRON_INGOT, LATEST));
    assertNull(Repair.getRepairProfile(Material.LEATHER_HORSE_ARMOR, LATEST));
    assertNull(Repair.getRepairProfile(Material.TURTLE_HELMET, LATEST));
    assertNull(Repair.getRepairProfile(Material.STONE, LATEST));
    assertNull(Repair.getRepairProfile(Material.DIRT, LATEST));
  }
}
