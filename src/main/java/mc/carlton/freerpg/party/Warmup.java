package mc.carlton.freerpg.party;

/**
 * The stand-still timer of a teleport. Only changing block position (walking, jumping, falling,
 * knockback) or taking damage cancels it; turning the head is not an input at all.
 */
public final class Warmup {

  public enum State { RUNNING, DONE, CANCELLED }

  private final long startMillis;
  private final long durationMillis;
  private boolean cancelled;

  public Warmup(long startMillis, int seconds) {
    this.startMillis = startMillis;
    this.durationMillis = seconds * 1000L;
  }

  public State tick(long now, boolean blockPositionChanged, boolean damaged) {
    if (cancelled || blockPositionChanged || damaged) {
      cancelled = true;
      return State.CANCELLED;
    }
    return now - startMillis >= durationMillis ? State.DONE : State.RUNNING;
  }

  /** Whole seconds still to wait, counting 5, 4, 3, 2, 1, then 0. */
  public int secondsLeft(long now) {
    long remaining = durationMillis - (now - startMillis);
    if (remaining <= 0) {
      return 0;
    }
    return (int) ((remaining + 999) / 1000);
  }
}
