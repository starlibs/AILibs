package jaicore.testproblems.enhancedttsp;

import it.unimi.dsi.fastutil.shorts.ShortList;
import it.unimi.dsi.fastutil.shorts.ShortLists;

public class EnhancedTTSPNode {
	private final short curLocation;
	private final ShortList curTour;
	private final double time;
	private final double timeTraveledSinceLastShortBreak;
	private final double timeTraveledSinceLastLongBreak;

	public EnhancedTTSPNode(final short curLocation, final ShortList curTour, final double time, final double timeTraveledSinceLastShortBreak, final double timeTraveledSinceLastLongBreak) {
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
		return this.curLocation;
	}

	public double getTime() {
		return this.time;
	}

	public double getTimeTraveledSinceLastShortBreak() {
		return this.timeTraveledSinceLastShortBreak;
	}

	public double getTimeTraveledSinceLastLongBreak() {
		return this.timeTraveledSinceLastLongBreak;
	}

	public ShortList getCurTour() {
		return ShortLists.unmodifiable(this.curTour);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + this.curLocation;
		result = prime * result + ((this.curTour == null) ? 0 : this.curTour.hashCode());
		long temp;
		temp = Double.doubleToLongBits(this.time);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(this.timeTraveledSinceLastLongBreak);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(this.timeTraveledSinceLastShortBreak);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		EnhancedTTSPNode other = (EnhancedTTSPNode) obj;
		if (this.curLocation != other.curLocation) {
			return false;
		}
		if (this.curTour == null) {
			if (other.curTour != null) {
				return false;
			}
		} else if (!this.curTour.equals(other.curTour)) {
			return false;
		}
		if (Double.doubleToLongBits(this.time) != Double.doubleToLongBits(other.time)) {
			return false;
		}
		if (Double.doubleToLongBits(this.timeTraveledSinceLastLongBreak) != Double.doubleToLongBits(other.timeTraveledSinceLastLongBreak)) {
			return false;
		}
		if (Double.doubleToLongBits(this.timeTraveledSinceLastShortBreak) != Double.doubleToLongBits(other.timeTraveledSinceLastShortBreak)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "EnhancedTTSPNode [curLocation=" + this.curLocation + ", curTour=" + this.curTour + ", time=" + this.time + ", timeTraveledSinceLastShortBreak=" + this.timeTraveledSinceLastShortBreak + ", timeTraveledSinceLastLongBreak="
				+ this.timeTraveledSinceLastLongBreak + "]";
	}
}
