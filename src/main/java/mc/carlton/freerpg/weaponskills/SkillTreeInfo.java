package mc.carlton.freerpg.weaponskills;

import org.bukkit.entity.Player;

/**
 * The texts and limits of the skill tree of one weapon skill. Every weapon skill has the same tree
 * shape as the main skills: three passives ("p1", "p2", "p3") and seven skills ("1a", "1b", "2a",
 * "2b", "3a", "3b", "M"), so only the texts differ.
 */
public interface SkillTreeInfo {

  WeaponSkill getSkill();

  /** Names of the seven skills in the order 1a, 1b, 2a, 2b, 3a, 3b, M. */
  String[] getSkillNames(Player p);

  /** Descriptions of the seven skills in the same order. */
  String[] getSkillDescriptions(Player p);

  /** Names of the three passives. */
  String[] getPassiveNames(Player p);

  /** Descriptions of the three passives. */
  String[] getPassiveDescriptions(Player p);

  /** The coloured stat line of a passive (index 0-2), for example "Duration: 8 s". */
  String getPassiveStat(Player p, int index, WeaponSkillData data);

  /** Description of the ability, shown on the first passive. */
  String getAbilityDescription(Player p);

  /** Highest level of a passive ("p1", "p2" or "p3"). */
  int getPassiveMax(String key);
}
