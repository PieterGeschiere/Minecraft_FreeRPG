package mc.carlton.freerpg.quests;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

/**
 * The runtime state of everyone's quests: deals the quests of the day, counts progress, and hands
 * out the wheel spin exactly once. It has no Bukkit objects, so it can be tested on its own.
 */
public final class QuestManager {

  /** Something the player should be told about. */
  public record QuestEvent(UUID player, Kind kind, ActiveQuest quest) {

    public enum Kind {
      /** A quest moved forward and is not finished. */
      PROGRESS,
      /** A quest was just finished. */
      QUEST_DONE,
      /** The last quest of the day was just finished, the spin is ready. */
      ALL_DONE
    }
  }

  private final QuestStorage storage;
  private final Supplier<List<QuestDefinition>> pool;
  private final IntSupplier questsPerDay;
  private final LongSupplier clock;
  private final Supplier<ZoneId> zone;
  private final IntSupplier resetHour;
  private final Random random;
  private final Consumer<QuestEvent> listener;
  private final Map<UUID, PlayerQuests> cache = new HashMap<>();

  private final QuestPeriod period;

  /** A manager for daily quests. */
  public QuestManager(QuestStorage storage, Supplier<List<QuestDefinition>> pool,
                      IntSupplier questsPerDay, LongSupplier clock, Supplier<ZoneId> zone,
                      IntSupplier resetHour, Random random, Consumer<QuestEvent> listener) {
    this(QuestPeriod.DAILY, storage, pool, questsPerDay, clock, zone, resetHour, random,
        listener);
  }

  /**
   * A manager for the quests of one period. {@code questsPerDay} is simply how many quests are
   * dealt for each new period, whatever its length.
   */
  public QuestManager(QuestPeriod period, QuestStorage storage,
                      Supplier<List<QuestDefinition>> pool, IntSupplier questsPerDay,
                      LongSupplier clock, Supplier<ZoneId> zone, IntSupplier resetHour,
                      Random random, Consumer<QuestEvent> listener) {
    this.period = period;
    this.storage = storage;
    this.pool = pool;
    this.questsPerDay = questsPerDay;
    this.clock = clock;
    this.zone = zone;
    this.resetHour = resetHour;
    this.random = random;
    this.listener = listener;
  }

  private String todayKey() {
    return QuestDay.keyFor(period, clock.getAsLong(), zone.get(), resetHour.getAsInt());
  }

  public QuestPeriod period() {
    return period;
  }

  /** The player's quests of today. Deals a new set when the saved one is from an earlier day. */
  public synchronized PlayerQuests today(UUID uuid) {
    PlayerQuests data = cache.get(uuid);
    if (data == null) {
      data = storage.load(uuid).orElse(null);
    }
    if (data == null || !data.dayKey().equals(todayKey())) {
      data = deal(uuid);
    }
    cache.put(uuid, data);
    return data;
  }

  private PlayerQuests deal(UUID uuid) {
    PlayerQuests data = new PlayerQuests(uuid, todayKey());
    for (QuestDefinition def : QuestDealer.deal(pool.get(), questsPerDay.getAsInt(), random)) {
      int target = QuestDealer.rollTarget(def, random);
      data.quests().add(new ActiveQuest(def.id(), def.type(), def.filter(), target, 0,
          def.name().replace("{target}", String.valueOf(target))));
    }
    save(data);
    return data;
  }

  private void save(PlayerQuests data) {
    storage.save(data);
  }

  /** Adds progress to every matching quest of today and tells the listener what happened. */
  public void record(UUID uuid, QuestType type, Set<String> tags, int amount) {
    if (amount <= 0) {
      return;
    }
    List<QuestEvent> pending = new ArrayList<>();
    synchronized (this) {
      PlayerQuests data = today(uuid);
      boolean changed = false;
      ActiveQuest lastFinished = null;
      for (ActiveQuest quest : data.quests()) {
        if (!QuestMatcher.matches(quest, type, tags)) {
          continue;
        }
        quest.add(amount);
        changed = true;
        if (quest.done()) {
          lastFinished = quest;
          pending.add(new QuestEvent(uuid, QuestEvent.Kind.QUEST_DONE, quest));
        } else {
          pending.add(new QuestEvent(uuid, QuestEvent.Kind.PROGRESS, quest));
        }
      }
      if (!changed) {
        return;
      }
      if (data.spin() == SpinState.NONE && data.allDone()) {
        data.setSpin(SpinState.READY);
        pending.add(new QuestEvent(uuid, QuestEvent.Kind.ALL_DONE, lastFinished));
      }
      save(data);
    }
    pending.forEach(listener);
  }

  /** Uses up today's spin. True only the first time after all quests are done. */
  public synchronized boolean claimSpin(UUID uuid) {
    PlayerQuests data = today(uuid);
    if (data.spin() != SpinState.READY) {
      return false;
    }
    data.setSpin(SpinState.CLAIMED);
    save(data);
    return true;
  }

  /** Forgets the cached copy (the file stays). */
  public synchronized void forget(UUID uuid) {
    cache.remove(uuid);
  }

  /** Admin: throws today's quests away and deals new ones. */
  public synchronized void resetPlayer(UUID uuid) {
    cache.put(uuid, deal(uuid));
  }
}
