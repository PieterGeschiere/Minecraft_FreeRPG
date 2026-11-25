package mc.carlton.freerpg.worldevents;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class WorldEventCommandsTest {

  @Test
  void everyStartResultHasItsOwnReply() {
    assertEquals("worldEventStarted",
        WorldEventCommands.startReplyKey(WorldEventManager.StartResult.STARTED));
    assertEquals("worldEventAlreadyActive",
        WorldEventCommands.startReplyKey(WorldEventManager.StartResult.ALREADY_ACTIVE));
    assertEquals("worldEventCannotStart",
        WorldEventCommands.startReplyKey(WorldEventManager.StartResult.CANNOT_START));
    assertEquals("worldEventDisabled",
        WorldEventCommands.startReplyKey(WorldEventManager.StartResult.DISABLED));
    assertEquals("worldEventUnknownWorld",
        WorldEventCommands.startReplyKey(WorldEventManager.StartResult.NO_WORLD));
  }

  @Test
  void theTypeNamesAreTheCommandNames() {
    assertEquals(9, WorldEventCommands.typeNames().size());
    assertTrue(WorldEventCommands.typeNames().contains("blood_moon"));
    assertTrue(WorldEventCommands.typeNames().contains("fishing_festival"));
  }
}
