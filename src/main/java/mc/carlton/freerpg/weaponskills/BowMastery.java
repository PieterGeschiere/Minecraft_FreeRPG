package mc.carlton.freerpg.weaponskills;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.SpectralArrow;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

/**
 * Bow Mastery: experience for hitting and killing with a bow (more for long shots), a skill tree
 * like the main skills, and the Arrow Storm ability. Left-click a bow to ready Arrow Storm, then
 * your next shots each fire a fan of extra arrows.
 */
public class BowMastery implements Listener {

  private static final WeaponSkill SKILL = WeaponSkill.BOW;

  // Passives (invest passive tokens)
  public static final String STORM_DURATION = "p1";
  public static final String DRAW_POWER = "p2";
  public static final String SNIPER = "p3";
  // Skills of the tree (levels, 1 skill token per level)
  public static final String EXTRA_ARROWS = "1a";
  public static final String PINNING_SHOT = "1b";
  public static final String VOLLEY_POWER = "2a";
  public static final String MARKED_PREY = "2b";
  public static final String FLAMING_VOLLEY = "3a";
  public static final String FLEET_FOOT = "3b";
  public static final String HAWKEYES_WRATH = "M";

  private static double abilityDouble(String key, double defaultValue) {
    return WeaponSkillConfig.getDouble("bow.ability." + key, defaultValue);
  }

  private static double expValue(String key, double defaultValue) {
    return WeaponSkillConfig.getDouble("bow.exp." + key, defaultValue);
  }

  public static double perk(String key, double defaultValue) {
    return WeaponSkillConfig.getDouble("bow.perks." + key, defaultValue);
  }

  /** Highest level of passive 2 and 3 (passive 1 has no limit, like in the main skills). */
  public static int getPassiveMax(String key) {
    return STORM_DURATION.equals(key) ? Integer.MAX_VALUE
        : WeaponSkillConfig.getInt("bow.passives." + key + ".maxLevel", 100);
  }

  /** Seconds Arrow Storm stays ready, growing with passive 1. */
  public static double getAbilityDuration(WeaponSkillData data) {
    return Math.min(abilityDouble("maxDurationSeconds", 14),
        abilityDouble("baseDurationSeconds", 8)
            + data.getPassive(STORM_DURATION) * abilityDouble("durationPerLevelSeconds", 0.02));
  }

  /** How many shots one activation of Arrow Storm affects. */
  public static int getAbilityCharges() {
    return (int) abilityDouble("maxShots", 3);
  }

  public static int getAbilityCooldown() {
    return (int) abilityDouble("cooldownSeconds", 90);
  }

  /** Arrows fired next to the arrow you shot while Arrow Storm is ready. */
  public static int getExtraArrows(WeaponSkillData data) {
    double arrows = abilityDouble("baseExtraArrows", 2)
        + data.getSkill(EXTRA_ARROWS) * perk("extraArrowsPerLevel", 1)
        + (data.getSkill(HAWKEYES_WRATH) > 0 ? perk("hawkeyesWrathArrows", 2) : 0);
    return (int) Math.min(abilityDouble("maxExtraArrows", 8), arrows);
  }

  /** Damage of an extra arrow as a fraction of a full arrow (1.0 = the same). */
  public static double getVolleyDamage(WeaponSkillData data) {
    return Math.min(1.0, abilityDouble("volleyDamage", 0.7)
        + data.getSkill(VOLLEY_POWER) * perk("volleyPowerPerLevel", 0.06));
  }

  /** Bonus damage of an arrow as a fraction (0.25 = +25%); snipers get more at long range. */
  public static double getDamageBonus(WeaponSkillData data, double distance) {
    double fullDistance = Math.max(1.0, perk("sniperFullDistance", 30));
    return data.getPassive(DRAW_POWER) * perk("drawPowerPerLevel", 0.003)
        + data.getPassive(SNIPER) * perk("sniperPerLevel", 0.004)
        * Math.max(0.0, Math.min(1.0, distance / fullDistance))
        + (data.getSkill(HAWKEYES_WRATH) > 0 ? perk("hawkeyesWrathDamage", 0.25) : 0);
  }

  public static double getPinningSeconds(WeaponSkillData data) {
    return data.getSkill(PINNING_SHOT) * perk("pinningSecondsPerLevel", 1.0);
  }

  public static double getMarkedSeconds(WeaponSkillData data) {
    return data.getSkill(MARKED_PREY) * perk("markedSecondsPerLevel", 2.0);
  }

  /** Exp of one arrow hit. Extra arrows of the storm give less. */
  public static double getHitExp(double finalDamage, double distance, boolean volley) {
    return expValue(volley ? "volleyHitBase" : "hitBase", volley ? 5 : 15)
        + expValue("hitPerDamage", 5) * finalDamage
        + expValue("hitPerBlock", 2) * Math.max(0, Math.min(50, distance));
  }

  private static boolean isHoldingBow(Player p) {
    return SKILL.isWeapon(p.getInventory().getItemInMainHand().getType());
  }

  /** Arrow Storm: the shot of a ready player also fires extra arrows in a fan. */
  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onShoot(EntityShootBowEvent e) {
    if (!WeaponSkillConfig.isEnabled() || !(e.getEntity() instanceof Player)
        || !(e.getProjectile() instanceof AbstractArrow) || e.getBow() == null
        || !SKILL.isWeapon(e.getBow().getType())) {
      return;
    }
    Player p = (Player) e.getEntity();
    if (!WeaponSkillConfig.getBoolean("bow.skillAllowed", true)) {
      return;
    }
    AbstractArrow origin = (AbstractArrow) e.getProjectile();
    ArrowTags.tag(origin, ArrowTags.BOW);
    WeaponSkillData data = WeaponSkillManager.get(p, SKILL);
    if (!data.isAbilityActive() || data.getAbilityCharges() <= 0) {
      return;
    }
    data.setAbilityCharges(data.getAbilityCharges() - 1);
    fireVolley(p, origin, data);
    if (data.getAbilityCharges() <= 0) {
      WeaponSkillRules.finishAbility(p, SKILL, data, true);
    }
  }

  private void fireVolley(Player p, AbstractArrow origin, WeaponSkillData data) {
    int extra = getExtraArrows(data);
    double spread = abilityDouble("spreadDegrees", 7);
    Vector velocity = origin.getVelocity();
    double speed = velocity.length();
    if (speed <= 0) {
      return;
    }
    Vector direction = velocity.clone().normalize();
    Location from = origin.getLocation();
    World world = from.getWorld();
    int fireTicks = data.getSkill(FLAMING_VOLLEY) > 0
        ? (int) (perk("flamingSeconds", 5) * 20) : 0;
    for (int i = 1; i <= extra; i++) {
      double angle = ((i + 1) / 2) * spread * (i % 2 == 1 ? 1 : -1);
      Vector dir = direction.clone().rotateAroundY(Math.toRadians(angle));
      AbstractArrow arrow = spawnCopy(world, from, origin, dir, speed);
      arrow.setShooter(p);
      arrow.setCritical(origin.isCritical());
      arrow.setDamage(origin.getDamage());
      arrow.setPickupStatus(AbstractArrow.PickupStatus.CREATIVE_ONLY);
      if (fireTicks > 0) {
        arrow.setFireTicks(fireTicks);
      }
      ArrowTags.tag(arrow, ArrowTags.BOW_VOLLEY);
    }
    world.playSound(from, Sound.ENTITY_ARROW_SHOOT, 1f, 1.4f);
    world.spawnParticle(Particle.CRIT, from, 12, 0.4, 0.4, 0.4, 0.2);
  }

  /** A new arrow of the same kind as the one shot: tipped arrows keep their potion, spectral stay spectral. */
  private static AbstractArrow spawnCopy(World world, Location from, AbstractArrow origin,
      Vector dir, double speed) {
    if (origin instanceof SpectralArrow) {
      return world.spawn(from, SpectralArrow.class,
          spectral -> spectral.setVelocity(dir.clone().multiply(speed)));
    }
    Arrow arrow = world.spawnArrow(from, dir, (float) speed, 0f);
    if (origin instanceof Arrow) {
      Arrow tipped = (Arrow) origin;
      if (tipped.getBasePotionType() != null) {
        arrow.setBasePotionType(tipped.getBasePotionType());
      }
      for (PotionEffect effect : tipped.getCustomEffects()) {
        arrow.addCustomEffect(effect, true);
      }
      if (tipped.getColor() != null) {
        arrow.setColor(tipped.getColor());
      }
    }
    return arrow;
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onHit(EntityDamageByEntityEvent e) {
    String tag = ArrowTags.get(e.getDamager());
    Player p = ArrowTags.shooter(e.getDamager());
    if (p == null || !ArrowTags.belongsTo(tag, ArrowTags.BOW) || !WeaponSkillConfig.isEnabled()
        || !(e.getEntity() instanceof LivingEntity) || e.getEntity() instanceof ArmorStand) {
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
    boolean volley = ArrowTags.BOW_VOLLEY.equals(tag);
    WeaponSkillData data = WeaponSkillManager.get(p, SKILL);
    double distance = p.getWorld() == target.getWorld()
        ? p.getLocation().distance(target.getLocation()) : 0;

    double multiplier = 1 + getDamageBonus(data, distance);
    if (volley) {
      multiplier *= getVolleyDamage(data);
    }
    e.setDamage(e.getDamage() * multiplier);

    double exp = getHitExp(e.getFinalDamage(), distance, volley);
    WeaponSkillManager.addExp(p, SKILL, exp * WeaponSkillRules.farmMultiplier(target));

    if (data.getSkill(PINNING_SHOT) > 0) {
      target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS,
          (int) (getPinningSeconds(data) * 20), 1));
    }
    if (data.getSkill(MARKED_PREY) > 0) {
      target.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING,
          (int) (getMarkedSeconds(data) * 20), 0));
    }
    if (data.getSkill(FLEET_FOOT) > 0) {
      p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,
          (int) (perk("fleetFootSeconds", 4) * 20), 0));
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
        ArrowTags.BOW)) {
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
    if (!isHoldingBow(p)) {
      return;
    }
    WeaponSkillData data = WeaponSkillManager.get(p, SKILL);
    AbilityActivator.activate(p, SKILL, data,
        e.useItemInHand() == org.bukkit.event.Event.Result.DENY, getAbilityDuration(data),
        getAbilityCharges(), getAbilityCooldown(), "weaponReadyBow",
        Sound.ENTITY_ARROW_HIT_PLAYER);
  }
}
