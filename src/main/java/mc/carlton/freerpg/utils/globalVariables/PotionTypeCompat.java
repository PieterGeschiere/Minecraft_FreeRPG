package mc.carlton.freerpg.utils.globalVariables;

import java.util.Map;
import org.bukkit.potion.PotionType;

/** Parses PotionType names, accepting the legacy names used by older FreeRPG configs. */
public final class PotionTypeCompat {

  private static final Map<String, String> LEGACY = Map.of(
      "INSTANT_HEAL", "HEALING",
      "INSTANT_DAMAGE", "HARMING",
      "REGEN", "REGENERATION",
      "SPEED", "SWIFTNESS",
      "JUMP", "LEAPING");

  private PotionTypeCompat() {
  }

  public static PotionType parse(String name) {
    String key = name.trim().toUpperCase();
    try {
      return PotionType.valueOf(LEGACY.getOrDefault(key, key));
    } catch (IllegalArgumentException e) {
      return null;
    }
  }
}
