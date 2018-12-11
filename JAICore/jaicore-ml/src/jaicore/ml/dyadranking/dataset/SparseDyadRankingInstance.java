package jaicore.ml.dyadranking.dataset;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import de.upb.isys.linearalgebra.Vector;
import jaicore.ml.core.dataset.ContainsNonNumericAttributesException;
import jaicore.ml.core.dataset.attribute.IAttributeValue;
import jaicore.ml.core.dataset.attribute.primitive.NumericAttributeType;
import jaicore.ml.core.dataset.attribute.primitive.NumericAttributeValue;
import jaicore.ml.dyadranking.Dyad;

/**
 * A dyad ranking instance implementation that assumes the same instance for all
 * dyads contained in its ordering. It saves the instance and alternatives
 * separately and contstructs dyads from them on request.
 * 
 * @author Helena Graf, Mirko Jürgens
 *
 */
public class SparseDyadRankingInstance implements IDyadRankingInstance {

	/* The 'x' value for this instance */
	private Vector instance;

	/* The 'y' value for this instance */
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

	@SuppressWarnings("unchecked")
	@Override
	public <T> IAttributeValue<T> getAttributeValue(int position, Class<T> type) {
		if (type.equals(Double.class)) {
			return (IAttributeValue<T>) new NumericAttributeValue(new NumericAttributeType(),
					instance.getValue(position));
		} else {
			throw new IllegalArgumentException("Sparse dyad ranking instances only have attributes of type double.");
		}
	}

	@Override
	public <T> IAttributeValue<T> getTargetValue(Class<T> type) {
		throw new UnsupportedOperationException(
				"Cannot get the target value of a sparse dyad ranking instance as the target is the ordering of the dyads.");
	}

	@Override
	public double[] getAsDoubleVector() throws ContainsNonNumericAttributesException {
		throw new UnsupportedOperationException(
				"Sparse dyad ranking instances cannot be converted to a double vector since the target type is an ordering of dyads.");
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
		return alternatives.size();
	}

}
