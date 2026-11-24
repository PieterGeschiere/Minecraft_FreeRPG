package mc.carlton.freerpg.quests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.Test;

class QuestDealerTest {

  private static QuestDefinition def(String id, int weight) {
    return new QuestDefinition(id, QuestType.BREAK_BLOCKS, "ANY", 5, 9, weight, "Mine {target}");
  }

  private static List<QuestDefinition> pool(int size) {
    List<QuestDefinition> pool = new ArrayList<>();
    for (int i = 0; i < size; i++) {
      pool.add(def("q" + i, 1));
    }
    return pool;
  }

  @Test
  void dealReturnsThreeDistinctQuests() {
    for (int seed = 0; seed < 50; seed++) {
      List<QuestDefinition> dealt = QuestDealer.deal(pool(6), 3, new Random(seed));
      assertEquals(3, dealt.size());
      Set<String> ids = new HashSet<>();
      dealt.forEach(q -> ids.add(q.id()));
      assertEquals(3, ids.size());
    }
  }

  @Test
  void dealWithSmallPoolReturnsWholePool() {
    assertEquals(2, QuestDealer.deal(pool(2), 3, new Random(1)).size());
  }

  @Test
  void dealWithEmptyPoolReturnsEmpty() {
    assertTrue(QuestDealer.deal(List.of(), 3, new Random(1)).isEmpty());
  }

  @Test
  void dealWithZeroCountReturnsEmpty() {
    assertTrue(QuestDealer.deal(pool(4), 0, new Random(1)).isEmpty());
  }

  @Test
  void weightZeroNeverDealt() {
    List<QuestDefinition> pool = new ArrayList<>(pool(4));
    pool.add(def("never", 0));
    Random random = new Random(7);
    for (int i = 0; i < 200; i++) {
      for (QuestDefinition q : QuestDealer.deal(pool, 3, random)) {
        assertFalse(q.id().equals("never"));
      }
    }
  }

  @Test
  void heavierWeightDealtMoreOften() {
    List<QuestDefinition> pool = List.of(def("heavy", 10), def("light", 1), def("other", 1));
    Random random = new Random(3);
    int heavy = 0;
    int light = 0;
    for (int i = 0; i < 2000; i++) {
      String first = QuestDealer.deal(pool, 1, random).get(0).id();
      if (first.equals("heavy")) {
        heavy++;
      } else if (first.equals("light")) {
        light++;
      }
    }
    assertTrue(heavy > light * 4, "heavy=" + heavy + " light=" + light);
  }

  @Test
  void rollTargetStaysInRange() {
    QuestDefinition def = def("a", 1);
    Random random = new Random(5);
    boolean sawMin = false;
    boolean sawMax = false;
    for (int i = 0; i < 500; i++) {
      int target = QuestDealer.rollTarget(def, random);
      assertTrue(target >= 5 && target <= 9);
      sawMin |= target == 5;
      sawMax |= target == 9;
    }
    assertTrue(sawMin && sawMax);
  }

  @Test
  void rollTargetWithEqualBounds() {
    QuestDefinition def = new QuestDefinition("a", QuestType.CATCH_FISH, "ANY", 7, 7, 1, "x");
    assertEquals(7, QuestDealer.rollTarget(def, new Random(1)));
  }

  @Test
  void rollTargetIsAtLeastOne() {
    QuestDefinition def = new QuestDefinition("a", QuestType.CATCH_FISH, "ANY", 0, 0, 1, "x");
    assertEquals(1, QuestDealer.rollTarget(def, new Random(1)));
  }
}
