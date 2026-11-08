package mc.carlton.freerpg.weaponskills;

import static mc.carlton.freerpg.weaponskills.TreeText.number;
import static mc.carlton.freerpg.weaponskills.TreeText.percent;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/** The texts of the Crossbow Mastery skill tree, taken from languages.yml. */
public class CrossbowTreeInfo implements SkillTreeInfo {

  @Override
  public WeaponSkill getSkill() {
    return WeaponSkill.CROSSBOW;
  }

  @Override
  public String[] getSkillNames(Player p) {
    String[] names = new String[7];
    for (int i = 0; i < names.length; i++) {
      names[i] = TreeText.tr(p, "weaponCrossbowSkill" + i);
    }
    return names;
  }

  @Override
  public String[] getSkillDescriptions(Player p) {
    return new String[]{
        TreeText.tr(p, "weaponCrossbowSkillDesc0",
            "v", number(CrossbowMastery.perk("wideBlastRadiusPerLevel", 0.5))),
        TreeText.tr(p, "weaponCrossbowSkillDesc1",
            "v", number(CrossbowMastery.perk("concussionSecondsPerLevel", 1.0))),
        TreeText.tr(p, "weaponCrossbowSkillDesc2",
            "v", number(CrossbowMastery.perk("heavyChargePerLevel", 1.0))),
        TreeText.tr(p, "weaponCrossbowSkillDesc3",
            "v", number(CrossbowMastery.perk("rendSecondsPerLevel", 2.0))),
        TreeText.tr(p, "weaponCrossbowSkillDesc4",
            "v", number(CrossbowMastery.perk("shrapnelDamage", 3))),
        TreeText.tr(p, "weaponCrossbowSkillDesc5",
            "v", number(CrossbowMastery.perk("recoilSeconds", 4))),
        TreeText.tr(p, "weaponCrossbowSkillDesc6",
            "v", percent(CrossbowMastery.perk("siegeWrathDamage", 0.25)),
            "w", percent(CrossbowMastery.perk("siegeWrathRadius", 1.5) - 1))};
  }

  @Override
  public String[] getPassiveNames(Player p) {
    return new String[]{
        TreeText.tr(p, "weaponCrossbowPassive0"),
        TreeText.tr(p, "weaponCrossbowPassive1"),
        TreeText.tr(p, "weaponCrossbowPassive2")};
  }

  @Override
  public String[] getPassiveDescriptions(Player p) {
    return new String[]{
        TreeText.tr(p, "weaponCrossbowPassiveDesc0"),
        TreeText.tr(p, "weaponCrossbowPassiveDesc1"),
        TreeText.tr(p, "weaponCrossbowPassiveDesc2")};
  }

  @Override
  public String getPassiveStat(Player p, int index, WeaponSkillData data) {
    switch (index) {
      case 0:
        return ChatColor.GRAY + TreeText.tr(p, "weaponStatDuration") + ": " + ChatColor.AQUA
            + Math.round(CrossbowMastery.getAbilityDuration(data) * 100) / 100.0 + " s"
            + ChatColor.GRAY + " (" + TreeText.tr(p, "weaponStatMax") + " "
            + WeaponSkillConfig.getDouble("crossbow.ability.maxDurationSeconds", 12) + " s)";
      case 1:
        return ChatColor.GRAY + TreeText.tr(p, "weaponStatBonus") + ": " + ChatColor.AQUA
            + percent(data.getPassive(CrossbowMastery.BOLT_POWER)
            * CrossbowMastery.perk("boltPowerPerLevel", 0.003));
      default:
        return ChatColor.GRAY + TreeText.tr(p, "weaponStatBonus") + ": " + ChatColor.AQUA
            + percent(data.getPassive(CrossbowMastery.POINT_BLANK)
            * CrossbowMastery.perk("pointBlankPerLevel", 0.005));
    }
  }

  @Override
  public String getAbilityDescription(Player p) {
    return TreeText.tr(p, "weaponCrossbowAbilityDesc",
        "charges", Integer.toString(CrossbowMastery.getAbilityCharges()),
        "cooldown", Integer.toString(CrossbowMastery.getAbilityCooldown()));
  }

  @Override
  public int getPassiveMax(String key) {
    return CrossbowMastery.getPassiveMax(key);
  }
}
