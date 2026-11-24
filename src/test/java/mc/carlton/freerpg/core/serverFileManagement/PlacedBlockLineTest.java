package mc.carlton.freerpg.core.serverFileManagement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class PlacedBlockLineTest {

  @Test
  void roundTripsNegativeCoordinatesAndSpacedWorldName() {
    String line = PlacedBlockLine.format("my world", -5, 64, -300);
    assertEquals("my world,-5,64,-300", line);
    PlacedBlockLine.Parsed p = PlacedBlockLine.parse(line).orElseThrow();
    assertEquals(new PlacedBlockLine.Parsed("my world", -5, 64, -300), p);
  }

  @Test
  void blankOrMalformedLinesAreEmpty() {
    assertEquals(Optional.empty(), PlacedBlockLine.parse(""));
    assertEquals(Optional.empty(), PlacedBlockLine.parse("w,1,2"));
    assertEquals(Optional.empty(), PlacedBlockLine.parse("w,a,2,3"));
    assertTrue(PlacedBlockLine.parse(null).isEmpty());
  }
}
