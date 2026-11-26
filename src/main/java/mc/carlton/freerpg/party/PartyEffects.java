package mc.carlton.freerpg.party;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

/**
 * Small sounds for what happens in a party: invites and requests arriving, members joining and
 * leaving, promotions and the end of a party. The teleport animation has its own effects in
 * {@link TeleportService}. Everything is switched by {@code notificationSounds} in party.yml.
 */
public final class PartyEffects {

  private PartyEffects() {
  }

  private static boolean enabled() {
    return PartyConfig.notificationSounds();
  }

  /** An invite arrived. */
  public static void invited(Player target) {
    if (!enabled()) {
      return;
    }
    target.playSound(target.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.9f, 1.2f);
    target.playSound(target.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.9f, 1.6f);
  }

  /** A teleport request arrived. */
  public static void requested(Player target) {
    if (!enabled()) {
      return;
    }
    target.playSound(target.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.8f, 1.4f);
  }

  /** The sound for each kind of party event, for every member that is online. */
  public static void event(PartyEvent e) {
    if (!enabled()) {
      return;
    }
    Party party = e.party();
    for (PartyMember member : party.members()) {
      Player online = Bukkit.getPlayer(member.uuid());
      if (online == null) {
        continue;
      }
      boolean isTarget = e.target() != null && e.target().equals(member.uuid());
      Location at = online.getLocation();
      switch (e.type()) {
        case MEMBER_JOINED:
          if (isTarget) {
            online.playSound(at, Sound.ENTITY_PLAYER_LEVELUP, 0.6f, 1.4f);
            online.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, at.clone().add(0, 1.2, 0), 15,
                0.4, 0.6, 0.4, 0);
          } else {
            online.playSound(at, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.8f, 1.4f);
          }
          break;
        case MEMBER_LEFT:
          if (!isTarget) {
            online.playSound(at, Sound.BLOCK_NOTE_BLOCK_BASS, 0.6f, 0.8f);
          }
          break;
        case MEMBER_KICKED:
          online.playSound(at, Sound.ENTITY_ITEM_BREAK, 0.7f, 0.8f);
          break;
        case PROMOTED:
        case OWNER_CHANGED:
          if (isTarget) {
            online.playSound(at, Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.8f, 1.2f);
            online.getWorld().spawnParticle(Particle.FIREWORK, at.clone().add(0, 1.5, 0), 20, 0.4,
                0.6, 0.4, 0.1);
          } else {
            online.playSound(at, Sound.BLOCK_NOTE_BLOCK_CHIME, 0.6f, 1.2f);
          }
          break;
        case DEMOTED:
          if (isTarget) {
            online.playSound(at, Sound.BLOCK_NOTE_BLOCK_BASS, 0.7f, 0.7f);
          }
          break;
        case DISBANDED:
          online.playSound(at, Sound.BLOCK_BELL_USE, 0.8f, 0.7f);
          break;
        case OPEN_CHANGED:
        case XP_SHARE_CHANGED:
        case FRIENDLY_FIRE_CHANGED:
        case SPAWN_SET:
        case SPAWN_REMOVED:
          online.playSound(at, Sound.UI_BUTTON_CLICK, 0.6f, 1.2f);
          break;
        default:
          break;
      }
    }
  }
}
