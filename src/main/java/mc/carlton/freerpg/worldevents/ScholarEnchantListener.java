package mc.carlton.freerpg.worldevents;

import mc.carlton.freerpg.FreeRPG;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;

/**
 * During Scholar's Moon an enchant that really happened gives back part of the levels it cost.
 * The refund is paid a tick later, after the server has taken the levels.
 */
public final class ScholarEnchantListener implements Listener {

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  void onEnchant(EnchantItemEvent e) {
    Player player = e.getEnchanter();
    if (!WorldEvents.isActive(WorldEventType.SCHOLARS_MOON, player.getWorld())
        || player.getGameMode() == GameMode.CREATIVE) {
      return;
    }
    // the three buttons cost 1, 2 and 3 levels
    int refund = ScholarsMoonEvent.levelRefund(e.whichButton() + 1,
        ScholarsMoonEvent.costMultiplier());
    if (refund <= 0) {
      return;
    }
    Bukkit.getScheduler().runTask(FreeRPG.getPlugin(FreeRPG.class), () -> {
      if (!player.isOnline()) {
        return;
      }
      player.giveExpLevels(refund);
      WorldEventEffects.scholarRefund(player);
      player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(
          WorldEventMessages.text(player, "worldEventScholarRefund", "levels",
              String.valueOf(refund))));
    });
  }
}
