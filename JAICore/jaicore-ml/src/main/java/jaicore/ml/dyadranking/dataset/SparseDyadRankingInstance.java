package jaicore.ml.dyadranking.dataset;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import de.upb.isys.linearalgebra.Vector;
import jaicore.ml.core.dataset.ContainsNonNumericAttributesException;
import jaicore.ml.core.dataset.INumericArrayInstance;
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
public class SparseDyadRankingInstance extends ADyadRankingInstance implements INumericArrayInstance {

	/* The 'x' value for this instance */
	private Vector instance;

	/* The 'y' value for this instance */
	private List<Vector> alternatives;

	/**
	 * Construct a new sparse dyad ranking instance containing the given instance
	 * vector and ordering of alternatives.
	 *
	 * @param instance the instance for all of the alternatives
	 * @param alternatives the ordering of alternatives that, when combined with the
	 *            instances is an ordering of dyads
	 */
	public SparseDyadRankingInstance(final Vector instance, final List<Vector> alternatives) {
		this.instance = instance;
		this.alternatives = Collections.unmodifiableList(alternatives);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> IAttributeValue<T> getAttributeValueAtPosition(final int position, final Class<T> type) {
		if (type.equals(Double.class)) {
			return (IAttributeValue<T>) this.getAttributeValue(position);
		} else {
			throw new IllegalArgumentException("Sparse dyad ranking instances only have attributes of type double.");
		}
	}

	@Override
	public SparseDyadRankingInstance getTargetValue() {
		return this;
	}

	@Override
	public double[] getAsDoubleVector() throws ContainsNonNumericAttributesException {
		throw new UnsupportedOperationException("Sparse dyad ranking instances cannot be converted to a double vector since the target type is an ordering of dyads.");
	}

	@Override
	public Iterator<Dyad> iterator() {
		return new Iterator<Dyad>() {

			private int index = 0;

			@Override
			public boolean hasNext() {
				return this.index < SparseDyadRankingInstance.this.alternatives.size();
			}

			@Override
			public Dyad next() {
				if (!this.hasNext()) {
					throw new NoSuchElementException();
				}

				return SparseDyadRankingInstance.this.getDyadAtPosition(this.index++);
			}
		};
	}

	@Override
	public Dyad getDyadAtPosition(final int position) {
		return new Dyad(this.instance, this.alternatives.get(position));
	}

	@Override
	public int length() {
		return this.alternatives.size();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SparseDyadRankingInstance: ");
		builder.append(System.lineSeparator());
		builder.append("Instance: ");
		builder.append(this.instance);
		builder.append(System.lineSeparator());
		builder.append("Alternatives: ");
		builder.append(this.alternatives);
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.alternatives == null) ? 0 : this.alternatives.hashCode());
		result = prime * result + ((this.instance == null) ? 0 : this.instance.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		SparseDyadRankingInstance other = (SparseDyadRankingInstance) obj;
		if (this.alternatives == null) {
			if (other.alternatives != null) {
				return false;
			}
		} else if (!this.alternatives.equals(other.alternatives)) {
			return false;
		}
		if (this.instance == null) {
			if (other.instance != null) {
				return false;
			}
		} else if (!this.instance.equals(other.instance)) {
			return false;
		}
		return true;
	}

	@Override
	public IAttributeValue<?>[] getAllAttributeValues() {
		throw new UnsupportedOperationException("Currently not implemented!");
	}

	@Override
	public int getNumberOfAttributes() {
		return this.instance.length();
	}

	@Override
	public IAttributeValue<Double> getAttributeValue(final int position) {
		return new NumericAttributeValue(new NumericAttributeType(), this.instance.getValue(position));
	}

}
