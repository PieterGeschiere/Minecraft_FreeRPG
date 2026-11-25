package mc.carlton.freerpg.worldevents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

/**
 * What players see and hear while a world event runs: a big title, a sound and a boss bar with the
 * time left. Every beat {@link #refresh()} brings every player to the right state, so joining,
 * changing world and dying need no special handling.
 */
public final class EventVisuals {

  private static final Map<UUID, WorldEventType> SHOWN = new HashMap<>();
  private static final Map<UUID, BossBar> BARS = new HashMap<>();

  private EventVisuals() {
  }

  /** "8:20" for 10000 ticks. */
  static String formatTime(long ticksLeft) {
    long seconds = Math.max(0, ticksLeft) / 20;
    return (seconds / 60) + ":" + String.format("%02d", seconds % 60);
  }

  public static void refresh() {
    Optional<ActiveEvent> current = WorldEventManager.current();
    Set<UUID> keep = new HashSet<>();
    if (current.isPresent()) {
      ActiveEvent event = current.get();
      WorldEvent logic = WorldEventManager.eventOf(event.type());
      for (Player player : Bukkit.getOnlinePlayers()) {
        if (!event.appliesTo(player.getWorld())) {
          continue;
        }
        keep.add(player.getUniqueId());
        if (SHOWN.put(player.getUniqueId(), event.type()) == null) {
          announce(player, event, logic);
        }
        updateBar(player, event, logic);
        logic.decorate(player, event);
      }
    }
    for (UUID id : new ArrayList<>(SHOWN.keySet())) {
      if (!keep.contains(id)) {
        clear(id, SHOWN.remove(id));
      }
    }
  }

  private static void announce(Player player, ActiveEvent event, WorldEvent logic) {
    WorldEventType type = event.type();
    if (WorldEventConfig.effect(type, "title", true)) {
      player.sendTitle(WorldEventMessages.text(player, type.titleKey()),
          WorldEventMessages.text(player, type.subtitleKey()), 10, 80, 30);
    }
    if (WorldEventConfig.effect(type, "sound", true)) {
      player.playSound(player.getLocation(), logic.announceSound(), 0.6f, 0.6f);
    }
  }

  private static void updateBar(Player player, ActiveEvent event, WorldEvent logic) {
    WorldEventType type = event.type();
    if (!WorldEventConfig.effect(type, "bossBar", true)) {
      return;
    }
    BossBar bar = BARS.computeIfAbsent(player.getUniqueId(),
        id -> Bukkit.createBossBar("", type.barColor(), BarStyle.SOLID));
    String[] extra = logic.barPlaceholders(event);
    String[] pairs = new String[extra.length + 2];
    pairs[0] = "time";
    pairs[1] = formatTime(WorldEventManager.ticksLeft(event));
    System.arraycopy(extra, 0, pairs, 2, extra.length);
    bar.setTitle(WorldEventMessages.text(player, type.barKey(), pairs));
    bar.setColor(type.barColor());
    bar.setProgress(WorldEventManager.progress(event));
    if (!bar.getPlayers().contains(player)) {
      bar.addPlayer(player);
    }
  }

  private static void clear(UUID id, WorldEventType type) {
    BossBar bar = BARS.remove(id);
    if (bar != null) {
      bar.removeAll();
    }
    WorldEvent logic = type == null ? null : WorldEventManager.eventOf(type);
    if (logic != null) {
      logic.clearPlayer(id);
    }
  }

  /** Removes every effect from every player (shutdown and reload). */
  public static void clearAll() {
    for (UUID id : new ArrayList<>(SHOWN.keySet())) {
      clear(id, SHOWN.remove(id));
    }
    for (UUID id : new ArrayList<>(BARS.keySet())) {
      clear(id, null);
    }
  }
}
