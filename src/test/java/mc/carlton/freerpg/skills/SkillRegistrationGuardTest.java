package mc.carlton.freerpg.skills;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class SkillRegistrationGuardTest {

  private static final Set<String> ALLOWED = Set.of("SkillRegistry.java", "SkillName.java");

  @Test
  void noHardCodedSkillListsOutsideAllowedFiles() throws IOException {
    Path root = Paths.get("src/main/java");
    List<String> offenders = new ArrayList<>();
    try (Stream<Path> files = Files.walk(root)) {
      for (Path file : files.filter(p -> p.toString().endsWith(".java"))
          .collect(Collectors.toList())) {
        if (ALLOWED.contains(file.getFileName().toString())) {
          continue;
        }
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        for (int i = 0; i < lines.size(); i++) {
          String line = lines.get(i);
          if (line.contains("\"smelting\"") && line.contains("\"enchanting\"")) {
            offenders.add(file + ":" + (i + 1));
          }
        }
      }
    }
    assertTrue(offenders.isEmpty(), "Hard-coded skill lists found: " + offenders);
  }
}
