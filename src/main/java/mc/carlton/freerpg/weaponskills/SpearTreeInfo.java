package mc.carlton.freerpg.weaponskills;

import org.bukkit.ChatColor;

/** The texts of the Spear Mastery skill tree. */
public class SpearTreeInfo implements SkillTreeInfo {

  @Override
  public WeaponSkill getSkill() {
    return WeaponSkill.SPEAR;
  }

  @Override
  public String[] getSkillNames() {
    return new String[]{"Long Reach", "Wide Pierce", "Skewer", "Hamstring", "Pin", "Triumph",
        "Dragoon's Wrath"};
  }

  @Override
  public String[] getSkillDescriptions() {
    return new String[]{
        "Piercing Rush reaches " + TreeText.number(SpearMastery.perk("longReachPerLevel", 0.5))
            + " blocks further per level.",
        "Piercing Rush is " + TreeText.number(SpearMastery.perk("widePierceWidth", 0.2))
            + " blocks wider per level.",
        "The pierce deals " + TreeText.percent(SpearMastery.perk("skewerPerLevel", 0.1))
            + " more of the hit's damage per level.",
        "Spear hits slow the target for "
            + TreeText.number(SpearMastery.perk("hamstringSecondsPerLevel", 1.0))
            + " second(s) per level.",
        "The last mob pierced by Piercing Rush is pinned in place for "
            + TreeText.number(SpearMastery.perk("pinSeconds", 2)) + " seconds.",
        "Killing a mob with a spear grants you Speed II for "
            + TreeText.number(SpearMastery.perk("triumphSeconds", 3)) + " seconds.",
        "Spear hits deal " + TreeText.percent(SpearMastery.perk("dragoonsWrathDamage", 0.25))
            + " more damage and Piercing Rush reaches "
            + TreeText.percent(SpearMastery.perk("dragoonsWrathLength", 1.5) - 1) + " further."};
  }

  @Override
  public String[] getPassiveNames() {
    return new String[]{"Rush Duration", "Momentum", "Mounted Charge"};
  }

  @Override
  public String[] getPassiveDescriptions() {
    return new String[]{"Increases how long Piercing Rush stays ready.",
        "Increases the damage of your spear while you are sprinting.",
        "Increases the damage of your spear while you are riding a mount."};
  }

  @Override
  public String getPassiveStat(int index, WeaponSkillData data) {
    switch (index) {
      case 0:
        return ChatColor.GRAY + "Duration: " + ChatColor.AQUA
            + Math.round(SpearMastery.getAbilityDuration(data) * 100) / 100.0 + " s"
            + ChatColor.GRAY + " (max "
            + WeaponSkillConfig.getDouble("spear.ability.maxDurationSeconds", 14) + " s)";
      case 1:
        return ChatColor.GRAY + "Bonus: " + ChatColor.AQUA + TreeText.percent(
            data.getPassive(SpearMastery.MOMENTUM) * SpearMastery.perk("momentumPerLevel",
                0.003));
      default:
        return ChatColor.GRAY + "Bonus: " + ChatColor.AQUA + TreeText.percent(
            data.getPassive(SpearMastery.MOUNTED_CHARGE) * SpearMastery.perk(
                "mountedChargePerLevel", 0.004));
    }
  }

  @Override
  public String getAbilityDescription() {
    return "Piercing Rush: sneak and left-click a spear to ready it. Your next "
        + SpearMastery.getAbilityCharges() + " spear hits also pierce every hostile mob in a line "
        + "behind the target. Cooldown " + SpearMastery.getAbilityCooldown() + " s.";
  }

  @Override
  public int getPassiveMax(String key) {
    return SpearMastery.getPassiveMax(key);
  }
}
