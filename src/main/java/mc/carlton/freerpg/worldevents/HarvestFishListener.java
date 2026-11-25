package mc.carlton.freerpg.worldevents;

import java.util.Random;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;

/** During Bountiful Harvest a caught fish sometimes comes twice. */
public final class HarvestFishListener implements Listener {

  private static final Random RANDOM = new Random();

  /** Only real fish come twice; treasure (enchanted books, saddles, name tags) never does. */
  static boolean isFish(Material material) {
    return material == Material.COD || material == Material.SALMON
        || material == Material.TROPICAL_FISH || material == Material.PUFFERFISH;
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  void onFish(PlayerFishEvent e) {
    Player p = e.getPlayer();
    if (e.getState() != PlayerFishEvent.State.CAUGHT_FISH
        || !WorldEvents.isActive(WorldEventType.BOUNTIFUL_HARVEST, p.getWorld())
        || p.getGameMode() == GameMode.CREATIVE || p.getGameMode() == GameMode.SPECTATOR
        || !(e.getCaught() instanceof Item caught) || !isFish(caught.getItemStack().getType())) {
      return;
    }
    if (!EventRules.rollStart(EventDefaults.dropChance(WorldEventType.BOUNTIFUL_HARVEST), RANDOM)) {
      return;
    }
    for (ItemStack left : p.getInventory().addItem(caught.getItemStack().clone()).values()) {
      p.getWorld().dropItemNaturally(p.getLocation(), left);
    }
  }
}
