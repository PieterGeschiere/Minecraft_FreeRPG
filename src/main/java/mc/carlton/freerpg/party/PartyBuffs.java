package mc.carlton.freerpg.party;

import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

/**
 * Party buffs. At the moment there is one: a bonus to the experience you earn for every party member
 * who is close to you. Members who are away from the keyboard, in another world or spectating do not
 * count, so the bonus cannot be farmed with idle accounts.
 */
public final class PartyBuffs {

  private PartyBuffs() {
  }

  /** The multiplier on the experience this player earns right now (1.0 when nothing applies). */
  public static double xpMultiplier(Player p) {
    if (!PartyConfig.buffXpEnabled() || Parties.manager() == null) {
      return 1.0;
    }
    Party party = Parties.manager().partyOf(p.getUniqueId()).orElse(null);
    if (party == null) {
      return 1.0;
    }
    long now = System.currentTimeMillis();
    double radius = PartyConfig.buffRadius();
    int nearby = 0;
    for (PartyMember member : party.members()) {
      UUID id = member.uuid();
      if (id.equals(p.getUniqueId())) {
        continue;
      }
      Player other = Bukkit.getPlayer(id);
      if (other == null || !other.getWorld().equals(p.getWorld())
          || other.getGameMode() == GameMode.SPECTATOR || PartyListener.isAfk(id, now)) {
        continue;
      }
      if (other.getLocation().distanceSquared(p.getLocation()) <= radius * radius) {
        nearby++;
      }
    }
    return PartyBuffMath.xpMultiplier(nearby, PartyConfig.buffXpPerMember(),
        PartyConfig.buffXpMax());
  }
}
