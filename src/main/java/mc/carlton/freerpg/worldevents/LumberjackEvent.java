package mc.carlton.freerpg.worldevents;

/**
 * Lumberjack's Day: woodcutting and axe mastery give more exp, and logs sometimes drop twice
 * (the extra drops are made by {@link BonusDropListener}).
 */
public final class LumberjackEvent extends SkillBoostEvent {

  public LumberjackEvent() {
    super(WorldEventType.LUMBERJACKS_DAY);
  }
}
