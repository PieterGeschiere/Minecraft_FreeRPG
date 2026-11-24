package mc.carlton.freerpg.resources;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

/** Every language of languages.yml has a text for every key the English block has. */
class LanguageCompletenessTest {

  /** The languages that must be complete ("custom" is the server owner's own copy). */
  private static final List<String> LANGUAGES = List.of("huHU", "frFR", "deDE", "plPL", "esCl",
      "ruRU", "ptBR", "csCZ", "koKR", "nlNL");

  private static ConfigurationSection languages() throws Exception {
    try (InputStream stream = LanguageCompletenessTest.class.getClassLoader()
        .getResourceAsStream("languages.yml")) {
      assertNotNull(stream);
      YamlConfiguration yaml = new YamlConfiguration();
      yaml.load(new InputStreamReader(stream, StandardCharsets.UTF_8)); // says why it is invalid
      return yaml.getConfigurationSection("lang");
    }
  }

  @Test
  void everyLanguageHasEveryKey() throws Exception {
    ConfigurationSection lang = languages();
    Set<String> english = lang.getConfigurationSection("enUs").getKeys(false);
    for (String code : LANGUAGES) {
      ConfigurationSection block = lang.getConfigurationSection(code);
      assertNotNull(block, "languages.yml has no block for " + code);
      Set<String> missing = new TreeSet<>(english);
      missing.removeAll(block.getKeys(false));
      assertTrue(missing.isEmpty(), code + " lacks " + missing.size() + " texts, for example "
          + missing.stream().limit(10).toList());
    }
  }

  @Test
  void noLanguageHasATextTheEnglishBlockLacks() throws Exception {
    ConfigurationSection lang = languages();
    Set<String> english = lang.getConfigurationSection("enUs").getKeys(false);
    for (String code : LANGUAGES) {
      Set<String> extra = new TreeSet<>(lang.getConfigurationSection(code).getKeys(false));
      extra.removeAll(english);
      assertTrue(extra.isEmpty(), code + " has texts English does not have: " + extra);
    }
  }

  @Test
  void placeholdersAndColorCodesSurviveTranslation() throws Exception {
    ConfigurationSection lang = languages();
    ConfigurationSection english = lang.getConfigurationSection("enUs");
    Pattern placeholder = Pattern.compile("\\{[A-Za-z]+\\}");
    List<String> problems = new ArrayList<>();
    for (String code : LANGUAGES) {
      ConfigurationSection block = lang.getConfigurationSection(code);
      for (String key : english.getKeys(false)) {
        String en = english.getString(key);
        String tr = block.getString(key);
        if (en == null || tr == null) {
          continue;
        }
        Set<String> wanted = new TreeSet<>();
        Matcher m = placeholder.matcher(en);
        while (m.find()) {
          wanted.add(m.group());
        }
        // only the party, quest and weapon texts promise the same placeholders in every language
        if (!key.startsWith("party") && !key.startsWith("quest")) {
          continue;
        }
        for (String ph : wanted) {
          if (!tr.contains(ph)) {
            problems.add(code + "." + key + " lost " + ph);
          }
        }
      }
    }
    assertTrue(problems.isEmpty(), "placeholders lost: " + problems.stream().limit(15).toList());
    assertFalse(english.getKeys(false).isEmpty());
    assertEquals(10, LANGUAGES.size());
  }
}
