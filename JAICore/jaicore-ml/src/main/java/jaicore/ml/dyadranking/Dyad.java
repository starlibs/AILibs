package jaicore.ml.dyadranking;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import de.upb.isys.linearalgebra.Vector;

/**
 * Represents a dyad consisting of an instance and an alternative, represented
 * by feature vectors.
 * 
 * @author Helena Graf
 *
 */
public class Dyad {

	/* The 'x' value of the dyad */
	private Vector instance;

	/* The 'y' value of the dyad */
	private Vector alternative;

	/**
	 * Construct a new dyad consisting of the given instance and alternative.
	 * 
	 * @param instance
	 *            The instance
	 * @param alternative
	 *            The alternative
	 */
	public Dyad(Vector instance, Vector alternative) {
		this.instance = instance;
		this.alternative = alternative;
	}

	/**
	 * Get the instance.
	 * 
	 * @return the instance
	 */
	public Vector getInstance() {
		return instance;
	}

	/**
	 * Get the alternative.
	 * 
	 * @return the alternative
	 */
	public Vector getAlternative() {
		return alternative;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Dyad (");
		builder.append("instance (");
		builder.append(instance);
		builder.append(")");
		builder.append("alternative (");
		builder.append(alternative);
		builder.append(")");
		builder.append(")");
		return builder.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Dyad)) {
			return false;
		}

		Dyad other = (Dyad) o;

		if (((this.instance != null && other.instance != null && this.alternative != null && other.alternative != null)
				&& other.instance.equals(this.instance) && other.alternative.equals(this.alternative))) {
			return (this.instance.equals(other.instance) && this.alternative.equals(other.alternative));
		} else if ((this.instance == null && other.instance == null && this.alternative == null
				&& other.alternative == null)) {
			return true;
		}

		return false;
	}

	@Override
	public int hashCode() {
		int result = 42;
		result = result * 31 + instance.hashCode();
		result = result * 31 + alternative.hashCode();
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
}
