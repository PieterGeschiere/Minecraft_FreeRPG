package mc.carlton.freerpg.weaponskills;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.AbstractArrow;
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
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

/**
 * Crossbow Mastery: experience for hitting and killing with a crossbow, a skill tree like the main
 * skills, and the Blast Bolt ability. Left-click a crossbow to ready Blast Bolt, then your next
 * bolts explode where they land and hurt the hostile mobs around the impact.
 */
public class CrossbowMastery implements Listener {

  private static final WeaponSkill SKILL = WeaponSkill.CROSSBOW;

  // Passives (invest passive tokens)
  public static final String BLAST_DURATION = "p1";
  public static final String BOLT_POWER = "p2";
  public static final String POINT_BLANK = "p3";
  // Skills of the tree (levels, 1 skill token per level)
  public static final String WIDE_BLAST = "1a";
  public static final String CONCUSSION = "1b";
  public static final String HEAVY_CHARGE = "2a";
  public static final String REND = "2b";
  public static final String SHRAPNEL = "3a";
  public static final String RECOIL_BOOST = "3b";
  public static final String SIEGE_WRATH = "M";

  /** True while the blast damages mobs, so those hits are not handled again. */
  private static boolean inEffect = false;
  /** Bolts that already blasted. */
  private static final java.util.Set<java.util.UUID> BLASTED = new java.util.HashSet<>();

  private static double abilityDouble(String key, double defaultValue) {
    return WeaponSkillConfig.getDouble("crossbow.ability." + key, defaultValue);
  }

  private static double expValue(String key, double defaultValue) {
    return WeaponSkillConfig.getDouble("crossbow.exp." + key, defaultValue);
  }

  public static double perk(String key, double defaultValue) {
    return WeaponSkillConfig.getDouble("crossbow.perks." + key, defaultValue);
  }

  /** Highest level of passive 2 and 3 (passive 1 has no limit, like in the main skills). */
  public static int getPassiveMax(String key) {
    return BLAST_DURATION.equals(key) ? Integer.MAX_VALUE
        : WeaponSkillConfig.getInt("crossbow.passives." + key + ".maxLevel", 100);
  }

  /** Seconds Blast Bolt stays ready, growing with passive 1. */
  public static double getAbilityDuration(WeaponSkillData data) {
    return Math.min(abilityDouble("maxDurationSeconds", 12),
        abilityDouble("baseDurationSeconds", 6)
            + data.getPassive(BLAST_DURATION) * abilityDouble("durationPerLevelSeconds", 0.02));
  }

  /** How many bolts one activation of Blast Bolt affects. */
  public static int getAbilityCharges() {
    return (int) abilityDouble("maxBolts", 3);
  }

  public static int getAbilityCooldown() {
    return (int) abilityDouble("cooldownSeconds", 100);
  }

  public static double getBlastRadius(WeaponSkillData data) {
    double radius = abilityDouble("baseRadius", 3.0)
        + data.getSkill(WIDE_BLAST) * perk("wideBlastRadiusPerLevel", 0.5);
    radius = data.getSkill(SIEGE_WRATH) > 0 ? radius * perk("siegeWrathRadius", 1.5) : radius;
    return Math.min(abilityDouble("maxRadius", 7.0), radius);
  }

  /** Damage the blast deals to every mob around the impact. */
  public static double getBlastDamage(WeaponSkillData data) {
    return abilityDouble("baseDamage", 4.0)
        + data.getSkill(HEAVY_CHARGE) * perk("heavyChargePerLevel", 1.0)
        + (data.getSkill(SHRAPNEL) > 0 ? perk("shrapnelDamage", 3) : 0);
  }

  public static double getConcussionSeconds(WeaponSkillData data) {
    return data.getSkill(CONCUSSION) * perk("concussionSecondsPerLevel", 1.0);
  }

  public static double getRendSeconds(WeaponSkillData data) {
    return data.getSkill(REND) * perk("rendSecondsPerLevel", 2.0);
  }

  /** Bonus damage of a bolt as a fraction (0.25 = +25%); point blank counts within a few blocks. */
  public static double getDamageBonus(WeaponSkillData data, double distance) {
    double bonus = data.getPassive(BOLT_POWER) * perk("boltPowerPerLevel", 0.003)
        + (data.getSkill(SIEGE_WRATH) > 0 ? perk("siegeWrathDamage", 0.25) : 0);
    if (distance <= perk("pointBlankDistance", 8)) {
      bonus += data.getPassive(POINT_BLANK) * perk("pointBlankPerLevel", 0.005);
    }
    return bonus;
  }

  /** Exp of one bolt hit. */
  public static double getHitExp(double finalDamage, double distance) {
    return expValue("hitBase", 15) + expValue("hitPerDamage", 5) * finalDamage
        + expValue("hitPerBlock", 2) * Math.max(0, Math.min(50, distance));
  }

  private static boolean isHoldingCrossbow(Player p) {
    return SKILL.isWeapon(p.getInventory().getItemInMainHand().getType());
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onShoot(EntityShootBowEvent e) {
    if (!WeaponSkillConfig.isEnabled() || !(e.getEntity() instanceof Player)
        || !(e.getProjectile() instanceof AbstractArrow) || e.getBow() == null
        || !SKILL.isWeapon(e.getBow().getType())
        || !WeaponSkillConfig.getBoolean("crossbow.skillAllowed", true)) {
      return;
    }
    Player p = (Player) e.getEntity();
    WeaponSkillData data = WeaponSkillManager.get(p, SKILL);
    if (data.isAbilityActive() && data.getAbilityCharges() > 0) {
      ArrowTags.tag(e.getProjectile(), ArrowTags.CROSSBOW_BLAST);
      ArrowTrail.start((AbstractArrow) e.getProjectile(), SKILL, data, false);
      data.setAbilityCharges(data.getAbilityCharges() - 1);
      if (data.getAbilityCharges() <= 0) {
        WeaponSkillRules.finishAbility(p, SKILL, data, true);
      }
    } else {
      ArrowTags.tag(e.getProjectile(), ArrowTags.CROSSBOW);
    }
    if (data.getSkill(RECOIL_BOOST) > 0) {
      p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,
          (int) (perk("recoilSeconds", 4) * 20), 0));
      AbilityEffects.perk(p.getLocation(), "recoil");
    }
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onHit(EntityDamageByEntityEvent e) {
    Player p = ArrowTags.shooter(e.getDamager());
    if (inEffect || p == null
        || !ArrowTags.belongsTo(ArrowTags.get(e.getDamager()), ArrowTags.CROSSBOW)
        || !WeaponSkillConfig.isEnabled() || !(e.getEntity() instanceof LivingEntity)
        || e.getEntity() instanceof ArmorStand) {
      return;
    }
    LivingEntity target = (LivingEntity) e.getEntity();
    if (!(target instanceof org.bukkit.entity.Enemy) && !(target instanceof org.bukkit.entity.Animals)
        && !(target instanceof Player)) {
      return; // villagers, golems and other friendly mobs give no exp and get no effects
    }
    if (!WeaponSkillRules.canHit(p, SKILL, target)) {
      return;
    }
    WeaponSkillData data = WeaponSkillManager.get(p, SKILL);
    double distance = p.getWorld() == target.getWorld()
        ? p.getLocation().distance(target.getLocation()) : 0;
    double bonus = getDamageBonus(data, distance);
    if (bonus > 0) {
      e.setDamage(e.getDamage() * (1 + bonus));
    }
    WeaponSkillManager.addExp(p, SKILL, getHitExp(e.getFinalDamage(), distance)
        * WeaponSkillRules.farmMultiplier(target));
  }

  /** A Blast Bolt that lands explodes: no block damage, only the hostile mobs around it. */
  @EventHandler(priority = EventPriority.MONITOR)
  public void onImpact(ProjectileHitEvent e) {
    if (!ArrowTags.CROSSBOW_BLAST.equals(ArrowTags.get(e.getEntity()))
        || !WeaponSkillConfig.isEnabled()) {
      return;
    }
    Player p = ArrowTags.shooter(e.getEntity());
    if (p == null) {
      return;
    }
    // Only blast once, even if the bolt pierces
    if (BLASTED.size() > 1000) {
      BLASTED.clear();
    }
    if (!BLASTED.add(e.getEntity().getUniqueId())) {
      return;
    }
    WeaponSkillData data = WeaponSkillManager.get(p, SKILL);
    Location center = e.getHitEntity() != null ? e.getHitEntity().getLocation()
        : e.getEntity().getLocation();
    blast(p, center.clone().add(0, 0.5, 0), e.getHitEntity(), data);
  }

  private void blast(Player p, Location center, Entity directHit, WeaponSkillData data) {
    double radius = getBlastRadius(data);
    double damage = getBlastDamage(data);
    boolean launch = data.getSkill(SHRAPNEL) > 0;
    World world = center.getWorld();
    world.spawnParticle(Particle.EXPLOSION, center, 1);
    world.spawnParticle(Particle.CRIT, center, 40, radius / 2, 0.4, radius / 2, 0.3);
    world.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 1.2f, 1.3f);
    AbilityEffects.blastImpact(center, radius);

    double hit = 0; // counted with the farm multiplier of every mob
    inEffect = true;
    try {
      for (Entity entity : world.getNearbyEntities(center, radius, radius, radius)) {
        if (entity == directHit || entity == p || !(entity instanceof Enemy)
            || !(entity instanceof LivingEntity)
            || entity.getLocation().distanceSquared(center) > radius * radius) {
          continue;
        }
        LivingEntity living = (LivingEntity) entity;
        living.damage(damage, p);
        AbilityEffects.damageNumber(living.getLocation(), damage, org.bukkit.ChatColor.GOLD);
        if (data.getSkill(CONCUSSION) > 0) {
          living.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS,
              (int) (getConcussionSeconds(data) * 20), 1));
          AbilityEffects.perk(living.getLocation(), "concussion");
        }
        if (data.getSkill(REND) > 0) {
          living.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS,
              (int) (getRendSeconds(data) * 20), 0));
          AbilityEffects.perk(living.getLocation(), "rend");
        }
        if (launch) {
          Vector push = living.getLocation().toVector().subtract(center.toVector());
          push.setY(0);
          if (push.lengthSquared() > 0) {
            push.normalize().multiply(0.5);
          }
          living.setVelocity(push.setY(0.7));
          AbilityEffects.perk(living.getLocation(), "shrapnel");
        }
        hit += WeaponSkillRules.farmMultiplier(living);
      }
    } finally {
      inEffect = false;
    }
    if (hit > 0) {
      WeaponSkillManager.addExp(p, SKILL, hit * expValue("blastPerEntity", 20));
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onKill(EntityDeathEvent e) {
    if (!WeaponSkillConfig.isEnabled() || e.getEntity() instanceof Player) {
      return;
    }
    Player killer = e.getEntity().getKiller();
    EntityDamageEvent cause = e.getEntity().getLastDamageCause();
    if (killer == null || !(cause instanceof EntityDamageByEntityEvent)
        || !ArrowTags.belongsTo(ArrowTags.get(((EntityDamageByEntityEvent) cause).getDamager()),
        ArrowTags.CROSSBOW)) {
      return;
    }
    WeaponSkillManager.addExp(killer, SKILL,
        WeaponSkillRules.killReward(killer, e.getEntity()) * expValue("killMultiplier", 1.0));
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
    if (!isHoldingCrossbow(p)) {
      return;
    }
    WeaponSkillData data = WeaponSkillManager.get(p, SKILL);
    AbilityActivator.activate(p, SKILL, data,
        e.useItemInHand() == org.bukkit.event.Event.Result.DENY, getAbilityDuration(data),
        getAbilityCharges(), getAbilityCooldown(), "weaponReadyCrossbow",
        Sound.ITEM_CROSSBOW_LOADING_END);
  }
}
