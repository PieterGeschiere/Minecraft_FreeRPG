package mc.carlton.freerpg.commands.sub;

import java.util.ArrayList;
import java.util.Map;
import mc.carlton.freerpg.core.info.player.PlayerStats;
import mc.carlton.freerpg.utils.game.LanguageSelector;
import mc.carlton.freerpg.utils.gui.GuiItem;
import mc.carlton.freerpg.utils.gui.GuiWrapper;
import org.bukkit.Material;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** Helpers shared by /frpg sub-commands. */
public final class CommandSupport {

  private CommandSupport() {
  }

  public static boolean isTargetOnline(Player target, CommandSender sender) {
    if (target == null) {
      if (sender instanceof Player) {
        Player p = (Player) sender;
        LanguageSelector lang = new LanguageSelector(p);
        p.sendMessage(ChatColor.RED + lang.getString("playerOffline"));
      } else {
        sender.sendMessage("Player not online");
      }
      return false;
    }
    return true;
  }

  public static int getArgumentAsInteger(String arg) {
    try {
      return Integer.valueOf(arg);
    } catch (NumberFormatException e) {
      return Integer.MAX_VALUE; //Sentinel meaning "could not convert"
    }
  }

  public static double getArgumentAsDouble(String arg) {
    try {
      return Double.valueOf(arg);
    } catch (NumberFormatException e) {
      return Double.MAX_VALUE; //Sentinel meaning "could not convert"
    }
  }

  public static void addToggleButton(Player p, GuiWrapper gui, String parseableName,
      Material iconItemType, int iconPosition, int toggleIndex) {
    LanguageSelector lang = new LanguageSelector(p);
    PlayerStats pStatClass = new PlayerStats(p);
    Map<String, ArrayList<Number>> pStat = pStatClass.getPlayerData();

    GuiItem guiItem = new GuiItem(iconItemType, iconPosition, gui);
    guiItem.setName(lang.translateMessage(parseableName));
    gui.addItem(guiItem);

    GuiItem guiItemToggle = new GuiItem(Material.LIME_DYE, iconPosition + 9, gui);
    if ((int) pStat.get("global").get(toggleIndex) > 0) {
      guiItemToggle.setNameColor(ChatColor.BOLD + ChatColor.GREEN.toString());
      guiItemToggle.setName(lang.getString("on0"));
    } else {
      guiItemToggle.setItemType(Material.GRAY_DYE);
      guiItemToggle.setNameColor(ChatColor.BOLD + ChatColor.RED.toString());
      guiItemToggle.setName(lang.getString("off0"));
    }
    gui.addItem(guiItemToggle);
  }

  public static void addToggleButton(Player p, GuiWrapper gui, String parseableName,
      Material iconItemType, int iconPosition, boolean toggleCondition) {
    LanguageSelector lang = new LanguageSelector(p);

    GuiItem guiItem = new GuiItem(iconItemType, iconPosition, gui);
    guiItem.setName(lang.translateMessage(parseableName));
    gui.addItem(guiItem);

    GuiItem guiItemToggle = new GuiItem(Material.LIME_DYE, iconPosition + 9, gui);
    if (toggleCondition) {
      guiItemToggle.setNameColor(ChatColor.BOLD + ChatColor.GREEN.toString());
      guiItemToggle.setName(lang.getString("on0"));
    } else {
      guiItemToggle.setItemType(Material.GRAY_DYE);
      guiItemToggle.setNameColor(ChatColor.BOLD + ChatColor.RED.toString());
      guiItemToggle.setName(lang.getString("off0"));
    }
    gui.addItem(guiItemToggle);
  }
}
