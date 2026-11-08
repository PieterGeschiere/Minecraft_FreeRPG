package mc.carlton.freerpg.weaponskills;

import static mc.carlton.freerpg.weaponskills.TreeText.number;
import static mc.carlton.freerpg.weaponskills.TreeText.percent;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/** The texts of the Mace Mastery skill tree, taken from languages.yml. */
public class MaceTreeInfo implements SkillTreeInfo {

  @Override
  public WeaponSkill getSkill() {
    return WeaponSkill.MACE;
  }

  @Override
  public String[] getSkillNames(Player p) {
    return new String[]{
        TreeText.tr(p, "weaponMaceSkill0"),
        TreeText.tr(p, "weaponMaceSkill1"),
        TreeText.tr(p, "weaponMaceSkill2"),
        TreeText.tr(p, "weaponMaceSkill3"),
        TreeText.tr(p, "weaponMaceSkill4"),
        TreeText.tr(p, "weaponMaceSkill5"),
        TreeText.tr(p, "weaponMaceSkill6")};
  }

  @Override
  public String[] getSkillDescriptions(Player p) {
    return new String[]{
        TreeText.tr(p, "weaponMaceSkillDesc0",
            "v", number(MaceMastery.perk("wideShockwaveRadiusPerLevel", 0.5))),
        TreeText.tr(p, "weaponMaceSkillDesc1",
            "v", number(MaceMastery.perk("staggerSecondsPerLevel", 1.0))),
        TreeText.tr(p, "weaponMaceSkillDesc2",
            "v", percent(MaceMastery.perk("aftershockPerLevel", 0.06))),
        TreeText.tr(p, "weaponMaceSkillDesc3",
            "v", number(MaceMastery.perk("rattleSecondsPerLevel", 2.0))),
        TreeText.tr(p, "weaponMaceSkillDesc4",
            "v", percent(MaceMastery.perk("earthshakerDamage", 0.2))),
        TreeText.tr(p, "weaponMaceSkillDesc5",
            "v", number(MaceMastery.perk("adrenalineSeconds", 4))),
        TreeText.tr(p, "weaponMaceSkillDesc6",
            "v", percent(MaceMastery.perk("titansWrathDamage", 0.25)),
            "w", percent(MaceMastery.perk("titansWrathRadius", 1.5) - 1))};
  }

  @Override
  public String[] getPassiveNames(Player p) {
    return new String[]{
        TreeText.tr(p, "weaponMacePassive0"),
        TreeText.tr(p, "weaponMacePassive1"),
        TreeText.tr(p, "weaponMacePassive2")};
  }

  @Override
  public String[] getPassiveDescriptions(Player p) {
    return new String[]{
        TreeText.tr(p, "weaponMacePassiveDesc0"),
        TreeText.tr(p, "weaponMacePassiveDesc1"),
        TreeText.tr(p, "weaponMacePassiveDesc2")};
  }

  @Override
  public String getPassiveStat(Player p, int index, WeaponSkillData data) {
    switch (index) {
      case 0:
        return ChatColor.GRAY + TreeText.tr(p, "weaponStatDuration") + ": " + ChatColor.AQUA
            + Math.round(MaceMastery.getAbilityDuration(data) * 100) / 100.0 + " s"
            + ChatColor.GRAY + " (" + TreeText.tr(p, "weaponStatMax") + " "
            + WeaponSkillConfig.getDouble("mace.ability.maxDurationSeconds", 12) + " s)";
      case 1:
        return ChatColor.GRAY + TreeText.tr(p, "weaponStatBonus") + ": " + ChatColor.AQUA
            + percent(data.getPassive(MaceMastery.SMASH_POWER) * MaceMastery.perk("smashPowerPerLevel", 0.003));
      default:
        return ChatColor.GRAY + TreeText.tr(p, "weaponStatReduction") + ": " + ChatColor.AQUA
            + percent(MaceMastery.getFallReduction(data));
    }
  }

  @Override
  public String getAbilityDescription(Player p) {
    return TreeText.tr(p, "weaponMaceAbilityDesc",
        "charges", Integer.toString(MaceMastery.getAbilityCharges()),
        "cooldown", Integer.toString(MaceMastery.getAbilityCooldown()));
  }

  @Override
  public int getPassiveMax(String key) {
    return MaceMastery.getPassiveMax(key);
  }
}
