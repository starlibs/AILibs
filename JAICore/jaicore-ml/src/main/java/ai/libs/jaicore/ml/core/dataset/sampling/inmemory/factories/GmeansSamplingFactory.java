package ai.libs.jaicore.ml.core.dataset.sampling.inmemory.factories;

import java.util.Random;

import org.apache.commons.math3.ml.clustering.Clusterable;
import org.apache.commons.math3.ml.distance.DistanceMeasure;
import org.apache.commons.math3.ml.distance.ManhattanDistance;
import org.api4.java.ai.ml.dataset.INumericFeatureInstance;
import org.api4.java.ai.ml.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.dataset.supervised.ISupervisedDataset;

import ai.libs.jaicore.ml.core.dataset.sampling.inmemory.GmeansSampling;
import ai.libs.jaicore.ml.core.dataset.sampling.inmemory.factories.interfaces.IRerunnableSamplingAlgorithmFactory;

public class GmeansSamplingFactory<Y, I extends INumericFeatureInstance & ILabeledInstance<Y> & Clusterable, D extends ISupervisedDataset<Double, Y, I>>
		implements IRerunnableSamplingAlgorithmFactory<Double, Y, I, D, GmeansSampling<Y, I, D>> {

	private GmeansSampling<Y, I, D> previousRun;
	private long clusterSeed = System.currentTimeMillis();
	private DistanceMeasure distanceMeassure = new ManhattanDistance();

	@Override
	public void setPreviousRun(final GmeansSampling<Y, I, D> previousRun) {
		this.previousRun = previousRun;
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
	public GmeansSampling<Y, I, D> getAlgorithm(final int sampleSize, final D inputDataset, final Random random) {
		GmeansSampling<Y, I, D> gmeansSampling = new GmeansSampling<>(this.clusterSeed, inputDataset);
		gmeansSampling.setSampleSize(sampleSize);
		gmeansSampling.setDistanceMeassure(this.distanceMeassure);
		if (this.previousRun != null && this.previousRun.getClusterResults() != null) {
			gmeansSampling.setClusterResults(this.previousRun.getClusterResults());
		}
		return gmeansSampling;
	}

}
