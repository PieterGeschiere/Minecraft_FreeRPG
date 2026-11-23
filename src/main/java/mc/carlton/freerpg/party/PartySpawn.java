package mc.carlton.freerpg.party;

/** Saved teleport location of a party. */
public record PartySpawn(String world, double x, double y, double z, float yaw, float pitch) {
}
