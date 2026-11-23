package mc.carlton.freerpg.party;

/**
 * Stops shared XP from being shared again. While XP is being handed to party members, any new
 * sharing attempt does nothing. (The server's main thread is the only user.)
 */
public final class XpShareGuard {

  private static boolean sharing;

  private XpShareGuard() {
  }

  public static boolean isSharing() {
    return sharing;
  }

  /** Runs the action unless a share is already in progress. */
  public static void share(Runnable action) {
    if (sharing) {
      return;
    }
    sharing = true;
    try {
      action.run();
    } finally {
      sharing = false;
    }
  }
}
