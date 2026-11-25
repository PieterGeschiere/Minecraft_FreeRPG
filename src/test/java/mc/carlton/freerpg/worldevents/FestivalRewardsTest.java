package mc.carlton.freerpg.worldevents;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FestivalRewardsTest {

  @BeforeEach
  void empty() {
    WorldEventConfig.loadFrom(new YamlConfiguration());
  }

  private static List<FestivalScores.Entry> ranking(int... scores) {
    List<FestivalScores.Entry> list = new ArrayList<>();
    for (int score : scores) {
      list.add(new FestivalScores.Entry(UUID.randomUUID(), score));
    }
    return list;
  }

  @Test
  void everyoneWhoScoredGetsSoulsAndExp() {
    List<FestivalScores.Entry> ranking = ranking(20, 15, 10, 5, 1);
    List<FestivalRewards.Payout> plan = FestivalRewards.plan(ranking,
        FestivalRewards.fromConfig());
    assertEquals(5, plan.size());
    for (FestivalRewards.Payout payout : plan) {
      assertTrue(payout.souls() >= 3, "souls " + payout.souls());
      assertTrue(payout.minecraftExp() >= 50);
    }
  }

  @Test
  void theTopThreeGetExtraAndASpin() {
    List<FestivalRewards.Payout> plan = FestivalRewards.plan(ranking(20, 15, 10, 5, 1),
        FestivalRewards.fromConfig());
    assertEquals(1, plan.get(0).rank());
    assertEquals(3 + 10, plan.get(0).souls());
    assertEquals(1, plan.get(0).bonusSpins());
    assertEquals(3 + 3, plan.get(2).souls());
    assertEquals(1, plan.get(2).bonusSpins());
    assertEquals(0, plan.get(3).bonusSpins());
    assertEquals(0, plan.get(4).bonusSpins());
    assertEquals(3, plan.get(4).souls());
  }

  @Test
  void fishingExpFollowsThePoints() {
    List<FestivalRewards.Payout> plan = FestivalRewards.plan(ranking(7),
        FestivalRewards.fromConfig());
    assertEquals(7 * 20, plan.get(0).fishingExp());
  }

  @Test
  void aNobodyRankingGivesAnEmptyPlan() {
    assertTrue(FestivalRewards.plan(List.of(), FestivalRewards.fromConfig()).isEmpty());
  }

  @Test
  void shortOrEmptyTopListsDoNotBreakThePlan() {
    YamlConfiguration yaml = new YamlConfiguration();
    yaml.set("events.fishingFestival.rewards.topSouls", List.of());
    yaml.set("events.fishingFestival.rewards.topBonusSpins", List.of(2));
    WorldEventConfig.loadFrom(yaml);
    List<FestivalRewards.Payout> plan = FestivalRewards.plan(ranking(9, 8, 7),
        FestivalRewards.fromConfig());
    assertEquals(3, plan.get(0).souls());
    assertEquals(2, plan.get(0).bonusSpins());
    assertEquals(0, plan.get(1).bonusSpins());
  }

  @Test
  void entriesWithoutPointsAreSkipped() {
    assertTrue(FestivalRewards.plan(ranking(0), FestivalRewards.fromConfig()).isEmpty());
  }
}
