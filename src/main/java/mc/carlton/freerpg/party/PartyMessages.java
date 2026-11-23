package mc.carlton.freerpg.party;

import mc.carlton.freerpg.utils.game.LanguageSelector;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * Texts of the party system, read from languages.yml (keys starting with "party"). Texts use
 * &amp; color codes and {name} placeholders. A placeholder value that starts with "@" is itself a
 * text key and gets translated, for example "@partyWordOn".
 */
public final class PartyMessages {

  private PartyMessages() {
  }

  /** The colored text for a player (their language). A null player gets English. */
  public static String text(Player p, String key, String... keyValues) {
    String raw = p == null ? LanguageSelector.getString(key, "enUs")
        : new LanguageSelector(p).getString(key);
    for (int i = 0; i + 1 < keyValues.length; i += 2) {
      String value = keyValues[i + 1];
      if (value != null && value.startsWith("@")) {
        value = text(p, value.substring(1));
      }
      raw = raw.replace("{" + keyValues[i] + "}", value == null ? "" : value);
    }
    return ChatColor.translateAlternateColorCodes('&', raw);
  }

  /** Text with the [Party] prefix. */
  public static String prefixed(Player p, String key, String... keyValues) {
    return text(p, "partyPrefix") + text(p, key, keyValues);
  }

  public static void send(Player p, String key, String... keyValues) {
    if (p != null) {
      p.sendMessage(prefixed(p, key, keyValues));
    }
  }

  /** English text with prefix, for notices that are stored until a player logs in. */
  public static String plain(String key, String... keyValues) {
    return prefixed(null, key, keyValues);
  }
}
