package mc.carlton.freerpg.elites;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class EliteDataTest {

  @Test
  void affixListRoundTrips() {
    List<String> ids = List.of("swift", "volatile");
    assertEquals("swift,volatile", EliteData.encode(ids));
    assertEquals(ids, EliteData.decode("swift,volatile"));
  }

  @Test
  void decodeIgnoresBlankPartsAndSpaces() {
    assertEquals(List.of("thorns", "armored"), EliteData.decode(" thorns, ,armored,"));
    assertTrue(EliteData.decode("").isEmpty());
    assertTrue(EliteData.decode(null).isEmpty());
  }
}
