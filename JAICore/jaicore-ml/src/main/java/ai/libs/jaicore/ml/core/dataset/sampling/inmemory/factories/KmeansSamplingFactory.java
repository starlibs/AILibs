package ai.libs.jaicore.ml.core.dataset.sampling.inmemory.factories;

import java.util.Random;

import org.apache.commons.math3.ml.clustering.Clusterable;
import org.apache.commons.math3.ml.distance.DistanceMeasure;
import org.apache.commons.math3.ml.distance.ManhattanDistance;
import org.api4.java.ai.ml.dataset.INumericFeatureInstance;
import org.api4.java.ai.ml.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.dataset.supervised.ISupervisedDataset;

import ai.libs.jaicore.ml.core.dataset.sampling.inmemory.KmeansSampling;
import ai.libs.jaicore.ml.core.dataset.sampling.inmemory.factories.interfaces.IRerunnableSamplingAlgorithmFactory;

public class KmeansSamplingFactory<Y, I extends INumericFeatureInstance & ILabeledInstance<Y> & Clusterable, D extends ISupervisedDataset<Double, Y, I>>
		implements IRerunnableSamplingAlgorithmFactory<Double, Y, I, D, KmeansSampling<Y, I, D>> {

	private KmeansSampling<Y, I, D> previousRun;
	private int k = -1;
	private long clusterSeed = System.currentTimeMillis();
	private DistanceMeasure distanceMeassure = new ManhattanDistance();

	@Override
	public void setPreviousRun(final KmeansSampling<Y, I, D> previousRun) {
		this.previousRun = previousRun;
	}

	/**
	 * Set how many clusters shall be created. Default is the sample size;
	 *
	 * @param k
	 *            Parameter k of k-means.
	 */
	public void setK(final int k) {
		this.k = k;
	}

	/**
	 * Set the seed the clustering will use for initialization. Default is without a
	 * fix seed and the system time instead.
	 *
	 * @param clusterSeed
	 */
	public void setClusterSeed(final long clusterSeed) {
		this.clusterSeed = clusterSeed;
	}

	/**
	 * Set the distance measure for the clustering. Default is the Manhattan
	 * distance.
	 *
	 * @param distanceMeassure
	 */
	public void setDistanceMeassure(final DistanceMeasure distanceMeassure) {
		this.distanceMeassure = distanceMeassure;
	}

	@Override
	public KmeansSampling<Y, I, D> getAlgorithm(final int sampleSize, final D inputDataset, final Random random) {
		int kValue = sampleSize;
		if (this.k > 0) {
			kValue = this.k;
		}
		KmeansSampling<Y, I, D> kmeansSampling = new KmeansSampling<>(this.clusterSeed, kValue, inputDataset);
		kmeansSampling.setSampleSize(sampleSize);
		kmeansSampling.setDistanceMeassure(this.distanceMeassure);
		if (this.previousRun != null && this.previousRun.getClusterResults() != null) {
			kmeansSampling.setClusterResults(this.previousRun.getClusterResults());
		}
		return kmeansSampling;
	}

}
