package mc.carlton.freerpg.worldevents;

import java.util.Random;
import java.util.Set;

/** The pure rules of world events: when to roll, when to start, when to end. No Bukkit objects. */
public final class EventRules {

  private static final long DAY = 24000L;

  /** Beats (10 ticks each) in one Minecraft day. */
  static final int BEATS_PER_DAY = 2400;

  private EventRules() {
  }

  /** Whether the roll of the day is due: inside the window and not rolled yet today. */
  static boolean due(long dayNumber, long lastRolledDay, long timeOfDay, int startTick,
      int endTick) {
    return dayNumber != lastRolledDay && timeOfDay >= startTick && timeOfDay < endTick;
  }

  static boolean rollStart(double chance, Random random) {
    return chance > 0.0 && random.nextDouble() < chance;
  }

  /** The world time at which an event that starts now ends (never right away). */
  static long nextEnd(long fullTime, int endTick) {
    long timeOfDay = fullTime % DAY;
    long delta = (((endTick - timeOfDay) % DAY) + DAY) % DAY;
    if (delta == 0) {
      delta = DAY;
    }
    return fullTime + delta;
  }

  /**
   * Whether a running event is over. A time-of-day event ({@code endFull >= 0}) ends when its end
   * time comes or the world time goes back (for example /time set day). Every event ends at its
   * beat cap, so a frozen day cycle cannot keep one running forever.
   */
  static boolean shouldEnd(long fullTime, long startFull, long endFull, int beatsElapsed,
      int maxBeats) {
    return (endFull >= 0 && (fullTime >= endFull || fullTime < startFull))
        || beatsElapsed >= maxBeats;
  }

  static boolean worldAllowed(String worldName, boolean isOverworldType, Set<String> worlds) {
    return isOverworldType && worlds.contains(worldName);
  }

  /** The time of day an event that rolled successfully starts at. */
  static int pickStartTick(int startTick, int endTick, boolean randomStart, Random random) {
    if (!randomStart || endTick <= startTick) {
      return startTick;
    }
    return startTick + random.nextInt(endTick - startTick);
  }

  /** Whether a pending start can happen now: its time has come and the window is not over. */
  static boolean startReached(long timeOfDay, int pendingTick, int endTick) {
    return timeOfDay >= pendingTick && timeOfDay < endTick;
  }

  /** A beat is 10 ticks (half a second). */
  static int durationBeats(int seconds) {
    return Math.max(0, seconds) * 2;
  }

  /** Whether enough time has passed since the last event ended. True when none has ended yet. */
  static boolean gapElapsed(int nowBeat, int lastEndBeat, int gapDays) {
    if (lastEndBeat < 0) {
      return true;
    }
    return (long) nowBeat - lastEndBeat >= (long) Math.max(0, gapDays) * BEATS_PER_DAY;
  }
}
