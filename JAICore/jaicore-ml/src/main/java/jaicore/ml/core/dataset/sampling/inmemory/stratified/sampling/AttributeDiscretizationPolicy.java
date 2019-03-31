package jaicore.ml.core.dataset.sampling.inmemory.stratified.sampling;

import java.util.List;

public class AttributeDiscretizationPolicy {

	private List<Interval> intervals;

	public AttributeDiscretizationPolicy(List<Interval> intervals) {
		super();
		this.intervals = intervals;
	}

	public List<Interval> getIntervals() {
		return intervals;
	}

	public void setIntervals(List<Interval> intervals) {
		this.intervals = intervals;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((intervals == null) ? 0 : intervals.hashCode());
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
		AttributeDiscretizationPolicy other = (AttributeDiscretizationPolicy) obj;
		if (intervals == null) {
			if (other.intervals != null) {
				return false;
			}
		} else if (!intervals.equals(other.intervals)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "AttributeDiscretizationPolicy [intervals=" + intervals + "]";
	}

}
