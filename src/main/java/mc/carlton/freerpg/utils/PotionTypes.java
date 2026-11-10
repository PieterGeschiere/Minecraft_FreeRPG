package mc.carlton.freerpg.utils;

import org.bukkit.potion.PotionType;

/**
 * Helpers for the modern potion model, where "extended" and "upgraded" potions are their own
 * {@link PotionType}s ({@code LONG_POISON}, {@code STRONG_POISON}) instead of flags on the removed
 * {@code PotionData} class.
 */
public final class PotionTypes {

  private static final String LONG_PREFIX = "LONG_";
  private static final String STRONG_PREFIX = "STRONG_";

  private PotionTypes() {
  }

  /**
   * Returns the variant of {@code base} with the given modifier, or {@code base} itself when that
   * variant does not exist (like the old {@code PotionData}, upgraded wins over extended).
   */
  public static PotionType of(PotionType base, boolean extended, boolean upgraded) {
    if (base == null || (!extended && !upgraded)) {
      return base;
    }
    String name = (upgraded ? STRONG_PREFIX : LONG_PREFIX) + getBase(base).name();
    try {
      return PotionType.valueOf(name);
    } catch (IllegalArgumentException e) {
      return base;
    }
  }

  /**
   * Returns the unmodified type of a potion, e.g. {@code POISON} for {@code LONG_POISON}.
   */
  public static PotionType getBase(PotionType type) {
    if (type == null) {
      return null;
    }
    String name = type.name();
    String baseName = null;
    if (name.startsWith(LONG_PREFIX)) {
      baseName = name.substring(LONG_PREFIX.length());
    } else if (name.startsWith(STRONG_PREFIX)) {
      baseName = name.substring(STRONG_PREFIX.length());
    }
    if (baseName == null) {
      return type;
    }
    try {
      return PotionType.valueOf(baseName);
    } catch (IllegalArgumentException e) {
      return type;
    }
  }

  public static boolean isExtended(PotionType type) {
    return type != null && type.name().startsWith(LONG_PREFIX);
  }

  public static boolean isUpgraded(PotionType type) {
    return type != null && type.name().startsWith(STRONG_PREFIX);
  }
}
