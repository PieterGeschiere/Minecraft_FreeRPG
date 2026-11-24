package mc.carlton.freerpg.elites;

import java.util.List;
import java.util.Random;
import org.bukkit.Material;
import org.bukkit.entity.AbstractSkeleton;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

/** Random, damaged gear for Blood Moon mobs. It almost never drops, so it is no loot source. */
public final class BloodMoonGear {

  /** The chance that a piece of Blood Moon gear drops when the mob dies. */
  static final float DROP_CHANCE = 0.02f;

  private BloodMoonGear() {
  }

  /** How much durability is left on a new piece: 40% to 90%. */
  static double leftFraction(Random random) {
    return 0.4 + random.nextDouble() * 0.5;
  }

  /** The damage value that leaves this fraction of the durability. */
  static int damageFor(int maxDurability, double leftFraction) {
    if (maxDurability <= 0) {
      return 0;
    }
    double left = Math.max(0.0, Math.min(1.0, leftFraction));
    return (int) Math.round(maxDurability * (1.0 - left));
  }

  /** Whether the mob wears gear that players can see (zombies and skeletons). */
  static boolean canWearGear(LivingEntity mob) {
    return mob instanceof Zombie || mob instanceof AbstractSkeleton;
  }

  /** Fills the empty armor slots, and the hand of zombies, by chance. */
  public static void equip(LivingEntity mob, Random random) {
    EntityEquipment equipment = mob.getEquipment();
    if (equipment == null || !canWearGear(mob)) {
      return;
    }
    fill(equipment, EquipmentSlot.HEAD, "helmet", random);
    fill(equipment, EquipmentSlot.CHEST, "chestplate", random);
    fill(equipment, EquipmentSlot.LEGS, "leggings", random);
    fill(equipment, EquipmentSlot.FEET, "boots", random);
    if (mob instanceof Zombie) {
      fill(equipment, EquipmentSlot.HAND, "weapon", random);
    }
  }

  private static void fill(EntityEquipment equipment, EquipmentSlot slot, String table,
      Random random) {
    ItemStack current = equipment.getItem(slot);
    if (current != null && current.getType() != Material.AIR) {
      return;
    }
    if (random.nextDouble() >= EliteConfig.gearChance(table)) {
      return;
    }
    List<Material> materials = EliteConfig.gearTable(table);
    if (materials.isEmpty()) {
      return;
    }
    Material material = materials.get(random.nextInt(materials.size()));
    ItemStack item = new ItemStack(material);
    ItemMeta meta = item.getItemMeta();
    if (meta instanceof Damageable damageable) {
      damageable.setDamage(damageFor(material.getMaxDurability(), leftFraction(random)));
      item.setItemMeta(meta);
    }
    equipment.setItem(slot, item);
    setDropChance(equipment, slot);
  }

  /** Spigot has one method per slot (the slot based one only exists on Paper). */
  private static void setDropChance(EntityEquipment equipment, EquipmentSlot slot) {
    switch (slot) {
      case HEAD -> equipment.setHelmetDropChance(DROP_CHANCE);
      case CHEST -> equipment.setChestplateDropChance(DROP_CHANCE);
      case LEGS -> equipment.setLeggingsDropChance(DROP_CHANCE);
      case FEET -> equipment.setBootsDropChance(DROP_CHANCE);
      default -> equipment.setItemInMainHandDropChance(DROP_CHANCE);
    }
  }
}
