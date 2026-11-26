package mc.carlton.freerpg.worldevents;

import java.util.Locale;
import java.util.Map;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;

/** Scores what players catch while the Fishing Festival runs. */
public final class FestivalListener implements Listener {

  private static final Map<Material, Integer> DEFAULT_POINTS = Map.of(
      Material.COD, 1, Material.SALMON, 2, Material.TROPICAL_FISH, 3, Material.PUFFERFISH, 3,
      Material.ENCHANTED_BOOK, 6, Material.NAME_TAG, 6, Material.SADDLE, 6,
      Material.NAUTILUS_SHELL, 6);

  /**
   * The points of a caught item. A {@code points} table in worldevents.yml replaces the default
   * one; items it does not list give nothing.
   */
  static int points(Material material) {
    ConfigurationSection table = WorldEventConfig.raw()
        .getConfigurationSection("events.fishingFestival.points");
    if (table == null) {
      return DEFAULT_POINTS.getOrDefault(material, 0);
    }
    return Math.max(0, table.getInt(material.name().toUpperCase(Locale.ROOT), 0));
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  void onFish(PlayerFishEvent e) {
    Player p = e.getPlayer();
    if (e.getState() != PlayerFishEvent.State.CAUGHT_FISH
        || (p.getGameMode() != GameMode.SURVIVAL && p.getGameMode() != GameMode.ADVENTURE)
        || !(e.getCaught() instanceof Item caught)) {
      return;
    }
    ActiveEvent event = WorldEventManager.current()
        .filter(running -> running.type() == WorldEventType.FISHING_FESTIVAL).orElse(null);
    if (event == null) {
      return;
    }
    int points = points(caught.getItemStack().getType());
    if (points <= 0) {
      return;
    }
    FestivalScores scores = event.state();
    scores.add(p.getUniqueId(), points);
    WorldEventEffects.festivalCatch(p, points);
    p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(
        WorldEventMessages.text(p, "worldEventFestivalCatch", "points", String.valueOf(points),
            "score", String.valueOf(scores.score(p.getUniqueId())))));
  }
}
