package ai.libs.jaicore.ml.core.filter.sampling.inmemory.stratified.sampling;

import java.util.List;

import org.apache.commons.math3.ml.clustering.Clusterable;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.apache.commons.math3.ml.distance.DistanceMeasure;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.api4.java.ai.ml.core.dataset.IDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cluster the data set with k-means into k Clusters, where each cluster stands
 * for one stratum. The datapoint assignment is performed with a lookup in the
 * clusters.
 *
 * @author Lukas Brandt
 */
public class KMeansStratiAssigner extends ClusterStratiAssigner {

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
	public void init(final IDataset<?> dataset, final int stratiAmount) {
		this.setDataset(dataset);

		// Perform initial Clustering of the dataset.
		JDKRandomGenerator rand = new JDKRandomGenerator();
		rand.setSeed(this.randomSeed);
		List<Clusterable> cDataset = (List<Clusterable>)dataset;
		KMeansPlusPlusClusterer<Clusterable> clusterer = new KMeansPlusPlusClusterer<>(stratiAmount, -1, this.distanceMeasure, rand);
		this.logger.info("Clustering dataset with {} instances.", dataset.size());
		this.setClusters(clusterer.cluster(cDataset));
		this.logger.info("Finished clustering");
	}

}
