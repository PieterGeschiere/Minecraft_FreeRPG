package mc.carlton.freerpg.quests;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Everything saved about one player's quests: the day they belong to, the quests, the spin. */
public final class PlayerQuests {

  private final UUID uuid;
  private final String dayKey;
  private final List<ActiveQuest> quests = new ArrayList<>();
  private SpinState spin = SpinState.NONE;

  public PlayerQuests(UUID uuid, String dayKey) {
    this.uuid = uuid;
    this.dayKey = dayKey;
  }

  public UUID uuid() {
    return uuid;
  }

  public String dayKey() {
    return dayKey;
  }

  public List<ActiveQuest> quests() {
    return quests;
  }

  public SpinState spin() {
    return spin;
  }

  public void setSpin(SpinState spin) {
    this.spin = spin;
  }

  /** True when there is at least one quest and every quest is done. */
  public boolean allDone() {
    if (quests.isEmpty()) {
      return false;
    }
    for (ActiveQuest quest : quests) {
      if (!quest.done()) {
        return false;
      }
    }
    return true;
  }
}
