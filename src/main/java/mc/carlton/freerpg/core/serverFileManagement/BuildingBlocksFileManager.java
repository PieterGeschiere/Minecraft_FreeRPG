package mc.carlton.freerpg.core.serverFileManagement;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import mc.carlton.freerpg.FreeRPG;
import mc.carlton.freerpg.config.ConfigLoad;
import mc.carlton.freerpg.skills.building.BuildingBlockTracker;
import org.apache.logging.log4j.Level;
import org.bukkit.plugin.Plugin;

/** Loads and saves ServerData/buildingBlocks.dat (who placed which block, for the Building skill). */
public class BuildingBlocksFileManager {

  private static File file() {
    Plugin plugin = FreeRPG.getPlugin(FreeRPG.class);
    return new File(new File(plugin.getDataFolder(), "ServerData"), "buildingBlocks.dat");
  }

  /** Replaces the live tracker with the saved one (or an empty one when there is no file yet). */
  public void load() {
    int max = new ConfigLoad().getBuildingTrackerMaxBlocks();
    File f = file();
    BuildingBlockTracker tracker = new BuildingBlockTracker(max);
    if (f.isFile()) {
      try (DataInputStream in = new DataInputStream(
          new BufferedInputStream(Files.newInputStream(f.toPath())))) {
        tracker = BuildingBlockTracker.read(in, max);
      } catch (IOException e) {
        FreeRPG.log(Level.ERROR, "[FreeRPG] Could not read buildingBlocks.dat, starting empty: "
            + e.getMessage());
      }
    }
    BuildingBlockTracker.setInstance(tracker);
  }

  /** Writes the tracker to a temporary file first so a crash cannot leave a half-written file. */
  public void save() {
    File f = file();
    File tmp = new File(f.getPath() + ".tmp");
    try {
      f.getParentFile().mkdirs();
      try (DataOutputStream out = new DataOutputStream(
          new BufferedOutputStream(Files.newOutputStream(tmp.toPath())))) {
        BuildingBlockTracker.instance().write(out);
      }
      Files.move(tmp.toPath(), f.toPath(), StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      FreeRPG.log(Level.ERROR, "[FreeRPG] Could not save buildingBlocks.dat: " + e.getMessage());
    }
  }
}
