package mc.carlton.freerpg.elites.affix;

import mc.carlton.freerpg.elites.Affix;
import mc.carlton.freerpg.elites.EliteConfig;
import mc.carlton.freerpg.elites.EliteData;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;

/** Calls minions to its side now and then while it fights. */
public class Summoner implements Affix {

  @Override
  public String id() {
    return "summoner";
  }

  @Override
  public boolean canApplyTo(EntityType type) {
    return AffixSupport.isCopyable(type);
  }

  /** How many minions to call so that there are never more than {@code maxAlive}. */
  static int minionsToSpawn(int alive, int wanted, int maxAlive) {
    return Math.max(0, Math.min(wanted, maxAlive - alive));
  }

  @Override
  public void tick(LivingEntity mob) {
    LivingEntity target = AffixSupport.target(mob);
    if (target == null) {
      return;
    }
    int interval = (int) EliteConfig.affixDouble(id(), "intervalSeconds", 10);
    if (!AffixSupport.due(mob, "frpgSummonTimer", interval)) {
      return;
    }
    int alive = 0;
    for (Entity near : mob.getNearbyEntities(20, 10, 20)) {
      if (EliteData.isMinion(near)) {
        alive++;
      }
    }
    int count = minionsToSpawn(alive, (int) EliteConfig.affixDouble(id(), "minions", 2),
        (int) EliteConfig.affixDouble(id(), "maxAlive", 6));
    for (int i = 0; i < count; i++) {
      Location where = mob.getLocation().add(Math.cos(i * 2.1) * 1.5, 0, Math.sin(i * 2.1) * 1.5);
      LivingEntity minion = AffixSupport.spawnMinion(mob, where);
      if (minion instanceof Mob m) {
        m.setTarget(target);
      }
      if (minion != null) {
        mob.getWorld().spawnParticle(Particle.CLOUD, where, 8, 0.3, 0.5, 0.3, 0.02);
      }
    }
  }
}
