package mc.carlton.freerpg.quests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class WheelAnimationTest {

  @Test
  void ringSlotsAre26UniqueEdgeSlots() {
    int[] slots = WheelAnimation.RING_SLOTS;
    assertEquals(26, slots.length);
    Set<Integer> unique = new HashSet<>();
    for (int slot : slots) {
      assertTrue(slot >= 0 && slot < 54);
      int row = slot / 9;
      int column = slot % 9;
      assertTrue(row == 0 || row == 5 || column == 0 || column == 8, "slot " + slot);
      unique.add(slot);
    }
    assertEquals(26, unique.size());
  }

  @Test
  void ringSlotsAreContinuousNeighbours() {
    int[] slots = WheelAnimation.RING_SLOTS;
    for (int i = 0; i < slots.length; i++) {
      int next = slots[(i + 1) % slots.length];
      int diff = Math.abs(next - slots[i]);
      assertTrue(diff == 1 || diff == 9, slots[i] + " -> " + next);
    }
  }

  @Test
  void sequenceEndsOnLandingForEveryIndex() {
    for (int landing = 0; landing < 26; landing++) {
      int[] sequence = WheelAnimation.stepSequence(26, landing, 60);
      assertEquals(landing, sequence[sequence.length - 1], "landing " + landing);
    }
  }

  @Test
  void sequenceLengthAtLeastMinSteps() {
    for (int landing = 0; landing < 26; landing++) {
      int[] sequence = WheelAnimation.stepSequence(26, landing, 60);
      assertTrue(sequence.length >= 60);
      assertTrue(sequence.length < 60 + 26);
    }
  }

  @Test
  void sequenceMovesOneStepAtATime() {
    int[] sequence = WheelAnimation.stepSequence(26, 9, 60);
    for (int i = 1; i < sequence.length; i++) {
      assertEquals((sequence[i - 1] + 1) % 26, sequence[i]);
    }
  }

  @Test
  void delaysNeverDecrease() {
    for (int total : new int[] {1, 2, 3, 30, 60, 85}) {
      int previous = 0;
      for (int step = 0; step < total; step++) {
        int delay = WheelAnimation.delayTicks(step, total);
        assertTrue(delay >= previous, "total " + total + " step " + step);
        assertTrue(delay >= 1);
        previous = delay;
      }
    }
  }

  @Test
  void lastDelayAtLeastEightTicks() {
    for (int total : new int[] {1, 2, 3, 30, 60, 85}) {
      assertTrue(WheelAnimation.delayTicks(total - 1, total) >= 8, "total " + total);
    }
  }

  @Test
  void firstStepsAreFast() {
    assertEquals(1, WheelAnimation.delayTicks(0, 60));
  }
}
