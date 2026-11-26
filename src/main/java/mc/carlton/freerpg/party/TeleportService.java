package mc.carlton.freerpg.party;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import mc.carlton.freerpg.core.info.server.WorldGuardChecks;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

/**
 * Teleports for party requests and the party spawn: the same checks, the same stand-still
 * timer, the same effects. Particles and sounds are spawned in the world, so everyone nearby
 * sees and hears them, at the start point and at the destination.
 */
public final class TeleportService {

  private static final long TICKS_PER_CHECK = 4L;

  private static final Map<UUID, Active> active = new HashMap<>();
  private static final Map<UUID, Long> lastDamaged = new HashMap<>();

  private TeleportService() {
  }

  private static final class Active {

    final Player mover;
    final UUID target;
    final Warmup warmup;
    final Supplier<Location> destination;
    final Runnable onSuccess;
    final World world;
    final int blockX;
    final int blockY;
    final int blockZ;
    final long startMillis;
    boolean damaged;
    int shownSeconds = -1;
    BukkitTask task;

    Active(Player mover, UUID target, Supplier<Location> destination, Runnable onSuccess,
        long now) {
      this.mover = mover;
      this.target = target;
      this.destination = destination;
      this.onSuccess = onSuccess;
      this.warmup = new Warmup(now, PartyConfig.tpaWarmupSeconds());
      Location l = mover.getLocation();
      this.world = l.getWorld();
      this.blockX = l.getBlockX();
      this.blockY = l.getBlockY();
      this.blockZ = l.getBlockZ();
      this.startMillis = now;
    }
  }

  // ------------------------------------------------------------------ combat

  /** A player took damage: starts the combat window and cancels a running warmup. */
  public static void noteDamage(UUID id) {
    lastDamaged.put(id, System.currentTimeMillis());
    Active a = active.get(id);
    if (a != null) {
      a.damaged = true;
    }
  }

  public static boolean inCombat(UUID id) {
    Long last = lastDamaged.get(id);
    return last != null
        && System.currentTimeMillis() - last < PartyConfig.combatSeconds() * 1000L;
  }

  public static void forget(UUID id) {
    lastDamaged.remove(id);
  }

  // ------------------------------------------------------------------ checks

  /**
   * Why the player cannot teleport (to the destination, when known): null if allowed, otherwise
   * a message key followed by its key/value pairs.
   */
  public static String[] blockReason(Player mover, Location destination) {
    return blockReason(mover, destination, true);
  }

  private static String[] blockReason(Player mover, Location destination,
      boolean checkCooldown) {
    long now = System.currentTimeMillis();
    long cooldown = Parties.teleports().cooldownRemaining(mover.getUniqueId(), now);
    if (checkCooldown && cooldown > 0) {
      return new String[] {"partyTpCooldown", "seconds", String.valueOf((cooldown + 999) / 1000)};
    }
    if (inCombat(mover.getUniqueId())) {
      return new String[] {"partyTpCombat"};
    }
    if (mover.isInsideVehicle()) {
      return new String[] {"partyTpRiding"};
    }
    List<String> blocked = PartyConfig.tpaBlockedWorlds();
    if (blocked.contains(mover.getWorld().getName())) {
      return new String[] {"partyTpBlockedWorld"};
    }
    if (destination != null && destination.getWorld() != null) {
      if (blocked.contains(destination.getWorld().getName())) {
        return new String[] {"partyTpBlockedWorld"};
      }
      if (!PartyConfig.tpaAllowCrossWorld() && !destination.getWorld().equals(mover.getWorld())) {
        return new String[] {"partyTpCrossWorld"};
      }
      if (!new WorldGuardChecks().canEnter(mover, destination)) {
        return new String[] {"partyTpRegion"};
      }
    }
    return null;
  }

  // ------------------------------------------------------------------- start

  /**
   * Starts the stand-still timer and teleports at the end.
   *
   * @param target     the party member being teleported to, or null (party spawn)
   * @param destination where to go; evaluated again at the end, null means "not available"
   * @param onSuccess  runs after a successful teleport (may be null)
   */
  public static void start(Player mover, UUID target, Supplier<Location> destination,
      Runnable onSuccess) {
    Location firstDestination = destination.get();
    if (firstDestination == null) {
      PartyMessages.send(mover, "partyTpaUnavailable", "player", "?");
      return;
    }
    String[] reason = blockReason(mover, firstDestination);
    if (reason != null) {
      PartyMessages.send(mover, reason[0], slice(reason));
      return;
    }
    cancel(mover.getUniqueId(), "partyTpCancelOther");
    Active a = new Active(mover, target, destination, onSuccess, System.currentTimeMillis());
    active.put(mover.getUniqueId(), a);
    a.task = Bukkit.getScheduler().runTaskTimer(Parties.plugin(), () -> tick(a), 0L,
        TICKS_PER_CHECK);
    if (PartyConfig.effectsEnabled()) {
      mover.getWorld().playSound(mover.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 0.7f, 1.6f);
      mover.getWorld().playSound(mover.getLocation(), Sound.BLOCK_PORTAL_TRIGGER, 0.35f, 1.4f);
    }
  }

  private static String[] slice(String[] reason) {
    String[] rest = new String[reason.length - 1];
    System.arraycopy(reason, 1, rest, 0, rest.length);
    return rest;
  }

  // -------------------------------------------------------------------- tick

  private static void tick(Active a) {
    Player mover = a.mover;
    if (!mover.isOnline() || mover.isDead()) {
      finishWithoutMessage(a);
      return;
    }
    if (a.target != null) {
      Player target = Bukkit.getPlayer(a.target);
      if (target == null || target.isDead() || !sameParty(mover, target)) {
        cancelWith(a, "partyTpCancelTarget", "player",
            target == null ? "?" : target.getName());
        return;
      }
    }
    long now = System.currentTimeMillis();
    Location l = mover.getLocation();
    boolean moved = !l.getWorld().equals(a.world) || l.getBlockX() != a.blockX
        || l.getBlockY() != a.blockY || l.getBlockZ() != a.blockZ;
    Warmup.State state = a.warmup.tick(now, moved, a.damaged);
    switch (state) {
      case CANCELLED:
        cancelWith(a, a.damaged ? "partyTpCancelDamage" : "partyTpCancelMoved");
        return;
      case DONE:
        complete(a);
        return;
      default:
        break;
    }
    int seconds = a.warmup.secondsLeft(now);
    if (seconds != a.shownSeconds) {
      a.shownSeconds = seconds;
      mover.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(
          PartyMessages.text(mover, "partyTpCountdown", "seconds", String.valueOf(seconds))));
      if (PartyConfig.effectsEnabled()) {
        float rise = 0.8f + 1.2f * progress(a, now);
        mover.getWorld().playSound(mover.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.8f,
            Math.min(2.0f, rise));
        mover.getWorld().playSound(mover.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.6f,
            Math.min(2.0f, rise));
      }
    }
    if (PartyConfig.effectsEnabled()) {
      spiral(mover.getLocation(), now - a.startMillis, progress(a, now));
    }
  }

  private static float progress(Active a, long now) {
    long total = Math.max(1L, PartyConfig.tpaWarmupSeconds() * 1000L);
    return Math.max(0f, Math.min(1f, (now - a.startMillis) / (float) total));
  }

  private static void complete(Active a) {
    Player mover = a.mover;
    Location destination = a.destination.get();
    if (destination == null || destination.getWorld() == null) {
      cancelWith(a, "partyTpaUnavailable", "player", "?");
      return;
    }
    // The target may have moved somewhere that is not allowed during the warmup
    String[] reason = blockReason(mover, destination, false);
    if (reason != null) {
      cancelWith(a, reason[0], slice(reason));
      return;
    }
    stop(a);
    Location origin = mover.getLocation();
    burst(origin, false);
    mover.teleport(destination);
    burst(destination, true);
    Parties.teleports().markUsed(mover.getUniqueId(), System.currentTimeMillis());
    PartyMessages.send(mover, "partyTpDone");
    if (a.target != null) {
      PartyMessages.send(Bukkit.getPlayer(a.target), "partyTpDone");
    }
    if (a.onSuccess != null) {
      a.onSuccess.run();
    }
  }

  private static boolean sameParty(Player a, Player b) {
    Party pa = Parties.manager().partyOf(a.getUniqueId()).orElse(null);
    Party pb = Parties.manager().partyOf(b.getUniqueId()).orElse(null);
    return pa != null && pa == pb;
  }

  // ------------------------------------------------------------------ cancel

  /** Cancels the warmup of this player (if any) with a message to them and the target. */
  public static void cancel(UUID mover, String key, String... kv) {
    Active a = active.get(mover);
    if (a != null) {
      cancelWith(a, key, kv);
    }
  }

  /** Cancels every warmup that involves this player, as the mover or as the target. */
  public static void cancelInvolving(UUID player, String key, String... kv) {
    for (Active a : new ArrayList<>(active.values())) {
      if (a.mover.getUniqueId().equals(player) || player.equals(a.target)) {
        cancelWith(a, key, kv);
      }
    }
  }

  public static void cancelAll() {
    for (Active a : new ArrayList<>(active.values())) {
      stop(a);
    }
  }

  private static void cancelWith(Active a, String key, String... kv) {
    stop(a);
    fizzle(a.mover);
    if (a.mover.isOnline()) {
      PartyMessages.send(a.mover, key, kv);
    }
    if (a.target != null) {
      PartyMessages.send(Bukkit.getPlayer(a.target), "partyTpCancelOther");
    }
  }

  private static void finishWithoutMessage(Active a) {
    stop(a);
  }

  private static void stop(Active a) {
    active.remove(a.mover.getUniqueId());
    if (a.task != null) {
      a.task.cancel();
    }
  }

  // ----------------------------------------------------------------- effects

  private static Particle particle(String name, Particle fallback) {
    try {
      return Particle.valueOf(name.toUpperCase());
    } catch (IllegalArgumentException e) {
      return fallback;
    }
  }

  private static Sound sound(String name) {
    try {
      return Sound.valueOf(name.toUpperCase());
    } catch (IllegalArgumentException e) {
      return Sound.ENTITY_ENDERMAN_TELEPORT;
    }
  }

  /**
   * A double helix that climbs the player and tightens while the timer runs, with a glowing
   * ring on the ground that shrinks and enchant runes drifting in toward the player.
   */
  private static void spiral(Location base, long elapsedMillis, float progress) {
    World world = base.getWorld();
    if (world == null) {
      return;
    }
    Particle main = particle(PartyConfig.effectParticle(), Particle.PORTAL);
    double radius = 1.2 - 0.6 * progress;
    double angle = elapsedMillis / 140.0;
    double height = (elapsedMillis % 1200) / 1200.0 * 2.2;
    for (int i = 0; i < 3; i++) {
      double a = angle + i * (2 * Math.PI / 3);
      double x = base.getX() + Math.cos(a) * radius;
      double z = base.getZ() + Math.sin(a) * radius;
      world.spawnParticle(main, x, base.getY() + height, z, 3, 0.03, 0.03, 0.03, 0.0);
      world.spawnParticle(Particle.END_ROD, x, base.getY() + 2.2 - height, z, 1, 0, 0, 0, 0.0);
    }
    // Ground ring that shrinks toward the player
    double ringRadius = 1.6 * (1.0 - progress) + 0.3;
    for (int i = 0; i < 12; i++) {
      double a = i * (Math.PI / 6) - angle * 0.5;
      world.spawnParticle(Particle.ENCHANT, base.getX() + Math.cos(a) * ringRadius,
          base.getY() + 0.1, base.getZ() + Math.sin(a) * ringRadius, 1, 0.0, 0.0, 0.0, 0.0);
    }
    if (progress > 0.6) {
      world.spawnParticle(Particle.ELECTRIC_SPARK, base.getX(), base.getY() + 1.0, base.getZ(),
          2 + (int) (progress * 6), 0.4, 0.8, 0.4, 0.05);
    }
  }

  /** The sound of a teleport that did not happen. */
  private static void fizzle(Player mover) {
    if (!PartyConfig.effectsEnabled() || !mover.isOnline()) {
      return;
    }
    Location at = mover.getLocation();
    World world = at.getWorld();
    world.playSound(at, Sound.BLOCK_BEACON_DEACTIVATE, 0.8f, 1.2f);
    world.playSound(at, Sound.ENTITY_ILLUSIONER_MIRROR_MOVE, 0.6f, 0.7f);
    world.spawnParticle(Particle.SMOKE, at.getX(), at.getY() + 1.0, at.getZ(), 25, 0.35, 0.7,
        0.35, 0.02);
  }

  /**
   * The effect at one end of the teleport: a light column, an expanding ring, a particle burst
   * and a layered sound. {@code arrival} makes it brighter and uses the reverse portal burst.
   */
  private static void burst(Location at, boolean arrival) {
    World world = at.getWorld();
    if (world == null) {
      return;
    }
    if (PartyConfig.effectsEnabled()) {
      Particle burst = particle(PartyConfig.effectBurstParticle(), Particle.REVERSE_PORTAL);
      Particle first = arrival ? burst : Particle.PORTAL;
      world.spawnParticle(first, at.getX(), at.getY() + 1.0, at.getZ(), 90, 0.4, 0.9, 0.4, 0.25);
      // Column of light
      for (int i = 0; i < 16; i++) {
        world.spawnParticle(Particle.END_ROD, at.getX(), at.getY() + i * 0.25, at.getZ(), 2, 0.1,
            0.0, 0.1, 0.02);
      }
      // Ring that shoots outward
      for (int i = 0; i < 36; i++) {
        double a = i * (Math.PI / 18);
        world.spawnParticle(Particle.FIREWORK, at.getX(), at.getY() + 0.15, at.getZ(), 0,
            Math.cos(a), 0.02, Math.sin(a), 0.35);
      }
      if (arrival) {
        world.spawnParticle(Particle.ELECTRIC_SPARK, at.getX(), at.getY() + 1.0, at.getZ(), 40,
            0.5, 0.9, 0.5, 0.3);
      }
    }
    world.playSound(at, sound(PartyConfig.effectSound()), 1.0f, arrival ? 1.2f : 0.8f);
    if (PartyConfig.effectsEnabled()) {
      world.playSound(at, Sound.BLOCK_AMETHYST_CLUSTER_BREAK, 1.0f, arrival ? 1.6f : 1.0f);
      world.playSound(at, arrival ? Sound.ENTITY_FIREWORK_ROCKET_LAUNCH
          : Sound.ENTITY_ENDER_EYE_DEATH, 0.8f, arrival ? 1.3f : 0.9f);
    }
  }
}
