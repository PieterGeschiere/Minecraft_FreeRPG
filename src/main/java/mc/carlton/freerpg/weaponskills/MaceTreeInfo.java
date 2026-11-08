package mc.carlton.freerpg.weaponskills;

import org.bukkit.ChatColor;

/** The texts of the Mace Mastery skill tree. */
public class MaceTreeInfo implements SkillTreeInfo {

  @Override
  public WeaponSkill getSkill() {
    return WeaponSkill.MACE;
  }

  @Override
  public String[] getSkillNames() {
    return new String[]{"Wide Shockwave", "Stagger", "Aftershock", "Rattle", "Earthshaker",
        "Adrenaline", "Titan's Wrath"};
  }

  @Override
  public String[] getSkillDescriptions() {
    return new String[]{
        "Increases the radius of Seismic Slam's shockwave by "
            + TreeText.number(MaceMastery.perk("wideShockwaveRadiusPerLevel", 0.5))
            + " blocks per level.",
        "Smash attacks slow the target for "
            + TreeText.number(MaceMastery.perk("staggerSecondsPerLevel", 1.0))
            + " second(s) per level.",
        "Increases the damage of Seismic Slam's shockwave by "
            + TreeText.percent(MaceMastery.perk("aftershockPerLevel", 0.06))
            + " of the smash damage per level.",
        "Smash attacks weaken the target for "
            + TreeText.number(MaceMastery.perk("rattleSecondsPerLevel", 2.0))
            + " seconds per level.",
        "Seismic Slam launches mobs into the air and its shockwave deals "
            + TreeText.percent(MaceMastery.perk("earthshakerDamage", 0.2)) + " more damage.",
        "Smash attacks grant you Speed and Resistance for "
            + TreeText.number(MaceMastery.perk("adrenalineSeconds", 4)) + " seconds.",
        "Smash attacks deal " + TreeText.percent(MaceMastery.perk("titansWrathDamage", 0.25))
            + " more damage and Seismic Slam's shockwave is "
            + TreeText.percent(MaceMastery.perk("titansWrathRadius", 1.5) - 1) + " wider."};
  }

  @Override
  public String[] getPassiveNames() {
    return new String[]{"Slam Duration", "Smash Power", "Soft Landing"};
  }

  @Override
  public String[] getPassiveDescriptions() {
    return new String[]{"Increases how long Seismic Slam stays ready.",
        "Increases the damage of your smash attacks.",
        "Reduces the fall damage you take while holding a mace."};
  }

  @Override
  public String getPassiveStat(int index, WeaponSkillData data) {
    switch (index) {
      case 0:
        return ChatColor.GRAY + "Duration: " + ChatColor.AQUA
            + Math.round(MaceMastery.getAbilityDuration(data) * 100) / 100.0 + " s"
            + ChatColor.GRAY + " (max "
            + WeaponSkillConfig.getDouble("mace.ability.maxDurationSeconds", 12) + " s)";
      case 1:
        return ChatColor.GRAY + "Bonus: " + ChatColor.AQUA + TreeText.percent(
            data.getPassive(MaceMastery.SMASH_POWER) * MaceMastery.perk("smashPowerPerLevel",
                0.003));
      default:
        return ChatColor.GRAY + "Reduction: " + ChatColor.AQUA
            + TreeText.percent(MaceMastery.getFallReduction(data));
    }
  }

  @Override
  public String getAbilityDescription() {
    return "Seismic Slam: right-click a mace to ready it. For a short time (up to "
        + MaceMastery.getAbilityCharges() + " smashes) your smash attacks send a shockwave "
        + "through nearby hostile mobs. Cooldown " + MaceMastery.getAbilityCooldown() + " s.";
  }

  @Override
  public int getPassiveMax(String key) {
    return MaceMastery.getPassiveMax(key);
  }
}
