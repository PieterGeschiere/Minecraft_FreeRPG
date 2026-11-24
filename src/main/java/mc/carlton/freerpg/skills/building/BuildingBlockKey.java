package mc.carlton.freerpg.skills.building;

/** Packs a block position into one long: x and z take 26 bits each, y takes 12 (offset by 2048). */
public final class BuildingBlockKey {

  private static final long XZ_MASK = 0x3FFFFFFL;
  private static final long Y_MASK = 0xFFFL;
  private static final int Y_OFFSET = 2048;

  private BuildingBlockKey() {
  }

  public static long pack(int x, int y, int z) {
    return ((x & XZ_MASK) << 38) | ((z & XZ_MASK) << 12) | ((y + Y_OFFSET) & Y_MASK);
  }

  public static int unpackX(long key) {
    return (int) (key >> 38) << 6 >> 6;
  }

  public static int unpackZ(long key) {
    return (int) ((key >> 12) & XZ_MASK) << 6 >> 6;
  }

  public static int unpackY(long key) {
    return (int) (key & Y_MASK) - Y_OFFSET;
  }
}
