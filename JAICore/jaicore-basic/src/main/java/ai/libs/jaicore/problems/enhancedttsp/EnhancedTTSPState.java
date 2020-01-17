package ai.libs.jaicore.problems.enhancedttsp;

import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortList;
import it.unimi.dsi.fastutil.shorts.ShortLists;

public class EnhancedTTSPState {
	private final EnhancedTTSPState parent;
	private final short curLocation;
	private final double time;
	private final double timeTraveledSinceLastShortBreak;
	private final double timeTraveledSinceLastLongBreak;

	public EnhancedTTSPState(final EnhancedTTSPState parent, final short curLocation, final double time, final double timeTraveledSinceLastShortBreak, final double timeTraveledSinceLastLongBreak) {
		super();
		this.parent = parent;
		this.curLocation = curLocation;
		if (time < 0) {
			throw new IllegalArgumentException("Cannot create TTSP node with negative time");
		}
		if (timeTraveledSinceLastShortBreak < 0) {
			throw new IllegalArgumentException("Cannot create TTSP node with negative time since last short break");
		}
		if (timeTraveledSinceLastLongBreak < 0) {
			throw new IllegalArgumentException("Cannot create TTSP node with negative time since last long break");
		}
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

	private ShortList getCurTourRec() {
		if (this.parent == null) {
			return new ShortArrayList(); // the starting location will NOT be part of the tour!
		}
		ShortList l = this.parent.getCurTourRec();
		l.add(this.curLocation);
		return l;
	}

	public ShortList getCurTour() {
		return ShortLists.unmodifiable(this.getCurTourRec());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + this.curLocation;
		result = prime * result + ((this.getCurTour() == null) ? 0 : this.getCurTour().hashCode());
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
		EnhancedTTSPState other = (EnhancedTTSPState) obj;
		if (this.curLocation != other.curLocation) {
			return false;
		}
		if (this.getCurTour() == null) {
			if (other.getCurTour() != null) {
				return false;
			}
		} else if (!this.getCurTour().equals(other.getCurTour())) {
			return false;
		}
		if (Double.doubleToLongBits(this.time) != Double.doubleToLongBits(other.time)) {
			return false;
		}
		if (Double.doubleToLongBits(this.timeTraveledSinceLastLongBreak) != Double.doubleToLongBits(other.timeTraveledSinceLastLongBreak)) {
			return false;
		}
		return Double.doubleToLongBits(this.timeTraveledSinceLastShortBreak) == Double.doubleToLongBits(other.timeTraveledSinceLastShortBreak);
	}

	@Override
	public String toString() {
		return "EnhancedTTSPNode [curLocation=" + this.curLocation + ", curTour=" + this.getCurTour() + ", time=" + this.time + ", timeTraveledSinceLastShortBreak=" + this.timeTraveledSinceLastShortBreak + ", timeTraveledSinceLastLongBreak="
				+ this.timeTraveledSinceLastLongBreak + "]";
	}
}
