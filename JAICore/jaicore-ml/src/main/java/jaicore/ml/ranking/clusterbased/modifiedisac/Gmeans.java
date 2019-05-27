package jaicore.ml.ranking.clusterbased.modifiedisac;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Helen
 *         The gmeans clustering algorithm needs no fix number of cluster center.
 * @param <A> The type of points to cluster
 */
public abstract class Gmeans<A> {
	protected final List<A> points;
	protected final List<A> center;

	public Gmeans(final List<A> toClusterPoints) {
		this.points = toClusterPoints;
		this.center = new ArrayList<>();
	}

	/**
	 * The clustering method uses the to cluster points and produces a list of
	 * cluster each consisting of a center and a list of its related points.
	 *
	 * @return The list of found Cluster.
	 */
	public abstract List<Cluster> gmeanscluster();

	public List<A> getPoints() {
		return this.points;
	}

	public List<A> getCenter() {
		return this.center;
	}
}
