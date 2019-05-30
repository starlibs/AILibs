package jaicore.ml.core.dataset.sampling.inmemory.factories;

import java.util.Random;

import org.apache.commons.math3.ml.distance.DistanceMeasure;
import org.apache.commons.math3.ml.distance.ManhattanDistance;

import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.INumericLabeledAttributeArrayInstance;
import jaicore.ml.core.dataset.sampling.inmemory.KmeansSampling;
import jaicore.ml.core.dataset.sampling.inmemory.factories.interfaces.IRerunnableSamplingAlgorithmFactory;

public class KmeansSamplingFactory<I extends INumericLabeledAttributeArrayInstance<? extends Number>, D extends IDataset<I>> implements IRerunnableSamplingAlgorithmFactory<D, KmeansSampling<I, D>> {

	private KmeansSampling<I, D> previousRun;
	private int k = -1;
	private long clusterSeed = System.currentTimeMillis();
	private DistanceMeasure distanceMeassure = new ManhattanDistance();

	@Override
	public void setPreviousRun(KmeansSampling<I, D> previousRun) {
		this.previousRun = previousRun;
	}

	/**
	 * Set how many clusters shall be created. Default is the sample size;
	 * 
	 * @param k
	 *            Parameter k of k-means.
	 */
	public void setK(int k) {
		this.k = k;
	}

	/**
	 * Set the seed the clustering will use for initialization. Default is without a
	 * fix seed and the system time instead.
	 * 
	 * @param clusterSeed
	 */
	public void setClusterSeed(long clusterSeed) {
		this.clusterSeed = clusterSeed;
	}

	/**
	 * Set the distance measure for the clustering. Default is the Manhattan
	 * distance.
	 * 
	 * @param distanceMeassure
	 */
	public void setDistanceMeassure(DistanceMeasure distanceMeassure) {
		this.distanceMeassure = distanceMeassure;
	}

	@Override
	public KmeansSampling<I, D> getAlgorithm(int sampleSize, D inputDataset, Random random) {
		int kValue = sampleSize;
		if (this.k > 0) {
			kValue = k;
		}
		KmeansSampling<I, D> kmeansSampling = new KmeansSampling<>(this.clusterSeed, kValue, inputDataset);
		kmeansSampling.setSampleSize(sampleSize);
		kmeansSampling.setDistanceMeassure(this.distanceMeassure);
		if (previousRun != null && previousRun.getClusterResults() != null) {
			kmeansSampling.setClusterResults(previousRun.getClusterResults());
		}
		return kmeansSampling;
	}

}
