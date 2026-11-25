package mc.carlton.freerpg.worldevents;

import org.bukkit.World;

/** The one world event that is running right now. */
public final class ActiveEvent {

  private final WorldEventType type;
  private final World world;
  private final long startFull;
  private final long endFull;
  private final int startBeat;
  private final int endBeat;
  private final int maxBeats;
  private Object state;

  ActiveEvent(WorldEventType type, World world, long startFull, long endFull, int startBeat,
      int endBeat, int maxBeats) {
    this.type = type;
    this.world = world;
    this.startFull = startFull;
    this.endFull = endFull;
    this.startBeat = startBeat;
    this.endBeat = endBeat;
    this.maxBeats = maxBeats;
  }

  public WorldEventType type() {
    return type;
  }

  /** The world of a world-scoped event, or null for an event of the whole server. */
  public World world() {
    return world;
  }

  public long startFull() {
    return startFull;
  }

  /** The world time at which a time-of-day event ends, or -1 for an event with a duration. */
  public long endFull() {
    return endFull;
  }

  public int startBeat() {
    return startBeat;
  }

  /** The beat at which an event with a duration ends, or -1. */
  public int endBeat() {
    return endBeat;
  }

  public int maxBeats() {
    return maxBeats;
  }

  /** Whether the event is going on in this world. */
  public boolean appliesTo(World other) {
    return world == null || world.equals(other);
  }

  /** Whatever the event keeps for itself while it runs (for example the scores). */
  @SuppressWarnings("unchecked")
  public <T> T state() {
    return (T) state;
  }

  public void setState(Object state) {
    this.state = state;
  }
}
