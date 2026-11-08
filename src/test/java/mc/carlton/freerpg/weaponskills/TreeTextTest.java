package mc.carlton.freerpg.weaponskills;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

/** Number formatting and text wrapping used by the weapon skill menus. */
class TreeTextTest {

  @Test
  void wholeNumbersLoseTheirDecimalPoint() {
    assertEquals("3", TreeText.number(3.0));
    assertEquals("0", TreeText.number(0.0));
    assertEquals("-2", TreeText.number(-2.0));
  }

  @Test
  void fractionsKeepTheirDecimals() {
    assertEquals("0.5", TreeText.number(0.5));
    assertEquals("2.25", TreeText.number(2.25));
  }

  @Test
  void percentShowsOneDecimal() {
    assertEquals("25.0%", TreeText.percent(0.25));
    assertEquals("0.3%", TreeText.percent(0.003));
    assertEquals("150.0%", TreeText.percent(1.5));
    assertEquals("0.0%", TreeText.percent(0));
  }

  @Test
  void wrapBreaksAtSpacesAndKeepsAllWords() {
    List<String> lines = TreeText.wrap("one two three four five six", 10);
    assertEquals(List.of("one two", "three four", "five six"), lines);
    assertEquals("one two three four five six", String.join(" ", lines));
  }

  @Test
  void wrapNeverSplitsALongWordAndSkipsNothing() {
    List<String> lines = TreeText.wrap("supercalifragilistic is long", 8);
    assertEquals("supercalifragilistic", lines.get(0));
    assertEquals("supercalifragilistic is long", String.join(" ", lines));
  }

  @Test
  void wrapOfShortTextIsOneLine() {
    assertEquals(List.of("short text"), TreeText.wrap("short text", 40));
  }

  @Test
  void everyLineStaysWithinTheWidthWhenWordsAreShort() {
    for (String line : TreeText.wrap("a bb ccc dddd eeeee ffffff ggggggg hhhhhhhh", 12)) {
      assertTrue(line.length() <= 12, line);
    }
  }
}
