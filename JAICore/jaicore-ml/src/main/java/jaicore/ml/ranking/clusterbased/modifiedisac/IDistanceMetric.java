package jaicore.ml.ranking.clusterbased.modifiedisac;

/**
 * @author Helen
 *
 *         Computes a distance of type D between a starting point of type A to an ending point B
 *
 * @param <D> The type of the distance
 * @param <A> The type of the starting point
 * @param <B> The type of the ending point
 */
public interface IDistanceMetric<D, A, B> {
	public D computeDistance(A start, B end);
}
