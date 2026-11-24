package mc.carlton.freerpg.quests;

/** Where a player is with today's wheel spin. */
public enum SpinState {
  /** Not all quests are done yet. */
  NONE,
  /** All quests are done and the spin can be claimed. */
  READY,
  /** The spin was used today. */
  CLAIMED
}
