package mc.carlton.freerpg.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import org.bukkit.Color;
import org.junit.jupiter.api.Test;

/** The small text and collection helpers used all over the plugin. */
class UtilityMethodsTest {

  @Test
  void capitalizeOnlyChangesTheFirstLetter() {
    assertEquals("", UtilityMethods.capitalizeString(""));
    assertEquals("A", UtilityMethods.capitalizeString("a"));
    assertEquals("Abc", UtilityMethods.capitalizeString("abc"));
    assertEquals("ABC", UtilityMethods.capitalizeString("ABC"));
    assertEquals("Iron golem", UtilityMethods.capitalizeString("iron golem"));
  }

  @Test
  void rankingStringsUseTheRightSuffix() {
    assertEquals("1st", UtilityMethods.intToRankingString(1));
    assertEquals("2nd", UtilityMethods.intToRankingString(2));
    assertEquals("3rd", UtilityMethods.intToRankingString(3));
    assertEquals("4th", UtilityMethods.intToRankingString(4));
    assertEquals("10th", UtilityMethods.intToRankingString(10));
    assertEquals("11th", UtilityMethods.intToRankingString(11));
    assertEquals("12th", UtilityMethods.intToRankingString(12));
    assertEquals("13th", UtilityMethods.intToRankingString(13));
    assertEquals("21st", UtilityMethods.intToRankingString(21));
    assertEquals("101st", UtilityMethods.intToRankingString(101));
    assertEquals("111th", UtilityMethods.intToRankingString(111));
    assertEquals("112th", UtilityMethods.intToRankingString(112));
  }

  @Test
  void alphabeticNumbersCountLikeSpreadsheetColumns() {
    assertEquals("A", UtilityMethods.toAlphabetic(0));
    assertEquals("Z", UtilityMethods.toAlphabetic(25));
    assertEquals("AA", UtilityMethods.toAlphabetic(26));
    assertEquals("AB", UtilityMethods.toAlphabetic(27));
    assertEquals("AZ", UtilityMethods.toAlphabetic(51));
    assertEquals("BA", UtilityMethods.toAlphabetic(52));
    assertEquals("ZZ", UtilityMethods.toAlphabetic(701));
    assertEquals("AAA", UtilityMethods.toAlphabetic(702));
    assertEquals("-A", UtilityMethods.toAlphabetic(-1));
  }

  @Test
  void camelCaseGetsSpacesBeforeCapitals() {
    assertEquals("mace Mastery", UtilityMethods.camelCaseToSpacedString("maceMastery"));
    assertEquals("crossbow Mastery", UtilityMethods.camelCaseToSpacedString("crossbowMastery"));
    assertEquals("already_snake", UtilityMethods.camelCaseToSpacedString("already_snake"));
    assertEquals("plain", UtilityMethods.camelCaseToSpacedString("plain"));
  }

  @Test
  void containsIgnoresCase() {
    assertTrue(UtilityMethods.stringContainsIgnoreCase("Iron Golem", "golem"));
    assertFalse(UtilityMethods.stringContainsIgnoreCase("Iron Golem", "zombie"));
    assertTrue(UtilityMethods.stringContainsIgnoreCase("Iron Golem", List.of("zombie", "IRON")));
    assertFalse(UtilityMethods.stringContainsIgnoreCase("Iron Golem", List.of()));
  }

  @Test
  void collectionLookupIgnoresCaseAndRestoresTheListCasing() {
    List<String> names = List.of("Iron Golem", "Zombie");
    assertTrue(UtilityMethods.stringCollectionContainsIgnoreCase(names, "iron golem"));
    assertFalse(UtilityMethods.stringCollectionContainsIgnoreCase(names, "creeper"));
    assertEquals("Iron Golem", UtilityMethods.convertStringToListCasing(names, "IRON GOLEM"));
    assertEquals("creeper", UtilityMethods.convertStringToListCasing(names, "creeper"));
  }

  @Test
  void collectionClassCheck() {
    assertTrue(UtilityMethods.collectionOnlyContainsOneClass(List.of("a", "b"), String.class));
    assertFalse(UtilityMethods.collectionOnlyContainsOneClass(List.of("a", 1), String.class));
    assertTrue(UtilityMethods.collectionOnlyContainsOneClass(Set.of(), String.class));
  }

  @Test
  void colorsAreReadFromBracketedRgbText() {
    assertEquals(Color.fromRGB(255, 0, 0), UtilityMethods.getColorFromString("(255,0,0)"));
    assertEquals(Color.fromRGB(10, 20, 30), UtilityMethods.getColorFromString("(10, 20, 30)"));
    // a color with the wrong number of parts silently becomes black
    assertEquals(Color.fromRGB(0, 0, 0), UtilityMethods.getColorFromString("(1,2)"));
  }

  @Test
  void colorsOutsideTheRgbRangeAreRejected() {
    assertThrows(IllegalArgumentException.class,
        () -> UtilityMethods.getColorFromString("(300,0,0)"));
  }
}
