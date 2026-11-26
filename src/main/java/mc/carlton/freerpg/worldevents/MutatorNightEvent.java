package mc.carlton.freerpg.worldevents;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 * Mutator Night: when it starts it picks a few different rule twists (see {@link Mutator}) and keeps
 * them for the night. Each twist is carried out by its own listener or per-player step, which
 * first asks {@link #isActive(Mutator, World)}.
 */
public final class MutatorNightEvent implements WorldEvent {

  private static final WorldEventType TYPE = WorldEventType.MUTATOR_NIGHT;
  private static final Random RANDOM = new Random();

  /** The twists of the running night. */
  static final class State {
    private final Set<Mutator> active;

    State(Set<Mutator> active) {
      this.active = active.isEmpty() ? EnumSet.noneOf(Mutator.class) : EnumSet.copyOf(active);
    }

    boolean has(Mutator mutator) {
      return active.contains(mutator);
    }

    Set<Mutator> all() {
      return active;
    }
  }

  /** How many twists a night picks (at least one). */
  static int count() {
    return Math.max(1, WorldEventConfig.integer(TYPE, "count", 2));
  }

  /** Whether this twist is part of the running Mutator Night in this world. */
  public static boolean isActive(Mutator mutator, World world) {
    return WorldEventManager.current()
        .filter(event -> event.type() == TYPE && event.appliesTo(world))
        .map(event -> {
          State state = event.state();
          return state != null && state.has(mutator);
        })
        .orElse(false);
  }

  @Override
  public WorldEventType type() {
    return TYPE;
  }

  /** There must be enough twists switched on to fill a night. */
  @Override
  public boolean canStart() {
    return MutatorPicker.available().size() >= count();
  }

  @Override
  public void onStart(ActiveEvent event) {
    State state = new State(MutatorPicker.pick(MutatorPicker.available(), count(), RANDOM));
    event.setState(state);
    for (Player player : Bukkit.getOnlinePlayers()) {
      if (!event.appliesTo(player.getWorld())) {
        continue;
      }
      WorldEventEffects.mutatorReveal(player, state.all());
      player.sendMessage(WorldEventMessages.text(player, "worldEventMutatorList"));
      for (Mutator mutator : state.all()) {
        player.sendMessage(WorldEventMessages.text(player, "worldEventMutatorEntry", "name",
            WorldEventMessages.text(player, mutator.nameKey()), "desc",
            WorldEventMessages.text(player, mutator.descKey())));
      }
    }
  }

  @Override
  public void onEnd(ActiveEvent event, boolean byAdmin) {
    // players first: Low Gravity must come off even if something below fails
    for (Player player : Bukkit.getOnlinePlayers()) {
      MutatorPlayerEffects.removeLowGravity(player);
    }
    if (event.world() != null) {
      MutatorMobListener.clearGlow(event.world());
    }
  }

  /** Every beat for each player in the world: Low Gravity and the extra hunger of Hungry Night. */
  @Override
  public void decorate(Player player, ActiveEvent event) {
    State state = event.state();
    if (state == null || player.getGameMode() == GameMode.SPECTATOR) {
      return;
    }
    if (state.has(Mutator.LOW_GRAVITY)) {
      MutatorPlayerEffects.applyLowGravity(player);
    }
    if (state.has(Mutator.HUNGRY_NIGHT) && player.getGameMode() != GameMode.CREATIVE
        && HungerRules.shouldDrain(player.getFoodLevel(), HungerRules.minFood())) {
      player.setExhaustion(player.getExhaustion() + (float) HungerRules.exhaustionPerBeat());
    }
  }

  /** The player left the world of the event (or it ended): take Low Gravity away. */
  @Override
  public void clearPlayer(UUID id) {
    Player player = Bukkit.getPlayer(id);
    if (player != null) {
      MutatorPlayerEffects.removeLowGravity(player);
    }
  }

  /** The names of the night's twists in English (title and boss bar are the same for everyone). */
  private static String[] names(ActiveEvent event) {
    State state = event.state();
    String list = state == null ? "-" : state.all().stream()
        .map(mutator -> WorldEventMessages.text(null, mutator.nameKey()))
        .collect(Collectors.joining(", "));
    return new String[] {"mutators", list.isEmpty() ? "-" : list};
  }

  @Override
  public String[] announcePlaceholders(ActiveEvent event) {
    return names(event);
  }

  @Override
  public String[] barPlaceholders(ActiveEvent event) {
    return names(event);
  }

  @Override
  public double soulMultiplier() {
    return WorldEventManager.current()
        .filter(event -> event.type() == TYPE)
        .map(event -> {
          State state = event.state();
          return state != null && state.has(Mutator.SOUL_STORM)
              ? Math.max(1.0, Mutator.SOUL_STORM.number("multiplier", 2.0)) : 1.0;
        })
        .orElse(1.0);
  }

  @Override
  public Sound announceSound() {
    return Sound.BLOCK_BEACON_ACTIVATE;
  }
}
