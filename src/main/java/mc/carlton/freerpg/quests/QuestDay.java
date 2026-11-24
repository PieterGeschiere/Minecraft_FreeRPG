package mc.carlton.freerpg.quests;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/** Works out which "quest day" a moment belongs to. Pure logic, no Bukkit. */
public final class QuestDay {

  private QuestDay() {
  }

  /**
   * ISO date (yyyy-MM-dd) of the quest day containing the instant. A quest day starts at
   * {@code resetHour}:00 in the zone, so with reset hour 3 the time 02:00 still belongs to the
   * previous date.
   */
  public static String keyFor(long epochMillis, ZoneId zone, int resetHour) {
    int hour = Math.max(0, Math.min(23, resetHour));
    ZonedDateTime time = Instant.ofEpochMilli(epochMillis).atZone(zone);
    return time.toLocalDateTime().minusHours(hour).toLocalDate().toString();
  }

  /** True when the saved key is the quest day of {@code nowMillis}. Null or garbage is false. */
  public static boolean isCurrent(String savedKey, long nowMillis, ZoneId zone, int resetHour) {
    return savedKey != null && savedKey.equals(keyFor(nowMillis, zone, resetHour));
  }
}
