package mc.carlton.freerpg.party;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class PartyBuffsAndLeaderboardTest {

  // ---- buff maths ----

  @Test
  void nobodyNearbyMeansNoBonus() {
    assertEquals(1.0, PartyBuffMath.xpMultiplier(0, 0.03, 0.15), 1e-12);
    assertEquals(1.0, PartyBuffMath.xpMultiplier(-2, 0.03, 0.15), 1e-12);
  }

  @Test
  void everyNearbyMemberAddsTheSameBonus() {
    assertEquals(1.03, PartyBuffMath.xpMultiplier(1, 0.03, 0.15), 1e-12);
    assertEquals(1.09, PartyBuffMath.xpMultiplier(3, 0.03, 0.15), 1e-12);
  }

  @Test
  void theBonusIsCapped() {
    assertEquals(1.15, PartyBuffMath.xpMultiplier(5, 0.03, 0.15), 1e-12);
    assertEquals(1.15, PartyBuffMath.xpMultiplier(50, 0.03, 0.15), 1e-12);
  }

  @Test
  void aBrokenConfigNeverReducesExperience() {
    assertEquals(1.0, PartyBuffMath.xpMultiplier(4, -0.05, 0.15), 1e-12);
    assertEquals(1.0, PartyBuffMath.xpMultiplier(4, 0.03, -1.0), 1e-12);
    assertEquals(1.0, PartyBuffMath.xpMultiplier(4, 0.0, 0.15), 1e-12);
  }

  // ---- leaderboard ----

  @Test
  void partiesAreRankedByScore() {
    List<PartyLeaderboard.Entry> ranked = PartyLeaderboard.rank(List.of(
        new PartyLeaderboard.Entry("Bakers", 3, 120),
        new PartyLeaderboard.Entry("Miners", 2, 400),
        new PartyLeaderboard.Entry("Farmers", 5, 250)));
    assertEquals(List.of("Miners", "Farmers", "Bakers"),
        ranked.stream().map(PartyLeaderboard.Entry::name).toList());
  }

  @Test
  void aTieGoesToTheBiggerPartyThenTheName() {
    List<PartyLeaderboard.Entry> ranked = PartyLeaderboard.rank(List.of(
        new PartyLeaderboard.Entry("zeta", 2, 100),
        new PartyLeaderboard.Entry("Alpha", 2, 100),
        new PartyLeaderboard.Entry("Big", 4, 100)));
    assertEquals(List.of("Big", "Alpha", "zeta"),
        ranked.stream().map(PartyLeaderboard.Entry::name).toList());
  }

  @Test
  void rankingDoesNotChangeTheInputAndHandlesEmpty() {
    List<PartyLeaderboard.Entry> input = List.of(new PartyLeaderboard.Entry("A", 1, 1),
        new PartyLeaderboard.Entry("B", 1, 2));
    PartyLeaderboard.rank(input);
    assertEquals("A", input.get(0).name());
    assertEquals(List.of(), PartyLeaderboard.rank(List.of()));
  }

  @Test
  void positionIsOneBasedAndIgnoresCase() {
    List<PartyLeaderboard.Entry> ranked = PartyLeaderboard.rank(List.of(
        new PartyLeaderboard.Entry("Miners", 2, 400), new PartyLeaderboard.Entry("Bakers", 3, 120)));
    assertEquals(1, PartyLeaderboard.positionOf(ranked, "miners"));
    assertEquals(2, PartyLeaderboard.positionOf(ranked, "BAKERS"));
    assertEquals(-1, PartyLeaderboard.positionOf(ranked, "nobody"));
  }
}
