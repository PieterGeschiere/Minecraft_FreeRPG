package mc.carlton.freerpg.worldevents;

/**
 * The rule twists Mutator Night picks from. Text keys are written out in full so the language-key
 * tests can see them. The settings of each twist live under {@code events.mutatorNight.mutators}.
 */
public enum Mutator {
  LOW_GRAVITY("lowGravity", "worldEventMutatorLowGravityName", "worldEventMutatorLowGravityDesc"),
  GLASS_CANNON("glassCannon", "worldEventMutatorGlassCannonName",
      "worldEventMutatorGlassCannonDesc"),
  EXPLODING_LOOT("explodingLoot", "worldEventMutatorExplodingLootName",
      "worldEventMutatorExplodingLootDesc"),
  HUNGRY_NIGHT("hungryNight", "worldEventMutatorHungryNightName",
      "worldEventMutatorHungryNightDesc"),
  SWIFT_MOBS("swiftMobs", "worldEventMutatorSwiftMobsName", "worldEventMutatorSwiftMobsDesc"),
  DOUBLE_TROUBLE("doubleTrouble", "worldEventMutatorDoubleTroubleName",
      "worldEventMutatorDoubleTroubleDesc"),
  SOUL_STORM("soulStorm", "worldEventMutatorSoulStormName", "worldEventMutatorSoulStormDesc"),
  GLOWING_MOBS("glowingMobs", "worldEventMutatorGlowingMobsName",
      "worldEventMutatorGlowingMobsDesc");

  private final String id;
  private final String nameKey;
  private final String descKey;

  Mutator(String id, String nameKey, String descKey) {
    this.id = id;
    this.nameKey = nameKey;
    this.descKey = descKey;
  }

  public String id() {
    return id;
  }

  public String nameKey() {
    return nameKey;
  }

  public String descKey() {
    return descKey;
  }

  private String path(String key) {
    return "events." + WorldEventType.MUTATOR_NIGHT.configKey() + ".mutators." + id + "." + key;
  }

  /** Whether the twist is switched on in the config (default yes). */
  public boolean enabled() {
    return WorldEventConfig.raw().getBoolean(path("enabled"), true);
  }

  /** How likely the twist is picked compared to the others; 0 means never (default 1). */
  public int weight() {
    return Math.max(0, WorldEventConfig.raw().getInt(path("weight"), 1));
  }

  /** A number setting of this twist. */
  public double number(String key, double def) {
    return WorldEventConfig.raw().getDouble(path(key), def);
  }
}
