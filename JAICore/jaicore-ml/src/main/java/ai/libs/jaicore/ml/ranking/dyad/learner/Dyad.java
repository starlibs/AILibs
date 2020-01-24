package ai.libs.jaicore.ml.ranking.dyad.learner;

import org.api4.java.ai.ml.ranking.dyad.dataset.IDyad;
import org.api4.java.common.math.IVector;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

/**
 * Represents a dyad consisting of an instance and an alternative, represented
 * by feature vectors.
 *
 * @author Helena Graf
 *
 */
public class Dyad implements IDyad {

	/* The 'x' value of the dyad */
	private IVector context;

	/* The 'y' value of the dyad */
	private IVector alternative;

	/**
	 * Construct a new dyad consisting of the given instance and alternative.
	 *
	 * @param instance
	 *            The instance
	 * @param alternative
	 *            The alternative
	 */
	public Dyad(final IVector instance, final IVector alternative) {
		this.context = instance;
		this.alternative = alternative;
	}

	/**
	 * Get the instance.
	 *
	 * @return the instance
	 */
	@Override
	public IVector getContext() {
		return this.context;
	}

	/**
	 * Get the alternative.
	 *
	 * @return the alternative
	 */
	@Override
	public IVector getAlternative() {
		return this.alternative;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Dyad (");
		builder.append("instance (");
		builder.append(this.context);
		builder.append(")");
		builder.append("alternative (");
		builder.append(this.alternative);
		builder.append(")");
		builder.append(")");
		return builder.toString();
	}

	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof Dyad)) {
			return false;
		}

		Dyad other = (Dyad) o;

		if (((this.context != null && other.context != null && this.alternative != null && other.alternative != null) && other.context.equals(this.context) && other.alternative.equals(this.alternative))) {
			return (this.context.equals(other.context) && this.alternative.equals(other.alternative));
		} else if ((this.context == null && other.context == null && this.alternative == null && other.alternative == null)) {
			return true;
		}

		return false;
	}

	@Override
	public int hashCode() {
		int result = 42;
		result = result * 31 + this.context.hashCode();
		result = result * 31 + this.alternative.hashCode();
		return result;
	}

	/**
	 * Converts a dyad to a {@link INDArray} row vector consisting of a
	 * concatenation of the instance and alternative features.
	 *
	 * @return The dyad in {@link INDArray} row vector form.
	 */
	public INDArray toVector() {
		INDArray instanceOfDyad = Nd4j.create(this.getContext().asArray());
		INDArray alternativeOfDyad = Nd4j.create(this.getAlternative().asArray());
		return Nd4j.hstack(instanceOfDyad, alternativeOfDyad);
	}

	@Override
	public double[] toDoubleVector() {
		double[] array = new double[this.getContext().length() + this.getAlternative().length()];
		this.getContext().asArray();
		System.arraycopy(array, 0, this.getContext(), 0, this.getContext().length());
		return array;
	}
}
