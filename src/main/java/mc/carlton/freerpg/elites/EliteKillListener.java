package mc.carlton.freerpg.elites;

import java.util.Map;
import java.util.Random;
import mc.carlton.freerpg.config.ConfigLoad;
import mc.carlton.freerpg.core.info.player.ChangeStats;
import mc.carlton.freerpg.party.XpShareGuard;
import mc.carlton.freerpg.utils.game.ExpFarmTracker;
import mc.carlton.freerpg.utils.globalVariables.ItemGroups;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

/** Gives the extra drops and exp when a player kills an elite. */
public class EliteKillListener implements Listener {

  private static final Random RANDOM = new Random();

  private static final RewardCooldown COOLDOWN = new RewardCooldown();

  /** Minions of Splitting and Summoner are no loot or exp source. */
  @EventHandler(priority = EventPriority.HIGH)
  void onMinionDeath(EntityDeathEvent e) {
    if (EliteRewards.suppressLoot(EliteData.isMinion(e.getEntity()))) {
      e.getDrops().clear();
      e.setDroppedExp(0);
    }
  }

  @EventHandler(priority = EventPriority.HIGH)
  void onEliteDeath(EntityDeathEvent e) {
    LivingEntity mob = e.getEntity();
    Player killer = mob.getKiller();
    if (killer == null || killer.getGameMode() == GameMode.CREATIVE
        || killer.getGameMode() == GameMode.SPECTATOR || !EliteData.isElite(mob)) {
      return;
    }
    boolean expFarm = new ExpFarmTracker().isExpFarm(mob);
    if (!EliteRewards.qualifies(true, EliteData.isMinion(mob), mob.hasMetadata("frpgSpawnerMob"),
        expFarm)) {
      return;
    }
    org.bukkit.Chunk chunk = mob.getLocation().getChunk();
    String place = mob.getWorld().getUID() + ":" + chunk.getX() + ":" + chunk.getZ();
    if (!COOLDOWN.tryClaim(place, System.currentTimeMillis(),
        EliteConfig.rewardCooldownSeconds() * 1000L)) {
      return;
    }
    for (Map.Entry<Material, Integer> drop
        : EliteRewards.rollDrops(EliteConfig.drops(), RANDOM).entrySet()) {
      e.getDrops().add(new ItemStack(drop.getKey(), drop.getValue()));
    }
    giveBonusExp(killer);
  }

  private static void giveBonusExp(Player killer) {
    ItemGroups groups = new ItemGroups();
    Material held = killer.getInventory().getItemInMainHand().getType();
    String skill = EliteRewards.skillFor(groups.getSwords().contains(held),
        groups.getAxes().contains(held));
    Integer base = new ConfigLoad().getExpMapForSkill(skill).get("killAnythingElse");
    int bonus = EliteRewards.bonusExp(base == null ? 0 : base, EliteConfig.expMultiplier());
    if (bonus > 0) {
      XpShareGuard.share(() -> new ChangeStats(killer).changeEXP(skill, bonus));
    }
  }
}
