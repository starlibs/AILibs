package jaicore.ml.ranking.clusterbased.modifiedisac;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Helen
 *
 * @param <A> The to cluster points
 * @param <D> The Type of the distance measure.
 */
public abstract class Kmeans<A,D> {
	protected List<A> points;
	protected List<A> center;
	protected int k;
	protected IDistanceMetric<D, A, A> metric;

	public Kmeans(final List<A> toClusterPoints, final IDistanceMetric<D, A, A> dist) {
		this.points = toClusterPoints;
		this.metric = dist;
		this.center = new ArrayList<>();
	}

	public abstract Map<double[], List<double[]>> kmeanscluster(int k);

	public abstract void initializeKMeans();
}
