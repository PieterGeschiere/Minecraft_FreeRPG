package mc.carlton.freerpg.quests;

/**
 * One entry of the quest pool in quests.yml.
 *
 * @param id        the key in quests.yml
 * @param type      what the quest counts
 * @param filter    upper-case tag the action must carry: ANY, ORES, LOGS, RIPE_CROPS, HOSTILE, an
 *                  entity type name or a skill name
 * @param minTarget smallest amount a player can be asked for
 * @param maxTarget largest amount a player can be asked for
 * @param weight    chance weight when dealing (0 = never dealt)
 * @param name      display text, {target} is replaced by the rolled amount
 */
public record QuestDefinition(String id, QuestType type, String filter, int minTarget,
                              int maxTarget, int weight, String name) {

  /** The same quest asking for {@code factor} times as much (a factor below 1 counts as 1). */
  public QuestDefinition scaled(int factor) {
    int f = Math.max(1, factor);
    return new QuestDefinition(id, type, filter, (int) Math.min(Integer.MAX_VALUE,
        (long) minTarget * f), (int) Math.min(Integer.MAX_VALUE, (long) maxTarget * f), weight,
        name);
  }
}
