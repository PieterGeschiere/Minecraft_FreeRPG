package mc.carlton.freerpg.worldevents;

import java.util.UUID;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

/** One kind of world event. The manager decides when it runs; the event decides what it does. */
public interface WorldEvent {

  WorldEventType type();

  /** Conditions beyond the chance and the time window (the festival needs enough players). */
  boolean canStart();

  void onStart(ActiveEvent event);

  /** Once a second while the event runs. */
  default void onTick(ActiveEvent event) {
  }

  /**
   * The event is over. {@code byAdmin} is true when an admin stopped it or the plugin shut down:
   * nothing should be paid out then.
   */
  void onEnd(ActiveEvent event, boolean byAdmin);

  /** The skill exp multiplier while the event runs (applies to players in the event's scope). */
  default double xpMultiplier(String skillName) {
    return 1.0;
  }

  /** Extra chance (0 to 1) that flame pick smelts a double drop while the event runs. */
  default double flamePickBonus() {
    return 0.0;
  }

  /** How much the souls from kills are multiplied while the event runs. */
  default double soulMultiplier() {
    return 1.0;
  }

  /** Extra {name, value, ...} pairs for the title and subtitle texts. */
  default String[] announcePlaceholders(ActiveEvent event) {
    return new String[0];
  }

  default Sound announceSound() {
    return Sound.UI_TOAST_CHALLENGE_COMPLETE;
  }

  /** Extra {name, value, ...} pairs for the boss bar text. */
  default String[] barPlaceholders(ActiveEvent event) {
    return new String[0];
  }

  /** Every beat, for each player the event applies to. */
  default void decorate(Player player, ActiveEvent event) {
  }

  /** The player is no longer in the event's scope (or it ended): remove what was shown. */
  default void clearPlayer(UUID player) {
  }
}
