package mc.carlton.freerpg.elites;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

/** Every elite / Blood Moon text the code asks for exists in languages.yml (English). */
class EliteLanguageKeysTest {

  private static final Pattern KEY =
      Pattern.compile("\"@?((?:elite|bloodMoon|worldEvent)[A-Z][A-Za-z]*)\"");

  private static ConfigurationSection english() throws Exception {
    try (InputStream stream = EliteLanguageKeysTest.class.getClassLoader()
        .getResourceAsStream("languages.yml")) {
      assertNotNull(stream);
      return YamlConfiguration.loadConfiguration(
          new InputStreamReader(stream, StandardCharsets.UTF_8)).getConfigurationSection("lang")
          .getConfigurationSection("enUs");
    }
  }

  private static String allCode() throws Exception {
    StringBuilder code = new StringBuilder();
    try (Stream<Path> files = Files.walk(Path.of("src/main/java"))) {
      for (Path file : (Iterable<Path>) files.filter(f -> f.toString().endsWith(".java"))::iterator) {
        code.append(Files.readString(file));
      }
    }
    return code.toString();
  }

  @Test
  void everyKeyUsedInTheCodeExists() throws Exception {
    ConfigurationSection english = english();
    Set<String> used = new TreeSet<>();
    Matcher m = KEY.matcher(allCode());
    while (m.find()) {
      used.add(m.group(1));
    }
    assertFalse(used.isEmpty());
    Set<String> missing = new TreeSet<>();
    for (String key : used) {
      String text = english.getString(key);
      if (text == null || text.isBlank()) {
        missing.add(key);
      }
    }
    assertTrue(missing.isEmpty(), "languages.yml (enUs) lacks " + missing);
  }

  @Test
  void everyEliteTextInLanguagesYmlIsUsedSomewhere() throws Exception {
    ConfigurationSection english = english();
    String code = allCode();
    Set<String> unused = new TreeSet<>();
    for (String key : english.getKeys(false)) {
      if ((key.startsWith("elite") || key.startsWith("bloodMoon") || key.startsWith("worldEvent"))
          && !code.contains("\"" + key + "\"")) {
        unused.add(key);
      }
    }
    assertTrue(unused.isEmpty(), "unused elite texts " + unused);
  }
}
