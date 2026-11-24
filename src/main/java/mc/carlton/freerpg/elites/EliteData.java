package mc.carlton.freerpg.elites;

import java.util.ArrayList;
import java.util.List;
import mc.carlton.freerpg.FreeRPG;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

/**
 * What makes a mob an elite is stored in the mob itself (its persistent data), so it survives
 * restarts and chunk unloads.
 */
public final class EliteData {

  private EliteData() {
  }

  private static NamespacedKey key(String name) {
    return new NamespacedKey(FreeRPG.getPlugin(FreeRPG.class), name);
  }

  public static String encode(List<String> ids) {
    return String.join(",", ids);
  }

  public static List<String> decode(String text) {
    List<String> ids = new ArrayList<>();
    if (text == null) {
      return ids;
    }
    for (String part : text.split(",")) {
      String id = part.trim();
      if (!id.isEmpty()) {
        ids.add(id);
      }
    }
    return ids;
  }

  public static boolean isElite(Entity entity) {
    return entity.getPersistentDataContainer().has(key("elite_affixes"),
        PersistentDataType.STRING);
  }

  public static List<String> affixes(Entity entity) {
    PersistentDataContainer data = entity.getPersistentDataContainer();
    return decode(data.get(key("elite_affixes"), PersistentDataType.STRING));
  }

  public static void mark(Entity entity, List<String> affixIds) {
    entity.getPersistentDataContainer().set(key("elite_affixes"), PersistentDataType.STRING,
        encode(affixIds));
  }

  /** A minion made by Splitting or Summoner: never an elite, never gives rewards. */
  public static void markMinion(Entity entity) {
    entity.getPersistentDataContainer().set(key("elite_minion"), PersistentDataType.BYTE,
        (byte) 1);
  }

  public static boolean isMinion(Entity entity) {
    return entity.getPersistentDataContainer().has(key("elite_minion"),
        PersistentDataType.BYTE);
  }
}
