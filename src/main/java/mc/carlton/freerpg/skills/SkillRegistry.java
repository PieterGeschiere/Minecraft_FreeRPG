package mc.carlton.freerpg.skills;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Single source of truth for the skill labels (data order) and their English titles.
 */
public final class SkillRegistry {

  private static final String GLOBAL = "global";
  private static final int MAIN_COUNT = 10;

  private static final List<String> LABELS = Collections.unmodifiableList(Arrays.asList(
      "digging", "woodcutting", "mining", "farming", "fishing", "archery", "beastMastery",
      "swordsmanship", "defense", "axeMastery", "repair", "agility", "alchemy", "smelting",
      "enchanting", "building"));

  private static final List<String> TITLES = Collections.unmodifiableList(Arrays.asList(
      "Digging", "Woodcutting", "Mining", "Farming", "Fishing", "Archery", "Beast Mastery",
      "Swordsmanship", "Defense", "Axe Mastery", "Repair", "Agility", "Alchemy", "Smelting",
      "Enchanting", "Building"));

  private static final List<String> MAIN_LABELS =
      Collections.unmodifiableList(new ArrayList<>(LABELS.subList(0, MAIN_COUNT)));
  private static final List<String> PASSIVE_LABELS =
      Collections.unmodifiableList(new ArrayList<>(LABELS.subList(MAIN_COUNT, LABELS.size())));

  private static final List<String> LABELS_WITH_GLOBAL = withExtra(LABELS, GLOBAL);
  private static final List<String> TITLES_WITH_GLOBAL = withExtra(TITLES, "Global");

  private SkillRegistry() {
  }

  private static List<String> withExtra(List<String> base, String extra) {
    List<String> list = new ArrayList<>(base);
    list.add(extra);
    return Collections.unmodifiableList(list);
  }

  /** The 16 skill labels, immutable. */
  public static List<String> labels() {
    return LABELS;
  }

  /** The 10 main (active-ability) skill labels, immutable. */
  public static List<String> mainLabels() {
    return MAIN_LABELS;
  }

  /** The 6 passive skill labels, immutable. */
  public static List<String> passiveLabels() {
    return PASSIVE_LABELS;
  }

  /** The 16 skill labels followed by "global", immutable. */
  public static List<String> labelsWithGlobal() {
    return LABELS_WITH_GLOBAL;
  }

  /** English titles of the 16 skills, immutable. */
  public static List<String> titles() {
    return TITLES;
  }

  /** English titles of the 16 skills followed by "Global", immutable. */
  public static List<String> titlesWithGlobal() {
    return TITLES_WITH_GLOBAL;
  }

  /** English title for a label (including "global"), or null if unknown. */
  public static String titleOf(String label) {
    int index = LABELS_WITH_GLOBAL.indexOf(label);
    return index == -1 ? null : TITLES_WITH_GLOBAL.get(index);
  }

  /** Index of the label among the 16 skills, -1 if unknown (including "global"). */
  public static int indexOf(String label) {
    return LABELS.indexOf(label);
  }

  public static boolean isSkill(String label) {
    return LABELS.contains(label);
  }

  public static boolean isMain(String label) {
    int index = indexOf(label);
    return index >= 0 && index < MAIN_COUNT;
  }

  public static boolean isPassive(String label) {
    int index = indexOf(label);
    return index >= MAIN_COUNT;
  }

  /** A fresh array of the 16 labels. */
  public static String[] labelArray() {
    return LABELS.toArray(new String[0]);
  }

  /** A fresh array of the 16 labels plus "global". */
  public static String[] labelArrayWithGlobal() {
    return LABELS_WITH_GLOBAL.toArray(new String[0]);
  }

  /** A fresh array of the 16 English titles. */
  public static String[] titleArray() {
    return TITLES.toArray(new String[0]);
  }
}
