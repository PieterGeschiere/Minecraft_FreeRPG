package mc.carlton.freerpg.worldevents;

import mc.carlton.freerpg.FreeRPG;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataType;

/**
 * Marks on entities that survive a chunk unload and a restart (plain Bukkit metadata is only kept
 * in memory and is lost when the chunk unloads).
 */
final class EntityTags {

  private EntityTags() {
  }

  private static NamespacedKey key(String name) {
    return new NamespacedKey(FreeRPG.getPlugin(FreeRPG.class), name.toLowerCase());
  }

  static boolean has(Entity entity, String name) {
    return entity.getPersistentDataContainer().has(key(name), PersistentDataType.BYTE);
  }

  static void set(Entity entity, String name) {
    entity.getPersistentDataContainer().set(key(name), PersistentDataType.BYTE, (byte) 1);
  }

  static void remove(Entity entity, String name) {
    entity.getPersistentDataContainer().remove(key(name));
  }
}
