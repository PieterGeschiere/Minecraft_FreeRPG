package mc.carlton.freerpg.quests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class BonusSpinsTest {

  @TempDir
  File folder;

  private File file() {
    return new File(folder, "bonus-spins.yml");
  }

  @Test
  void spinsAreAddedAndUsedUpOneByOne() {
    BonusSpins spins = new BonusSpins(file());
    UUID player = UUID.randomUUID();
    spins.add(player, 2);
    assertEquals(2, spins.count(player));
    assertTrue(spins.consume(player));
    assertTrue(spins.consume(player));
    assertFalse(spins.consume(player));
    assertEquals(0, spins.count(player));
  }

  @Test
  void spinsSurviveARestart() {
    UUID player = UUID.randomUUID();
    new BonusSpins(file()).add(player, 3);
    assertEquals(3, new BonusSpins(file()).count(player));
  }

  @Test
  void zeroOrNegativeAmountsChangeNothing() {
    BonusSpins spins = new BonusSpins(file());
    UUID player = UUID.randomUUID();
    spins.add(player, 0);
    spins.add(player, -3);
    assertEquals(0, spins.count(player));
    spins.add(player, 1);
    spins.add(player, -5);
    assertEquals(1, spins.count(player));
  }

  @Test
  void aBrokenFileMeansNoSpinsAndDoesNotThrow() throws Exception {
    Files.writeString(file().toPath(), "not: [valid");
    BonusSpins spins = new BonusSpins(file());
    assertEquals(0, spins.count(UUID.randomUUID()));
    UUID player = UUID.randomUUID();
    spins.add(player, 1);
    assertEquals(1, new BonusSpins(file()).count(player));
  }

  @Test
  void eachPlayerHasHisOwnSpins() {
    BonusSpins spins = new BonusSpins(file());
    UUID a = UUID.randomUUID();
    UUID b = UUID.randomUUID();
    spins.add(a, 1);
    assertFalse(spins.consume(b));
    assertTrue(spins.consume(a));
  }
}
