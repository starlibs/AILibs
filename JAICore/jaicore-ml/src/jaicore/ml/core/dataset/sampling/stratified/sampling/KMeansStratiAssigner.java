package jaicore.ml.core.dataset.sampling.stratified.sampling;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.apache.commons.math3.ml.distance.DistanceMeasure;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.IInstance;

/**
 * Cluster the data set with k-means into k Clusters, where each cluster stands
 * for one stratum. The datapoint assignment is performed with a lookup in the
 * clusters.
 * 
 * @author Lukas Brandt
 */
public class KMeansStratiAssigner implements IStratiAssigner {

	private static Logger LOG = LoggerFactory.getLogger(KMeansStratiAssigner.class);

	private int randomSeed;
	private KMeansPlusPlusClusterer<IInstance> clusterer;
	private DistanceMeasure distanceMeasure;
	private List<CentroidCluster<IInstance>> clusters;

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
	public void init(IDataset dataset, int stratiAmount) {
		// Perform initial Clustering of the dataset.
		this.clusterer = new KMeansPlusPlusClusterer<>(stratiAmount, -1, this.distanceMeasure,
				new JDKRandomGenerator(this.randomSeed));
		this.clusters = this.clusterer.cluster(dataset);
	}

	@Override
	public int assignToStrati(IInstance datapoint) {
		// Search for the cluster that contains the datapoint.
		for (int i = 0; i < this.clusters.size(); i++) {
			List<IInstance> clusterPoints = this.clusters.get(i).getPoints();
			for (int n = 0; n < clusterPoints.size(); n++) {
				if (Arrays.equals(datapoint.getPoint(), clusterPoints.get(n).getPoint())) {
					return i;
				}
			}
		}
		throw new Error("Datapoint was not found in any cluster. This should not happen.");
	}

	@Override
	public void setNumCPUs(int numberOfCPUs) {
		LOG.warn("setNumCPUs() is not supported for this class");
	}

	@Override
	public int getNumCPUs() {
		return 1;
	}

}
