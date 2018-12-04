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
	public <T> IAttributeValue<T> getAttributeValue(int position, Class<T> type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> IAttributeValue<T> getTargetValue(Class<T> type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double[] getAsDoubleVector() throws ContainsNonNumericAttributesException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator<Dyad> iterator() {
		return dyads.iterator();
	}

	@Override
	public Dyad getDyadAtPosition(int position) {
		return dyads.get(position);
	}

}
