package mc.carlton.freerpg.worldevents;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class EventVisualsTest {

  @Test
  void timeLeftIsShownAsMinutesAndSeconds() {
    assertEquals("8:20", EventVisuals.formatTime(10000));
    assertEquals("0:05", EventVisuals.formatTime(100));
    assertEquals("0:00", EventVisuals.formatTime(-50));
  }
}
