package mc.carlton.freerpg.quests;

import mc.carlton.freerpg.utils.game.LanguageSelector;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * Texts of the quest system, read from languages.yml (keys starting with "quest"). Texts use
 * &amp; color codes and {name} placeholders. Languages without the key show the English text.
 */
public final class QuestMessages {

  private QuestMessages() {
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

  /** Text with the [Quests] prefix. */
  public static String prefixed(Player p, String key, String... keyValues) {
    return text(p, "questPrefix") + text(p, key, keyValues);
  }

  public static void send(Player p, String key, String... keyValues) {
    if (p != null) {
      p.sendMessage(prefixed(p, key, keyValues));
    }
  }
}
