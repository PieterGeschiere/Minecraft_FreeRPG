package mc.carlton.freerpg.weaponskills;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import mc.carlton.freerpg.FreeRPG;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

/**
 * Sounds and visuals of the weapon skills: the burst when an ability is readied, the aura and the
 * boss bar (time and shots left) while it is ready, the puff when it ends, impact rings, floating
 * damage numbers, level-up fanfares and the sounds of the perks. Every part can be switched off in
 * the effects: section of weaponSkills.yml.
 */
final class AbilityEffects {

  /** The boss bar and the repeating task of a player whose ability is ready. */
  private static final class Hud {

    final BukkitTask task;
    final BossBar bar;

    Hud(BukkitTask task, BossBar bar) {
      this.task = task;
      this.bar = bar;
    }
  }

  private static final Map<UUID, Hud> HUDS = new HashMap<>();
  private static final int MAX_DAMAGE_NUMBERS = 40;
  private static int activeDamageNumbers = 0;

  private AbilityEffects() {
  }

  /** Whether a kind of effect is switched on (all are on by default). */
  static boolean on(String key) {
    return WeaponSkillConfig.isEnabled() && WeaponSkillConfig.getBoolean("effects." + key, true);
  }

  static Color color(WeaponSkill skill) {
    switch (skill) {
      case MACE:
        return Color.fromRGB(255, 190, 40);
      case TRIDENT:
        return Color.fromRGB(40, 140, 255);
      case SPEAR:
        return Color.fromRGB(70, 220, 90);
      case BOW:
        return Color.fromRGB(255, 225, 90);
      default:
        return Color.fromRGB(235, 60, 40);
    }
  }

  static BarColor barColor(WeaponSkill skill) {
    switch (skill) {
      case MACE:
        return BarColor.YELLOW;
      case TRIDENT:
        return BarColor.BLUE;
      case SPEAR:
        return BarColor.GREEN;
      case BOW:
        return BarColor.WHITE;
      default:
        return BarColor.RED;
    }
  }

  // ---------------------------------------------------------------- sounds

  static void sound(Location at, Sound sound, float volume, float pitch) {
    if (on("sounds") && at != null && at.getWorld() != null) {
      at.getWorld().playSound(at, sound, volume, pitch);
    }
  }

  static void soundLater(Location at, Sound sound, float volume, float pitch, long delayTicks) {
    if (!on("sounds") || at == null || at.getWorld() == null) {
      return;
    }
    Bukkit.getScheduler().runTaskLater(FreeRPG.getPlugin(FreeRPG.class),
        () -> at.getWorld().playSound(at, sound, volume, pitch), delayTicks);
  }

  /** Sound of a perk that just worked, a different sound and pitch for each one. */
  static void perk(Location at, String id) {
    switch (id) {
      case "stagger":
        sound(at, Sound.ENTITY_IRON_GOLEM_ATTACK, 0.6f, 0.8f);
        break;
      case "rattle":
        sound(at, Sound.BLOCK_BONE_BLOCK_BREAK, 1f, 0.7f);
        break;
      case "adrenaline":
        sound(at, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, 1.6f);
        break;
      case "barbed":
        sound(at, Sound.ENTITY_ARROW_HIT, 1f, 1.3f);
        break;
      case "seashield":
        sound(at, Sound.ENTITY_PLAYER_SPLASH, 0.8f, 1.5f);
        break;
      case "hamstring":
        sound(at, Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK, 0.8f, 1.2f);
        break;
      case "pin":
        sound(at, Sound.ITEM_TRIDENT_HIT, 1f, 0.8f);
        break;
      case "triumph":
        sound(at, Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, 1f, 1.2f);
        break;
      case "pinning":
        sound(at, Sound.BLOCK_POWDER_SNOW_STEP, 1f, 0.8f);
        break;
      case "marked":
        sound(at, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1f, 1.6f);
        break;
      case "fleet":
        sound(at, Sound.ENTITY_PHANTOM_FLAP, 0.6f, 1.8f);
        break;
      case "concussion":
        sound(at, Sound.BLOCK_ANVIL_LAND, 0.4f, 1.6f);
        break;
      case "rend":
        sound(at, Sound.ENTITY_EVOKER_CAST_SPELL, 0.5f, 1.4f);
        break;
      case "recoil":
        sound(at, Sound.ENTITY_FIREWORK_ROCKET_SHOOT, 0.6f, 1.4f);
        break;
      case "shrapnel":
        sound(at, Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, 0.8f, 1.2f);
        break;
      default:
        break;
    }
  }

  /** The level-up fanfare: four notes on a different instrument for every weapon skill. */
  static void levelUp(Player p, WeaponSkill skill) {
    Location at = p.getLocation();
    if (!on("sounds")) {
      p.playSound(at, Sound.BLOCK_NOTE_BLOCK_HARP, 1, 1);
      return;
    }
    Sound instrument;
    switch (skill) {
      case MACE:
        instrument = Sound.BLOCK_NOTE_BLOCK_BASS;
        break;
      case TRIDENT:
        instrument = Sound.BLOCK_NOTE_BLOCK_CHIME;
        break;
      case SPEAR:
        instrument = Sound.BLOCK_NOTE_BLOCK_FLUTE;
        break;
      case BOW:
        instrument = Sound.BLOCK_NOTE_BLOCK_PLING;
        break;
      default:
        instrument = Sound.BLOCK_NOTE_BLOCK_BELL;
        break;
    }
    float[] notes = {0.75f, 0.94f, 1.12f, 1.5f};
    for (int i = 0; i < notes.length; i++) {
      soundLater(at, instrument, 1f, notes[i], i * 3L);
    }
    soundLater(at, Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.2f, 12L);
  }

  /** A chime for a new skill token. */
  static void tokenEarned(Player p) {
    Location at = p.getLocation();
    sound(at, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1f, 1.2f);
    soundLater(at, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.8f, 1.5f, 4L);
  }

  /** Arrow Storm: the extra arrows leave one after the other with a rising pitch. */
  static void volleySound(Location at, int extraArrows) {
    sound(at, Sound.ENTITY_ARROW_SHOOT, 1f, 1.0f);
    for (int i = 1; i <= Math.min(extraArrows, 6); i++) {
      soundLater(at, Sound.ENTITY_ARROW_SHOOT, 0.8f, 1.0f + i * 0.08f, i);
    }
  }

  // ------------------------------------------------------------ activation

  /** Burst, sounds, boss bar and aura for an ability that was just readied. */
  static void activated(Player p, WeaponSkill skill, WeaponSkillData data) {
    Location at = p.getLocation();
    switch (skill) {
      case MACE:
        sound(at, Sound.BLOCK_ANVIL_LAND, 0.6f, 0.6f);
        sound(at, Sound.ENTITY_IRON_GOLEM_ATTACK, 1f, 0.7f);
        break;
      case TRIDENT:
        sound(at, Sound.ITEM_TRIDENT_THUNDER, 0.5f, 1.2f);
        break;
      case SPEAR:
        sound(at, Sound.ENTITY_PLAYER_ATTACK_CRIT, 1f, 1.2f);
        break;
      case BOW:
        for (int i = 0; i < 4; i++) {
          soundLater(at, Sound.ENTITY_PHANTOM_SWOOP, 0.7f, 0.6f + i * 0.2f, i * 2L);
        }
        break;
      default:
        sound(at, Sound.ENTITY_CREEPER_PRIMED, 0.8f, 1.0f);
        soundLater(at, Sound.BLOCK_FIRE_EXTINGUISH, 0.6f, 1.4f, 6L);
        break;
    }
    if (on("particles")) {
      ring(at, 2.4, color(skill), true);
    }
    startHud(p, skill, data);
  }

  /** The puff and soft sound when an ability ends. */
  static void ended(Player p, WeaponSkill skill) {
    Location at = p.getLocation();
    sound(at, Sound.BLOCK_BEACON_DEACTIVATE, 0.6f, 1.5f);
    if (on("particles")) {
      p.getWorld().spawnParticle(Particle.CLOUD, at.clone().add(0, 1, 0), 12, 0.4, 0.5, 0.4, 0.02);
    }
    stopHud(p.getUniqueId());
  }

  // ------------------------------------------------------ boss bar and aura

  private static void stopHud(UUID uuid) {
    Hud hud = HUDS.remove(uuid);
    if (hud != null) {
      hud.task.cancel();
      hud.bar.removeAll();
    }
  }

  private static void startHud(Player p, WeaponSkill skill, WeaponSkillData data) {
    boolean showBar = on("hud");
    boolean showAura = on("particles");
    UUID uuid = p.getUniqueId();
    stopHud(uuid);
    if (!showBar && !showAura) {
      return;
    }
    double total = Math.max(1, data.getAbilityActiveUntil() - System.currentTimeMillis());
    BossBar bar = showBar ? Bukkit.createBossBar("", barColor(skill), BarStyle.SOLID) : null;
    if (bar != null) {
      bar.addPlayer(p);
    }
    BukkitTask task = new BukkitRunnable() {
      private int tick = 0;

      @Override
      public void run() {
        Player online = Bukkit.getPlayer(uuid);
        if (online == null || !data.isAbilityActive() || data.getAbilityCharges() <= 0) {
          Hud current = HUDS.get(uuid);
          if (current != null && current.bar == bar) {
            HUDS.remove(uuid);
          }
          if (bar != null) {
            bar.removeAll();
          }
          cancel();
          return;
        }
        long left = Math.max(0, data.getAbilityActiveUntil() - System.currentTimeMillis());
        double fraction = Math.max(0.0, Math.min(1.0, left / total));
        if (bar != null) {
          bar.setProgress(fraction);
          bar.setTitle(title(skill, data.getAbilityCharges(), left));
        }
        if (showAura) {
          aura(online, skill, tick, fraction);
        }
        tick += 2;
      }
    }.runTaskTimer(FreeRPG.getPlugin(FreeRPG.class), 0L, 2L);
    HUDS.put(uuid, new Hud(task, bar));
  }

  /** The text of the boss bar: ability name, one dot for every shot left and the seconds left. */
  static String title(WeaponSkill skill, int charges, long millisLeft) {
    return skill.getColor() + "" + ChatColor.BOLD + skill.getAbilityName() + ChatColor.RESET
        + "  " + ChatColor.WHITE + "●".repeat(Math.max(0, charges)) + "  " + ChatColor.GRAY
        + String.format(Locale.ROOT, "%.1fs", millisLeft / 1000.0);
  }

  /** Orbs circling the player and a few sparks of the weapon, fewer as the time runs out. */
  private static void aura(Player p, WeaponSkill skill, int tick, double fraction) {
    World world = p.getWorld();
    Location base = p.getLocation();
    Particle.DustOptions dust = new Particle.DustOptions(color(skill), 1.2f);
    int orbs = fraction > 0.3 ? 3 : 2;
    double angle = tick * 0.30;
    for (int i = 0; i < orbs; i++) {
      double a = angle + i * (Math.PI * 2 / orbs);
      double y = 0.9 + Math.sin(tick * 0.15 + i) * 0.5;
      world.spawnParticle(Particle.DUST, base.clone().add(Math.cos(a) * 0.85, y, Math.sin(a) * 0.85),
          1, 0, 0, 0, 0, dust);
    }
    Location chest = base.clone().add(0, 1.0, 0);
    switch (skill) {
      case MACE:
        if (tick % 6 == 0) {
          world.spawnParticle(Particle.CRIT, base.clone().add(0, 0.1, 0), 4, 0.5, 0.05, 0.5, 0.05);
        }
        break;
      case TRIDENT:
        world.spawnParticle(Particle.BUBBLE_POP, chest, 2, 0.5, 0.7, 0.5, 0.01);
        break;
      case SPEAR:
        if (tick % 4 == 0) {
          world.spawnParticle(Particle.END_ROD, chest, 1, 0.4, 0.6, 0.4, 0.01);
        }
        break;
      case BOW:
        if (tick % 4 == 0) {
          world.spawnParticle(Particle.CRIT, chest, 2, 0.4, 0.6, 0.4, 0.05);
        }
        break;
      default:
        if (tick % 4 == 0) {
          world.spawnParticle(Particle.FLAME, chest, 1, 0.3, 0.6, 0.3, 0.01);
        }
        break;
    }
  }

  // ------------------------------------------------ rings and damage numbers

  /**
   * A ring on the ground. When it expands it grows from the middle to the radius in a few ticks,
   * otherwise it appears at once.
   */
  static void ring(Location center, double radius, Color color, boolean expanding) {
    if (center == null || center.getWorld() == null) {
      return;
    }
    World world = center.getWorld();
    Location ground = center.clone().add(0, 0.15, 0);
    Particle.DustOptions dust = new Particle.DustOptions(color, 1.4f);
    if (!expanding) {
      circle(world, ground, radius, dust);
      return;
    }
    new BukkitRunnable() {
      private int step = 1;

      @Override
      public void run() {
        circle(world, ground, radius * step / 5.0, dust);
        if (step++ >= 5) {
          cancel();
        }
      }
    }.runTaskTimer(FreeRPG.getPlugin(FreeRPG.class), 0L, 1L);
  }

  /** The ring where an attack landed (Blast Bolt, Piercing Rush), if impact rings are on. */
  static void impactRing(Location center, double radius, Color color) {
    if (on("impactRings")) {
      ring(center, radius, color, true);
    }
  }

  private static void circle(World world, Location center, double radius,
      Particle.DustOptions dust) {
    int points = (int) Math.max(16, radius * 10);
    for (int i = 0; i < points; i++) {
      double a = i * (Math.PI * 2 / points);
      world.spawnParticle(Particle.DUST, center.getX() + Math.cos(a) * radius, center.getY(),
          center.getZ() + Math.sin(a) * radius, 1, 0, 0, 0, 0, dust);
    }
  }

  /** The text of a damage number: 6.0 shows as -6, 4.5 as -4.5. */
  static String damageText(double damage) {
    double rounded = Math.round(damage * 10) / 10.0;
    return "-" + (rounded == Math.rint(rounded) ? String.valueOf((int) rounded)
        : String.valueOf(rounded));
  }

  /** A damage number that floats up for a second and disappears. */
  static void damageNumber(Location at, double damage, ChatColor color) {
    if (!on("damageNumbers") || damage <= 0 || at == null || at.getWorld() == null
        || activeDamageNumbers >= MAX_DAMAGE_NUMBERS) {
      return;
    }
    ThreadLocalRandom random = ThreadLocalRandom.current();
    Location start = at.clone().add((random.nextDouble() - 0.5) * 0.6, 1.4,
        (random.nextDouble() - 0.5) * 0.6);
    TextDisplay display = at.getWorld().spawn(start, TextDisplay.class, text -> {
      text.setText(color + "" + ChatColor.BOLD + damageText(damage));
      text.setBillboard(Display.Billboard.CENTER);
      text.setPersistent(false);
      text.setShadowed(true);
      text.setBackgroundColor(Color.fromARGB(0, 0, 0, 0));
    });
    activeDamageNumbers++;
    new BukkitRunnable() {
      private int ticks = 0;

      @Override
      public void run() {
        if (!display.isValid() || ticks >= 20) {
          display.remove();
          activeDamageNumbers = Math.max(0, activeDamageNumbers - 1);
          cancel();
          return;
        }
        display.teleport(display.getLocation().add(0, 0.05, 0));
        ticks++;
      }
    }.runTaskTimer(FreeRPG.getPlugin(FreeRPG.class), 1L, 1L);
  }

  /** Blast Bolt landing: deeper boom, a fuse crack and the ring of the explosion. */
  static void blastImpact(Location center, double radius) {
    sound(center, Sound.ENTITY_GENERIC_EXPLODE, 1.2f, 0.8f);
    sound(center, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1f, 0.7f);
    impactRing(center, radius, color(WeaponSkill.CROSSBOW));
  }
}
