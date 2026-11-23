package mc.carlton.freerpg.party;

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

/** Every party text the code asks for exists in languages.yml (English), and none is blank. */
class PartyLanguageKeysTest {

  private static final Pattern KEY = Pattern.compile("\"@?(party[A-Z][A-Za-z]*)\"");

  private static ConfigurationSection english() throws Exception {
    try (InputStream stream = PartyLanguageKeysTest.class.getClassLoader()
        .getResourceAsStream("languages.yml")) {
      assertNotNull(stream);
      return YamlConfiguration.loadConfiguration(
          new InputStreamReader(stream, StandardCharsets.UTF_8)).getConfigurationSection("lang")
          .getConfigurationSection("enUs");
    }
  }

  @Test
  void everyPartyKeyUsedInTheCodeExists() throws Exception {
    ConfigurationSection english = english();
    Set<String> used = new TreeSet<>();
    try (Stream<Path> files = Files.walk(Path.of("src/main/java"))) {
      for (Path file : (Iterable<Path>) files.filter(f -> f.toString().endsWith(".java"))::iterator) {
        Matcher m = KEY.matcher(Files.readString(file));
        while (m.find()) {
          used.add(m.group(1));
        }
      }
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
}
