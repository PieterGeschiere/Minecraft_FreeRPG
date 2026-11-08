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
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
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
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

/**
 * Mace Mastery: experience for hitting, smashing and killing with a mace, a skill tree with three
 * passives (invested with passive tokens) and three skills (unlocked with skill tokens), and the
 * Seismic Slam ability.
 */
public class MaceMastery implements Listener {

  private static final WeaponSkill SKILL = WeaponSkill.MACE;

  // Passives (invest passive tokens)
  public static final String SLAM_DURATION = "p1";
  public static final String SMASH_POWER = "p2";
  public static final String SOFT_LANDING = "p3";
  // Skills of the tree (levels, 1 skill token per level)
  public static final String WIDE_SHOCKWAVE = "1a";
  public static final String STAGGER = "1b";
  public static final String AFTERSHOCK = "2a";
  public static final String RATTLE = "2b";
  public static final String EARTHSHAKER = "3a";
  public static final String ADRENALINE = "3b";
  public static final String TITANS_WRATH = "M";

  /** True while Seismic Slam damages mobs, so those hits are not handled again. */
  private static boolean inShockwave = false;
  private final Map<UUID, Long> lastMessage = new HashMap<>();

  private static double abilityDouble(String key, double defaultValue) {
    return WeaponSkillConfig.getDouble("mace.ability." + key, defaultValue);
  }

  private static double expValue(String key, double defaultValue) {
    return WeaponSkillConfig.getDouble("mace.exp." + key, defaultValue);
  }

  public static double perk(String key, double defaultValue) {
    return WeaponSkillConfig.getDouble("mace.perks." + key, defaultValue);
  }

  /** Highest level of passive 2 and 3 (passive 1 has no limit, like in the main skills). */
  public static int getPassiveMax(String key) {
    return SLAM_DURATION.equals(key) ? Integer.MAX_VALUE
        : WeaponSkillConfig.getInt("mace.passives." + key + ".maxLevel", 100);
  }

  /** Seconds Seismic Slam stays ready, growing with passive 1. */
  public static double getAbilityDuration(WeaponSkillData data) {
    return Math.min(abilityDouble("maxDurationSeconds", 12),
        abilityDouble("baseDurationSeconds", 6)
            + data.getPassive(SLAM_DURATION) * abilityDouble("durationPerLevelSeconds", 0.02));
  }

  /** How many shockwaves one activation of Seismic Slam can create. */
  public static int getAbilityCharges() {
    return (int) abilityDouble("maxShockwaves", 3);
  }

  public static int getAbilityCooldown() {
    return (int) abilityDouble("cooldownSeconds", 120);
  }

  /** Smash damage bonus as a fraction (0.25 = +25%). */
  public static double getSmashBonus(WeaponSkillData data) {
    return data.getPassive(SMASH_POWER) * perk("smashPowerPerLevel", 0.003)
        + (data.getSkill(TITANS_WRATH) > 0 ? perk("titansWrathDamage", 0.25) : 0);
  }

  /** Fraction of fall damage removed while holding a mace. */
  public static double getFallReduction(WeaponSkillData data) {
    return Math.min(0.95, data.getPassive(SOFT_LANDING) * perk("softLandingPerLevel", 0.006));
  }

  public static double getShockwaveRadius(WeaponSkillData data) {
    double radius = abilityDouble("baseRadius", 3.0)
        + data.getSkill(WIDE_SHOCKWAVE) * perk("wideShockwaveRadiusPerLevel", 0.5);
    radius = data.getSkill(TITANS_WRATH) > 0 ? radius * perk("titansWrathRadius", 1.5) : radius;
    return Math.min(abilityDouble("maxRadius", 7.0), radius);
  }

  /** Shockwave damage as a fraction of the smash damage. */
  public static double getShockwaveDamage(WeaponSkillData data) {
    return abilityDouble("damageMultiplier", 0.4)
        + data.getSkill(AFTERSHOCK) * perk("aftershockPerLevel", 0.06)
        + (data.getSkill(EARTHSHAKER) > 0 ? perk("earthshakerDamage", 0.2) : 0);
  }

  public static double getStaggerSeconds(WeaponSkillData data) {
    return data.getSkill(STAGGER) * perk("staggerSecondsPerLevel", 1.0);
  }

  public static double getRattleSeconds(WeaponSkillData data) {
    return data.getSkill(RATTLE) * perk("rattleSecondsPerLevel", 2.0);
  }

  private static boolean isHoldingMace(Player p) {
    return SKILL.isWeapon(p.getInventory().getItemInMainHand().getType());
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onHit(EntityDamageByEntityEvent e) {
    if (inShockwave || !WeaponSkillConfig.isEnabled()) {
      return;
    }
    if (!(e.getDamager() instanceof Player)
        || !(e.getEntity() instanceof LivingEntity)
        || e.getEntity() instanceof ArmorStand
        || e.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
      return;
    }
    Player p = (Player) e.getDamager();
    LivingEntity target = (LivingEntity) e.getEntity();
    if (!isHoldingMace(p) || !WeaponSkillRules.canHit(p, SKILL, target)) {
      return;
    }
    WeaponSkillData data = WeaponSkillManager.get(p, SKILL);
    double fallDistance = p.getFallDistance();
    boolean smash = fallDistance > 1.5 && !p.isOnGround() && !p.isFlying() && !p.isGliding();

    if (smash) {
      double bonus = getSmashBonus(data);
      if (bonus > 0) {
        e.setDamage(e.getDamage() * (1 + bonus));
      }
    }
    double damage = e.getDamage();

    // Weak swings (attack not charged) give less exp, so spam-clicking is not worth it
    double charge = Math.max(0.0, Math.min(1.0, p.getAttackCooldown()));
    double exp = expValue("hitBase", 10) * charge
        + expValue("hitPerDamage", 5) * e.getFinalDamage();
    if (smash) {
      exp += (expValue("smashBase", 150) + expValue("smashPerBlock", 25) * Math.min(fallDistance,
          30)) * charge;
    }
    WeaponSkillManager.addExp(p, SKILL, exp * WeaponSkillRules.farmMultiplier(target));

    if (smash) {
      if (data.getSkill(STAGGER) > 0) {
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS,
            (int) (getStaggerSeconds(data) * 20), 1));
      }
      if (data.getSkill(RATTLE) > 0) {
        target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS,
            (int) (getRattleSeconds(data) * 20), 0));
      }
      if (data.isAbilityActive()) {
        shockwave(p, target, damage, data);
      }
      if (data.getSkill(ADRENALINE) > 0) {
        int ticks = (int) (perk("adrenalineSeconds", 4) * 20);
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, ticks, 0));
        p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, ticks, 0));
      }
    }
  }

  private void shockwave(Player p, LivingEntity target, double smashDamage,
      WeaponSkillData data) {
    double radius = getShockwaveRadius(data);
    boolean earthshaker = data.getSkill(EARTHSHAKER) > 0;
    double multiplier = getShockwaveDamage(data);

    Location center = target.getLocation();
    center.getWorld().spawnParticle(Particle.EXPLOSION, center, 1);
    center.getWorld().spawnParticle(Particle.CLOUD, center, 40, radius / 2, 0.2, radius / 2, 0.05);
    center.getWorld().playSound(center, Sound.ITEM_MACE_SMASH_GROUND_HEAVY, 1.5f, 1f);

    double hit = 0; // counted with the farm multiplier of every mob
    inShockwave = true;
    try {
      for (Entity entity : center.getWorld().getNearbyEntities(center, radius, radius, radius)) {
        if (entity == target || entity == p || !(entity instanceof Enemy)
            || !(entity instanceof LivingEntity)) {
          continue;
        }
        if (entity.getLocation().distanceSquared(center) > radius * radius) {
          continue;
        }
        LivingEntity living = (LivingEntity) entity;
        living.damage(smashDamage * multiplier, p);
        Vector push = entity.getLocation().toVector().subtract(center.toVector());
        push.setY(0);
        if (push.lengthSquared() > 0) {
          push.normalize().multiply(0.6);
        }
        push.setY(earthshaker ? 0.8 : 0.35);
        living.setVelocity(push);
        hit += WeaponSkillRules.farmMultiplier(living);
      }
    } finally {
      inShockwave = false;
    }
    if (hit > 0) {
      WeaponSkillManager.addExp(p, SKILL, hit * expValue("shockwavePerEntity", 20));
    }
    // Every activation only has a few shockwaves, then the ability ends
    data.setAbilityCharges(data.getAbilityCharges() - 1);
    if (data.getAbilityCharges() <= 0) {
      WeaponSkillRules.finishAbility(p, SKILL, data, true);
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onKill(EntityDeathEvent e) {
    if (!WeaponSkillConfig.isEnabled()) {
      return;
    }
    Player killer = e.getEntity().getKiller();
    if (killer == null || e.getEntity() instanceof Player || !isHoldingMace(killer)) {
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

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onFallDamage(EntityDamageEvent e) {
    if (e.getCause() != EntityDamageEvent.DamageCause.FALL || !(e.getEntity() instanceof Player)
        || !WeaponSkillConfig.isEnabled()) {
      return;
    }
    Player p = (Player) e.getEntity();
    if (!isHoldingMace(p)) {
      return;
    }
    double reduction = getFallReduction(WeaponSkillManager.get(p, SKILL));
    if (reduction > 0) {
      e.setDamage(e.getDamage() * (1 - reduction));
    }
  }

  @EventHandler
  public void onInteract(PlayerInteractEvent e) {
    if (e.getHand() != EquipmentSlot.HAND || !WeaponSkillConfig.isEnabled()) {
      return;
    }
    if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) {
      return;
    }
    Player p = e.getPlayer();
    if (!isHoldingMace(p)) {
      return;
    }
    if (e.getClickedBlock() != null && (e.getClickedBlock().getType().isInteractable()
        || new mc.carlton.freerpg.utils.globalVariables.ItemGroups().getActionableBlocks()
        .contains(e.getClickedBlock().getType())) && !p.isSneaking()) {
      return; // opening a chest or door, not activating the ability
    }
    if (!WeaponSkillRules.canUseAbility(p, SKILL,
        e.useItemInHand() == org.bukkit.event.Event.Result.DENY)) {
      return;
    }
    WeaponSkillData data = WeaponSkillManager.get(p, SKILL);
    ActionBarMessages bar = new ActionBarMessages(p);
    long now = System.currentTimeMillis();
    if (!data.isAbilityEnabled()) {
      return;
    }
    if (data.isAbilityActive()) {
      return;
    }
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
    bar.sendMessage(ChatColor.GREEN + WeaponSkillRules.text(p, "weaponReadyMace",
        "ability", SKILL.getAbilityName(), "seconds",
        Double.toString(Math.round(duration * 10) / 10.0), "count",
        Integer.toString(getAbilityCharges())));
    p.playSound(p.getLocation(), Sound.ITEM_MACE_SMASH_AIR, 1f, 1f);
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
