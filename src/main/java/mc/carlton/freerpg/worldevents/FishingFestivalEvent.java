package mc.carlton.freerpg.worldevents;

import java.util.List;
import java.util.UUID;
import mc.carlton.freerpg.core.info.player.ChangeStats;
import mc.carlton.freerpg.party.XpShareGuard;
import mc.carlton.freerpg.quests.Quests;
import mc.carlton.freerpg.skills.perksAndAbilities.Global;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

/**
 * The Fishing Festival: everyone on the server fishes for points. When it ends everyone who
 * scored is paid (souls and exp, the top places more and a free wheel spin) and the winners are
 * announced. An admin stop or a shutdown pays nothing.
 */
public final class FishingFestivalEvent implements WorldEvent {

  static boolean enoughPlayers(int online, int minimum) {
    return online >= minimum;
  }

  @Override
  public WorldEventType type() {
    return WorldEventType.FISHING_FESTIVAL;
  }

  @Override
  public boolean canStart() {
    return enoughPlayers(Bukkit.getOnlinePlayers().size(),
        WorldEventConfig.integer(type(), "minPlayers", 2));
  }

  @Override
  public void onStart(ActiveEvent event) {
    event.setState(new FestivalScores());
  }

  @Override
  public Sound announceSound() {
    return Sound.ENTITY_PLAYER_SPLASH_HIGH_SPEED;
  }

  @Override
  public String[] barPlaceholders(ActiveEvent event) {
    FestivalScores scores = event.state();
    List<FestivalScores.Entry> ranking = scores == null ? List.of() : scores.ranking();
    if (ranking.isEmpty()) {
      return new String[] {"leader", "-", "score", "0"};
    }
    FestivalScores.Entry leader = ranking.get(0);
    return new String[] {"leader", nameOf(leader.player()), "score",
        String.valueOf(leader.score())};
  }

  @Override
  public void onEnd(ActiveEvent event, boolean byAdmin) {
    FestivalScores scores = event.state();
    if (byAdmin || scores == null) {
      return;
    }
    List<FestivalScores.Entry> ranking = scores.ranking();
    announceResult(ranking);
    for (FestivalRewards.Payout payout : FestivalRewards.plan(ranking,
        FestivalRewards.fromConfig())) {
      Player player = Bukkit.getPlayer(payout.player());
      if (player == null) {
        continue; // offline at the end: nothing is paid
      }
      try {
        pay(player, payout);
      } catch (RuntimeException e) {
        Bukkit.getLogger().severe("Could not pay the Fishing Festival prize to "
            + player.getName() + ": " + e);
      }
    }
  }

  private static void pay(Player player, FestivalRewards.Payout payout) {
    new Global(player).addSouls(payout.souls());
    player.giveExp(payout.minecraftExp());
    WorldEventEffects.festivalPayout(player, payout.bonusSpins() > 0);
    XpShareGuard.share(() -> new ChangeStats(player).changeEXP("fishing", payout.fishingExp()));
    if (payout.bonusSpins() > 0 && Quests.bonusSpins() != null) {
      Quests.bonusSpins().add(payout.player(), payout.bonusSpins());
    }
    player.sendMessage(WorldEventMessages.prefixed(player, "worldEventFestivalReward", "souls",
        String.valueOf(payout.souls()), "exp", String.valueOf(payout.minecraftExp()),
        "fishingExp", String.valueOf(payout.fishingExp())));
    if (payout.bonusSpins() > 0) {
      TextComponent line = new TextComponent(WorldEventMessages.prefixed(player,
          "worldEventFestivalSpinLine", "spins", String.valueOf(payout.bonusSpins())));
      line.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/quests bonus"));
      player.spigot().sendMessage(line);
    }
  }

  /** Tells everyone who won, or that nobody fished. */
  private static void announceResult(List<FestivalScores.Entry> ranking) {
    for (Player player : Bukkit.getOnlinePlayers()) {
      if (ranking.isEmpty()) {
        player.sendMessage(WorldEventMessages.prefixed(player, "worldEventFestivalNobody"));
        continue;
      }
      player.sendMessage(WorldEventMessages.prefixed(player, "worldEventFestivalResultHeader"));
      for (int i = 0; i < Math.min(3, ranking.size()); i++) {
        player.sendMessage(WorldEventMessages.text(player, "worldEventFestivalResultEntry",
            "rank", String.valueOf(i + 1), "player", nameOf(ranking.get(i).player()), "score",
            String.valueOf(ranking.get(i).score())));
      }
    }
  }

  private static String nameOf(UUID id) {
    String name = Bukkit.getOfflinePlayer(id).getName();
    return name == null ? "?" : name;
  }
}
