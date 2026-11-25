package mc.carlton.freerpg.worldevents;

import java.util.ArrayList;
import java.util.List;

/**
 * What the Fishing Festival pays out. Everyone who scored gets souls and exp; the top places get
 * extra and possibly a free wheel spin. Pure: it only turns a ranking into a list of payouts.
 */
public final class FestivalRewards {

  /** What one player receives. */
  public record Payout(java.util.UUID player, int rank, int souls, int minecraftExp,
      int fishingExp, int bonusSpins) {
  }

  /** The reward settings; the {@code top…} lists hold the extra for place 1, 2, 3, ... */
  public record Settings(int participationSouls, int[] topSouls, int participationMcExp,
      int[] topMcExp, int fishingExpPerPoint, int[] topBonusSpins) {
  }

  private static final WorldEventType TYPE = WorldEventType.FISHING_FESTIVAL;

  private FestivalRewards() {
  }

  static Settings fromConfig() {
    return new Settings(
        Math.max(0, WorldEventConfig.integer(TYPE, "rewards.participationSouls", 3)),
        list("rewards.topSouls", new int[] {10, 6, 3}),
        Math.max(0, WorldEventConfig.integer(TYPE, "rewards.participationMcExp", 50)),
        list("rewards.topMcExp", new int[] {300, 200, 100}),
        Math.max(0, WorldEventConfig.integer(TYPE, "rewards.fishingExpPerPoint", 20)),
        list("rewards.topBonusSpins", new int[] {1, 1, 1}));
  }

  private static int[] list(String key, int[] defaults) {
    if (!WorldEventConfig.isSet(TYPE, key)) {
      return defaults;
    }
    List<Integer> values = WorldEventConfig.raw()
        .getIntegerList("events." + TYPE.configKey() + "." + key);
    int[] array = new int[values.size()];
    for (int i = 0; i < array.length; i++) {
      array[i] = Math.max(0, values.get(i));
    }
    return array;
  }

  private static int at(int[] values, int index) {
    return index < values.length ? values[index] : 0;
  }

  /** One payout per player who scored, in ranking order. Players without points are skipped. */
  static List<Payout> plan(List<FestivalScores.Entry> ranking, Settings settings) {
    List<Payout> plan = new ArrayList<>();
    int index = 0;
    for (FestivalScores.Entry entry : ranking) {
      if (entry.score() <= 0) {
        continue;
      }
      plan.add(new Payout(entry.player(), index + 1,
          settings.participationSouls() + at(settings.topSouls(), index),
          settings.participationMcExp() + at(settings.topMcExp(), index),
          entry.score() * settings.fishingExpPerPoint(),
          at(settings.topBonusSpins(), index)));
      index++;
    }
    return plan;
  }
}
