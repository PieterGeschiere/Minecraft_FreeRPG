package mc.carlton.freerpg.skills.building;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class BuildingLogicTest {

  private static final UUID WORLD_A = new UUID(1, 1);
  private static final UUID WORLD_B = new UUID(2, 2);
  private static final UUID ALICE = new UUID(10, 10);
  private static final UUID BOB = new UUID(20, 20);

  // ---- block key ----

  @Test
  void blockKeyRoundTripsExtremeAndNegativeCoordinates() {
    int[][] spots = {{-30000000, -64, 29999999}, {0, 320, 0}, {-1, -1, -1}, {29999999, 2047, -30000000}};
    for (int[] s : spots) {
      long key = BuildingBlockKey.pack(s[0], s[1], s[2]);
      assertEquals(s[0], BuildingBlockKey.unpackX(key));
      assertEquals(s[1], BuildingBlockKey.unpackY(key));
      assertEquals(s[2], BuildingBlockKey.unpackZ(key));
    }
  }

  // ---- tracker ----

  @Test
  void trackerRemembersAndForgetsOwners() {
    BuildingBlockTracker tracker = new BuildingBlockTracker(100);
    assertNull(tracker.ownerOf(WORLD_A, 1, 2, 3));
    assertTrue(tracker.track(WORLD_A, 1, 2, 3, ALICE));
    assertEquals(ALICE, tracker.ownerOf(WORLD_A, 1, 2, 3));
    assertNull(tracker.ownerOf(WORLD_B, 1, 2, 3));
    tracker.untrack(WORLD_A, 1, 2, 3);
    assertNull(tracker.ownerOf(WORLD_A, 1, 2, 3));
    assertEquals(0, tracker.size());
    tracker.untrack(WORLD_A, 9, 9, 9); // untracking something unknown is harmless
  }

  @Test
  void trackerStopsAtTheCapButKeepsWhatItHas() {
    BuildingBlockTracker tracker = new BuildingBlockTracker(2);
    assertTrue(tracker.track(WORLD_A, 0, 0, 0, ALICE));
    assertTrue(tracker.track(WORLD_A, 1, 0, 0, ALICE));
    assertFalse(tracker.track(WORLD_A, 2, 0, 0, BOB));
    assertEquals(ALICE, tracker.ownerOf(WORLD_A, 0, 0, 0));
    assertNull(tracker.ownerOf(WORLD_A, 2, 0, 0));
    assertTrue(tracker.isFull());
    assertTrue(tracker.consumeFullWarning());
    assertFalse(tracker.consumeFullWarning());
    // re-tracking an existing spot is still allowed and can change its owner
    assertTrue(tracker.track(WORLD_A, 0, 0, 0, BOB));
    assertEquals(BOB, tracker.ownerOf(WORLD_A, 0, 0, 0));
    assertEquals(2, tracker.size());
  }

  @Test
  void trackerSurvivesASaveAndLoad() throws IOException {
    BuildingBlockTracker tracker = new BuildingBlockTracker(100);
    tracker.track(WORLD_A, -5, 64, -300, ALICE);
    tracker.track(WORLD_B, 7, -64, 8, BOB);
    tracker.track(WORLD_B, 7, 100, 8, ALICE);
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    tracker.write(new DataOutputStream(bytes));
    BuildingBlockTracker loaded = BuildingBlockTracker.read(
        new DataInputStream(new ByteArrayInputStream(bytes.toByteArray())), 100);
    assertEquals(3, loaded.size());
    assertEquals(ALICE, loaded.ownerOf(WORLD_A, -5, 64, -300));
    assertEquals(BOB, loaded.ownerOf(WORLD_B, 7, -64, 8));
    assertEquals(ALICE, loaded.ownerOf(WORLD_B, 7, 100, 8));
    assertNull(loaded.ownerOf(WORLD_A, 7, -64, 8));
  }

  @Test
  void loadingRespectsTheCap() throws IOException {
    BuildingBlockTracker tracker = new BuildingBlockTracker(10);
    for (int i = 0; i < 5; i++) {
      tracker.track(WORLD_A, i, 0, 0, ALICE);
    }
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    tracker.write(new DataOutputStream(bytes));
    BuildingBlockTracker loaded = BuildingBlockTracker.read(
        new DataInputStream(new ByteArrayInputStream(bytes.toByteArray())), 3);
    assertEquals(3, loaded.size());
  }

  @Test
  void loadingAnUnknownVersionFails() {
    byte[] garbage = {0, 0, 0, 99};
    org.junit.jupiter.api.Assertions.assertThrows(IOException.class,
        () -> BuildingBlockTracker.read(new DataInputStream(new ByteArrayInputStream(garbage)), 10));
  }

  // ---- XP tiers and rules ----

  @Test
  void tiersAreFlatAndNotValueBased() {
    assertEquals("basicBlock", BuildingBlockTiers.tierKey("NETHERITE_BLOCK"));
    assertEquals("basicBlock", BuildingBlockTiers.tierKey("DIRT"));
    assertEquals("decorativeBlock", BuildingBlockTiers.tierKey("OAK_STAIRS"));
    assertEquals("decorativeBlock", BuildingBlockTiers.tierKey("white_concrete"));
    assertEquals("basicBlock", BuildingBlockTiers.tierKey(null));
  }

  @Test
  void noXpForReplacingABlockYouJustBroke() {
    BuildingXpRules rules = new BuildingXpRules(30_000, 40);
    rules.recordBreak(ALICE, WORLD_A, 1, 2, 3, 1_000);
    assertFalse(rules.shouldAward(ALICE, "DIRT", WORLD_A, 1, 2, 3, 1_000 + 29_999));
    assertTrue(rules.shouldAward(ALICE, "DIRT", WORLD_A, 1, 2, 3, 1_000 + 30_001));
    // a different player at that spot is unaffected
    rules.recordBreak(ALICE, WORLD_A, 5, 5, 5, 0);
    assertTrue(rules.shouldAward(BOB, "DIRT", WORLD_A, 5, 5, 5, 10));
  }

  @Test
  void perMaterialPerMinuteCapAppliesAndResets() {
    BuildingXpRules rules = new BuildingXpRules(30_000, 40);
    for (int i = 0; i < 40; i++) {
      assertTrue(rules.shouldAward(ALICE, "DIRT", WORLD_A, i, 0, 0, 1_000), "placement " + i);
    }
    assertFalse(rules.shouldAward(ALICE, "DIRT", WORLD_A, 100, 0, 0, 2_000));
    assertTrue(rules.shouldAward(ALICE, "STONE", WORLD_A, 100, 0, 0, 2_000));
    assertTrue(rules.shouldAward(ALICE, "DIRT", WORLD_A, 101, 0, 0, 1_000 + 60_000));
  }

  // ---- chances ----

  @Test
  void chanceTableLookup() {
    double[] weatherproof = {0.2, 0.4, 0.6, 0.8, 1.0};
    assertEquals(0.0, BuildingChances.chanceForLevel(weatherproof, 0));
    assertEquals(0.6, BuildingChances.chanceForLevel(weatherproof, 3));
    assertEquals(1.0, BuildingChances.chanceForLevel(weatherproof, 99));
    assertEquals(0.0, BuildingChances.chanceForLevel(weatherproof, -4));
    assertEquals(0.0, BuildingChances.chanceForLevel(null, 3));
  }

  @Test
  void survivalRoll() {
    assertTrue(BuildingChances.survives(1.0, 0.9999));
    assertFalse(BuildingChances.survives(0.0, 0.0));
    assertFalse(BuildingChances.survives(0.5, 0.5));
  }

  // ---- demolition ----

  @Test
  void demolitionDamageRounding() {
    assertEquals(1, DemolitionRules.reducedDamage(1, 0.5, 0.4));
    assertEquals(0, DemolitionRules.reducedDamage(1, 0.5, 0.6));
    assertEquals(5, DemolitionRules.reducedDamage(10, 0.5, 0.9));
    assertEquals(2, DemolitionRules.reducedDamage(3, 0.5, 0.2));
    assertEquals(0, DemolitionRules.reducedDamage(0, 0.5, 0.0));
  }

  // ---- reach ----

  @Test
  void reachBonusIsCapped() {
    assertEquals(0.0, BuildingReach.bonus(0, 0.01, 3.0));
    assertEquals(0.25, BuildingReach.bonus(25, 0.01, 3.0), 1e-9);
    assertEquals(3.0, BuildingReach.bonus(10_000, 0.01, 3.0));
    assertEquals(0.0, BuildingReach.bonus(-5, 0.01, 3.0));
  }
}
