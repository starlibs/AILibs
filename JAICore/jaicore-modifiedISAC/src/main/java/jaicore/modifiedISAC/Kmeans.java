package jaicore.modifiedISAC;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Helen
 *
 * @param <A> The to cluster points 
 * @param <D> The Type of the distance measure.
 */
abstract public class Kmeans<A,D> {
	public ArrayList<A> points;
	public ArrayList<A> center;
	public int k;
	public IDistanceMetric<D, A, A> metric;

	public Kmeans(ArrayList<A> toClusterPoints, IDistanceMetric<D, A, A> dist) {
		this.points = toClusterPoints;
		this.metric = dist;
		this.center = new ArrayList<A>();
	}

	public abstract HashMap<double[], ArrayList<double[]>> kmeanscluster(int k);

	public abstract void initializeKMeans();
}
