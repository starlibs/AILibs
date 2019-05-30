package jaicore.ml.core.dataset.sampling.inmemory;

import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.apache.commons.math3.ml.distance.DistanceMeasure;
import org.apache.commons.math3.random.JDKRandomGenerator;

import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.basic.algorithm.exceptions.AlgorithmException;
import jaicore.ml.core.dataset.AILabeledAttributeArrayDataset;
import jaicore.ml.core.dataset.DatasetCreationException;
import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.INumericLabeledAttributeArrayInstance;

/**
 * Implementation of a sampling method using kmeans-clustering. This algorithm
 * produces clusters of the given points and checks weather all points in a
 * cluster have the same target Attribute. If yes only the point nearest to the
 * center is added, otherwise the whole cluster is added to the sample.
 * <p>
 * Caution: This does ignore the given sample size!
 * 
 * @author jnowack
 *
 */
public class KmeansSampling<I extends INumericLabeledAttributeArrayInstance<? extends Number>, D extends IDataset<I>> extends ClusterSampling<I, D> {
	/* number of clusters, if -1 use sample size */
	private int k;

	/**
	 * Implementation of a sampling method using kmeans-clustering.
	 * 
	 * @param seed
	 *            RAndom Seed
	 * @param k
	 *            number of clusters
	 */
	public KmeansSampling(long seed, int k, D input) {
		super(seed, input);
		this.k = k;
	}

	/**
	 * Implementation of a sampling method using kmeans-clustering. The sample size
	 * will be used as the number of clusters.
	 * 
	 * @param seed
	 *            Random Seed
	 * @param dist
	 *            {@link DistanceMeasure} to be used
	 */
	public KmeansSampling(long seed, DistanceMeasure dist, D input) {
		super(seed, dist, input);
		this.k = -1;

	}

	/**
	 * Implementation of a sampling method using kmeans-clustering.
	 * 
	 * @param seed
	 *            Random Seed
	 * @param k
	 *            number of clusters
	 * @param dist
	 *            {@link DistanceMeasure} to be used
	 */
	public KmeansSampling(long seed, int k, DistanceMeasure dist, D input) {
		super(seed, dist, input);
		this.k = k;

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

			// create cluster
			JDKRandomGenerator r = new JDKRandomGenerator();
			r.setSeed(seed);
			// update k if k=-1
			if (k == -1) {
				k = sampleSize;
			}
			if (clusterResults == null) {
				KMeansPlusPlusClusterer<I> kMeansCluster = new KMeansPlusPlusClusterer<>(k, -1, distanceMeassure, r);
				clusterResults = kMeansCluster.cluster(getInput());
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
