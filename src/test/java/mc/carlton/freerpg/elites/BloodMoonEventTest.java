package mc.carlton.freerpg.elites;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import mc.carlton.freerpg.worldevents.WorldEventType;
import org.junit.jupiter.api.Test;

class BloodMoonEventTest {

  @Test
  void weatherIsOnlyClearedWhenTheBloodMoonMadeIt() {
    assertTrue(BloodMoonEvent.shouldClearWeather(true, false));
    assertFalse(BloodMoonEvent.shouldClearWeather(true, true), "it was already storming");
    assertFalse(BloodMoonEvent.shouldClearWeather(false, false), "thunder effect is off");
  }

  @Test
  void itIsTheBloodMoonEventThatCanAlwaysStart() {
    BloodMoonEvent event = new BloodMoonEvent();
    assertEquals(WorldEventType.BLOOD_MOON, event.type());
    assertTrue(event.canStart());
  }
}
