package mc.carlton.freerpg.quests;

import java.util.Set;

/** Decides whether something that happened counts for a quest. Pure logic, no Bukkit. */
public final class QuestMatcher {

  private QuestMatcher() {
  }

  public static boolean matches(ActiveQuest quest, QuestType type, Set<String> tags) {
    return quest.type() == type && !quest.done() && tags.contains(quest.filter());
  }
}
