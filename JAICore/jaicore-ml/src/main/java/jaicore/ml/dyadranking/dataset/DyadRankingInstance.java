package jaicore.ml.dyadranking.dataset;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import jaicore.ml.dyadranking.Dyad;

/**
 * A general implementation of a dyad ranking instance that contains an
 * immutable list of dyad to represent the ordering of dyads.
 * 
 * The order is assumed to represent the ground truth (i.e., the label of the instance)
 * 
 * @author Helena Graf
 *
 */
public class DyadRankingInstance extends ADyadRankingInstance {

	/* the ordering of dyads kept by this instance */
	private List<Dyad> dyads;

	/**
	 * Construct a new dyad ranking instance that saves the given ordering of dyads
	 * immutably.
	 * 
	 * @param dyads the ordering of dyads to be stored in this instance
	 */
	public DyadRankingInstance(List<Dyad> dyads) {
		this.dyads = Collections.unmodifiableList(dyads);
	}

	@Override
	public Iterator<Dyad> iterator() {
		return dyads.iterator();
	}

	@Override
	public Dyad getDyadAtPosition(int position) {
		return dyads.get(position);
	}

	@Override
	public int length() {
		return dyads.size();
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof IDyadRankingInstance)) {
			return false;
		}
		
		IDyadRankingInstance drInstance = (IDyadRankingInstance) o;

		for (int i = 0; i < drInstance.length(); i++) {
			if (!(drInstance.getDyadAtPosition(i)).equals(this.getDyadAtPosition(i)))
				return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = 42;
		result = result * 31 + dyads.hashCode();
		return result;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DyadRankingInstance: ");
		builder.append(dyads);
		return builder.toString();
	}

	@Override
	public DyadRankingInstance getTargetValue() {
		return this;
	}
}
