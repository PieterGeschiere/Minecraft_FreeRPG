package mc.carlton.freerpg.party;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

/**
 * A blue P in front of the names of party members, in the tab list and above their heads. The
 * nameplate uses a scoreboard team on the scoreboard of every online player, because the plugin
 * (and others) give players their own scoreboards. Players that are already in another team on a
 * scoreboard are left alone there.
 */
public final class PartyDisplay {

  private static final String TEAM_NAME = "frpg_party";
  private static final String PREFIX = ChatColor.BLUE + "P " + ChatColor.RESET;

  private PartyDisplay() {
  }

  public static void refreshAll() {
    if (Parties.manager() == null) {
      return;
    }
    for (Player p : Bukkit.getOnlinePlayers()) {
      updateTablist(p);
    }
    for (Player viewer : Bukkit.getOnlinePlayers()) {
      updateNameplates(viewer.getScoreboard());
    }
  }

  private static boolean inParty(Player p) {
    return Parties.manager().partyOf(p.getUniqueId()).isPresent();
  }

  private static void updateTablist(Player p) {
    if (PartyConfig.displayTablist() && inParty(p)) {
      p.setPlayerListName(PREFIX + p.getName());
    } else if (p.getPlayerListName().startsWith(PREFIX)) {
      p.setPlayerListName(null); // only undo our own marker, never another plugin's name
    }
  }

  private static void updateNameplates(Scoreboard board) {
    Team team = board.getTeam(TEAM_NAME);
    if (!PartyConfig.displayNameplate()) {
      if (team != null) {
        team.unregister();
      }
      return;
    }
    if (team == null) {
      team = board.registerNewTeam(TEAM_NAME);
    }
    team.setPrefix(PREFIX);
    for (Player p : Bukkit.getOnlinePlayers()) {
      String entry = p.getName();
      boolean wanted = inParty(p);
      if (wanted && team.hasEntry(entry)) {
        continue;
      }
      if (wanted) {
        Team other = board.getEntryTeam(entry);
        if (other == null) {
          team.addEntry(entry);
        }
      } else if (team.hasEntry(entry)) {
        team.removeEntry(entry);
      }
    }
  }

  /** Removes everything again (plugin shutdown). */
  public static void clearAll() {
    for (Player p : Bukkit.getOnlinePlayers()) {
      if (p.getPlayerListName().startsWith(PREFIX)) {
        p.setPlayerListName(null);
      }
      Team team = p.getScoreboard().getTeam(TEAM_NAME);
      if (team != null) {
        team.unregister();
      }
    }
  }
}
