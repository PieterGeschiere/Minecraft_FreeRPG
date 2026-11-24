package mc.carlton.freerpg.quests;

import java.util.Locale;

/** How long a set of quests lasts: a day, a week or a season (a calendar quarter). */
public enum QuestPeriod {
  DAILY,
  WEEKLY,
  SEASONAL;

  /** Lower-case name, used in quests.yml, commands and file names. */
  public String id() {
    return name().toLowerCase(Locale.ROOT);
  }

  /** The period with this name (any case), or null when there is none. */
  public static QuestPeriod parse(String text) {
    if (text == null) {
      return null;
    }
    for (QuestPeriod period : values()) {
      if (period.id().equals(text.trim().toLowerCase(Locale.ROOT))) {
        return period;
      }
    }
    return null;
  }
}
