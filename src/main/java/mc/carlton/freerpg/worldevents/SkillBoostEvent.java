package mc.carlton.freerpg.worldevents;

/**
 * An event whose main effect is more exp in a few skills (the skills and the multiplier come from
 * the config). Events that do more than that extend it and add their own parts.
 */
public abstract class SkillBoostEvent implements WorldEvent {

  private final WorldEventType type;

  protected SkillBoostEvent(WorldEventType type) {
    this.type = type;
  }

  @Override
  public WorldEventType type() {
    return type;
  }

  @Override
  public boolean canStart() {
    return true;
  }

  @Override
  public void onStart(ActiveEvent event) {
  }

  @Override
  public void onEnd(ActiveEvent event, boolean byAdmin) {
  }

  @Override
  public double xpMultiplier(String skillName) {
    return EventDefaults.skills(type).contains(skillName) ? EventDefaults.xpMultiplier(type)
        : 1.0;
  }
}
