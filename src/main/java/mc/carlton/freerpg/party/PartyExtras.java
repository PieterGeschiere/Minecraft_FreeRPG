package mc.carlton.freerpg.party;

import java.util.ArrayList;
import java.util.List;
import mc.carlton.freerpg.core.info.player.Leaderboards;
import mc.carlton.freerpg.quests.ActiveQuest;
import mc.carlton.freerpg.quests.PlayerQuests;
import mc.carlton.freerpg.quests.QuestConfig;
import mc.carlton.freerpg.quests.Quests;
import org.bukkit.entity.Player;

/** The party leaderboard (/party top) and the shared party quests (/party quests). */
final class PartyExtras {

  private static final int TOP_SIZE = 10;
  private static final int BAR_LENGTH = 10;

  private PartyExtras() {
  }

  /** The best parties by the total level of their members, and where the player's party stands. */
  static boolean top(Player p) {
    Leaderboards leaderboards = new Leaderboards();
    List<PartyLeaderboard.Entry> entries = new ArrayList<>();
    for (Party party : Parties.manager().all()) {
      long score = 0;
      for (PartyMember member : party.members()) {
        score += leaderboards.getGlobalLevel(member.uuid());
      }
      entries.add(new PartyLeaderboard.Entry(party.name(), party.size(), score));
    }
    List<PartyLeaderboard.Entry> ranked = PartyLeaderboard.rank(entries);
    if (ranked.isEmpty()) {
      PartyMessages.send(p, "partyTopNone");
      return true;
    }
    p.sendMessage(PartyMessages.text(p, "partyTopHeader"));
    for (int i = 0; i < Math.min(TOP_SIZE, ranked.size()); i++) {
      PartyLeaderboard.Entry entry = ranked.get(i);
      p.sendMessage(PartyMessages.text(p, "partyTopLine", "rank", String.valueOf(i + 1), "party",
          entry.name(), "size", String.valueOf(entry.members()), "score",
          String.format("%,d", entry.score())));
    }
    Party own = Parties.manager().partyOf(p.getUniqueId()).orElse(null);
    if (own != null) {
      int position = PartyLeaderboard.positionOf(ranked, own.name());
      if (position > 0) {
        p.sendMessage(PartyMessages.text(p, "partyTopYours", "rank", String.valueOf(position),
            "total", String.valueOf(ranked.size())));
      }
    }
    return true;
  }

  /** Today's quests of the player's party, with everyone's combined progress. */
  static boolean quests(Player p) {
    Party party = Parties.manager().partyOf(p.getUniqueId()).orElse(null);
    if (party == null) {
      PartyMessages.send(p, "partyNotIn");
      return true;
    }
    PlayerQuests quests = QuestConfig.partyQuestsEnabled() ? Quests.partyQuests(party.name())
        : null;
    if (quests == null) {
      PartyMessages.send(p, "partyQuestsOff");
      return true;
    }
    p.sendMessage(PartyMessages.text(p, "partyQuestsHeader", "party", party.name()));
    for (ActiveQuest quest : quests.quests()) {
      p.sendMessage(PartyMessages.text(p, "partyQuestLine", "status", quest.done() ? "§a✔" : "§c✘",
          "quest", quest.name(), "progress", String.valueOf(quest.progress()), "target",
          String.valueOf(quest.target()), "bar", bar(quest)));
    }
    p.sendMessage(PartyMessages.text(p, "partyQuestsReward"));
    return true;
  }

  private static String bar(ActiveQuest quest) {
    int filled = (int) Math.round((double) quest.progress() / quest.target() * BAR_LENGTH);
    StringBuilder bar = new StringBuilder("§a");
    for (int i = 0; i < BAR_LENGTH; i++) {
      if (i == filled) {
        bar.append("§8");
      }
      bar.append('■');
    }
    return bar.toString();
  }
}
