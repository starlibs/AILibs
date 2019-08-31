package ai.libs.jaicore.ml.core.filter.sampling.inmemory;

import org.apache.commons.math3.ml.clustering.Clusterable;
import org.apache.commons.math3.ml.distance.DistanceMeasure;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.exception.DatasetCreationException;
import org.api4.java.algorithm.events.AlgorithmEvent;
import org.api4.java.algorithm.exceptions.AlgorithmException;

import ai.libs.jaicore.ml.clustering.learner.GMeans;

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
public class GmeansSampling<I extends ILabeledInstance & Clusterable, D extends ILabeledDataset<I>> extends ClusterSampling<I, D> {

	public GmeansSampling(final long seed, final DistanceMeasure dist, final D input) {
		super(seed, dist, input);
	}

	public GmeansSampling(final long seed, final D input) {
		super(seed, input);
	}

	@SuppressWarnings("unchecked")
	@Override
	public AlgorithmEvent nextWithException() throws AlgorithmException, InterruptedException {
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
				this.clusterResults = gMeansCluster.cluster();
			}

			return this.activate();
		case ACTIVE:
			this.doAlgorithmStep();
			break;
		case INACTIVE:
			this.doInactiveStep();
			break;
		default:
			throw new IllegalStateException("Unknown algorithm state " + this.getState());
		}
		return null;
	}

}
