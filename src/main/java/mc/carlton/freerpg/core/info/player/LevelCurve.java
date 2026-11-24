package mc.carlton.freerpg.core.info.player;

/** Pure leveling maths: exponential growth up to a level, then linear exp per level. */
public final class LevelCurve {

  private final double base;
  private final int referenceLevel;
  private final int referenceExp;
  private final int linearStartLevel;
  private final int linearExp;

  public LevelCurve(double base, int referenceLevel, int referenceExp, int linearStartLevel,
      int linearExp) {
    this.base = base;
    this.referenceLevel = referenceLevel;
    this.referenceExp = referenceExp;
    this.linearStartLevel = linearStartLevel;
    this.linearExp = linearExp;
  }

  public int expFromLevel(int level) {
    int exp = 0;
    if (level > linearStartLevel) {
      int exponentialMaxExp = expFromLevel(linearStartLevel);
      exp = exponentialMaxExp + linearExp * (level - linearStartLevel);
    } else {
      exp = (int) Math.floor(
          referenceExp * ((Math.pow(base, level) - 1) / (Math.pow(base, referenceLevel) - 1)));
    }
    return exp;
  }

  public int levelFromExp(int exp) {
    int exponentialMaxExp = expFromLevel(linearStartLevel);
    int level = 0;
    if (exp <= exponentialMaxExp) {
      level = (int) Math.floor(
          ((Math.log((exp * (1.0 / referenceExp) * (Math.pow(base, referenceLevel) - 1)) + 1)
              / Math.log(base))));
    } else {
      level = (int) Math.floor((exp - exponentialMaxExp) / linearExp) + linearStartLevel;
    }
    return settleLevel(level, exp);
  }

  /** The log formula can land one level low on an exact threshold; settle it against the real thresholds. */
  private int settleLevel(int level, int exp) {
    int result = Math.max(0, level);
    while (expFromLevel(result + 1) <= exp) {
      result++;
    }
    while (result > 0 && expFromLevel(result) > exp) {
      result--;
    }
    return result;
  }

  public static int tokensGained(int oldLevel, int newLevel, double levelsPerToken) {
    int oldTokens = (int) Math.floor(oldLevel / levelsPerToken);
    int newTokens = (int) Math.floor(newLevel / levelsPerToken);
    return newTokens - oldTokens;
  }
}
