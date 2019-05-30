package jaicore.ml.core.dataset.sampling.inmemory.factories;

import java.util.Random;

import org.apache.commons.math3.ml.distance.DistanceMeasure;
import org.apache.commons.math3.ml.distance.ManhattanDistance;

import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.INumericLabeledAttributeArrayInstance;
import jaicore.ml.core.dataset.sampling.inmemory.GmeansSampling;
import jaicore.ml.core.dataset.sampling.inmemory.factories.interfaces.IRerunnableSamplingAlgorithmFactory;

public class GmeansSamplingFactory<I extends INumericLabeledAttributeArrayInstance<? extends Number>, D extends IDataset<I>> implements IRerunnableSamplingAlgorithmFactory<D, GmeansSampling<I, D>> {

	private GmeansSampling<I, D> previousRun;
	private long clusterSeed = System.currentTimeMillis();
	private DistanceMeasure distanceMeassure = new ManhattanDistance();

	@Override
	public void setPreviousRun(GmeansSampling<I, D> previousRun) {
		this.previousRun = previousRun;
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
	public GmeansSampling<I, D> getAlgorithm(int sampleSize, D inputDataset, Random random) {
		GmeansSampling<I, D> gmeansSampling = new GmeansSampling<>(this.clusterSeed, inputDataset);
		gmeansSampling.setSampleSize(sampleSize);
		gmeansSampling.setDistanceMeassure(this.distanceMeassure);
		if (previousRun != null && previousRun.getClusterResults() != null) {
			gmeansSampling.setClusterResults(previousRun.getClusterResults());
		}
		return gmeansSampling;
	}

}
