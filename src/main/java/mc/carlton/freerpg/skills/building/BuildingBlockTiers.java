package mc.carlton.freerpg.skills.building;

import java.util.List;
import java.util.Locale;

/** Flat experience tiers: decorative blocks earn more than basic ones, no per-material values. */
public final class BuildingBlockTiers {

  public static final String BASIC = "basicBlock";
  public static final String DECORATIVE = "decorativeBlock";
  private static final List<String> DECORATIVE_WORDS = List.of("STAIRS", "SLAB", "WALL", "FENCE",
      "GLASS", "CONCRETE", "TERRACOTTA", "BANNER", "LANTERN", "CARPET", "BRICK");

  private BuildingBlockTiers() {
  }

  public static String tierKey(String materialName) {
    String name = materialName == null ? "" : materialName.toUpperCase(Locale.ROOT);
    for (String word : DECORATIVE_WORDS) {
      if (name.contains(word)) {
        return DECORATIVE;
      }
    }
    return BASIC;
  }
}
