package mc.carlton.freerpg.events.misc;

import java.util.ArrayList;
import java.util.Map;
import mc.carlton.freerpg.config.ConfigLoad;
import mc.carlton.freerpg.core.info.player.PlayerStats;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerPrepareCrafting implements Listener {

  @EventHandler
  public void craftEvent(PrepareItemCraftEvent event) {
    ItemStack[] contents = event.getInventory().getContents();
    ItemStack firstInContents = contents[0];
    Player p = (Player) event.getView().getPlayer();
    PlayerStats pStatClass = new PlayerStats(p);
    Map<String, ArrayList<Number>> pStat = pStatClass.getPlayerData();

    if ((firstInContents.getType() == Material.TNT) && (firstInContents.getAmount() == 1)) {
      ConfigLoad configLoad = new ConfigLoad();
      if (!configLoad.getAllowedSkillsMap().get("mining")) {
        return;
      }
      int moreBombsLevel = (int) pStat.get("mining").get(8);
      if (moreBombsLevel > 0) {
        firstInContents.setAmount(Math.min(6, moreBombsLevel + 1));
      }
    }


  }
}
