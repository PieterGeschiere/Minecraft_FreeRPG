package mc.carlton.freerpg.core.serverFileManagement;

import java.util.ArrayList;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

/** Test helper exposing the package-private PlayerStatsFilePreparation seam to other packages. */
public final class PlayerYamlTestSupport {

  private PlayerYamlTestSupport() {
  }

  /** Builds a freshly created default player YAML (no file, no Bukkit server). */
  public static FileConfiguration newDefaultPlayerYaml() {
    YamlConfiguration config = new YamlConfiguration();
    ArrayList<Double> tokens = new ArrayList<>();
    for (int i = 0; i < 4; i++) {
      tokens.add(0.0);
    }
    tokens.add(0.0);
    tokens.add(0.0);
    tokens.add(0.0);
    ArrayList<Integer> souls = new ArrayList<>();
    souls.add(0);
    new PlayerStatsFilePreparation(config).createPlayerYAML("Steve", "en", tokens, souls);
    return config;
  }
}
