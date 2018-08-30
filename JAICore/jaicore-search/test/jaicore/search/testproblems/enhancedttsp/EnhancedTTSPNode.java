package jaicore.search.testproblems.enhancedttsp;

import java.util.Set;

public class EnhancedTTSPNode {
	private final short curLocation;
	private final Set<Short> unvisitedLocations;
	private final double time;
	private final double timeTraveledSinceLastShortBreak;
	private final double timeTraveledSinceLastLongBreak;

	public EnhancedTTSPNode(short curLocation, Set<Short> unvisitedLocations, double time,
			double timeTraveledSinceLastShortBreak, double timeTraveledSinceLastLongBreak) {
		super();
		this.curLocation = curLocation;
		this.unvisitedLocations = unvisitedLocations;
		assert time >= 0 : "Cannot create TTSP node with negative time";
		assert timeTraveledSinceLastShortBreak >= 0 : "Cannot create TTSP node with negative time since last short break";
		assert timeTraveledSinceLastLongBreak >= 0 : "Cannot create TTSP node with negative time since last long break";
		this.time = time;
		this.timeTraveledSinceLastShortBreak = timeTraveledSinceLastShortBreak;
		this.timeTraveledSinceLastLongBreak = timeTraveledSinceLastLongBreak;
	}

	public short getCurLocation() {
		return curLocation;
	}

	public Set<Short> getUnvisitedLocations() {
		return unvisitedLocations;
	}

	public double getTime() {
		return time;
	}

	public double getTimeTraveledSinceLastShortBreak() {
		return timeTraveledSinceLastShortBreak;
	}

	public double getTimeTraveledSinceLastLongBreak() {
		return timeTraveledSinceLastLongBreak;
	}
}
