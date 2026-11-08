package mc.carlton.freerpg.weaponskills;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

/**
 * Trident Mastery: experience for stabbing, throwing and killing with a trident, a skill tree like
 * the main skills, and the Maelstrom ability. Left-click a trident to ready Maelstrom, then your
 * next thrown tridents create a whirlpool that pulls nearby hostile mobs towards the impact.
 */
public class TridentMastery implements Listener {

  private static final WeaponSkill SKILL = WeaponSkill.TRIDENT;

  // Passives (invest passive tokens)
  public static final String MAELSTROM_DURATION = "p1";
  public static final String THROWING_POWER = "p2";
  public static final String TIDAL_MIGHT = "p3";
  // Skills of the tree (levels, 1 skill token per level)
  public static final String WIDE_MAELSTROM = "1a";
  public static final String UNDERTOW = "1b";
  public static final String MAELSTROM_BITE = "2a";
  public static final String BARBED_TIPS = "2b";
  public static final String TIDAL_BURST = "3a";
  public static final String SEA_SHIELD = "3b";
  public static final String POSEIDONS_WRATH = "M";

  private static final Particle.DustOptions WATER_BLUE = new Particle.DustOptions(
      Color.fromRGB(30, 110, 255), 1.8f);
  private static final Particle.DustOptions WATER_LIGHT = new Particle.DustOptions(
      Color.fromRGB(150, 235, 255), 1.4f);

  /** True while the maelstrom damages mobs, so those hits are not handled again. */
  private static boolean inEffect = false;
  private final Map<UUID, Long> lastMessage = new HashMap<>();

  private static double abilityDouble(String key, double defaultValue) {
    return WeaponSkillConfig.getDouble("trident.ability." + key, defaultValue);
  }

  private static double expValue(String key, double defaultValue) {
    return WeaponSkillConfig.getDouble("trident.exp." + key, defaultValue);
  }

  public static double perk(String key, double defaultValue) {
    return WeaponSkillConfig.getDouble("trident.perks." + key, defaultValue);
  }

  /** Highest level of passive 2 and 3 (passive 1 has no limit, like in the main skills). */
  public static int getPassiveMax(String key) {
    return MAELSTROM_DURATION.equals(key) ? Integer.MAX_VALUE
        : WeaponSkillConfig.getInt("trident.passives." + key + ".maxLevel", 100);
  }

  /** Seconds Maelstrom stays ready, growing with passive 1. */
  public static double getAbilityDuration(WeaponSkillData data) {
    return Math.min(abilityDouble("maxDurationSeconds", 14),
        abilityDouble("baseDurationSeconds", 8)
            + data.getPassive(MAELSTROM_DURATION) * abilityDouble("durationPerLevelSeconds", 0.02));
  }

  /** How many thrown tridents one activation of Maelstrom affects. */
  public static int getAbilityCharges() {
    return (int) abilityDouble("maxThrows", 2);
  }

  public static int getAbilityCooldown() {
    return (int) abilityDouble("cooldownSeconds", 90);
  }

  public static double getMaelstromSeconds() {
    return abilityDouble("maelstromSeconds", 3);
  }

  /** Bonus damage of thrown tridents as a fraction (0.25 = +25%). */
  public static double getThrownBonus(WeaponSkillData data, boolean inWaterOrRain) {
    double bonus = data.getPassive(THROWING_POWER) * perk("throwingPowerPerLevel", 0.003)
        + (data.getSkill(POSEIDONS_WRATH) > 0 ? perk("poseidonsWrathDamage", 0.25) : 0);
    if (inWaterOrRain) {
      bonus += data.getPassive(TIDAL_MIGHT) * perk("tidalMightPerLevel", 0.004);
    }
    return bonus;
  }

  public static double getMaelstromRadius(WeaponSkillData data) {
    double radius = abilityDouble("baseRadius", 4.0)
        + data.getSkill(WIDE_MAELSTROM) * perk("wideMaelstromRadiusPerLevel", 0.5);
    radius = data.getSkill(POSEIDONS_WRATH) > 0 ? radius * perk("poseidonsWrathRadius", 1.5)
        : radius;
    return Math.min(abilityDouble("maxRadius", 8.0), radius);
  }

  public static double getPullStrength(WeaponSkillData data) {
    return abilityDouble("pullStrength", 0.35)
        * (1 + data.getSkill(UNDERTOW) * perk("undertowPerLevel", 0.1));
  }

  /** Damage dealt to each pulled mob every half second. */
  public static double getBiteDamage(WeaponSkillData data) {
    return data.getSkill(MAELSTROM_BITE) * perk("biteDamagePerLevel", 0.5);
  }

  public static double getBarbedSeconds(WeaponSkillData data) {
    return data.getSkill(BARBED_TIPS) * perk("barbedSecondsPerLevel", 1.0);
  }

  private static boolean isHoldingTrident(Player p) {
    return SKILL.isWeapon(p.getInventory().getItemInMainHand().getType());
  }

  private static boolean isInWaterOrRain(Player p) {
    if (p.isInWater()) {
      return true;
    }
    World world = p.getWorld();
    return world.hasStorm() && p.getLocation().getBlockY()
        >= world.getHighestBlockYAt(p.getLocation());
  }

  /** The player who threw the trident, or null. */
  private static Player getThrower(Entity entity) {
    if (entity instanceof Trident && ((Trident) entity).getShooter() instanceof Player) {
      return (Player) ((Trident) entity).getShooter();
    }
    return null;
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onHit(EntityDamageByEntityEvent e) {
    if (inEffect || !WeaponSkillConfig.isEnabled() || !(e.getEntity() instanceof LivingEntity)
        || e.getEntity() instanceof ArmorStand) {
      return;
    }
    LivingEntity target = (LivingEntity) e.getEntity();
    Player p = getThrower(e.getDamager());
    boolean thrown = p != null;
    if (!thrown) {
      if (!(e.getDamager() instanceof Player)
          || e.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK
          || !isHoldingTrident((Player) e.getDamager())) {
        return;
      }
      p = (Player) e.getDamager();
    }
    if (!WeaponSkillRules.canHit(p, SKILL, target)) {
      return;
    }
    WeaponSkillData data = WeaponSkillManager.get(p, SKILL);

    double exp;
    if (thrown) {
      double bonus = getThrownBonus(data, isInWaterOrRain(p));
      if (bonus > 0) {
        e.setDamage(e.getDamage() * (1 + bonus));
      }
      double distance = Math.min(50, p.getLocation().distance(target.getLocation()));
      exp = expValue("throwBase", 25) + expValue("throwPerDamage", 5) * e.getFinalDamage()
          + expValue("throwPerBlock", 3) * distance;
      if (data.getSkill(BARBED_TIPS) > 0) {
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS,
            (int) (getBarbedSeconds(data) * 20), 1));
      }
      if (data.getSkill(SEA_SHIELD) > 0) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION,
            (int) (perk("seaShieldSeconds", 8) * 20), 0));
      }
    } else {
      // Weak swings (attack not charged) give less exp, so spam-clicking is not worth it
      double charge = Math.max(0.0, Math.min(1.0, p.getAttackCooldown()));
      exp = expValue("hitBase", 10) * charge
          + expValue("hitPerDamage", 5) * e.getFinalDamage();
    }
    WeaponSkillManager.addExp(p, SKILL, exp * WeaponSkillRules.farmMultiplier(target));
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onKill(EntityDeathEvent e) {
    if (!WeaponSkillConfig.isEnabled() || e.getEntity() instanceof Player) {
      return;
    }
    Player killer = e.getEntity().getKiller();
    if (killer == null) {
      return;
    }
    boolean thrownKill = false;
    EntityDamageEvent cause = e.getEntity().getLastDamageCause();
    if (cause instanceof EntityDamageByEntityEvent) {
      thrownKill = getThrower(((EntityDamageByEntityEvent) cause).getDamager()) != null;
    }
    if (!thrownKill && !isHoldingTrident(killer)) {
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
  }

  /** A thrown trident that lands while Maelstrom is ready creates the whirlpool. */
  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onProjectileHit(ProjectileHitEvent e) {
    Player p = getThrower(e.getEntity());
    if (p == null || !WeaponSkillConfig.isEnabled()) {
      return;
    }
    WeaponSkillData data = WeaponSkillManager.get(p, SKILL);
    if (!data.isAbilityActive() || data.getAbilityCharges() <= 0) {
      return;
    }
    Location center = e.getHitEntity() != null ? e.getHitEntity().getLocation()
        : e.getEntity().getLocation();
    startMaelstrom(p, center.clone().add(0, 0.5, 0), data);
    data.setAbilityCharges(data.getAbilityCharges() - 1);
    if (data.getAbilityCharges() <= 0) {
      WeaponSkillRules.finishAbility(p, SKILL, data, true);
    }
  }

  private void startMaelstrom(Player p, Location center, WeaponSkillData data) {
    double radius = getMaelstromRadius(data);
    double strength = getPullStrength(data);
    double bite = getBiteDamage(data);
    boolean burst = data.getSkill(TIDAL_BURST) > 0;
    int totalTicks = (int) (getMaelstromSeconds() * 20);
    World world = center.getWorld();
    world.playSound(center, Sound.ENTITY_GENERIC_SPLASH, 1.5f, 0.7f);
    world.spawnParticle(Particle.SPLASH, center, 60, radius / 3, 0.3, radius / 3, 0.4);
    new ActionBarMessages(p).sendMessage(ChatColor.AQUA + ">>>Maelstrom<<<");
    Set<UUID> counted = new HashSet<>();

    new BukkitRunnable() {
      private int ticks = 0;

      @Override
      public void run() {
        if (ticks >= totalTicks) {
          if (burst) {
            burst(p, center, radius);
          }
          cancel();
          return;
        }
        drawVortex(world, center, radius, ticks);
        if (ticks % 10 == 0) {
          world.playSound(center, Sound.ENTITY_PLAYER_SWIM, 1.2f, 0.6f);
        }

        int exp = 0;
        for (Entity entity : world.getNearbyEntities(center, radius, radius, radius)) {
          if (!(entity instanceof Enemy) || !(entity instanceof LivingEntity)) {
            continue;
          }
          LivingEntity living = (LivingEntity) entity;
          Vector toCenter = center.toVector().subtract(living.getLocation().toVector());
          if (toCenter.length() > radius) {
            continue;
          }
          world.spawnParticle(Particle.BUBBLE_POP, living.getLocation().add(0, 1, 0), 4, 0.3, 0.5,
              0.3, 0.05);
          if (toCenter.length() > 0.8) {
            living.setVelocity(toCenter.normalize().multiply(strength).setY(0.08));
          }
          if (bite > 0 && ticks % 10 == 0) {
            inEffect = true;
            try {
              living.damage(bite, p);
            } finally {
              inEffect = false;
            }
          }
          if (counted.add(living.getUniqueId())) {
            exp += expValue("maelstromPerEntity", 15);
          }
        }
        if (exp > 0 && p.isOnline()) {
          WeaponSkillManager.addExp(p, SKILL, exp);
        }
        ticks += 2;
      }
    }.runTaskTimer(FreeRPG.getPlugin(FreeRPG.class), 0L, 2L);
  }

  /**
   * Draws the whirlpool: three spiral arms that wind inwards and upwards like a funnel, a ring on
   * the ground that shows the radius, and splashes in the middle.
   */
  private static void drawVortex(World world, Location center, double radius, int ticks) {
    double angle = ticks * 0.35;
    for (int arm = 0; arm < 3; arm++) {
      for (int j = 0; j <= 8; j++) {
        double t = j / 8.0;
        double r = radius * (1 - t * 0.75);
        double a = angle + arm * (Math.PI * 2 / 3) + t * 3.5;
        world.spawnParticle(Particle.DUST, center.getX() + Math.cos(a) * r,
            center.getY() + t * 2.2, center.getZ() + Math.sin(a) * r, 1, 0, 0, 0, 0,
            j % 2 == 0 ? WATER_BLUE : WATER_LIGHT);
      }
    }
    if (ticks % 4 == 0) {
      int points = (int) Math.max(24, radius * 8);
      for (int i = 0; i < points; i++) {
        double a = i * (Math.PI * 2 / points);
        world.spawnParticle(Particle.DUST, center.getX() + Math.cos(a) * radius, center.getY() - 0.4,
            center.getZ() + Math.sin(a) * radius, 1, 0, 0, 0, 0, WATER_LIGHT);
      }
    }
    world.spawnParticle(Particle.SPLASH, center, 12, radius / 4, 0.4, radius / 4, 0.2);
    world.spawnParticle(Particle.BUBBLE_POP, center, 6, radius / 3, 0.6, radius / 3, 0.05);
    if (ticks % 6 == 0) {
      world.spawnParticle(Particle.CLOUD, center, 3, radius / 4, 0.2, radius / 4, 0.02);
    }
  }

  /** Tidal Burst: the maelstrom ends with an explosion of water. */
  private void burst(Player p, Location center, double radius) {
    World world = center.getWorld();
    world.spawnParticle(Particle.EXPLOSION, center, 1);
    world.spawnParticle(Particle.SPLASH, center, 80, radius / 2, 0.5, radius / 2, 0.3);
    world.playSound(center, Sound.ENTITY_PLAYER_SPLASH_HIGH_SPEED, 1.5f, 0.8f);
    double damage = perk("tidalBurstDamage", 6);
    inEffect = true;
    try {
      for (Entity entity : world.getNearbyEntities(center, radius, radius, radius)) {
        if (!(entity instanceof Enemy) || !(entity instanceof LivingEntity)) {
          continue;
        }
        LivingEntity living = (LivingEntity) entity;
        Vector push = living.getLocation().toVector().subtract(center.toVector());
        push.setY(0);
        if (push.lengthSquared() > 0) {
          push.normalize().multiply(0.4);
        }
        living.damage(damage, p);
        living.setVelocity(push.setY(0.8));
      }
    } finally {
      inEffect = false;
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
    if (!isHoldingTrident(p)) {
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
        bar.sendMessage(ChatColor.RED + WeaponSkillRules.text(p, "weaponCooldown",
            "ability", SKILL.getAbilityName(), "seconds", Long.toString(seconds)));
      }
      return;
    }
    double duration = getAbilityDuration(data);
    int cooldown = WeaponSkillRules.adjustCooldown(p, getAbilityCooldown());
    data.setAbilityCharges(getAbilityCharges());
    data.setAbilityCooldownSeconds(cooldown);
    data.setAbilityActiveUntil(now + (long) (duration * 1000));
    data.setAbilityCooldownUntil(now + (long) ((duration + cooldown) * 1000));
    bar.sendMessage(ChatColor.GREEN + WeaponSkillRules.text(p, "weaponReadyTrident",
        "ability", SKILL.getAbilityName(), "seconds",
        Double.toString(Math.round(duration * 10) / 10.0), "count",
        Integer.toString(getAbilityCharges())));
    p.playSound(p.getLocation(), Sound.AMBIENT_UNDERWATER_ENTER, 1f, 1f);
    UUID uuid = p.getUniqueId();
    Bukkit.getScheduler().runTaskLater(FreeRPG.getPlugin(FreeRPG.class), () -> {
      Player online = Bukkit.getPlayer(uuid);
      if (online != null && !data.isAbilityActive() && data.getAbilityCharges() > 0) {
        WeaponSkillRules.finishAbility(online, SKILL, data, true);
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
