package ai.libs.jaicore.ml.core.dataset.sampling.inmemory.factories;

import java.util.Comparator;
import java.util.Random;

import org.apache.commons.math3.ml.clustering.Clusterable;
import org.api4.java.ai.ml.dataset.INumericFeatureInstance;
import org.api4.java.ai.ml.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.dataset.supervised.ISupervisedDataset;

import ai.libs.jaicore.ml.core.dataset.sampling.inmemory.SystematicSampling;
import ai.libs.jaicore.ml.core.dataset.sampling.inmemory.factories.interfaces.IRerunnableSamplingAlgorithmFactory;

public class SystematicSamplingFactory<Y, I extends INumericFeatureInstance & ILabeledInstance<Y> & Clusterable, D extends ISupervisedDataset<Double, Y, I>>
		implements IRerunnableSamplingAlgorithmFactory<Double, Y, I, D, SystematicSampling<Y, I, D>> {

	private Comparator<I> datapointComparator = null;
	private SystematicSampling<Y, I, D> previousRun = null;

	/**
	 * Set a custom comparator that will be used to sort the datapoints before
	 * sampling.
	 *
	 * @param datapointComparator
	 *            Comparator for two datapoints.
	 */
	public void setDatapointComparator(final Comparator<I> datapointComparator) {
		this.datapointComparator = datapointComparator;
	}

	@Override
	public void setPreviousRun(final SystematicSampling<Y, I, D> previousRun) {
		this.previousRun = previousRun;
	}

	@Override
	public SystematicSampling<Y, I, D> getAlgorithm(final int sampleSize, final D inputDataset, final Random random) {
		SystematicSampling<Y, I, D> systematicSampling;
		if (this.datapointComparator != null) {
			systematicSampling = new SystematicSampling<>(random, this.datapointComparator, inputDataset);
		} else {
			systematicSampling = new SystematicSampling<>(random, inputDataset);
		}
		systematicSampling.setSampleSize(sampleSize);
		if (this.previousRun != null && this.previousRun.getSortedDataset() != null) {
			systematicSampling.setSortedDataset(this.previousRun.getSortedDataset());
		}
		return systematicSampling;
	}

}
