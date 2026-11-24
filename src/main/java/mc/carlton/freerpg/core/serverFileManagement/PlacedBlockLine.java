package mc.carlton.freerpg.core.serverFileManagement;

import java.util.Optional;

/** One line of blockLocations.dat: {@code world,x,y,z}. */
public final class PlacedBlockLine {

  public record Parsed(String world, int x, int y, int z) {

  }

  private PlacedBlockLine() {
  }

  public static String format(String world, int x, int y, int z) {
    return world + "," + x + "," + y + "," + z;
  }

  public static Optional<Parsed> parse(String line) {
    if (line == null || line.isBlank()) {
      return Optional.empty();
    }
    String[] parts = line.split(",");
    if (parts.length < 4) {
      return Optional.empty();
    }
    try {
      return Optional.of(new Parsed(parts[0], Integer.parseInt(parts[1]),
          Integer.parseInt(parts[2]), Integer.parseInt(parts[3])));
    } catch (NumberFormatException e) {
      return Optional.empty();
    }
  }
}
