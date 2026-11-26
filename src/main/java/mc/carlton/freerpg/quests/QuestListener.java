package mc.carlton.freerpg.quests;

import java.util.Set;
import org.bukkit.entity.Enemy;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.inventory.MerchantInventory;
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
      for (QuestManager m : Quests.activeManagers()) {
        m.today(e.getPlayer().getUniqueId());
      }
    } catch (RuntimeException ex) {
      Quests.plugin().getLogger().warning("Could not load quests of " + e.getPlayer().getName()
          + ": " + ex.getMessage());
    }
  }

  @EventHandler
  public void onQuit(PlayerQuitEvent e) {
    for (QuestPeriod period : QuestPeriod.values()) {
      Quests.manager(period).forget(e.getPlayer().getUniqueId());
    }
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

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onSmelt(FurnaceExtractEvent e) {
    if (e.getItemAmount() > 0) {
      Quests.record(e.getPlayer(), QuestType.SMELT_ITEMS,
          Set.of("ANY", e.getItemType().name()), e.getItemAmount());
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onEnchant(EnchantItemEvent e) {
    Quests.record(e.getEnchanter(), QuestType.ENCHANT_ITEMS, Set.of("ANY"), 1);
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onTrade(InventoryClickEvent e) {
    if (e.getInventory() instanceof MerchantInventory && e.getSlot() == 2
        && e.getClickedInventory() == e.getInventory()
        && e.getCurrentItem() != null && !e.getCurrentItem().getType().isAir()
        && e.getWhoClicked() instanceof Player p) {
      Quests.record(p, QuestType.TRADE_VILLAGERS, Set.of("ANY"), 1);
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onTame(EntityTameEvent e) {
    if (e.getOwner() instanceof Player p) {
      Quests.record(p, QuestType.TAME_ANIMALS, Set.of("ANY"), 1);
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onShear(PlayerShearEntityEvent e) {
    Quests.record(e.getPlayer(), QuestType.SHEAR_SHEEP, Set.of("ANY"), 1);
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onPlace(BlockPlaceEvent e) {
    Quests.record(e.getPlayer(), QuestType.PLACE_BLOCKS,
        Set.of("ANY", e.getBlockPlaced().getType().name()), 1);
  }
}
