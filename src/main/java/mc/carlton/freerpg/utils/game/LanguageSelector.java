package mc.carlton.freerpg.utils.game;

import java.util.Map;
import mc.carlton.freerpg.config.ConfigLoad;
import mc.carlton.freerpg.core.info.player.PlayerStats;
import mc.carlton.freerpg.utils.globalVariables.StringsAndOtherData;
import org.bukkit.entity.Player;

public class LanguageSelector {

  Player p;
  String playerLanguage;


  public LanguageSelector(Player player) {
    if (player != null) {
      this.p = player;
      PlayerStats languageStat = new PlayerStats(p);
      ConfigLoad configLoad = new ConfigLoad();
      if (configLoad.isForceLanguage()) {
        this.playerLanguage = configLoad.getDefaultLanguage();
      } else {
        this.playerLanguage = languageStat.getPlayerLanguage();
      }
    }
  }

  /**
   * Translates a string into english assuming pieces identified by ::translateable;: are a id
   *
   * @param message a String that may contain identifier for parts of string to be translated
   *                Example: "/frpg help [::page::]" --> "/frpg help [page]"
   * @return
   */
  public static String getEnglishMessage(String message) {
    return translateMessage(message, "enUs");
  }

  /**
   * @param message      a String that may contain identifier for parts of string to be translated
   *                     Example: "/frpg help [$:page:$]" --> "/frpg help [page]"
   * @param languageCode a valid language identifier found in languages.yml
   * @return
   */
  public static String translateMessage(String message, String languageCode) {
    String[] splitString = message.split("::");
    String translatedMessage = "";
    for (int i = 0; i < splitString.length; i++) {
      if (i % 2 == 0) { //Even indicies are never translated
        translatedMessage += splitString[i];
      } else { //Odd indicies are always translated
        translatedMessage += getString(splitString[i], languageCode);
      }
    }
    return translatedMessage;
  }

  private static Map<String, String> bundledEnglish;

  /** The English texts of the languages.yml inside the jar, used when the server's copy is old. */
  private static Map<String, String> getBundledEnglish() {
    if (bundledEnglish == null) {
      Map<String, String> texts = new java.util.HashMap<>();
      try (java.io.InputStream stream = mc.carlton.freerpg.FreeRPG.getPlugin(
          mc.carlton.freerpg.FreeRPG.class).getResource("languages.yml")) {
        if (stream != null) {
          org.bukkit.configuration.ConfigurationSection section =
              org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(
                  new java.io.InputStreamReader(stream, java.nio.charset.StandardCharsets.UTF_8))
                  .getConfigurationSection("lang.enUs");
          if (section != null) {
            for (String key : section.getKeys(false)) {
              texts.put(key, section.getString(key));
            }
          }
        }
      } catch (java.io.IOException | RuntimeException e) {
        // no fallback available, the identifier is shown instead
      }
      bundledEnglish = texts;
    }
    return bundledEnglish;
  }

  public static String getString(String id,
      String languageCode) { //Used if we want to ignore the player's language choice
    String text = "";
    StringsAndOtherData stringsAndOtherData = new StringsAndOtherData();
    Map<String, String> idToStringMap = stringsAndOtherData.getIdToStringMap();
    Object text0 = idToStringMap.get(languageCode + "." + id);
    if (text0 != null) {
      text = text0.toString();
    } else if (idToStringMap.get("enUs." + id) != null) {
      text = idToStringMap.get("enUs." + id); //Not translated yet, fall back to English
    } else if (getBundledEnglish().get(id) != null) {
      text = getBundledEnglish().get(id); //languages.yml on the server lacks the key (not updated)
    } else {
      text = id; //If the text isn't found, the next best thing is the identifier
    }
    return text;
  }

  public String getLanguage() {
    return playerLanguage;
  }

  public String getString(String id) {
    return getString(id, playerLanguage);
  }

  /**
   * Translates a string into the player's preffered language assuming pieces identified by
   * $:translateable;$ are a id
   *
   * @param message a String that may contain identifier for parts of string to be translated
   *                Example: "/frpg help [$:page:$]" --> "/frpg help [page]"
   * @return
   */
  public String translateMessage(String message) {
    return translateMessage(message, playerLanguage);
  }


}
