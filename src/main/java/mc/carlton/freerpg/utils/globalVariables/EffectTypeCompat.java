package mc.carlton.freerpg.utils.globalVariables;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.potion.PotionEffectType;

/**
 * Parses PotionEffectType names, accepting both the modern registry names (jump_boost, haste) and
 * the legacy Bukkit names used by older FreeRPG configs (JUMP, FAST_DIGGING).
 */
public final class EffectTypeCompat {

  /**
   * Legacy Bukkit name to modern registry key, for the effects that were renamed.
   */
  private static final Map<String, String> LEGACY_TO_MODERN = Map.of(
      "SLOW", "slowness",
      "FAST_DIGGING", "haste",
      "SLOW_DIGGING", "mining_fatigue",
      "INCREASE_DAMAGE", "strength",
      "HEAL", "instant_health",
      "HARM", "instant_damage",
      "JUMP", "jump_boost",
      "CONFUSION", "nausea",
      "DAMAGE_RESISTANCE", "resistance");

  private static final Map<String, String> MODERN_TO_LEGACY = new HashMap<>();

  static {
    LEGACY_TO_MODERN.forEach((legacy, modern) -> MODERN_TO_LEGACY.put(modern, legacy));
  }

  private EffectTypeCompat() {
  }

  /**
   * @return the effect type for a modern or legacy name, or null if there is none
   */
  public static PotionEffectType parse(String name) {
    if (name == null) {
      return null;
    }
    String trimmed = name.trim();
    String key = LEGACY_TO_MODERN.getOrDefault(trimmed.toUpperCase(Locale.ROOT),
        trimmed.toLowerCase(Locale.ROOT));
    NamespacedKey namespacedKey = NamespacedKey.fromString(key);
    return namespacedKey == null ? null : Registry.EFFECT.get(namespacedKey);
  }

  /**
   * @return the legacy Bukkit name of an effect type (e.g. FAST_DIGGING for haste)
   */
  public static String legacyName(PotionEffectType effectType) {
    String key = effectType.getKeyOrThrow().getKey();
    return MODERN_TO_LEGACY.getOrDefault(key, key.toUpperCase(Locale.ROOT));
  }
}
