package mc.carlton.freerpg.weaponskills;

import java.util.ArrayList;
import java.util.List;

/** Small text helpers for the weapon skill menus. */
final class TreeText {

  private TreeText() {
  }

  /** 3.0 becomes "3", 0.5 stays "0.5". */
  static String number(double value) {
    return value == Math.rint(value) ? String.valueOf((int) value) : String.valueOf(value);
  }

  /** 0.25 becomes "25.0%". */
  static String percent(double fraction) {
    return Math.round(fraction * 1000) / 10.0 + "%";
  }

  /** Splits text into lines of at most the given length, breaking at spaces. */
  static List<String> wrap(String text, int width) {
    List<String> lines = new ArrayList<>();
    StringBuilder line = new StringBuilder();
    for (String word : text.split(" ")) {
      if (line.length() > 0 && line.length() + word.length() + 1 > width) {
        lines.add(line.toString());
        line = new StringBuilder();
      }
      if (line.length() > 0) {
        line.append(' ');
      }
      line.append(word);
    }
    if (line.length() > 0) {
      lines.add(line.toString());
    }
    return lines;
  }
}
