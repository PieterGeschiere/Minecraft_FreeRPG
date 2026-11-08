package mc.carlton.freerpg.weaponskills;

import org.bukkit.ChatColor;

/** The texts of the Trident Mastery skill tree. */
public class TridentTreeInfo implements SkillTreeInfo {

  @Override
  public WeaponSkill getSkill() {
    return WeaponSkill.TRIDENT;
  }

  @Override
  public String[] getSkillNames() {
    return new String[]{"Wide Maelstrom", "Undertow", "Maelstrom Bite", "Barbed Tips",
        "Tidal Burst", "Sea Shield", "Poseidon's Wrath"};
  }

  @Override
  public String[] getSkillDescriptions() {
    return new String[]{
        "Increases the radius of the Maelstrom by "
            + TreeText.number(TridentMastery.perk("wideMaelstromRadiusPerLevel", 0.5))
            + " blocks per level.",
        "The Maelstrom pulls mobs in " + TreeText.percent(
            TridentMastery.perk("undertowPerLevel", 0.1)) + " harder per level.",
        "The Maelstrom damages the mobs it pulls, "
            + TreeText.number(TridentMastery.perk("biteDamagePerLevel", 0.5))
            + " per half second per level.",
        "Thrown tridents slow the target for "
            + TreeText.number(TridentMastery.perk("barbedSecondsPerLevel", 1.0))
            + " second(s) per level.",
        "The Maelstrom ends with a burst of water that damages and launches mobs.",
        "Thrown tridents that hit grant you two Absorption hearts for "
            + TreeText.number(TridentMastery.perk("seaShieldSeconds", 8)) + " seconds.",
        "Thrown tridents deal " + TreeText.percent(TridentMastery.perk("poseidonsWrathDamage", 0.25))
            + " more damage and the Maelstrom is "
            + TreeText.percent(TridentMastery.perk("poseidonsWrathRadius", 1.5) - 1) + " wider."};
  }

  @Override
  public String[] getPassiveNames() {
    return new String[]{"Maelstrom Duration", "Throwing Power", "Tidal Might"};
  }

  @Override
  public String[] getPassiveDescriptions() {
    return new String[]{"Increases how long Maelstrom stays ready.",
        "Increases the damage of thrown tridents.",
        "Increases the damage of tridents while you are in water or in the rain."};
  }

  @Override
  public String getPassiveStat(int index, WeaponSkillData data) {
    switch (index) {
      case 0:
        return ChatColor.GRAY + "Duration: " + ChatColor.AQUA
            + Math.round(TridentMastery.getAbilityDuration(data) * 100) / 100.0 + " s"
            + ChatColor.GRAY + " (max "
            + WeaponSkillConfig.getDouble("trident.ability.maxDurationSeconds", 14) + " s)";
      case 1:
        return ChatColor.GRAY + "Bonus: " + ChatColor.AQUA + TreeText.percent(
            data.getPassive(TridentMastery.THROWING_POWER) * TridentMastery.perk(
                "throwingPowerPerLevel", 0.003));
      default:
        return ChatColor.GRAY + "Bonus: " + ChatColor.AQUA + TreeText.percent(
            data.getPassive(TridentMastery.TIDAL_MIGHT) * TridentMastery.perk(
                "tidalMightPerLevel", 0.004));
    }
  }

  @Override
  public String getAbilityDescription() {
    return "Maelstrom: left-click a trident to ready it. Your next " + TridentMastery
        .getAbilityCharges() + " thrown tridents create a whirlpool where they land, pulling "
        + "nearby hostile mobs towards it for " + TreeText.number(
        TridentMastery.getMaelstromSeconds()) + " seconds. Cooldown "
        + TridentMastery.getAbilityCooldown() + " s.";
  }

  @Override
  public int getPassiveMax(String key) {
    return TridentMastery.getPassiveMax(key);
  }
}
