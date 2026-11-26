package mc.carlton.freerpg.worldevents;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

/**
 * Tells the loot a mob would drop anyway from the items it carries. A zombie or skeleton that
 * picked up an item (for example one a player threw next to it) drops it with a certain chance, and
 * that item must never be copied, destroyed or counted as loot: it belongs to whoever lost it.
 */
public final class LootGuard {

  /** The drops of a mob, split into what it carried and the real loot. */
  record Split<T>(List<T> sticky, List<T> loot) {
  }

  private LootGuard() {
  }

  /** A certain drop chance (vanilla uses 2.0 for picked up items) means the mob carried it. */
  static boolean isSticky(float dropChance) {
    return dropChance >= 1.0f;
  }

  /** The worn and held items of the mob that it will certainly drop. */
  static List<ItemStack> stickyEquipment(LivingEntity mob) {
    List<ItemStack> sticky = new ArrayList<>();
    EntityEquipment equipment = mob.getEquipment();
    if (equipment == null) {
      return sticky;
    }
    add(sticky, equipment.getHelmet(), equipment.getHelmetDropChance());
    add(sticky, equipment.getChestplate(), equipment.getChestplateDropChance());
    add(sticky, equipment.getLeggings(), equipment.getLeggingsDropChance());
    add(sticky, equipment.getBoots(), equipment.getBootsDropChance());
    add(sticky, equipment.getItemInMainHand(), equipment.getItemInMainHandDropChance());
    add(sticky, equipment.getItemInOffHand(), equipment.getItemInOffHandDropChance());
    return sticky;
  }

  private static void add(List<ItemStack> sticky, ItemStack item, float dropChance) {
    if (item != null && !item.getType().isAir() && isSticky(dropChance)) {
      sticky.add(item.clone());
    }
  }

  /** Splits item drops: see {@link #splitBy}. Items count as the same when they are similar. */
  static Split<ItemStack> split(List<ItemStack> drops, List<ItemStack> carried) {
    return splitBy(drops, carried, ItemStack::isSimilar);
  }

  /**
   * Splits the drops: each carried item takes out one matching drop (so a mob that carried one
   * sword keeps one sword safe, not all of them). The rest is the loot. The matching rule is a
   * parameter so the logic can be tested without a running server.
   */
  static <T> Split<T> splitBy(List<T> drops, List<T> carried,
      java.util.function.BiPredicate<T, T> same) {
    List<T> pool = new ArrayList<>(carried);
    List<T> sticky = new ArrayList<>();
    List<T> loot = new ArrayList<>();
    for (T drop : drops) {
      int match = -1;
      for (int i = 0; i < pool.size(); i++) {
        if (same.test(pool.get(i), drop)) {
          match = i;
          break;
        }
      }
      if (match >= 0) {
        pool.remove(match);
        sticky.add(drop);
      } else {
        loot.add(drop);
      }
    }
    return new Split<>(sticky, loot);
  }
}
