package mc.carlton.freerpg.worldevents;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

/**
 * Runs the world events: at most one at a time on the whole server. Every Minecraft day each
 * event rolls once for its chance; a successful roll starts the event at its time of day, unless
 * another event is running or the gap after the last event has not passed yet (then the event
 * waits, and the roll is not used up).
 */
public final class WorldEventManager {

  private static final long DAY = 24000L;

  /** A time-of-day event never outlasts a day of server time, even when the world time is frozen. */
  private static final int MAX_BEATS = 2400; // 10 ticks per beat

  /** What happened when an event was asked to start. */
  public enum StartResult { STARTED, ALREADY_ACTIVE, CANNOT_START, DISABLED, NO_WORLD }

  private record Pending(long day, int tick) {
  }

  private static final Map<WorldEventType, WorldEvent> REGISTRY = new EnumMap<>(WorldEventType.class);
  private static final Map<String, Long> LAST_ROLLED = new HashMap<>();
  private static final Map<String, Pending> PENDING = new HashMap<>();
  private static final Random RANDOM = new Random();
  private static ActiveEvent active;
  private static BukkitTask task;
  private static int beat;
  private static int lastEndBeat = -1;

  private WorldEventManager() {
  }

  // ------------------------------------------------------------------ registry and state

  static void register(WorldEvent event) {
    REGISTRY.put(event.type(), event);
  }

  static WorldEvent eventOf(WorldEventType type) {
    return REGISTRY.get(type);
  }

  public static Optional<ActiveEvent> current() {
    return Optional.ofNullable(active);
  }

  /** Whether this kind of event is going on in the world (events of the whole server are everywhere). */
  public static boolean isActive(WorldEventType type, World world) {
    return active != null && active.type() == type && active.appliesTo(world);
  }

  public static int beat() {
    return beat;
  }

  /** The world whose clock an event follows: its own world, or the first world for a server event. */
  private static World clockWorld(ActiveEvent event) {
    return event.world() != null ? event.world() : Bukkit.getWorlds().get(0);
  }

  /** Ticks left of the event (0 when it is over). */
  static long ticksLeft(ActiveEvent event) {
    if (event.endFull() >= 0) {
      return Math.max(0, event.endFull() - clockWorld(event).getFullTime());
    }
    return Math.max(0, (long) (event.endBeat() - beat) * 10);
  }

  /** How much of the event is left, from 1.0 down to 0.0. */
  static double progress(ActiveEvent event) {
    long total = event.endFull() >= 0 ? event.endFull() - event.startFull()
        : (long) (event.endBeat() - event.startBeat()) * 10;
    if (total <= 0) {
      return 0.0;
    }
    return Math.max(0.0, Math.min(1.0, (double) ticksLeft(event) / total));
  }

  // ------------------------------------------------------------------ start and stop

  /**
   * Starts an event. {@code forced} (an admin) skips the enabled switch and the event's own
   * conditions; nothing can start while another event runs. The world is ignored for events of
   * the whole server.
   */
  public static StartResult start(WorldEventType type, World world, boolean forced) {
    WorldEvent event = REGISTRY.get(type);
    if (event == null) {
      return StartResult.DISABLED;
    }
    if (active != null) {
      return StartResult.ALREADY_ACTIVE;
    }
    if (type.scope() == WorldEventType.Scope.WORLD && world == null) {
      return StartResult.NO_WORLD;
    }
    if (!forced && !WorldEventConfig.enabled(type)) {
      return StartResult.DISABLED;
    }
    if (!forced && !event.canStart()) {
      return StartResult.CANNOT_START;
    }
    World scope = type.scope() == WorldEventType.Scope.SERVER ? null : world;
    World clock = scope != null ? scope : Bukkit.getWorlds().get(0);
    long now = clock.getFullTime();
    int seconds = WorldEventConfig.durationSeconds(type);
    ActiveEvent started;
    if (seconds > 0) {
      int length = EventRules.durationBeats(seconds);
      started = new ActiveEvent(type, scope, now, -1, beat, beat + length, length);
    } else {
      started = new ActiveEvent(type, scope, now,
          EventRules.nextEnd(now, WorldEventConfig.endTick(type)), beat, -1, MAX_BEATS);
    }
    active = started;
    try {
      event.onStart(started);
    } catch (RuntimeException e) {
      active = null;
      throw e;
    }
    EventVisuals.refresh();
    return StartResult.STARTED;
  }

  /** Ends the running event. Returns false when there was none. */
  public static boolean stop(boolean byAdmin) {
    ActiveEvent ended = active;
    if (ended == null) {
      return false;
    }
    active = null;
    lastEndBeat = beat;
    try {
      REGISTRY.get(ended.type()).onEnd(ended, byAdmin);
    } finally {
      for (Player player : Bukkit.getOnlinePlayers()) {
        if (ended.appliesTo(player.getWorld())) {
          player.sendMessage(WorldEventMessages.text(player, ended.type().endedKey()));
        }
      }
      EventVisuals.refresh();
    }
    return true;
  }

  // ------------------------------------------------------------------ the tick

  private static void tickEvents() {
    if (active != null) {
      ActiveEvent running = active;
      boolean worldGone = running.world() != null && Bukkit.getWorld(running.world().getUID()) == null;
      if (worldGone || EventRules.shouldEnd(clockWorld(running).getFullTime(), running.startFull(),
          running.endFull(), beat - running.startBeat(), running.maxBeats())) {
        stop(false);
      } else {
        REGISTRY.get(running.type()).onTick(running);
      }
      return;
    }
    if (!EventRules.gapElapsed(beat, lastEndBeat, WorldEventConfig.minGapDays())) {
      return;
    }
    for (WorldEventType type : WorldEventType.values()) {
      WorldEvent event = REGISTRY.get(type);
      if (event == null) {
        continue;
      }
      for (World world : rollWorlds(type)) {
        if (active != null) {
          return;
        }
        rollFor(type, event, world);
      }
    }
  }

  /** The worlds an event rolls in; an event of the whole server rolls once, in the first one. */
  private static List<World> rollWorlds(WorldEventType type) {
    List<World> worlds = new ArrayList<>();
    for (World world : Bukkit.getWorlds()) {
      boolean overworldType = world.getEnvironment() == World.Environment.NORMAL;
      if (EventRules.worldAllowed(world.getName(), overworldType, WorldEventConfig.worlds(type))) {
        worlds.add(world);
        if (type.scope() == WorldEventType.Scope.SERVER) {
          break;
        }
      }
    }
    return worlds;
  }

  private static void rollFor(WorldEventType type, WorldEvent event, World world) {
    String key = type.name() + ":" + world.getUID();
    long day = world.getFullTime() / DAY;
    long timeOfDay = world.getTime();
    int endTick = WorldEventConfig.endTick(type);
    Pending pending = PENDING.get(key);
    if (pending != null && pending.day() != day) {
      PENDING.remove(key);
      pending = null;
    }
    if (pending == null) {
      if (!WorldEventConfig.enabled(type)) {
        return;
      }
      int startTick = WorldEventConfig.startTick(type);
      if (!EventRules.due(day, LAST_ROLLED.getOrDefault(key, -1L), timeOfDay, startTick, endTick)) {
        return;
      }
      LAST_ROLLED.put(key, day);
      if (!EventRules.rollStart(WorldEventConfig.chance(type), RANDOM)) {
        return;
      }
      pending = new Pending(day, EventRules.pickStartTick(startTick, endTick,
          WorldEventConfig.randomStart(type), RANDOM));
      PENDING.put(key, pending);
    }
    if (timeOfDay >= endTick) {
      PENDING.remove(key); // the window closed before the event could start
    } else if (EventRules.startReached(timeOfDay, pending.tick(), endTick) && event.canStart()) {
      PENDING.remove(key);
      start(type, world, false);
    }
  }

  // ------------------------------------------------------------------ life cycle

  static void begin(Plugin plugin) {
    shutdown();
    task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
      beat++;
      if (beat % 2 == 0) {
        tickEvents();
      }
      EventVisuals.refresh();
    }, 10L, 10L);
  }

  /** Stops the task, ends the running event without paying anything, and removes all effects. */
  static void shutdown() {
    if (task != null) {
      task.cancel();
      task = null;
    }
    ActiveEvent ended = active;
    active = null;
    if (ended != null) {
      try {
        REGISTRY.get(ended.type()).onEnd(ended, true);
      } catch (RuntimeException e) {
        Bukkit.getLogger().warning("World event " + ended.type() + " failed to end cleanly: " + e);
      }
    }
    LAST_ROLLED.clear();
    PENDING.clear();
    lastEndBeat = -1;
    EventVisuals.clearAll();
  }
}
