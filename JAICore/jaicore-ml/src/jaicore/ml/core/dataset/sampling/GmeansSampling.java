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
import jaicore.ml.clustering.GMeans;
import jaicore.ml.core.dataset.IInstance;

/**
 * Implementation of a sampling method using gmeans-clustering.
 * 
 * @author jnowack
 *
 */
public class GmeansSampling extends ASamplingAlgorithm {

	private GMeans<IInstance> gMeansCluster;
	private List<CentroidCluster<IInstance>> clusterResults;

	/**
	 * Implementation of a sampling method using gmeans-clustering.
	 */
	public GmeansSampling() {
	}

	@Override
	public AlgorithmEvent nextWithException() throws Exception {
		switch (this.getState()) {
		case created:
			// Initialize variables
			this.sample = this.createEmptyDatasetFromInputSchema();

			// create cluster
			gMeansCluster = new GMeans<IInstance>(getInput(), new ManhattanDistance());
			clusterResults = gMeansCluster.cluster();

			this.setState(AlgorithmState.active);
			return new AlgorithmInitializedEvent();
		case active:
			for (CentroidCluster<IInstance> cluster : clusterResults) {
				boolean same = true;
				for (int i = 1; i < cluster.getPoints().size(); i++) {
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
