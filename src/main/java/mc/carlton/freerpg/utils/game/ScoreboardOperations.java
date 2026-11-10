package mc.carlton.freerpg.utils.game;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import mc.carlton.freerpg.core.info.player.PlayerStats;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

public class ScoreboardOperations {

  public void setPlayerPowerLevel(Player p) {
    //Get the global level
    PlayerStats pStatClass = new PlayerStats(p);
    UUID uuid = p.getUniqueId();
    Map<UUID, Map<String, ArrayList<Number>>> statAll = pStatClass.getData();
    Map<String, ArrayList<Number>> pStatAll = statAll.get(uuid);
    ArrayList<Number> pStats = pStatAll.get("global");
    int globalLevel = (int) pStats.get(0);
    String pName = p.getName();
    for (Player otherPlayer : Bukkit.getOnlinePlayers()) {
      //Gets all other players scoreboards
      Scoreboard board = otherPlayer.getScoreboard();
      if (board == null || board == Bukkit.getScoreboardManager().getMainScoreboard()) {
        board = Bukkit.getScoreboardManager().getNewScoreboard();
      }

      //sets the score for all other players
      Objective obj = board.getObjective(DisplaySlot.BELOW_NAME);
      if (obj == null) {
        String displayName = otherPlayer.getDisplayName();
        int takenLength = (int) Math.min(displayName.length(), 11);
        String name = displayName.substring(0, takenLength) + "-frpg";
        obj = board.registerNewObjective(name, Criteria.DUMMY, "test");
      }
      Score score = obj.getScore(pName);
      score.setScore(globalLevel);
      otherPlayer.setScoreboard(board);
    }
  }
}
