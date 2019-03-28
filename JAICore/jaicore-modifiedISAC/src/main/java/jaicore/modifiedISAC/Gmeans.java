package jaicore.modifiedISAC;

import java.util.ArrayList;

/**
 * @author Helen
 *The gmeans clustering algorithm needs no fix number of cluster center.
 * @param <A> The list of points that are to cluster
 * @param <D> The list of found centers
 */
public abstract class Gmeans<A, D> {
	public ArrayList<A> points;
	public ArrayList<A> center;

	public Gmeans(ArrayList<A> toClusterPoints) {
		this.points = toClusterPoints;
		this.center = new ArrayList<A>();
	}

	/** The clustering method uses the to cluster points and produces a list of 
	 * 	cluster each consisting of a center and a list of its related points.
	 * @return The list of found Cluster.
	 */
	public abstract ArrayList<Cluster> gmeanscluster();

}
