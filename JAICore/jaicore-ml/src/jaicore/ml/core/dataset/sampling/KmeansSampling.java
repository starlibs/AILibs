package jaicore.ml.core.dataset.sampling;

import java.util.List;

import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.apache.commons.math3.ml.distance.ManhattanDistance;
import org.apache.commons.math3.random.JDKRandomGenerator;

import jaicore.basic.algorithm.AlgorithmEvent;
import jaicore.basic.algorithm.AlgorithmFinishedEvent;
import jaicore.basic.algorithm.AlgorithmInitializedEvent;
import jaicore.basic.algorithm.AlgorithmState;
import jaicore.ml.core.dataset.IClusterableInstance;

/**
 * Implementation of a sampling method using kmeans-clustering.
 * 
 * @author jnowack
 *
 */
public class KmeansSampling extends ASamplingAlgorithm {

	private KMeansPlusPlusClusterer<IClusterableInstance> kMeansCluster;
	private List<CentroidCluster<IClusterableInstance>> clusterResults;

	/**
	 * Implementation of a sampling method using kmeans-clustering.
	 * 
	 * @param k             Number of clusters.
	 * @param maxIterations max number of iterations or -1 for no limit.
	 * @param seed          seed for random generator
	 */
	public KmeansSampling(int k, int maxIterations, int seed) {
		this.kMeansCluster = new KMeansPlusPlusClusterer<IClusterableInstance>(k, maxIterations,
				new ManhattanDistance(), new JDKRandomGenerator(seed));
	}

	@Override
	public AlgorithmEvent nextWithException() throws Exception {
		switch (this.getState()) {
		case created:
			// Initialize variables
			// TODO: create empty dataset
			this.sample = null;

			// create cluster
			clusterResults = kMeansCluster.cluster(getInput().getClusterableInstances());

			this.setState(AlgorithmState.active);
			return new AlgorithmInitializedEvent();
		case active:
			for (CentroidCluster<IClusterableInstance> cluster : clusterResults) {
				boolean same = true;
				for (int i = 1; i < cluster.getPoints().size(); i++) {
					// TODO find way to compare targetValues
					if (!cluster.getPoints().get(i - 1).getTargetValue(Object.class)
							.equals(cluster.getPoints().get(i).getTargetValue(Object.class))) {
						same = false;
						break;
					}
				}
				if (same) {
					// if all points are the same only add the center
					// sample.add(cluster.getCenter().getPoint()); // TODO create/add center point
				} else {
					for (int i = 0; i < cluster.getPoints().size(); i++) {
						sample.add(cluster.getPoints().get(i));
					}
				}

			}

		case inactive: {
			if (this.sample.size() < this.sampleSize) {
				throw new Exception("Expected sample size was not reached before termination");
			} else {
				return new AlgorithmFinishedEvent();
			}
		}
		default:
			throw new IllegalStateException("Unknown algorithm state " + this.getState());
		}
	}
}
