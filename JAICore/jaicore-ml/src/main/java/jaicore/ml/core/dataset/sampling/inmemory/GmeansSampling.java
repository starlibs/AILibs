package jaicore.ml.core.dataset.sampling.inmemory;

import org.apache.commons.math3.ml.distance.DistanceMeasure;

import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.basic.algorithm.exceptions.AlgorithmException;
import jaicore.ml.clustering.GMeans;
import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.IInstance;

/**
 * Implementation of a sampling method using gmeans-clustering. This algorithm
 * produces clusters of the given points and checks weather all points in a
 * cluster have the same target Attribute. If yes only the point nearest to the
 * center is added, otherwise the whole cluster is added to the sample.
 * <p>
 * Caution: This does ignore the given sample size!
 * 
 * @author jnowack
 *
 */
public class GmeansSampling<I extends IInstance> extends ClusterSampling<I> {

	public GmeansSampling(long seed, DistanceMeasure dist, IDataset<I> input) {
		super(seed, dist, input);
	}

	public GmeansSampling(long seed, IDataset<I> input) {
		super(seed, input);
	}

	@Override
	public AlgorithmEvent nextWithException() throws AlgorithmException {
		switch (this.getState()) {
		case created:
			// Initialize variables
			this.sample = getInput().createEmpty();

			if (this.clusterResults == null) {
				// create cluster
				GMeans<I> gMeansCluster = new GMeans<>(getInput(), distanceMeassure, seed);
				clusterResults = gMeansCluster.cluster();
			}

			return this.activate();
		case active:
			this.doAlgorithmStep();
			break;
		case inactive:
			this.doInactiveStep();
			break;
		default:
			throw new IllegalStateException("Unknown algorithm state " + this.getState());
		}
		return null;
	}

}
