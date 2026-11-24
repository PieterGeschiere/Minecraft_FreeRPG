package mc.carlton.freerpg.skills.perksAndAbilities;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import mc.carlton.freerpg.config.ConfigLoad;
import mc.carlton.freerpg.core.info.player.PlayerStats;
import mc.carlton.freerpg.skills.building.BuildingBlockTiers;
import mc.carlton.freerpg.skills.building.BuildingXpRules;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/** The Building skill: experience for placing blocks, plus helpers the protection perks use. */
public class Building extends Skill {

  public static final String SKILL_NAME = "building";
  /** Stat indexes in the player's skill array (same layout as the other passive skills). */
  public static final int PASSIVE_REACH = 4;
  public static final int PERK_WEATHERPROOF = 7;
  public static final int PERK_FORTIFIED = 9;
  public static final int PERK_DEMOLITION = 13;

  private static BuildingXpRules xpRules;

  public Building(Player p) {
    super(p);
    ConfigLoad configLoad = new ConfigLoad();
    expMap = configLoad.getExpMapForSkill(SKILL_NAME);
  }

  public static boolean isEnabled() {
    Boolean allowed = new ConfigLoad().getAllowedSkillsMap().get(SKILL_NAME);
    return allowed == null || allowed;
  }

  /** The shared anti-farming rules, built from the config the first time they are needed. */
  public static synchronized BuildingXpRules xpRules() {
    if (xpRules == null) {
      ConfigLoad configLoad = new ConfigLoad();
      xpRules = new BuildingXpRules(configLoad.getBuildingXpGraceMillis(),
          configLoad.getBuildingXpPerMaterialPerMinute());
    }
    return xpRules;
  }

  /** Awards experience for a block this player just placed (flat tiers, no per-material values). */
  public void onBlockPlaced(Block block) {
    if (expMap == null) {
      return;
    }
    Integer exp = expMap.get(BuildingBlockTiers.tierKey(block.getType().name()));
    if (exp != null && exp > 0) {
      increaseStats.changeEXP(SKILL_NAME, exp);
    }
  }

  /**
   * A perk or passive value from the loaded stats of any player (online, or recent and preloaded).
   * 0 when the player's stats are not loaded.
   */
  public static int statOf(UUID player, int statIndex) {
    try {
      Map<String, ArrayList<Number>> stats = new PlayerStats(player).getData().get(player);
      if (stats == null || !stats.containsKey(SKILL_NAME)) {
        return 0;
      }
      ArrayList<Number> building = stats.get(SKILL_NAME);
      return statIndex < building.size() ? building.get(statIndex).intValue() : 0;
    } catch (RuntimeException e) {
      return 0;
    }
  }
}
