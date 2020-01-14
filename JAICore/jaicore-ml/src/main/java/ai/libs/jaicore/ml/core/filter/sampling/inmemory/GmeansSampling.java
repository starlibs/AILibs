package ai.libs.jaicore.ml.core.filter.sampling.inmemory;

import org.apache.commons.math3.ml.distance.DistanceMeasure;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.exception.DatasetCreationException;
import org.api4.java.algorithm.events.IAlgorithmEvent;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;

import ai.libs.jaicore.ml.clustering.learner.GMeans;
import ai.libs.jaicore.ml.core.filter.sampling.IClusterableInstance;

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
public class GmeansSampling<I extends IClusterableInstance, D extends ILabeledDataset<I>> extends ClusterSampling<I, D> {

	public GmeansSampling(final long seed, final DistanceMeasure dist, final D input) {
		super(seed, dist, input);
		if (input.size() > 1000) {
			throw new IllegalArgumentException("GMeans does not support datasets with more than 1000 points, because it has quadratic (non-interruptible) runtime.");
		}
	}

	public GmeansSampling(final long seed, final D input) {
		super(seed, input);
		if (input.size() > 1000) {
			throw new IllegalArgumentException("GMeans does not support datasets with more than 1000 points, because it has quadratic (non-interruptible) runtime.");
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public IAlgorithmEvent nextWithException() throws AlgorithmException, InterruptedException, AlgorithmTimeoutedException, AlgorithmExecutionCanceledException {
		switch (this.getState()) {
		case CREATED:
			// Initialize variables
			try {
				this.sample = (D) this.getInput().createEmptyCopy();
			} catch (DatasetCreationException e) {
				throw new AlgorithmException("Could not create a copy of the dataset.", e);
			}

			if (this.clusterResults == null) {
				// create cluster
				GMeans<I> gMeansCluster = new GMeans<>(this.getInput(), this.distanceMeassure, this.seed);
				this.clusterResults = gMeansCluster.cluster(); // this is not interruptible!!
			}
			return this.activate();
		case ACTIVE:
			return this.doAlgorithmStep();
		default:
			throw new IllegalStateException("Unknown algorithm state " + this.getState());
		}
	}

}
