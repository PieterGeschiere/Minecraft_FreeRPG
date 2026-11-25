package mc.carlton.freerpg.worldevents;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FestivalScoresTest {

  private final UUID a = UUID.randomUUID();
  private final UUID b = UUID.randomUUID();

  @BeforeEach
  void empty() {
    WorldEventConfig.loadFrom(new YamlConfiguration());
  }

  @Test
  void scoresAddUpPerPlayerAndAreRankedHighestFirst() {
    FestivalScores scores = new FestivalScores();
    scores.add(a, 1);
    scores.add(b, 3);
    scores.add(a, 1);
    assertEquals(2, scores.score(a));
    List<FestivalScores.Entry> ranking = scores.ranking();
    assertEquals(b, ranking.get(0).player());
    assertEquals(a, ranking.get(1).player());
  }

  @Test
  void zeroAndNegativePointsAreIgnored() {
    FestivalScores scores = new FestivalScores();
    scores.add(a, 0);
    scores.add(a, -1);
    assertEquals(0, scores.score(a));
    assertTrue(scores.ranking().isEmpty(), "players with no points are not listed");
  }

  @Test
  void aTieIsWonByWhoReachedTheScoreFirst() {
    FestivalScores scores = new FestivalScores();
    scores.add(a, 3);
    scores.add(b, 3);
    assertEquals(a, scores.ranking().get(0).player());
    scores.add(b, 1);
    scores.add(a, 1);
    // both have 4 now; b reached 4 first
    assertEquals(b, scores.ranking().get(0).player());
  }

  @Test
  void anEmptyTrackerHasAnEmptyRanking() {
    assertTrue(new FestivalScores().ranking().isEmpty());
  }

  @Test
  void caughtItemsScoreAccordingToTheDefaultTable() {
    assertEquals(1, FestivalListener.points(Material.COD));
    assertEquals(2, FestivalListener.points(Material.SALMON));
    assertEquals(3, FestivalListener.points(Material.TROPICAL_FISH));
    assertEquals(3, FestivalListener.points(Material.PUFFERFISH));
    assertEquals(6, FestivalListener.points(Material.ENCHANTED_BOOK));
    assertEquals(0, FestivalListener.points(Material.STICK), "junk gives nothing");
  }

  @Test
  void thePointsTableCanBeChangedInTheConfig() {
    YamlConfiguration yaml = new YamlConfiguration();
    yaml.set("events.fishingFestival.points.COD", 5);
    WorldEventConfig.loadFrom(yaml);
    assertEquals(5, FestivalListener.points(Material.COD));
    assertEquals(0, FestivalListener.points(Material.SALMON), "a table in the file replaces the default");
  }

  @Test
  void theFestivalNeedsEnoughPlayersToStart() {
    assertFalse(FishingFestivalEvent.enoughPlayers(1, 2));
    assertTrue(FishingFestivalEvent.enoughPlayers(2, 2));
    assertTrue(FishingFestivalEvent.enoughPlayers(0, 0));
  }
}
