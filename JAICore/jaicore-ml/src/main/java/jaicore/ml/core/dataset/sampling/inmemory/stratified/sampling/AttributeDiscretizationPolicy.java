package jaicore.ml.core.dataset.sampling.inmemory.stratified.sampling;

import java.util.List;

import org.apache.commons.math3.geometry.euclidean.oned.Interval;

public class AttributeDiscretizationPolicy {

	private List<Interval> intervals;

	public AttributeDiscretizationPolicy(final List<Interval> intervals) {
		super();
		this.intervals = intervals;
	}

	public List<Interval> getIntervals() {
		return this.intervals;
	}

	public void setIntervals(final List<Interval> intervals) {
		this.intervals = intervals;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.intervals == null) ? 0 : this.intervals.hashCode());
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
		AttributeDiscretizationPolicy other = (AttributeDiscretizationPolicy) obj;
		if (this.intervals == null) {
			if (other.intervals != null) {
				return false;
			}
		} else if (!this.intervals.equals(other.intervals)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "AttributeDiscretizationPolicy [intervals=" + this.intervals + "]";
	}

}
