package mc.carlton.freerpg.worldevents;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MutatorPickerTest {

  @BeforeEach
  void empty() {
    WorldEventConfig.loadFrom(new YamlConfiguration());
  }

  private static Map<Mutator, Integer> allEqual() {
    Map<Mutator, Integer> weights = new EnumMap<>(Mutator.class);
    for (Mutator mutator : Mutator.values()) {
      weights.put(mutator, 1);
    }
    return weights;
  }

  @Test
  void itPicksTheWantedNumberOfDifferentTwistsAndEveryTwistCanAppear() {
    Random random = new Random(8);
    Set<Mutator> seen = EnumSet.noneOf(Mutator.class);
    for (int i = 0; i < 500; i++) {
      Set<Mutator> picked = MutatorPicker.pick(allEqual(), 2, random);
      assertEquals(2, picked.size());
      seen.addAll(picked);
    }
    assertEquals(EnumSet.allOf(Mutator.class), seen);
  }

  @Test
  void twistsWithoutWeightNeverAppear() {
    Random random = new Random(8);
    Map<Mutator, Integer> weights = new EnumMap<>(Mutator.class);
    weights.put(Mutator.LOW_GRAVITY, 1);
    weights.put(Mutator.SOUL_STORM, 0);
    weights.put(Mutator.GLOWING_MOBS, 0);
    for (int i = 0; i < 100; i++) {
      assertEquals(EnumSet.of(Mutator.LOW_GRAVITY), MutatorPicker.pick(weights, 2, random));
    }
  }

  @Test
  void anEmptyMapOrAZeroCountGivesNothingAndNeverThrows() {
    Random random = new Random(8);
    assertTrue(MutatorPicker.pick(new EnumMap<>(Mutator.class), 2, random).isEmpty());
    assertTrue(MutatorPicker.pick(allEqual(), 0, random).isEmpty());
    assertTrue(MutatorPicker.pick(allEqual(), -1, random).isEmpty());
  }

  @Test
  void aHeavyWeightIsPickedFarMoreOften() {
    Random random = new Random(8);
    Map<Mutator, Integer> weights = new EnumMap<>(Mutator.class);
    weights.put(Mutator.GLASS_CANNON, 100);
    weights.put(Mutator.SWIFT_MOBS, 1);
    int heavy = 0;
    for (int i = 0; i < 2000; i++) {
      if (MutatorPicker.pick(weights, 1, random).contains(Mutator.GLASS_CANNON)) {
        heavy++;
      }
    }
    assertTrue(heavy > 1600, "heavy picked " + heavy);
  }

  @Test
  void asKeptNumberOfTwistsIsNeverMoreThanAvailable() {
    Map<Mutator, Integer> weights = new EnumMap<>(Mutator.class);
    weights.put(Mutator.LOW_GRAVITY, 1);
    assertEquals(1, MutatorPicker.pick(weights, 5, new Random(1)).size());
  }

  @Test
  void theConfigDecidesWhichTwistsAreAvailable() {
    assertEquals(8, MutatorPicker.available().size());
    YamlConfiguration yaml = new YamlConfiguration();
    yaml.set("events.mutatorNight.mutators.lowGravity.enabled", false);
    yaml.set("events.mutatorNight.mutators.glassCannon.weight", 0);
    yaml.set("events.mutatorNight.mutators.soulStorm.weight", 5);
    WorldEventConfig.loadFrom(yaml);
    Map<Mutator, Integer> available = MutatorPicker.available();
    assertEquals(6, available.size());
    assertFalse(available.containsKey(Mutator.LOW_GRAVITY));
    assertFalse(available.containsKey(Mutator.GLASS_CANNON));
    assertEquals(5, available.get(Mutator.SOUL_STORM));
  }

  @Test
  void everyTwistHasAUniqueIdAndItsTexts() {
    Set<String> ids = new HashSet<>();
    Set<String> keys = new HashSet<>();
    for (Mutator mutator : Mutator.values()) {
      assertTrue(ids.add(mutator.id()));
      assertTrue(keys.add(mutator.nameKey()));
      assertTrue(keys.add(mutator.descKey()));
    }
    assertEquals(8, Mutator.values().length);
  }

  @Test
  void theEventNeedsEnoughTwistsToStart() {
    MutatorNightEvent event = new MutatorNightEvent();
    assertEquals(WorldEventType.MUTATOR_NIGHT, event.type());
    assertTrue(event.canStart());
    YamlConfiguration yaml = new YamlConfiguration();
    for (Mutator mutator : Mutator.values()) {
      yaml.set("events.mutatorNight.mutators." + mutator.id() + ".enabled", false);
    }
    yaml.set("events.mutatorNight.mutators.soulStorm.enabled", true);
    WorldEventConfig.loadFrom(yaml);
    assertFalse(event.canStart(), "one twist is not enough for a count of two");
    yaml.set("events.mutatorNight.count", 1);
    assertTrue(event.canStart());
  }

  @Test
  void noTwistIsActiveWithoutARunningEvent() {
    assertFalse(MutatorNightEvent.isActive(Mutator.SOUL_STORM, null));
    assertEquals(1.0, new MutatorNightEvent().soulMultiplier());
  }
}
