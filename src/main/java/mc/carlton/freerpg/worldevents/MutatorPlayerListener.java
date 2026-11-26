package mc.carlton.freerpg.worldevents;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * The player side of Mutator Night: Hungry Night's food boost, and the cleanup of Low Gravity
 * whenever a player joins, leaves or changes world (the next beat gives it back if it still applies).
 */
public final class MutatorPlayerListener implements Listener {

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  void onFood(FoodLevelChangeEvent e) {
    if (!(e.getEntity() instanceof Player player)
        || !MutatorNightEvent.isActive(Mutator.HUNGRY_NIGHT, player.getWorld())) {
      return;
    }
    e.setFoodLevel(HungerRules.boostedFood(player.getFoodLevel(), e.getFoodLevel(),
        Mutator.HUNGRY_NIGHT.number("foodMultiplier", 1.5)));
  }

  /** Also removes what a crash left behind: the modifiers are saved with the player. */
  @EventHandler
  void onJoin(PlayerJoinEvent e) {
    MutatorPlayerEffects.removeLowGravity(e.getPlayer());
  }

  @EventHandler
  void onQuit(PlayerQuitEvent e) {
    MutatorPlayerEffects.removeLowGravity(e.getPlayer());
  }

  @EventHandler
  void onWorldChange(PlayerChangedWorldEvent e) {
    MutatorPlayerEffects.removeLowGravity(e.getPlayer());
  }
}
