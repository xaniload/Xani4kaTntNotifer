package dev.xani.tntcartcatguard.client;

public final class CatGuardState {
	private final int nearbyCount;
	private final double nearestDistance;

	public static final CatGuardState EMPTY = new CatGuardState(0, Double.POSITIVE_INFINITY);

	public CatGuardState(int nearbyCount, double nearestDistance) {
		this.nearbyCount = nearbyCount;
		this.nearestDistance = nearestDistance;
	}

	public int nearbyCount() {
		return nearbyCount;
	}

	public double nearestDistance() {
		return nearestDistance;
	}

	public boolean hasThreat() {
		return nearbyCount > 0;
	}
}
