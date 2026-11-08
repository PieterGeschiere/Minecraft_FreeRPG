package mc.carlton.freerpg.events.misc;

import mc.carlton.freerpg.events.newEvents.FrpgAbilityItemMovedEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerMoveAbilityItem implements Listener {

  @EventHandler
  public void moveAbilityItem(FrpgAbilityItemMovedEvent e) {
    e.setCancelled(true); //We simply want to cancel the ability item being moved in all cases
  }
}
