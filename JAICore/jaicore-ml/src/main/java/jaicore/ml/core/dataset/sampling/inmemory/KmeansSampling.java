package jaicore.ml.core.dataset.sampling.inmemory;

import java.util.List;

import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.apache.commons.math3.ml.distance.DistanceMeasure;
import org.apache.commons.math3.ml.distance.ManhattanDistance;
import org.apache.commons.math3.random.JDKRandomGenerator;

import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.basic.algorithm.exceptions.AlgorithmException;
import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.IInstance;
import jaicore.ml.core.dataset.sampling.SampleElementAddedEvent;

/**
 * Implementation of a sampling method using kmeans-clustering. This algorithm
 * produces clusters of the given points and checks weather all points in a
 * cluster have the same target Attribute. If yes only the point nearest to the
 * center is added, otherwise the whole cluster is added to the sample.
 * <p>
 * Caution: This does ignore the given sample size!
 * 
 * @author jnowack
 *
 */
public class KmeansSampling<I extends IInstance> extends ASamplingAlgorithm<I> {

	private List<CentroidCluster<I>> clusterResults = null;
	private int currentCluster = 0;

	private DistanceMeasure distanceMeassure = new ManhattanDistance();
	private long seed;
	/* number of clusters, if -1 use sample size */
	private int k;

	/**
	 * Implementation of a sampling method using kmeans-clustering.
	 * 
	 * @param seed
	 *            RAndom Seed
	 * @param k
	 *            number of clusters
	 */
	public KmeansSampling(long seed, int k, IDataset<I> input) {
		super(input);
		this.seed = seed;
		this.k = k;
	}

	/**
	 * Implementation of a sampling method using kmeans-clustering. The sample size
	 * will be used as the number of clusters.
	 * 
	 * @param seed
	 *            Random Seed
	 * @param dis
	 *            {@link DistanceMeasure} to be used
	 */
	public KmeansSampling(long seed, DistanceMeasure dis, IDataset<I> input) {
		super(input);
		this.seed = seed;
		this.k = -1;
		this.distanceMeassure = dis;
	}

	/**
	 * Implementation of a sampling method using kmeans-clustering.
	 * 
	 * @param seed
	 *            Random Seed
	 * @param k
	 *            number of clusters
	 * @param dis
	 *            {@link DistanceMeasure} to be used
	 */
	public KmeansSampling(long seed, int k, DistanceMeasure dis, IDataset<I> input) {
		super(input);
		this.seed = seed;
		this.k = k;
		this.distanceMeassure = dis;
	}

	public void setDistanceMeassure(DistanceMeasure distanceMeassure) {
		this.distanceMeassure = distanceMeassure;
	}

	@Override
	public AlgorithmEvent nextWithException() throws AlgorithmException {
		switch (this.getState()) {
		case created:
			// Initialize variables
			this.sample = getInput().createEmpty();

			// create cluster
			JDKRandomGenerator r = new JDKRandomGenerator();
			r.setSeed(seed);
			// update k if k=-1
			if (k == -1) {
				k = sampleSize;
			}
			if (clusterResults == null) {
				KMeansPlusPlusClusterer<I> kMeansCluster = new KMeansPlusPlusClusterer<I>(k, -1, distanceMeassure, r);
				clusterResults = kMeansCluster.cluster(getInput());
			}

			return this.activate();
		case active:
			if (currentCluster < clusterResults.size()) {
				CentroidCluster<I> cluster = clusterResults.get(currentCluster++);
				boolean same = true;
				for (int i = 1; i < cluster.getPoints().size(); i++) {
					if (!cluster.getPoints().get(i - 1).getTargetValue(Double.class)
							.equals(cluster.getPoints().get(i).getTargetValue(Double.class))) {
						same = false;
						break;
					}
				}
				if (same) {
					I near = cluster.getPoints().get(0);
					double dist = Double.MAX_VALUE;
					for (I p : cluster.getPoints()) {
						double newDist = distanceMeassure.compute(p.getPoint(), cluster.getCenter().getPoint());
						if (newDist < dist) {
							near = p;
							dist = newDist;
						}
					}
					sample.add(near);
				} else {
					// find a solution to not sample all points here
					for (int i = 0; i < cluster.getPoints().size(); i++) {
						sample.add(cluster.getPoints().get(i));
					}
				}
				return new SampleElementAddedEvent(getId());
			} else {
				return this.terminate();
			}
		case inactive: {
			if (this.sample.size() < this.sampleSize) {
				throw new AlgorithmException("Expected sample size was not reached before termination");
			} else {
				return this.terminate();
			}
		}
		default:
			throw new IllegalStateException("Unknown algorithm state " + this.getState());
		}
	}

	public List<CentroidCluster<I>> getClusterResults() {
		return clusterResults;
	}

	public void setClusterResults(List<CentroidCluster<I>> clusterResults) {
		this.clusterResults = clusterResults;
	}

}
