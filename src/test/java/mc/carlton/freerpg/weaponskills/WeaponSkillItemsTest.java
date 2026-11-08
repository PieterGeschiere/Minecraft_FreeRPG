package mc.carlton.freerpg.weaponskills;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import mc.carlton.freerpg.skills.perksAndAbilities.Repair;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;

/** Which items belong to which weapon skill, arrow tags and tokens. */
class WeaponSkillItemsTest {

  @Test
  void eachWeaponBelongsToExactlyOneSkill() {
    assertTrue(WeaponSkill.BOW.isWeapon(Material.BOW));
    assertFalse(WeaponSkill.BOW.isWeapon(Material.CROSSBOW));
    assertTrue(WeaponSkill.CROSSBOW.isWeapon(Material.CROSSBOW));
    assertFalse(WeaponSkill.CROSSBOW.isWeapon(Material.BOW));
    assertTrue(WeaponSkill.MACE.isWeapon(Material.MACE));
    assertTrue(WeaponSkill.TRIDENT.isWeapon(Material.TRIDENT));
    assertFalse(WeaponSkill.TRIDENT.isWeapon(Material.MACE));
  }

  @Test
  void everySpearTierCountsAsASpear() {
    for (String tier : new String[]{"WOODEN", "STONE", "COPPER", "IRON", "GOLDEN", "DIAMOND",
        "NETHERITE"}) {
      assertTrue(WeaponSkill.SPEAR.isWeapon(Material.valueOf(tier + "_SPEAR")), tier);
    }
    assertFalse(WeaponSkill.SPEAR.isWeapon(Material.IRON_SWORD));
  }

  @Test
  void nothingIsAWeaponOfAnySkill() {
    for (WeaponSkill skill : WeaponSkill.values()) {
      assertFalse(skill.isWeapon(null));
      assertFalse(skill.isWeapon(Material.AIR));
      assertFalse(skill.isWeapon(Material.STICK));
    }
  }

  @Test
  void noWeaponIsSharedBetweenTwoSkills() {
    for (Material material : Material.values()) {
      int owners = 0;
      for (WeaponSkill skill : WeaponSkill.values()) {
        if (skill.isWeapon(material)) {
          owners++;
        }
      }
      assertTrue(owners <= 1, material + " belongs to " + owners + " weapon skills");
    }
  }

  @Test
  void arrowTagsBelongToTheirSkillByPrefix() {
    assertTrue(ArrowTags.belongsTo(ArrowTags.BOW, ArrowTags.BOW));
    assertTrue(ArrowTags.belongsTo(ArrowTags.BOW_VOLLEY, ArrowTags.BOW));
    assertTrue(ArrowTags.belongsTo(ArrowTags.CROSSBOW, ArrowTags.CROSSBOW));
    assertTrue(ArrowTags.belongsTo(ArrowTags.CROSSBOW_BLAST, ArrowTags.CROSSBOW));
    assertFalse(ArrowTags.belongsTo(ArrowTags.CROSSBOW, ArrowTags.BOW));
    assertFalse(ArrowTags.belongsTo(ArrowTags.BOW, ArrowTags.CROSSBOW));
    assertFalse(ArrowTags.belongsTo(null, ArrowTags.BOW));
  }

  @Test
  void passiveTokensComeFromLevelsMinusWhatIsInvested() {
    WeaponSkillData data = new WeaponSkillData();
    assertEquals(0, WeaponSkillManager.getAvailablePassiveTokens(data));
    data.setLevel(5);
    assertEquals(5, WeaponSkillManager.getAvailablePassiveTokens(data));
    data.setPassive("p1", 2);
    assertEquals(3, WeaponSkillManager.getAvailablePassiveTokens(data));
    data.setPassive("p2", 100);
    assertEquals(0, WeaponSkillManager.getAvailablePassiveTokens(data)); // never negative
  }

  @Test
  void skillTokensComeFromEveryHundredLevelsPlusBonusMinusUnlockedSkills() {
    WeaponSkillData data = new WeaponSkillData();
    data.setLevel(99);
    assertEquals(0, WeaponSkillManager.getAvailableSkillTokens(data));
    data.setLevel(250);
    assertEquals(2, WeaponSkillManager.getAvailableSkillTokens(data));
    data.setBonusSkillTokens(1);
    assertEquals(3, WeaponSkillManager.getAvailableSkillTokens(data));
    data.setSkill("1a", 2);
    assertEquals(1, WeaponSkillManager.getAvailableSkillTokens(data));
    data.setSkill("1b", 10);
    assertEquals(0, WeaponSkillManager.getAvailableSkillTokens(data)); // never negative
  }

  @Test
  void copperGearCostsTheRightNumberOfIngots() {
    assertEquals(3, Repair.copperIngotCost(Material.COPPER_AXE));
    assertEquals(3, Repair.copperIngotCost(Material.COPPER_PICKAXE));
    assertEquals(2, Repair.copperIngotCost(Material.COPPER_SWORD));
    assertEquals(2, Repair.copperIngotCost(Material.COPPER_SPEAR));
    assertEquals(1, Repair.copperIngotCost(Material.COPPER_SHOVEL));
    assertEquals(5, Repair.copperIngotCost(Material.COPPER_HELMET));
    assertEquals(8, Repair.copperIngotCost(Material.COPPER_CHESTPLATE));
    assertEquals(7, Repair.copperIngotCost(Material.COPPER_LEGGINGS));
    assertEquals(4, Repair.copperIngotCost(Material.COPPER_BOOTS));
    assertEquals(4, Repair.copperIngotCost(Material.COPPER_NAUTILUS_ARMOR));
    assertEquals(0, Repair.copperIngotCost(Material.DIAMOND_SWORD));
    assertEquals(0, Repair.copperIngotCost(Material.COPPER_INGOT));
  }
}
