package mc.carlton.freerpg.elites;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

/**
 * Keeps track of the loaded elites: calls their affixes once a second and shows a boss bar to the
 * players near them.
 */
public final class EliteTracker {

  private static final double BAR_RANGE_SQUARED = 30.0 * 30.0;

  private static final Map<UUID, LivingEntity> TRACKED = new HashMap<>();
  private static final Map<UUID, BossBar> BARS = new HashMap<>();
  private static BukkitTask task;

  private EliteTracker() {
  }

  public static void start(Plugin plugin) {
    stop();
    for (org.bukkit.World world : Bukkit.getWorlds()) {
      for (LivingEntity entity : world.getLivingEntities()) {
        if (EliteData.isElite(entity)) {
          track(entity);
        }
      }
    }
    task = Bukkit.getScheduler().runTaskTimer(plugin, EliteTracker::tick, 20L, 20L);
  }

  public static void stop() {
    if (task != null) {
      task.cancel();
      task = null;
    }
    for (BossBar bar : BARS.values()) {
      bar.removeAll();
    }
    BARS.clear();
    TRACKED.clear();
  }

  public static void track(LivingEntity mob) {
    TRACKED.put(mob.getUniqueId(), mob);
  }

  public static void untrack(UUID id) {
    TRACKED.remove(id);
    BossBar bar = BARS.remove(id);
    if (bar != null) {
      bar.removeAll();
    }
  }

  public static int trackedCount() {
    return TRACKED.size();
  }

  private static void tick() {
    Iterator<Map.Entry<UUID, LivingEntity>> it = TRACKED.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry<UUID, LivingEntity> entry = it.next();
      LivingEntity mob = entry.getValue();
      if (!mob.isValid() || mob.isDead()) {
        BossBar bar = BARS.remove(entry.getKey());
        if (bar != null) {
          bar.removeAll();
        }
        it.remove();
        continue;
      }
      // one misbehaving elite must not stop the others from being updated
      try {
        for (String id : EliteData.affixes(mob)) {
          Affix affix = AffixRegistry.get(id);
          if (affix != null) {
            affix.tick(mob);
          }
        }
        updateBar(entry.getKey(), mob);
      } catch (RuntimeException ex) {
        Bukkit.getLogger().log(java.util.logging.Level.WARNING,
            "[FreeRPG] An elite affix failed on a " + mob.getType() + ": " + ex);
      }
    }
  }

  private static void updateBar(UUID id, LivingEntity mob) {
    BossBar bar = BARS.computeIfAbsent(id, key -> Bukkit.createBossBar(
        mob.getCustomName() == null ? "Elite" : mob.getCustomName(), BarColor.RED,
        BarStyle.SEGMENTED_10));
    AttributeInstance max = mob.getAttribute(Attribute.MAX_HEALTH);
    double maxHealth = max == null ? 20.0 : max.getValue();
    bar.setProgress(Math.max(0.0, Math.min(1.0, mob.getHealth() / maxHealth)));
    for (Player player : mob.getWorld().getPlayers()) {
      boolean near = player.getLocation().distanceSquared(mob.getLocation()) <= BAR_RANGE_SQUARED;
      boolean showing = bar.getPlayers().contains(player);
      if (near && !showing) {
        bar.addPlayer(player);
      } else if (!near && showing) {
        bar.removePlayer(player);
      }
    }
    for (Player viewer : bar.getPlayers()) {
      if (!viewer.getWorld().equals(mob.getWorld())) {
        bar.removePlayer(viewer);
      }
    }
  }
}
