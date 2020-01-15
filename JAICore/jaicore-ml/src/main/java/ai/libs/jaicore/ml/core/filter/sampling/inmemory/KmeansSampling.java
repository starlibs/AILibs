package ai.libs.jaicore.ml.core.filter.sampling.inmemory;

import org.apache.commons.math3.ml.clustering.Clusterable;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.apache.commons.math3.ml.distance.DistanceMeasure;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.exception.DatasetCreationException;
import org.api4.java.algorithm.events.IAlgorithmEvent;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;

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
public class KmeansSampling<I extends ILabeledInstance & Clusterable, D extends ILabeledDataset<I>> extends ClusterSampling<I, D> {
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
	public KmeansSampling(final long seed, final int k, final D input) {
		super(seed, input);
		this.k = k;
		if (input.size() > 1000) {
			throw new IllegalArgumentException("KMeansSampling does not support datasets with more than 1000 points, because it has quadratic (non-interruptible) runtime.");
		}
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
	public KmeansSampling(final long seed, final DistanceMeasure dist, final D input) {
		super(seed, dist, input);
		this.k = -1;
		if (input.size() > 1000) {
			throw new IllegalArgumentException("KMeansSampling does not support datasets with more than 1000 points, because it has quadratic (non-interruptible) runtime.");
		}
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
	public KmeansSampling(final long seed, final int k, final DistanceMeasure dist, final D input) {
		super(seed, dist, input);
		this.k = k;

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

			// create cluster
			JDKRandomGenerator r = new JDKRandomGenerator();
			r.setSeed(this.seed);
			// update k if k=-1
			if (this.k == -1) {
				this.k = this.sampleSize;
			}
			if (this.clusterResults == null) {
				KMeansPlusPlusClusterer<I> kMeansCluster = new KMeansPlusPlusClusterer<>(this.k, -1, this.distanceMeassure, r);
				this.clusterResults = kMeansCluster.cluster(this.getInput()); // this is not interruptible!!
			}
			return this.activate();
		case ACTIVE:
			return this.doAlgorithmStep();
		default:
			throw new IllegalStateException("Unknown algorithm state " + this.getState());
		}
	}

}
