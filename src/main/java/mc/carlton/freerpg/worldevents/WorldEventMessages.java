package mc.carlton.freerpg.worldevents;

import mc.carlton.freerpg.utils.game.LanguageSelector;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * Texts of the world events, read from languages.yml. Texts use &amp; color codes and {name}
 * placeholders. Languages without the key show the English text.
 */
public final class WorldEventMessages {

  private WorldEventMessages() {
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

  /** Text with the [Events] prefix. */
  public static String prefixed(Player p, String key, String... keyValues) {
    return text(p, "worldEventPrefix") + text(p, key, keyValues);
  }
}
