package mc.carlton.freerpg.elites;

import mc.carlton.freerpg.utils.game.LanguageSelector;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * Texts of the elite and Blood Moon system, read from languages.yml (keys starting with "elite"
 * or "bloodMoon"). Texts use &amp; color codes and {name} placeholders. Languages without the key
 * show the English text.
 */
public final class EliteMessages {

  private EliteMessages() {
  }

  /** The colored text for a player (their language). A null player gets English. */
  public static String text(Player p, String key, String... keyValues) {
    String raw = p == null ? LanguageSelector.getString(key, "enUs")
        : new LanguageSelector(p).getString(key);
    for (int i = 0; i + 1 < keyValues.length; i += 2) {
      String value = keyValues[i + 1];
      raw = raw.replace("{" + keyValues[i] + "}", value == null ? "" : value);
    }
    return ChatColor.translateAlternateColorCodes('&', raw);
  }

  /** Text with the [Elites] prefix. */
  public static String prefixed(Player p, String key, String... keyValues) {
    return text(p, "elitePrefix") + text(p, key, keyValues);
  }
}
