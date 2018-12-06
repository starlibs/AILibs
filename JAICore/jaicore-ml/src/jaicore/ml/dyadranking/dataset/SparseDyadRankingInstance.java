package jaicore.ml.dyadranking.dataset;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import de.upb.isys.linearalgebra.Vector;
import jaicore.ml.core.dataset.ContainsNonNumericAttributesException;
import jaicore.ml.core.dataset.attribute.IAttributeValue;
import jaicore.ml.dyadranking.Dyad;

/**
 * A dyad ranking instance implementation that assumes the same instance for all
 * dyads contained in its ordering. It saves the instance and alternatives
 * separately and contstructs dyads from them on request.
 * 
 * @author Helena Graf
 *
 */
public class SparseDyadRankingInstance implements IDyadRankingInstance {

	private Vector instance;
	private List<Vector> alternatives;

	/**
	 * Construct a new sparse dyad ranking instance containing the given instance
	 * vector and ordering of alternatives.
	 * 
	 * @param instance
	 *            the instance for all of the alternatives
	 * @param alternatives
	 *            the ordering of alternatives that, when combined with the
	 *            instances is an ordering of dyads
	 */
	public SparseDyadRankingInstance(Vector instance, List<Vector> alternatives) {
		this.instance = instance;
		this.alternatives = Collections.unmodifiableList(alternatives);
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
		return new Iterator<Dyad>() {

			int index = 0;

			@Override
			public boolean hasNext() {
				return index < alternatives.size();
			}

			@Override
			public Dyad next() {
				if (!hasNext()) {
					throw new NoSuchElementException();
				}

				return getDyadAtPosition(index++);
			}
		};
	}

	@Override
	public Dyad getDyadAtPosition(int position) {
		return new Dyad(instance, alternatives.get(position));
	}

	@Override
	public int length() {
		// TODO Auto-generated method stub
		return 0;
	}

}
