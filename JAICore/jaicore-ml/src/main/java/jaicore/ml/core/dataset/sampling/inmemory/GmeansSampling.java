package jaicore.ml.core.dataset.sampling.inmemory;

import org.apache.commons.math3.ml.distance.DistanceMeasure;

import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.basic.algorithm.exceptions.AlgorithmException;
import jaicore.ml.clustering.GMeans;
import jaicore.ml.core.dataset.AILabeledAttributeArrayDataset;
import jaicore.ml.core.dataset.DatasetCreationException;
import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.INumericLabeledAttributeArrayInstance;

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
public class GmeansSampling<I extends INumericLabeledAttributeArrayInstance<? extends Number>, D extends IDataset<I>> extends ClusterSampling<I, D> {

	public GmeansSampling(long seed, DistanceMeasure dist, D input) {
		super(seed, dist, input);
	}

	public GmeansSampling(long seed, D input) {
		super(seed, input);
	}

	@Override
	public AlgorithmEvent nextWithException() throws AlgorithmException {
		switch (this.getState()) {
		case CREATED:
			// Initialize variables
			try {
				this.sample = (D)getInput().createEmpty();
			} catch (DatasetCreationException e) {
				throw new AlgorithmException(e, "Could not create a copy of the dataset.");
			}

			if (this.clusterResults == null) {
				// create cluster
				GMeans<I> gMeansCluster = new GMeans<>(getInput(), distanceMeassure, seed);
				clusterResults = gMeansCluster.cluster();
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
