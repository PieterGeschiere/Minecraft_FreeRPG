package mc.carlton.freerpg.events.misc;

import mc.carlton.freerpg.core.info.player.AbilityTracker;
import mc.carlton.freerpg.skills.perksAndAbilities.Fishing;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;

public class PlayerFish implements Listener {

  @EventHandler(priority = EventPriority.HIGH)
  void onPlayerFish(PlayerFishEvent e) {
    Player p = e.getPlayer();
    if (e.isCancelled()) {
      return;
    }
    if (!p.hasPermission("freeRPG.fish")) {
      return;
    }
    if (p.getGameMode() == GameMode.CREATIVE) {
      return;
    }

    World world = p.getWorld();
    Entity caughtThing = e.getCaught();
    AbilityTracker abilities = new AbilityTracker(p);
    Integer[] pAbilities = abilities.getPlayerAbilities();

    if (e.getState() == PlayerFishEvent.State.FISHING) {
      if (pAbilities[4] > -1) {
        Fishing fishingClass = new Fishing(p);
        fishingClass.enableAbility();
      }
    } else if (e.getState() == PlayerFishEvent.State.REEL_IN) {
      Fishing fishingClass = new Fishing(p);
      if (pAbilities[4] == -2) {
        fishingClass.superBait(e.getHook(), caughtThing, world);
      }

    } else if (e.getState() == PlayerFishEvent.State.IN_GROUND) {
      Fishing fishingClass = new Fishing(p);
      fishingClass.grapplingHook(e.getHook(), world);
    } else if (e.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
      Fishing fishingClass = new Fishing(p);
      if (pAbilities[4] == -2) {
        fishingClass.superBait(e.getHook(), caughtThing, world);
      }
    } else if (e.getState() == PlayerFishEvent.State.CAUGHT_ENTITY) {
      Fishing fishingClass = new Fishing(p);
      fishingClass.rob(e.getHook(), e.getCaught(), world);
    }

  }
}
