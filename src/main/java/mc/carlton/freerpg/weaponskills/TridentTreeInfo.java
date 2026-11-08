package mc.carlton.freerpg.weaponskills;

import static mc.carlton.freerpg.weaponskills.TreeText.number;
import static mc.carlton.freerpg.weaponskills.TreeText.percent;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/** The texts of the Trident Mastery skill tree, taken from languages.yml. */
public class TridentTreeInfo implements SkillTreeInfo {

  @Override
  public WeaponSkill getSkill() {
    return WeaponSkill.TRIDENT;
  }

  @Override
  public String[] getSkillNames(Player p) {
    return new String[]{
        TreeText.tr(p, "weaponTridentSkill0"),
        TreeText.tr(p, "weaponTridentSkill1"),
        TreeText.tr(p, "weaponTridentSkill2"),
        TreeText.tr(p, "weaponTridentSkill3"),
        TreeText.tr(p, "weaponTridentSkill4"),
        TreeText.tr(p, "weaponTridentSkill5"),
        TreeText.tr(p, "weaponTridentSkill6")};
  }

  @Override
  public String[] getSkillDescriptions(Player p) {
    return new String[]{
        TreeText.tr(p, "weaponTridentSkillDesc0",
            "v", number(TridentMastery.perk("wideMaelstromRadiusPerLevel", 0.5))),
        TreeText.tr(p, "weaponTridentSkillDesc1",
            "v", percent(TridentMastery.perk("undertowPerLevel", 0.1))),
        TreeText.tr(p, "weaponTridentSkillDesc2",
            "v", number(TridentMastery.perk("biteDamagePerLevel", 0.5))),
        TreeText.tr(p, "weaponTridentSkillDesc3",
            "v", number(TridentMastery.perk("barbedSecondsPerLevel", 1.0))),
        TreeText.tr(p, "weaponTridentSkillDesc4"),
        TreeText.tr(p, "weaponTridentSkillDesc5",
            "v", number(TridentMastery.perk("seaShieldSeconds", 8))),
        TreeText.tr(p, "weaponTridentSkillDesc6",
            "v", percent(TridentMastery.perk("poseidonsWrathDamage", 0.25)),
            "w", percent(TridentMastery.perk("poseidonsWrathRadius", 1.5) - 1))};
  }

  @Override
  public String[] getPassiveNames(Player p) {
    return new String[]{
        TreeText.tr(p, "weaponTridentPassive0"),
        TreeText.tr(p, "weaponTridentPassive1"),
        TreeText.tr(p, "weaponTridentPassive2")};
  }

  @Override
  public String[] getPassiveDescriptions(Player p) {
    return new String[]{
        TreeText.tr(p, "weaponTridentPassiveDesc0"),
        TreeText.tr(p, "weaponTridentPassiveDesc1"),
        TreeText.tr(p, "weaponTridentPassiveDesc2")};
  }

  @Override
  public String getPassiveStat(Player p, int index, WeaponSkillData data) {
    switch (index) {
      case 0:
        return ChatColor.GRAY + TreeText.tr(p, "weaponStatDuration") + ": " + ChatColor.AQUA
            + Math.round(TridentMastery.getAbilityDuration(data) * 100) / 100.0 + " s"
            + ChatColor.GRAY + " (" + TreeText.tr(p, "weaponStatMax") + " "
            + WeaponSkillConfig.getDouble("trident.ability.maxDurationSeconds", 14) + " s)";
      case 1:
        return ChatColor.GRAY + TreeText.tr(p, "weaponStatBonus") + ": " + ChatColor.AQUA
            + percent(data.getPassive(TridentMastery.THROWING_POWER) * TridentMastery.perk("throwingPowerPerLevel", 0.003));
      default:
        return ChatColor.GRAY + TreeText.tr(p, "weaponStatBonus") + ": " + ChatColor.AQUA
            + percent(data.getPassive(TridentMastery.TIDAL_MIGHT) * TridentMastery.perk("tidalMightPerLevel", 0.004));
    }
  }

  @Override
  public String getAbilityDescription(Player p) {
    return TreeText.tr(p, "weaponTridentAbilityDesc",
        "charges", Integer.toString(TridentMastery.getAbilityCharges()),
        "seconds", number(TridentMastery.getMaelstromSeconds()),
        "cooldown", Integer.toString(TridentMastery.getAbilityCooldown()));
  }

  @Override
  public int getPassiveMax(String key) {
    return TridentMastery.getPassiveMax(key);
  }
}
