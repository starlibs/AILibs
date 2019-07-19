package ai.libs.jaicore.ml.core.dataset.sampling.inmemory.stratified.sampling;

import org.apache.commons.math3.ml.clustering.Clusterable;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.apache.commons.math3.ml.distance.DistanceMeasure;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.api4.java.ai.ml.core.dataset.IDataset;
import org.api4.java.ai.ml.core.dataset.INumericArrayInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cluster the data set with k-means into k Clusters, where each cluster stands
 * for one stratum. The datapoint assignment is performed with a lookup in the
 * clusters.
 *
 * @author Lukas Brandt
 */
public class KMeansStratiAssigner<I extends INumericArrayInstance & Clusterable, D extends IDataset<I>> extends ClusterStratiAssigner<I, D> {

	private Logger logger = LoggerFactory.getLogger(KMeansStratiAssigner.class);
	/**
	 * Constructor for KMeansStratiAssigner.
	 *
	 * @param distanceMeasure
	 *            Distance measure for datapoints, for example Manhattan or
	 *            Euclidian.
	 * @param randomSeed
	 *            Seed for random numbers.
	 */
	public KMeansStratiAssigner(final DistanceMeasure distanceMeasure, final int randomSeed) {
		this.randomSeed = randomSeed;
		this.distanceMeasure = distanceMeasure;
	}

	@Override
	public void init(final D dataset, final int stratiAmount) {
		// Perform initial Clustering of the dataset.
		JDKRandomGenerator rand = new JDKRandomGenerator();
		rand.setSeed(this.randomSeed);
		KMeansPlusPlusClusterer<I> clusterer = new KMeansPlusPlusClusterer<>(stratiAmount, -1, this.distanceMeasure, rand);
		this.logger.info("Clustering dataset with {} instances.", dataset.size());
		this.clusters = clusterer.cluster(dataset);
		this.logger.info("Finished clustering");
	}

}
