package mc.carlton.freerpg.quests;

/** A quest a player was dealt today. It keeps its own type, filter and target. */
public final class ActiveQuest {

  private final String id;
  private final QuestType type;
  private final String filter;
  private final int target;
  private final String name;
  private int progress;

  public ActiveQuest(String id, QuestType type, String filter, int target, int progress,
                     String name) {
    this.id = id;
    this.type = type;
    this.filter = filter;
    this.target = Math.max(1, target);
    this.progress = Math.max(0, Math.min(this.target, progress));
    this.name = name;
  }

  public String id() {
    return id;
  }

  public QuestType type() {
    return type;
  }

  public String filter() {
    return filter;
  }

  public int target() {
    return target;
  }

  public int progress() {
    return progress;
  }

  /** Display text with the target already filled in. */
  public String name() {
    return name;
  }

  public boolean done() {
    return progress >= target;
  }

  /** Adds progress, never above the target. Negative amounts are ignored. */
  public void add(int amount) {
    if (amount > 0) {
      progress = (int) Math.min((long) target, (long) progress + amount);
    }
  }
}
