package jaicore.ml.dyadranking;

import de.upb.isys.linearalgebra.Vector;

/**
 * Represents a dyad consisting of an instance and an alternative, represented
 * by feature vectors.
 * 
 * @author Helena Graf
 *
 */
public class Dyad {

	private Vector instance;
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
		builder.append(")");
		builder.append(")");
		return builder.toString();
	}
}
