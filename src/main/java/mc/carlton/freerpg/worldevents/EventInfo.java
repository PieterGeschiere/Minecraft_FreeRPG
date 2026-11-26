package mc.carlton.freerpg.worldevents;

import java.util.Locale;

/** The numbers in the chat text that says what an event does, read from the live config. */
public final class EventInfo {

  private EventInfo() {
  }

  /** A number for chat: whole numbers without decimals, others with one. */
  static String format(double value) {
    if (Math.abs(value - Math.rint(value)) < 0.005) {
      return String.valueOf((long) Math.rint(value));
    }
    return String.format(Locale.ROOT, "%.1f", value);
  }

  static String percent(double fraction) {
    return format(fraction * 100.0);
  }

  /** {name, value, ...} pairs for the info text of an event. Unused pairs do no harm. */
  static String[] placeholders(WorldEventType type) {
    return new String[] {
        "xp", format(EventDefaults.xpMultiplier(type)),
        "drop", percent(WorldEventConfig.number(type, "dropChance", 0.25)),
        "reduction", percent(WorldEventConfig.number(type, "spawnReduction", 0.6)),
        "flame", percent(WorldEventConfig.number(type, "flamePickBonus", 0.25)),
        "cost", percent(WorldEventConfig.number(type, "levelCostMultiplier", 0.5)),
        "speed", String.valueOf(WorldEventConfig.integer(type, "speedAmplifier", 0) + 1),
        "minScale", format(WorldEventConfig.number(type, "minScale", 1.4)),
        "maxScale", format(WorldEventConfig.number(type, "maxScale", 2.2)),
        "count", String.valueOf(Math.max(1, WorldEventConfig.integer(type, "count", 2)))
    };
  }
}
