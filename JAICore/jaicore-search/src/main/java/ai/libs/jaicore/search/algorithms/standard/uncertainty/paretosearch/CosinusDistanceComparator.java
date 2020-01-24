package ai.libs.jaicore.search.algorithms.standard.uncertainty.paretosearch;

import java.util.Comparator;

import ai.libs.jaicore.search.algorithms.standard.bestfirst.ENodeAnnotation;
import ai.libs.jaicore.search.model.travesaltree.BackPointerPath;

public class CosinusDistanceComparator<T, A, V extends Comparable<V>> implements Comparator<BackPointerPath<T, A, V>> {

	public final double x1;
	public final double x2;

	public CosinusDistanceComparator(final double x1, final double x2) {
		this.x1 = x1;
		this.x2 = x2;
	}

	/**
	 * Compares the cosine distance of two nodes to x.
	 *
	 * @param first
	 * @param second
	 * @return negative iff first < second, 0 iff first == second, positive iff first > second
	 */
	@Override
	public int compare(final BackPointerPath<T, A, V> first, final BackPointerPath<T, A, V> second) {

		Double firstF = (Double) first.getAnnotation(ENodeAnnotation.F_SCORE.name());
		Double firstU = (Double) first.getAnnotation(ENodeAnnotation.F_UNCERTAINTY.name());

		Double secondF = (Double) second.getAnnotation(ENodeAnnotation.F_SCORE.name());
		Double secondU = (Double) second.getAnnotation(ENodeAnnotation.F_UNCERTAINTY.name());

		double cosDistanceFirst = 1 - this.cosineSimilarity(firstF, firstU);
		double cosDistanceSecond = 1 - this.cosineSimilarity(secondF, secondU);

		return (int) ((cosDistanceFirst - cosDistanceSecond) * 10000);
	}

	/**
	 * Cosine similarity to x.
	 *
	 * @param f
	 * @param u
	 * @return
	 */
	public double cosineSimilarity(final double f, final double u) {
		return (this.x1 * f + this.x2 * u) / (Math.sqrt(f * f + u * u) * Math.sqrt(this.x1 * this.x1 + this.x2 * this.x2));
	}

}
