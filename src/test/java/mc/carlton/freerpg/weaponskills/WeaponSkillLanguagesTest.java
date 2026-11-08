package mc.carlton.freerpg.weaponskills;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** Every language of languages.yml must translate all weapon skill texts, with the same placeholders. */
class WeaponSkillLanguagesTest {

  private static final List<String> LANGUAGES = List.of("huHU", "frFR", "deDE", "plPL", "esCl",
      "ruRU", "ptBR", "csCZ", "koKR");
  private static final Pattern PLACEHOLDER = Pattern.compile("\\{\\w+}");

  private static ConfigurationSection lang;

  @BeforeAll
  static void load() throws Exception {
    try (InputStream stream = WeaponSkillLanguagesTest.class.getClassLoader()
        .getResourceAsStream("languages.yml")) {
      assertNotNull(stream, "languages.yml is not on the classpath");
      lang = YamlConfiguration.loadConfiguration(
          new InputStreamReader(stream, StandardCharsets.UTF_8)).getConfigurationSection("lang");
    }
    assertNotNull(lang);
  }

  private static List<String> weaponKeys() {
    List<String> keys = new ArrayList<>();
    for (String key : lang.getConfigurationSection("enUs").getKeys(false)) {
      if (key.startsWith("weapon")) {
        keys.add(key);
      }
    }
    return keys;
  }

  private static TreeSet<String> placeholders(String text) {
    TreeSet<String> found = new TreeSet<>();
    Matcher m = PLACEHOLDER.matcher(text);
    while (m.find()) {
      found.add(m.group());
    }
    return found;
  }

  @Test
  void englishHasTheWeaponTexts() {
    assertFalse(weaponKeys().isEmpty());
    assertEquals(143, weaponKeys().size());
  }

  @Test
  void everyLanguageTranslatesEveryWeaponText() {
    for (String language : LANGUAGES) {
      ConfigurationSection section = lang.getConfigurationSection(language);
      assertNotNull(section, language + " is missing");
      for (String key : weaponKeys()) {
        String text = section.getString(key);
        assertNotNull(text, language + " has no " + key);
        assertFalse(text.isBlank(), language + "." + key + " is empty");
      }
    }
  }

  @Test
  void everyLanguageHasExactlyTheKeysOfEnglish() {
    java.util.Set<String> english = lang.getConfigurationSection("enUs").getKeys(false);
    for (String language : LANGUAGES) {
      java.util.Set<String> keys = lang.getConfigurationSection(language).getKeys(false);
      java.util.Set<String> missing = new TreeSet<>(english);
      missing.removeAll(keys);
      java.util.Set<String> extra = new TreeSet<>(keys);
      extra.removeAll(english);
      assertTrue(missing.isEmpty(), language + " is missing " + missing);
      assertTrue(extra.isEmpty(), language + " has unknown keys (misspelled?) " + extra);
    }
  }

  @Test
  void textsUsedByTheCodeExist() {
    // keys that the Java code asks for with a literal name and that were once missing
    assertNotNull(lang.getString("enUs.requiredSouls"));
  }

  @Test
  void translationsKeepThePlaceholdersOfTheEnglishText() {
    for (String language : LANGUAGES) {
      ConfigurationSection section = lang.getConfigurationSection(language);
      for (String key : weaponKeys()) {
        assertEquals(placeholders(lang.getString("enUs." + key)),
            placeholders(section.getString(key)), language + "." + key);
      }
    }
  }
}
