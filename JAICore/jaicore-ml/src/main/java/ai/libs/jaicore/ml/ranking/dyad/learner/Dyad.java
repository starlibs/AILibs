package ai.libs.jaicore.ml.ranking.dyad.learner;

import org.api4.java.ai.ml.ranking.dyad.dataset.IDyad;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import ai.libs.jaicore.math.linearalgebra.IVector;

/**
 * Represents a dyad consisting of an instance and an alternative, represented
 * by feature vectors.
 *
 * @author Helena Graf
 *
 */
public class Dyad implements IDyad {

	/* The 'x' value of the dyad */
	private IVector instance;

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
		this.instance = instance;
		this.alternative = alternative;
	}

	/**
	 * Get the instance.
	 *
	 * @return the instance
	 */
	@Override
	public IVector getInstance() {
		return this.instance;
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
		builder.append(this.instance);
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

		if (((this.instance != null && other.instance != null && this.alternative != null && other.alternative != null) && other.instance.equals(this.instance) && other.alternative.equals(this.alternative))) {
			return (this.instance.equals(other.instance) && this.alternative.equals(other.alternative));
		} else if ((this.instance == null && other.instance == null && this.alternative == null && other.alternative == null)) {
			return true;
		}

		return false;
	}

	@Override
	public int hashCode() {
		int result = 42;
		result = result * 31 + this.instance.hashCode();
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
		INDArray instanceOfDyad = Nd4j.create(this.getInstance().asArray());
		INDArray alternativeOfDyad = Nd4j.create(this.getAlternative().asArray());
		return Nd4j.hstack(instanceOfDyad, alternativeOfDyad);
	}

	@Override
	public double[] toDoubleVector() {
		double[] array = new double[this.getInstance().length() + this.getAlternative().length()];
		this.getInstance().asArray();
		System.arraycopy(array, 0, this.getInstance(), 0, this.getInstance().length());
		return array;
	}
}
