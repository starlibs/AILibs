package ai.libs.jaicore.ml.core.filter.sampling.inmemory.factories;

import java.util.Random;

import org.apache.commons.math3.ml.distance.DistanceMeasure;
import org.apache.commons.math3.ml.distance.ManhattanDistance;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;

import ai.libs.jaicore.ml.core.filter.sampling.IClusterableInstance;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.GmeansSampling;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.factories.interfaces.IRerunnableSamplingAlgorithmFactory;

public class GmeansSamplingFactory<I extends IClusterableInstance, D extends ILabeledDataset<I>> extends ASampleAlgorithmFactory<D, GmeansSampling<I, D>> implements IRerunnableSamplingAlgorithmFactory<D, GmeansSampling<I, D>> {

	private GmeansSampling<I, D> previousRun;
	private long clusterSeed = System.currentTimeMillis();
	private DistanceMeasure distanceMeassure = new ManhattanDistance();

	@Override
	public void setPreviousRun(final GmeansSampling<I, D> previousRun) {
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
	public GmeansSampling<I, D> getAlgorithm(final int sampleSize, final D inputDataset, final Random random) {
		GmeansSampling<I, D> gmeansSampling = new GmeansSampling<>(this.clusterSeed, inputDataset);
		gmeansSampling.setSampleSize(sampleSize);
		gmeansSampling.setDistanceMeassure(this.distanceMeassure);
		if (this.previousRun != null && this.previousRun.getClusterResults() != null) {
			gmeansSampling.setClusterResults(this.previousRun.getClusterResults());
		}
		return gmeansSampling;
	}

}
