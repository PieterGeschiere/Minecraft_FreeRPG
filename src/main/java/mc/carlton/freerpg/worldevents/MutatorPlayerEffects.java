package mc.carlton.freerpg.worldevents;

import java.util.List;
import mc.carlton.freerpg.FreeRPG;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;

/**
 * The attribute changes of the Low Gravity twist. Modifiers added to a player are saved with the
 * player, so they are always found again by their fixed keys and removed: when the night ends, when
 * the player leaves, and at login (which also cleans up after a server crash).
 */
public final class MutatorPlayerEffects {

  /** One attribute change of Low Gravity. */
  private record Part(Attribute attribute, String suffix, AttributeModifier.Operation operation,
      String setting, double def) {
  }

  /**
   * The key suffix of each part. These are plain constants on purpose: the attributes themselves
   * can only be looked up on a running server, so the parts are built when they are needed.
   */
  private static final List<String> SUFFIXES = List.of("gravity", "jump", "safefall");

  private static List<Part> parts() {
    return List.of(
        new Part(Attribute.GRAVITY, SUFFIXES.get(0), AttributeModifier.Operation.ADD_SCALAR,
            "gravity", -0.5),
        new Part(Attribute.JUMP_STRENGTH, SUFFIXES.get(1), AttributeModifier.Operation.ADD_SCALAR,
            "jump", 0.3),
        new Part(Attribute.SAFE_FALL_DISTANCE, SUFFIXES.get(2),
            AttributeModifier.Operation.ADD_NUMBER, "safeFall", 6.0));
  }

  private MutatorPlayerEffects() {
  }

  /** The key suffix of each part (each part has its own modifier key). */
  static List<String> partSuffixes() {
    return SUFFIXES;
  }

  private static NamespacedKey key(Part part) {
    return new NamespacedKey(FreeRPG.getPlugin(FreeRPG.class), "event_low_gravity_" + part.suffix());
  }

  private static AttributeModifier find(AttributeInstance instance, NamespacedKey key) {
    for (AttributeModifier modifier : instance.getModifiers()) {
      if (key.equals(modifier.getKey())) {
        return modifier;
      }
    }
    return null;
  }

  /** Gives the player Low Gravity (does nothing for the parts the player already has). */
  static void applyLowGravity(Player player) {
    for (Part part : parts()) {
      AttributeInstance instance = player.getAttribute(part.attribute());
      NamespacedKey key = key(part);
      if (instance == null || find(instance, key) != null) {
        continue;
      }
      instance.addModifier(new AttributeModifier(key,
          Mutator.LOW_GRAVITY.number(part.setting(), part.def()), part.operation(),
          EquipmentSlotGroup.ANY));
    }
  }

  /** Takes Low Gravity away, whatever is left of it. Safe to call at any time. */
  static void removeLowGravity(Player player) {
    for (Part part : parts()) {
      AttributeInstance instance = player.getAttribute(part.attribute());
      if (instance == null) {
        continue;
      }
      AttributeModifier modifier = find(instance, key(part));
      for (int tries = 0; modifier != null && tries < 10; tries++) {
        instance.removeModifier(modifier);
        modifier = find(instance, key(part));
      }
    }
  }
}
