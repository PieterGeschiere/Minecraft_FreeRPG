package mc.carlton.freerpg.core.info.server;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import org.bukkit.Location;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PlacedBlocksManagerTest {

  private final PlacedBlocksManager manager = new PlacedBlocksManager();

  @BeforeEach
  void reset() {
    manager.setBlocksMap(new HashSet<>());
  }

  @Test
  void addThenTrackedThenRemoved() {
    Location loc = new Location(null, 1, 2, 3);
    manager.addLocation(loc);
    assertTrue(manager.isLocationTracked(new Location(null, 1, 2, 3)));
    manager.removeLocation(loc);
    assertFalse(manager.isLocationTracked(loc));
  }

  @Test
  void removingUntrackedDoesNotThrow() {
    assertDoesNotThrow(() -> manager.removeLocation(new Location(null, 9, 9, 9)));
  }
}
