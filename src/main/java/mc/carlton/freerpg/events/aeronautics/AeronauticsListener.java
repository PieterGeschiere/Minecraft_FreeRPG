package mc.carlton.freerpg.events.aeronautics;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import mc.carlton.freerpg.config.ConfigLoad;
import mc.carlton.freerpg.skills.aeronautics.AeronauticsRules;
import mc.carlton.freerpg.skills.building.DemolitionRules;
import mc.carlton.freerpg.skills.perksAndAbilities.Aeronautics;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.meta.FireworkMeta;

/**
 * Glue for the Aeronautics skill. The maths lives in skills/aeronautics; this class only reads
 * events, asks those helpers, and applies the result. It never hands out items or free rockets.
 */
public class AeronauticsListener implements Listener {

  private final Random random = new Random();
  /** Players who already used the boost that Tailwind improves since they last took off. */
  private final Set<UUID> boostedSinceTakeoff = new HashSet<>();

  // ---------------------------------------------------------------- experience

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onMove(PlayerMoveEvent e) {
    Player p = e.getPlayer();
    if (!p.isGliding()) {
      return;
    }
    Location from = e.getFrom();
    Location to = e.getTo();
    if (to == null || from.getWorld() == null || !from.getWorld().equals(to.getWorld())) {
      return;
    }
    if (!Aeronautics.isEnabled()) {
      return;
    }
    double dx = to.getX() - from.getX();
    double dz = to.getZ() - from.getZ();
    int steps = Aeronautics.xpRules().addDistance(p.getUniqueId(), Math.sqrt(dx * dx + dz * dz),
        System.currentTimeMillis());
    if (steps > 0) {
      new Aeronautics(p).onGlideSteps(steps);
    }
  }

  @EventHandler
  public void onToggleGlide(EntityToggleGlideEvent e) {
    if (!(e.getEntity() instanceof Player)) {
      return;
    }
    UUID id = e.getEntity().getUniqueId();
    boostedSinceTakeoff.remove(id); // a new flight starts (or ends) with a fresh first rocket
    if (!e.isGliding()) {
      Aeronautics.xpRules().reset(id);
    }
  }

  @EventHandler
  public void onQuit(PlayerQuitEvent e) {
    boostedSinceTakeoff.remove(e.getPlayer().getUniqueId());
    Aeronautics.xpRules().reset(e.getPlayer().getUniqueId());
  }

  // ---------------------------------------------------------------- Glide Efficiency

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onElytraDamage(PlayerItemDamageEvent e) {
    if (e.getItem().getType() != Material.ELYTRA || !Aeronautics.isEnabled()) {
      return;
    }
    ConfigLoad config = new ConfigLoad();
    double multiplier = AeronauticsRules.durabilityMultiplier(
        Aeronautics.statOf(e.getPlayer().getUniqueId(), Aeronautics.PASSIVE_EFFICIENCY),
        config.getAeronauticsEfficiencyPerPoint(), config.getAeronauticsEfficiencyCap());
    if (multiplier < 1.0) {
      e.setDamage(DemolitionRules.reducedDamage(e.getDamage(), multiplier, random.nextDouble()));
    }
  }

  // ---------------------------------------------------------------- Soft Landing

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onWallCrash(EntityDamageEvent e) {
    if (e.getCause() != EntityDamageEvent.DamageCause.FLY_INTO_WALL
        || !(e.getEntity() instanceof Player) || !Aeronautics.isEnabled()) {
      return;
    }
    int level = Aeronautics.statOf(e.getEntity().getUniqueId(), Aeronautics.PERK_SOFT_LANDING);
    e.setDamage(AeronauticsRules.damageAfterSoftLanding(e.getDamage(),
        new ConfigLoad().getAeronauticsSoftLandingReductions(), level));
  }

  // ---------------------------------------------------------------- Long Burn and Tailwind

  /**
   * Spigot has no elytra-boost event, so a boost is recognised as a rocket launched straight
   * ahead (not shot at an angle from a crossbow) by a player who is gliding.
   */
  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onRocketBoost(EntitySpawnEvent e) {
    if (!(e.getEntity() instanceof Firework firework) || firework.isShotAtAngle()) {
      return;
    }
    if (!(firework.getShooter() instanceof Player player) || !player.isGliding()
        || !Aeronautics.isEnabled()) {
      return;
    }
    UUID id = player.getUniqueId();
    boolean first = boostedSinceTakeoff.add(id);
    ConfigLoad config = new ConfigLoad();
    int extra = AeronauticsRules.extraRocketPower(config.getAeronauticsLongBurnPower(),
        Aeronautics.statOf(id, Aeronautics.PERK_LONG_BURN),
        Aeronautics.statOf(id, Aeronautics.PERK_TAILWIND) >= 1, first,
        config.getAeronauticsTailwindPower());
    if (extra <= 0) {
      return;
    }
    FireworkMeta meta = firework.getFireworkMeta();
    meta.setPower(AeronauticsRules.boostedPower(meta.getPower(), extra));
    firework.setFireworkMeta(meta); // also recalculates how long the rocket flies
  }
}
