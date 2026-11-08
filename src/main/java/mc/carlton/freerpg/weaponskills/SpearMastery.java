package mc.carlton.freerpg.weaponskills;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import mc.carlton.freerpg.FreeRPG;
import mc.carlton.freerpg.config.ConfigLoad;
import mc.carlton.freerpg.core.info.player.ChangeStats;
import mc.carlton.freerpg.utils.game.ActionBarMessages;
import mc.carlton.freerpg.utils.globalVariables.EntityGroups;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Enemy;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

/**
 * Spear Mastery: experience for hitting and killing with a spear (more for charging in at full
 * sprint or on a mount), a skill tree like the main skills, and the Piercing Rush ability.
 * Sneak + left-click a spear to ready Piercing Rush, then your next spear hits also pierce every
 * hostile mob in a line behind the target.
 */
public class SpearMastery implements Listener {

  private static final WeaponSkill SKILL = WeaponSkill.SPEAR;

  // Passives (invest passive tokens)
  public static final String RUSH_DURATION = "p1";
  public static final String MOMENTUM = "p2";
  public static final String MOUNTED_CHARGE = "p3";
  // Skills of the tree (levels, 1 skill token per level)
  public static final String LONG_REACH = "1a";
  public static final String WIDE_PIERCE = "1b";
  public static final String SKEWER = "2a";
  public static final String HAMSTRING = "2b";
  public static final String PIN = "3a";
  public static final String TRIUMPH = "3b";
  public static final String DRAGOONS_WRATH = "M";

  private static final Particle.DustOptions STEEL = new Particle.DustOptions(
      Color.fromRGB(225, 225, 235), 1.4f);

  /** True while the pierce damages mobs, so those hits are not handled again. */
  private static boolean inEffect = false;
  private final Map<UUID, Long> lastMessage = new HashMap<>();

  private static double abilityDouble(String key, double defaultValue) {
    return WeaponSkillConfig.getDouble("spear.ability." + key, defaultValue);
  }

  private static double expValue(String key, double defaultValue) {
    return WeaponSkillConfig.getDouble("spear.exp." + key, defaultValue);
  }

  public static double perk(String key, double defaultValue) {
    return WeaponSkillConfig.getDouble("spear.perks." + key, defaultValue);
  }

  /** Highest level of passive 2 and 3 (passive 1 has no limit, like in the main skills). */
  public static int getPassiveMax(String key) {
    return RUSH_DURATION.equals(key) ? Integer.MAX_VALUE
        : WeaponSkillConfig.getInt("spear.passives." + key + ".maxLevel", 100);
  }

  /** Seconds Piercing Rush stays ready, growing with passive 1. */
  public static double getAbilityDuration(WeaponSkillData data) {
    return Math.min(abilityDouble("maxDurationSeconds", 14),
        abilityDouble("baseDurationSeconds", 8)
            + data.getPassive(RUSH_DURATION) * abilityDouble("durationPerLevelSeconds", 0.02));
  }

  /** How many hits one activation of Piercing Rush affects. */
  public static int getAbilityCharges() {
    return (int) abilityDouble("maxHits", 3);
  }

  public static int getAbilityCooldown() {
    return (int) abilityDouble("cooldownSeconds", 90);
  }

  /** Damage bonus of a hit as a fraction (0.25 = +25%). */
  public static double getDamageBonus(WeaponSkillData data, boolean sprinting, boolean mounted) {
    double bonus = data.getSkill(DRAGOONS_WRATH) > 0 ? perk("dragoonsWrathDamage", 0.25) : 0;
    if (sprinting) {
      bonus += data.getPassive(MOMENTUM) * perk("momentumPerLevel", 0.003);
    }
    if (mounted) {
      bonus += data.getPassive(MOUNTED_CHARGE) * perk("mountedChargePerLevel", 0.004);
    }
    return bonus;
  }

  /** Length in blocks of the line Piercing Rush pierces behind the target. */
  public static double getPierceLength(WeaponSkillData data) {
    double length = abilityDouble("baseLength", 5.0)
        + data.getSkill(LONG_REACH) * perk("longReachPerLevel", 0.5);
    length = data.getSkill(DRAGOONS_WRATH) > 0 ? length * perk("dragoonsWrathLength", 1.5) : length;
    return Math.min(abilityDouble("maxLength", 10.0), length);
  }

  /** Half the width in blocks of the pierced line. */
  public static double getPierceWidth(WeaponSkillData data) {
    return abilityDouble("baseWidth", 1.0) + data.getSkill(WIDE_PIERCE) * perk("widePierceWidth",
        0.2);
  }

  /** Damage of the pierce as a fraction of the hit that caused it. */
  public static double getPierceDamage(WeaponSkillData data) {
    return abilityDouble("damageMultiplier", 0.5) + data.getSkill(SKEWER) * perk(
        "skewerPerLevel", 0.1);
  }

  public static double getHamstringSeconds(WeaponSkillData data) {
    return data.getSkill(HAMSTRING) * perk("hamstringSecondsPerLevel", 1.0);
  }

  private static boolean isHoldingSpear(Player p) {
    return SKILL.isWeapon(p.getInventory().getItemInMainHand().getType());
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onHit(EntityDamageByEntityEvent e) {
    if (inEffect || !WeaponSkillConfig.isEnabled() || !(e.getDamager() instanceof Player)
        || !(e.getEntity() instanceof LivingEntity) || e.getEntity() instanceof ArmorStand) {
      return;
    }
    Player p = (Player) e.getDamager();
    LivingEntity target = (LivingEntity) e.getEntity();
    if (!isHoldingSpear(p) || !WeaponSkillRules.canHit(p, SKILL, target)) {
      return;
    }
    WeaponSkillData data = WeaponSkillManager.get(p, SKILL);
    boolean sprinting = p.isSprinting();
    boolean mounted = p.isInsideVehicle();

    double bonus = getDamageBonus(data, sprinting, mounted);
    if (bonus > 0) {
      e.setDamage(e.getDamage() * (1 + bonus));
    }
    double damage = e.getDamage();

    double exp = expValue("hitBase", 10) + expValue("hitPerDamage", 5) * e.getFinalDamage();
    if (sprinting) {
      exp += expValue("chargeBonus", 30);
    }
    if (mounted) {
      exp += expValue("mountedBonus", 30);
    }
    WeaponSkillManager.addExp(p, SKILL, exp * WeaponSkillRules.farmMultiplier(target));

    if (data.getSkill(HAMSTRING) > 0) {
      target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS,
          (int) (getHamstringSeconds(data) * 20), 1));
    }
    if (data.isAbilityActive() && data.getAbilityCharges() > 0) {
      pierce(p, target, damage, data);
      data.setAbilityCharges(data.getAbilityCharges() - 1);
      if (data.getAbilityCharges() <= 0) {
        data.setAbilityActiveUntil(0);
        new ActionBarMessages(p).sendMessage(
            ChatColor.RED + ">>>" + SKILL.getAbilityName() + " ended<<<");
      }
    }
  }

  /** Damages every hostile mob in a line behind the target. */
  private void pierce(Player p, LivingEntity target, double hitDamage, WeaponSkillData data) {
    World world = target.getWorld();
    Location start = target.getLocation().add(0, 1, 0);
    Vector direction = target.getLocation().toVector().subtract(p.getLocation().toVector());
    direction.setY(0);
    if (direction.lengthSquared() == 0) {
      return;
    }
    direction.normalize();
    double length = getPierceLength(data);
    double width = getPierceWidth(data);
    double damage = hitDamage * getPierceDamage(data);

    // The line: steel-coloured trail with sparks, ending in a sweep
    for (double d = 0; d <= length; d += 0.4) {
      Location point = start.clone().add(direction.clone().multiply(d));
      world.spawnParticle(Particle.DUST, point, 2, 0.05, 0.05, 0.05, 0, STEEL);
      world.spawnParticle(Particle.CRIT, point, 1, 0.05, 0.05, 0.05, 0.02);
    }
    world.spawnParticle(Particle.SWEEP_ATTACK, start.clone().add(direction.clone().multiply(
        length / 2)), 1);
    world.playSound(start, Sound.ITEM_TRIDENT_HIT, 1.2f, 1.5f);

    LivingEntity last = null;
    double lastDistance = -1;
    int hit = 0;
    inEffect = true;
    try {
      for (Entity entity : world.getNearbyEntities(start, length + width, 3, length + width)) {
        if (entity == target || entity == p || !(entity instanceof Enemy)
            || !(entity instanceof LivingEntity)) {
          continue;
        }
        Vector toEntity = entity.getLocation().toVector().subtract(start.toVector());
        toEntity.setY(0);
        double along = toEntity.dot(direction);
        double sideways = toEntity.clone().subtract(direction.clone().multiply(along)).length();
        if (along < 0 || along > length || sideways > width) {
          continue;
        }
        LivingEntity living = (LivingEntity) entity;
        living.damage(damage, p);
        hit++;
        if (along > lastDistance) {
          lastDistance = along;
          last = living;
        }
      }
    } finally {
      inEffect = false;
    }
    if (last != null && data.getSkill(PIN) > 0) {
      // The mob at the end of the line is pinned in place
      last.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS,
          (int) (perk("pinSeconds", 2) * 20), 6));
      world.spawnParticle(Particle.CRIT, last.getLocation().add(0, 1, 0), 15, 0.3, 0.5, 0.3, 0.1);
    }
    if (hit > 0) {
      WeaponSkillManager.addExp(p, SKILL, hit * expValue("piercePerEntity", 15));
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onKill(EntityDeathEvent e) {
    if (!WeaponSkillConfig.isEnabled() || e.getEntity() instanceof Player) {
      return;
    }
    Player killer = e.getEntity().getKiller();
    if (killer == null || !isHoldingSpear(killer)) {
      return;
    }
    // Reuse the mob values of Swordsmanship: collect what it would give instead of giving it
    double[] reward = {0};
    ChangeStats collector = new ChangeStats(killer) {
      @Override
      public void changeEXP(String skillName, int expChange) {
        reward[0] += expChange;
      }
    };
    new EntityGroups().killEntity(e.getEntity(), "swordsmanship",
        new ConfigLoad().getExpMapForSkill("swordsmanship"), collector);
    WeaponSkillManager.addExp(killer, SKILL, reward[0] * expValue("killMultiplier", 1.0));

    WeaponSkillData data = WeaponSkillManager.get(killer, SKILL);
    if (data.getSkill(TRIUMPH) > 0) {
      killer.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,
          (int) (perk("triumphSeconds", 3) * 20), 1));
    }
  }

  @EventHandler
  public void onInteract(PlayerInteractEvent e) {
    if (e.getHand() != EquipmentSlot.HAND || !WeaponSkillConfig.isEnabled()) {
      return;
    }
    if (e.getAction() != Action.LEFT_CLICK_AIR && e.getAction() != Action.LEFT_CLICK_BLOCK) {
      return;
    }
    Player p = e.getPlayer();
    if (!p.isSneaking() || !isHoldingSpear(p)) {
      return;
    }
    if (!WeaponSkillRules.canUseAbility(p, SKILL,
        e.useItemInHand() == org.bukkit.event.Event.Result.DENY)) {
      return;
    }
    WeaponSkillData data = WeaponSkillManager.get(p, SKILL);
    if (!data.isAbilityEnabled() || data.isAbilityActive()) {
      return;
    }
    ActionBarMessages bar = new ActionBarMessages(p);
    long now = System.currentTimeMillis();
    if (now < data.getAbilityCooldownUntil()) {
      if (canSendMessage(p, now)) {
        long seconds = (data.getAbilityCooldownUntil() - now) / 1000 + 1;
        bar.sendMessage(
            ChatColor.RED + SKILL.getAbilityName() + " is on cooldown for " + seconds + "s");
      }
      return;
    }
    double duration = getAbilityDuration(data);
    int cooldown = WeaponSkillRules.adjustCooldown(p, getAbilityCooldown());
    data.setAbilityCharges(getAbilityCharges());
    data.setAbilityActiveUntil(now + (long) (duration * 1000));
    data.setAbilityCooldownUntil(now + (long) ((duration + cooldown) * 1000));
    bar.sendMessage(ChatColor.GREEN + ">>>" + SKILL.getAbilityName() + " ready: strike within "
        + Math.round(duration * 10) / 10.0 + "s (" + getAbilityCharges() + " hits)<<<");
    p.playSound(p.getLocation(), Sound.ITEM_TRIDENT_RIPTIDE_1, 1f, 1.4f);
    UUID uuid = p.getUniqueId();
    WeaponSkillRules.scheduleReady(p, SKILL, data, (long) ((duration + cooldown) * 20) + 2);
    Bukkit.getScheduler().runTaskLater(FreeRPG.getPlugin(FreeRPG.class), () -> {
      Player online = Bukkit.getPlayer(uuid);
      if (online != null && !data.isAbilityActive() && data.getAbilityCharges() > 0) {
        data.setAbilityCharges(0);
        new ActionBarMessages(online).sendMessage(
            ChatColor.RED + ">>>" + SKILL.getAbilityName() + " ended<<<");
      }
    }, (long) (duration * 20) + 2);
  }

  private boolean canSendMessage(Player p, long now) {
    Long last = lastMessage.get(p.getUniqueId());
    if (last != null && now - last < 3000) {
      return false;
    }
    lastMessage.put(p.getUniqueId(), now);
    return true;
  }
}
