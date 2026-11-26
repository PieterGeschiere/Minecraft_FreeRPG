package mc.carlton.freerpg.worldevents;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import mc.carlton.freerpg.config.ConfigLoad;
import mc.carlton.freerpg.core.info.player.ChangeStats;
import mc.carlton.freerpg.elites.EliteRewards;
import mc.carlton.freerpg.elites.RewardCooldown;
import mc.carlton.freerpg.party.XpShareGuard;
import mc.carlton.freerpg.utils.game.ExpFarmTracker;
import mc.carlton.freerpg.utils.globalVariables.ItemGroups;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Killing a Titan mob gives bonus exp in the skill of your weapon, and each piece of its loot can
 * drop twice. Items the mob carried (for example ones a player threw next to it) are never copied.
 * Mobs from spawners and exp farms get no bonus, and a chunk pays it out at most once per cooldown
 * (the same guards as elite rewards).
 */
public final class TitanLootListener implements Listener {

  private static final WorldEventType TYPE = WorldEventType.TITAN_NIGHT;
  private static final Random RANDOM = new Random();
  private static final RewardCooldown COOLDOWN = new RewardCooldown();

  @EventHandler(priority = EventPriority.HIGH)
  void onDeath(EntityDeathEvent e) {
    LivingEntity mob = e.getEntity();
    Player killer = mob.getKiller();
    if (!mob.hasMetadata(TitanSpawnListener.TITAN_TAG) || killer == null
        || killer.getGameMode() == GameMode.CREATIVE || killer.getGameMode() == GameMode.SPECTATOR
        || mob.hasMetadata("frpgSpawnerMob") || new ExpFarmTracker().isExpFarm(mob)) {
      return;
    }
    Chunk chunk = mob.getLocation().getChunk();
    String place = mob.getWorld().getUID() + ":" + chunk.getX() + ":" + chunk.getZ();
    long cooldownMillis = Math.max(0, WorldEventConfig.integer(TYPE, "chunkCooldownSeconds", 120))
        * 1000L;
    if (!COOLDOWN.tryClaim(place, System.currentTimeMillis(), cooldownMillis)) {
      return;
    }
    double chance = EventDefaults.dropChance(TYPE);
    List<ItemStack> extra = new ArrayList<>();
    for (ItemStack drop : LootGuard.split(e.getDrops(), LootGuard.stickyEquipment(mob)).loot()) {
      if (EventRules.rollStart(chance, RANDOM)) {
        extra.add(drop.clone());
      }
    }
    e.getDrops().addAll(extra);
    giveBonusExp(killer);
    WorldEventEffects.titanKill(killer, mob);
  }

  private static void giveBonusExp(Player killer) {
    ItemGroups groups = new ItemGroups();
    Material held = killer.getInventory().getItemInMainHand().getType();
    String skill = EliteRewards.skillFor(groups.getSwords().contains(held),
        groups.getAxes().contains(held));
    Integer base = new ConfigLoad().getExpMapForSkill(skill).get("killAnythingElse");
    int bonus = TitanRules.bonusExp(base == null ? 0 : base, TitanRules.killExpMultiplier());
    if (bonus > 0) {
      XpShareGuard.share(() -> new ChangeStats(killer).changeEXP(skill, bonus));
    }
  }
}
