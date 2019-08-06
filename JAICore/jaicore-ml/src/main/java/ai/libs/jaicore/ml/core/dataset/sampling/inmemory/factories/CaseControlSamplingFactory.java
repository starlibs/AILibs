package ai.libs.jaicore.ml.core.dataset.sampling.inmemory.factories;

import java.util.Random;

import org.api4.java.ai.ml.dataset.IFeatureInstance;
import org.api4.java.ai.ml.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.dataset.supervised.ISupervisedDataset;

import ai.libs.jaicore.ml.core.dataset.sampling.inmemory.casecontrol.CaseControlSampling;
import ai.libs.jaicore.ml.core.dataset.sampling.inmemory.factories.interfaces.IRerunnableSamplingAlgorithmFactory;

public class CaseControlSamplingFactory<X, Y, I extends IFeatureInstance<X> & ILabeledInstance<Y>, D extends ISupervisedDataset<X, Y, I>> implements IRerunnableSamplingAlgorithmFactory<X, Y, I, D, CaseControlSampling<X, Y, I, D>> {

	private CaseControlSampling<X, Y, I, D> previousRun = null;

	@Override
	public void setPreviousRun(final CaseControlSampling<X, Y, I, D> previousRun) {
		this.previousRun = previousRun;
	}

	@Override
	public CaseControlSampling<X, Y, I, D> getAlgorithm(final int sampleSize, final D inputDataset, final Random random) {
		CaseControlSampling<X, Y, I, D> caseControlSampling = new CaseControlSampling<>(random, inputDataset);
		if (this.previousRun != null && this.previousRun.getProbabilityBoundaries() != null) {
			caseControlSampling.setProbabilityBoundaries(this.previousRun.getProbabilityBoundaries());
		}
		caseControlSampling.setSampleSize(sampleSize);
		return caseControlSampling;
	}

}
