package mc.carlton.freerpg.quests;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.IsoFields;

/** Works out which "quest day" (and week and season) a moment belongs to. Pure logic, no Bukkit. */
public final class QuestDay {

  private QuestDay() {
  }

  /** The calendar date of the quest day: a day starts at {@code resetHour}:00 in the zone. */
  private static LocalDate questDate(long epochMillis, ZoneId zone, int resetHour) {
    int hour = Math.max(0, Math.min(23, resetHour));
    ZonedDateTime time = Instant.ofEpochMilli(epochMillis).atZone(zone);
    return time.toLocalDateTime().minusHours(hour).toLocalDate();
  }

  /**
   * ISO date (yyyy-MM-dd) of the quest day containing the instant. A quest day starts at
   * {@code resetHour}:00 in the zone, so with reset hour 3 the time 02:00 still belongs to the
   * previous date.
   */
  public static String keyFor(long epochMillis, ZoneId zone, int resetHour) {
    return questDate(epochMillis, zone, resetHour).toString();
  }

  /**
   * The key of the period (day, ISO week such as {@code 2026-W47}, or calendar quarter such as
   * {@code 2026-Q4}) that the instant belongs to. Weeks and seasons change at the same hour a quest
   * day does, so a week starts on Monday at the reset hour.
   */
  public static String keyFor(QuestPeriod period, long epochMillis, ZoneId zone, int resetHour) {
    LocalDate date = questDate(epochMillis, zone, resetHour);
    switch (period) {
      case WEEKLY:
        return String.format("%d-W%02d", date.get(IsoFields.WEEK_BASED_YEAR),
            date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR));
      case SEASONAL:
        return date.getYear() + "-Q" + date.get(IsoFields.QUARTER_OF_YEAR);
      default:
        return date.toString();
    }
  }

  /** True when the saved key is the quest day of {@code nowMillis}. Null or garbage is false. */
  public static boolean isCurrent(String savedKey, long nowMillis, ZoneId zone, int resetHour) {
    return savedKey != null && savedKey.equals(keyFor(nowMillis, zone, resetHour));
  }
}
