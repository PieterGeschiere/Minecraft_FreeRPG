package mc.carlton.freerpg.commands.sub.menu;

import java.util.ArrayList;
import java.util.Map;
import mc.carlton.freerpg.config.ConfigLoad;
import mc.carlton.freerpg.utils.game.LanguageSelector;
import mc.carlton.freerpg.utils.globalVariables.StringsAndOtherData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

/** One of the skill tree menus opened by /frpg skillTree. */
public interface SkillTreeMenu {

  void open(Player p, String skillName, Inventory gui, Map<String, ArrayList<Number>> pStatAll,
      Map<String, String[]> perksMap, Map<String, String[]> descriptionsMap,
      Map<String, String[]> passivePerksMap, Map<String, String[]> passiveDescriptionsMap,
      LanguageSelector lang, ConfigLoad configLoad, StringsAndOtherData stringsAndOtherData,
      String refundCost);
}
