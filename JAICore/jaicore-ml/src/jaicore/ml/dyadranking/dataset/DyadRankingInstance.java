package jaicore.ml.dyadranking.dataset;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import jaicore.ml.core.dataset.ContainsNonNumericAttributesException;
import jaicore.ml.core.dataset.attribute.IAttributeValue;
import jaicore.ml.dyadranking.Dyad;

/**
 * A general implementation of a dyad ranking instance that contains an
 * immutable list of dyad to represent the ordering of dyads.
 * 
 * @author Helena Graf
 *
 */
public class DyadRankingInstance implements IDyadRankingInstance {

	/* the ordering of dyads kept by this instance */
	private List<Dyad> dyads;

	/**
	 * Construct a new dyad ranking instance that saves the given ordering of dyads
	 * immutably.
	 * 
	 * @param dyads
	 *            the ordering of dyads to be stored in this instance
	 */
	public DyadRankingInstance(List<Dyad> dyads) {
		this.dyads = Collections.unmodifiableList(dyads);
	}

	@Override
	public <T> IAttributeValue<T> getAttributeValue(int position, Class<T> type) {
		throw new UnsupportedOperationException(
				"Cannot get the attribute value for a dyad ranking instance since each dyad has separate attributes.");
	}

	@Override
	public <T> IAttributeValue<T> getTargetValue(Class<T> type) {
		throw new UnsupportedOperationException(
				"Cannot get the target value of a dyad ranking instance since the target is an ordering of dyads.");
	}

	@Override
	public double[] getAsDoubleVector() throws ContainsNonNumericAttributesException {
		throw new UnsupportedOperationException(
				"Dyad ranking instances cannot be converted to a double vector since the target type is an ordering of dyads.");
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
		if (!(o instanceof DyadRankingInstance)) {
			return false;
		}

		DyadRankingInstance dRInstance = (DyadRankingInstance) o;

		if (!dyads.equals(dRInstance.dyads)) {
			return false;
		}

		return super.equals(o);
	}

	@Override
	public int hashCode() {
		int result = 42;
		result = result * 31 + dyads.hashCode();
		return result;
	}

}
