package jaicore.search.testproblems.enhancedttsp;

import it.unimi.dsi.fastutil.shorts.ShortList;

public class EnhancedTTSPNode {
	private final short curLocation;
	private final ShortList curTour;
	private final double time;
	private final double timeTraveledSinceLastShortBreak;
	private final double timeTraveledSinceLastLongBreak;

	public EnhancedTTSPNode(short curLocation, ShortList curTour, double time, double timeTraveledSinceLastShortBreak, double timeTraveledSinceLastLongBreak) {
		super();
		this.curLocation = curLocation;
		this.curTour = curTour;
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

	public double getTime() {
		return time;
	}

	public double getTimeTraveledSinceLastShortBreak() {
		return timeTraveledSinceLastShortBreak;
	}

	public double getTimeTraveledSinceLastLongBreak() {
		return timeTraveledSinceLastLongBreak;
	}

	public ShortList getCurTour() {
		return curTour;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + curLocation;
		result = prime * result + ((curTour == null) ? 0 : curTour.hashCode());
		long temp;
		temp = Double.doubleToLongBits(time);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(timeTraveledSinceLastLongBreak);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(timeTraveledSinceLastShortBreak);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EnhancedTTSPNode other = (EnhancedTTSPNode) obj;
		if (curLocation != other.curLocation)
			return false;
		if (curTour == null) {
			if (other.curTour != null)
				return false;
		} else if (!curTour.equals(other.curTour))
			return false;
		if (Double.doubleToLongBits(time) != Double.doubleToLongBits(other.time))
			return false;
		if (Double.doubleToLongBits(timeTraveledSinceLastLongBreak) != Double.doubleToLongBits(other.timeTraveledSinceLastLongBreak))
			return false;
		if (Double.doubleToLongBits(timeTraveledSinceLastShortBreak) != Double.doubleToLongBits(other.timeTraveledSinceLastShortBreak))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "EnhancedTTSPNode [curLocation=" + curLocation + ", curTour=" + curTour + ", time=" + time + ", timeTraveledSinceLastShortBreak=" + timeTraveledSinceLastShortBreak
				+ ", timeTraveledSinceLastLongBreak=" + timeTraveledSinceLastLongBreak + "]";
	}
}
