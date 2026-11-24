package mc.carlton.freerpg.skills.building;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Remembers which player placed which block, so Weatherproof, Fortified Blocks and Demolition
 * Expert only ever apply to a player's own blocks. Owners are stored as small indexes to keep
 * millions of blocks affordable. Not tied to Bukkit, so it can be tested without a server.
 */
public class BuildingBlockTracker {

  private static final int FORMAT_VERSION = 1;
  private static BuildingBlockTracker instance =
      new BuildingBlockTracker(BuildingConfig.DEFAULT_TRACKER_MAX_BLOCKS);

  private final int maxBlocks;
  private final Map<UUID, Map<Long, Integer>> blocksByWorld = new HashMap<>();
  private final List<UUID> owners = new ArrayList<>();
  private final Map<UUID, Integer> ownerIndexes = new HashMap<>();
  private int size;
  private boolean fullWarningPending = true;

  public BuildingBlockTracker(int maxBlocks) {
    this.maxBlocks = maxBlocks;
  }

  public static synchronized BuildingBlockTracker instance() {
    return instance;
  }

  public static synchronized void setInstance(BuildingBlockTracker tracker) {
    instance = tracker;
  }

  /** Records the owner of a block. Returns false (and records nothing) when the tracker is full. */
  public synchronized boolean track(UUID world, int x, int y, int z, UUID owner) {
    Map<Long, Integer> blocks = blocksByWorld.computeIfAbsent(world, w -> new HashMap<>());
    long key = BuildingBlockKey.pack(x, y, z);
    boolean isNew = !blocks.containsKey(key);
    if (isNew && size >= maxBlocks) {
      return false;
    }
    blocks.put(key, ownerIndex(owner));
    if (isNew) {
      size++;
    }
    return true;
  }

  /** The owner of the block, or null when it is not tracked. */
  public synchronized UUID ownerOf(UUID world, int x, int y, int z) {
    Map<Long, Integer> blocks = blocksByWorld.get(world);
    if (blocks == null) {
      return null;
    }
    Integer index = blocks.get(BuildingBlockKey.pack(x, y, z));
    return index == null ? null : owners.get(index);
  }

  public synchronized void untrack(UUID world, int x, int y, int z) {
    Map<Long, Integer> blocks = blocksByWorld.get(world);
    if (blocks != null && blocks.remove(BuildingBlockKey.pack(x, y, z)) != null) {
      size--;
    }
  }

  public synchronized int size() {
    return size;
  }

  public synchronized boolean isFull() {
    return size >= maxBlocks;
  }

  /** True exactly once after the tracker first became full, so the caller can log one warning. */
  public synchronized boolean consumeFullWarning() {
    if (isFull() && fullWarningPending) {
      fullWarningPending = false;
      return true;
    }
    return false;
  }

  private int ownerIndex(UUID owner) {
    Integer index = ownerIndexes.get(owner);
    if (index == null) {
      index = owners.size();
      owners.add(owner);
      ownerIndexes.put(owner, index);
    }
    return index;
  }

  public synchronized void write(DataOutputStream out) throws IOException {
    out.writeInt(FORMAT_VERSION);
    out.writeInt(owners.size());
    for (UUID owner : owners) {
      out.writeLong(owner.getMostSignificantBits());
      out.writeLong(owner.getLeastSignificantBits());
    }
    out.writeInt(blocksByWorld.size());
    for (Map.Entry<UUID, Map<Long, Integer>> world : blocksByWorld.entrySet()) {
      out.writeLong(world.getKey().getMostSignificantBits());
      out.writeLong(world.getKey().getLeastSignificantBits());
      out.writeInt(world.getValue().size());
      for (Map.Entry<Long, Integer> block : world.getValue().entrySet()) {
        out.writeLong(block.getKey());
        out.writeInt(block.getValue());
      }
    }
  }

  public static BuildingBlockTracker read(DataInputStream in, int maxBlocks) throws IOException {
    BuildingBlockTracker tracker = new BuildingBlockTracker(maxBlocks);
    int version = in.readInt();
    if (version != FORMAT_VERSION) {
      throw new IOException("Unknown buildingBlocks.dat version " + version);
    }
    int ownerCount = in.readInt();
    for (int i = 0; i < ownerCount; i++) {
      tracker.ownerIndex(new UUID(in.readLong(), in.readLong()));
    }
    int worldCount = in.readInt();
    for (int w = 0; w < worldCount; w++) {
      UUID world = new UUID(in.readLong(), in.readLong());
      int blockCount = in.readInt();
      Map<Long, Integer> blocks = tracker.blocksByWorld.computeIfAbsent(world, k -> new HashMap<>());
      for (int b = 0; b < blockCount; b++) {
        long key = in.readLong();
        int ownerIndex = in.readInt();
        if (ownerIndex >= 0 && ownerIndex < tracker.owners.size() && tracker.size < maxBlocks) {
          if (blocks.put(key, ownerIndex) == null) {
            tracker.size++;
          }
        }
      }
    }
    return tracker;
  }
}
