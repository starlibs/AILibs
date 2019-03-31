package jaicore.ml.core.dataset.sampling.inmemory.stratified.sampling;

import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.apache.commons.math3.ml.distance.DistanceMeasure;
import org.apache.commons.math3.random.JDKRandomGenerator;

import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.IInstance;

/**
 * Cluster the data set with k-means into k Clusters, where each cluster stands
 * for one stratum. The datapoint assignment is performed with a lookup in the
 * clusters.
 * 
 * @author Lukas Brandt
 */
public class KMeansStratiAssigner<I extends IInstance> extends ClusterStratiAssigner<I> {

	/**
	 * Constructor for KMeansStratiAssigner.
	 * 
	 * @param distanceMeasure
	 *            Distance measure for datapoints, for example Manhattan or
	 *            Euclidian.
	 * @param randomSeed
	 *            Seed for random numbers.
	 */
	public KMeansStratiAssigner(DistanceMeasure distanceMeasure, int randomSeed) {
		this.randomSeed = randomSeed;
		this.distanceMeasure = distanceMeasure;
	}

	@Override
	public void init(IDataset<I> dataset, int stratiAmount) {
		// Perform initial Clustering of the dataset.
		JDKRandomGenerator rand = new JDKRandomGenerator();
		rand.setSeed(this.randomSeed);
		KMeansPlusPlusClusterer<I> clusterer = new KMeansPlusPlusClusterer<>(stratiAmount, -1, this.distanceMeasure,
				rand);
		this.clusters = clusterer.cluster(dataset);
	}

}
