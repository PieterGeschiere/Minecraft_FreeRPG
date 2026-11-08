package mc.carlton.freerpg.weaponskills;

import static mc.carlton.freerpg.weaponskills.TreeText.number;
import static mc.carlton.freerpg.weaponskills.TreeText.percent;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/** The texts of the Bow Mastery skill tree, taken from languages.yml. */
public class BowTreeInfo implements SkillTreeInfo {

  @Override
  public WeaponSkill getSkill() {
    return WeaponSkill.BOW;
  }

  @Override
  public String[] getSkillNames(Player p) {
    String[] names = new String[7];
    for (int i = 0; i < names.length; i++) {
      names[i] = TreeText.tr(p, "weaponBowSkill" + i);
    }
    return names;
  }

  @Override
  public String[] getSkillDescriptions(Player p) {
    return new String[]{
        TreeText.tr(p, "weaponBowSkillDesc0",
            "v", number(BowMastery.perk("extraArrowsPerLevel", 1))),
        TreeText.tr(p, "weaponBowSkillDesc1",
            "v", number(BowMastery.perk("pinningSecondsPerLevel", 1.0))),
        TreeText.tr(p, "weaponBowSkillDesc2",
            "v", percent(BowMastery.perk("volleyPowerPerLevel", 0.06))),
        TreeText.tr(p, "weaponBowSkillDesc3",
            "v", number(BowMastery.perk("markedSecondsPerLevel", 2.0))),
        TreeText.tr(p, "weaponBowSkillDesc4",
            "v", number(BowMastery.perk("flamingSeconds", 5))),
        TreeText.tr(p, "weaponBowSkillDesc5",
            "v", number(BowMastery.perk("fleetFootSeconds", 4))),
        TreeText.tr(p, "weaponBowSkillDesc6",
            "v", percent(BowMastery.perk("hawkeyesWrathDamage", 0.25)),
            "w", number(BowMastery.perk("hawkeyesWrathArrows", 2)))};
  }

  @Override
  public String[] getPassiveNames(Player p) {
    return new String[]{
        TreeText.tr(p, "weaponBowPassive0"),
        TreeText.tr(p, "weaponBowPassive1"),
        TreeText.tr(p, "weaponBowPassive2")};
  }

  @Override
  public String[] getPassiveDescriptions(Player p) {
    return new String[]{
        TreeText.tr(p, "weaponBowPassiveDesc0"),
        TreeText.tr(p, "weaponBowPassiveDesc1"),
        TreeText.tr(p, "weaponBowPassiveDesc2")};
  }

  @Override
  public String getPassiveStat(Player p, int index, WeaponSkillData data) {
    switch (index) {
      case 0:
        return ChatColor.GRAY + TreeText.tr(p, "weaponStatDuration") + ": " + ChatColor.AQUA
            + Math.round(BowMastery.getAbilityDuration(data) * 100) / 100.0 + " s"
            + ChatColor.GRAY + " (" + TreeText.tr(p, "weaponStatMax") + " "
            + WeaponSkillConfig.getDouble("bow.ability.maxDurationSeconds", 14) + " s)";
      case 1:
        return ChatColor.GRAY + TreeText.tr(p, "weaponStatBonus") + ": " + ChatColor.AQUA
            + percent(data.getPassive(BowMastery.DRAW_POWER)
            * BowMastery.perk("drawPowerPerLevel", 0.003));
      default:
        return ChatColor.GRAY + TreeText.tr(p, "weaponStatBonus") + ": " + ChatColor.AQUA
            + percent(data.getPassive(BowMastery.SNIPER) * BowMastery.perk("sniperPerLevel", 0.004));
    }
  }

  @Override
  public String getAbilityDescription(Player p) {
    return TreeText.tr(p, "weaponBowAbilityDesc",
        "charges", Integer.toString(BowMastery.getAbilityCharges()),
        "cooldown", Integer.toString(BowMastery.getAbilityCooldown()));
  }

  @Override
  public int getPassiveMax(String key) {
    return BowMastery.getPassiveMax(key);
  }
}
