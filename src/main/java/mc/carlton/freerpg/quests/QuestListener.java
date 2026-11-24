package mc.carlton.freerpg.quests;

import java.util.Set;
import org.bukkit.entity.Enemy;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

/** Counts the things quests ask for that are not already counted by the skill code. */
public final class QuestListener implements Listener {

  @EventHandler
  public void onJoin(PlayerJoinEvent e) {
    try {
      Quests.manager().today(e.getPlayer().getUniqueId());
    } catch (RuntimeException ex) {
      Quests.plugin().getLogger().warning("Could not load quests of " + e.getPlayer().getName()
          + ": " + ex.getMessage());
    }
  }

  @EventHandler
  public void onQuit(PlayerQuitEvent e) {
    Quests.manager().forget(e.getPlayer().getUniqueId());
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onKill(EntityDeathEvent e) {
    Player killer = e.getEntity().getKiller();
    if (killer == null || e.getEntity() instanceof Player) {
      return;
    }
    Quests.record(killer, QuestType.KILL_MOBS,
        QuestTags.forMob(e.getEntityType().name(), e.getEntity() instanceof Enemy), 1);
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onFish(PlayerFishEvent e) {
    if (e.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
      Quests.record(e.getPlayer(), QuestType.CATCH_FISH, Set.of("ANY"), 1);
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onCraft(CraftItemEvent e) {
    if (!(e.getWhoClicked() instanceof Player p) || e.getRecipe() == null) {
      return;
    }
    int perCraft = Math.max(1, e.getRecipe().getResult().getAmount());
    int crafts = 1;
    if (e.isShiftClick()) {
      int smallest = Integer.MAX_VALUE;
      for (ItemStack stack : e.getInventory().getMatrix()) {
        if (stack != null && !stack.getType().isAir()) {
          smallest = Math.min(smallest, stack.getAmount());
        }
      }
      crafts = smallest == Integer.MAX_VALUE ? 1 : smallest;
    }
    Quests.record(p, QuestType.CRAFT_ITEMS, Set.of("ANY"), perCraft * crafts);
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onBreed(EntityBreedEvent e) {
    if (e.getBreeder() instanceof Player p) {
      Quests.record(p, QuestType.BREED_ANIMALS, Set.of("ANY"), 1);
    }
  }
}
