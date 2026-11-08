package mc.carlton.freerpg.weaponskills;

import static mc.carlton.freerpg.weaponskills.TreeText.number;
import static mc.carlton.freerpg.weaponskills.TreeText.percent;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/** The texts of the Spear Mastery skill tree, taken from languages.yml. */
public class SpearTreeInfo implements SkillTreeInfo {

  @Override
  public WeaponSkill getSkill() {
    return WeaponSkill.SPEAR;
  }

  @Override
  public String[] getSkillNames(Player p) {
    return new String[]{
        TreeText.tr(p, "weaponSpearSkill0"),
        TreeText.tr(p, "weaponSpearSkill1"),
        TreeText.tr(p, "weaponSpearSkill2"),
        TreeText.tr(p, "weaponSpearSkill3"),
        TreeText.tr(p, "weaponSpearSkill4"),
        TreeText.tr(p, "weaponSpearSkill5"),
        TreeText.tr(p, "weaponSpearSkill6")};
  }

  @Override
  public String[] getSkillDescriptions(Player p) {
    return new String[]{
        TreeText.tr(p, "weaponSpearSkillDesc0",
            "v", number(SpearMastery.perk("longReachPerLevel", 0.5))),
        TreeText.tr(p, "weaponSpearSkillDesc1",
            "v", number(SpearMastery.perk("widePierceWidth", 0.2))),
        TreeText.tr(p, "weaponSpearSkillDesc2",
            "v", percent(SpearMastery.perk("skewerPerLevel", 0.1))),
        TreeText.tr(p, "weaponSpearSkillDesc3",
            "v", number(SpearMastery.perk("hamstringSecondsPerLevel", 1.0))),
        TreeText.tr(p, "weaponSpearSkillDesc4",
            "v", number(SpearMastery.perk("pinSeconds", 2))),
        TreeText.tr(p, "weaponSpearSkillDesc5",
            "v", number(SpearMastery.perk("triumphSeconds", 3))),
        TreeText.tr(p, "weaponSpearSkillDesc6",
            "v", percent(SpearMastery.perk("dragoonsWrathDamage", 0.25)),
            "w", percent(SpearMastery.perk("dragoonsWrathLength", 1.5) - 1))};
  }

  @Override
  public String[] getPassiveNames(Player p) {
    return new String[]{
        TreeText.tr(p, "weaponSpearPassive0"),
        TreeText.tr(p, "weaponSpearPassive1"),
        TreeText.tr(p, "weaponSpearPassive2")};
  }

  @Override
  public String[] getPassiveDescriptions(Player p) {
    return new String[]{
        TreeText.tr(p, "weaponSpearPassiveDesc0"),
        TreeText.tr(p, "weaponSpearPassiveDesc1"),
        TreeText.tr(p, "weaponSpearPassiveDesc2")};
  }

  @Override
  public String getPassiveStat(Player p, int index, WeaponSkillData data) {
    switch (index) {
      case 0:
        return ChatColor.GRAY + TreeText.tr(p, "weaponStatDuration") + ": " + ChatColor.AQUA
            + Math.round(SpearMastery.getAbilityDuration(data) * 100) / 100.0 + " s"
            + ChatColor.GRAY + " (" + TreeText.tr(p, "weaponStatMax") + " "
            + WeaponSkillConfig.getDouble("spear.ability.maxDurationSeconds", 14) + " s)";
      case 1:
        return ChatColor.GRAY + TreeText.tr(p, "weaponStatBonus") + ": " + ChatColor.AQUA
            + percent(data.getPassive(SpearMastery.MOMENTUM) * SpearMastery.perk("momentumPerLevel", 0.003));
      default:
        return ChatColor.GRAY + TreeText.tr(p, "weaponStatBonus") + ": " + ChatColor.AQUA
            + percent(data.getPassive(SpearMastery.MOUNTED_CHARGE) * SpearMastery.perk("mountedChargePerLevel", 0.004));
    }
  }

  @Override
  public String getAbilityDescription(Player p) {
    return TreeText.tr(p, "weaponSpearAbilityDesc",
        "charges", Integer.toString(SpearMastery.getAbilityCharges()),
        "cooldown", Integer.toString(SpearMastery.getAbilityCooldown()));
  }

  @Override
  public int getPassiveMax(String key) {
    return SpearMastery.getPassiveMax(key);
  }
}
