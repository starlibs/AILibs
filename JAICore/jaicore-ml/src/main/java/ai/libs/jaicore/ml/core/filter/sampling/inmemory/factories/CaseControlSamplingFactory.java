package ai.libs.jaicore.ml.core.filter.sampling.inmemory.factories;

import java.util.Random;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;

import ai.libs.jaicore.ml.core.filter.sampling.inmemory.casecontrol.CaseControlSampling;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.factories.interfaces.IRerunnableSamplingAlgorithmFactory;

public class CaseControlSamplingFactory<I extends ILabeledInstance, D extends ILabeledDataset<I>> implements IRerunnableSamplingAlgorithmFactory<D, CaseControlSampling<I, D>> {

	private CaseControlSampling<I, D> previousRun = null;

	@Override
	public void setPreviousRun(final CaseControlSampling<I, D> previousRun) {
		this.previousRun = previousRun;
	}

	@Override
	public CaseControlSampling<I, D> getAlgorithm(final int sampleSize, final D inputDataset, final Random random) {
		CaseControlSampling<I, D> caseControlSampling = new CaseControlSampling<>(random, inputDataset);
		if (this.previousRun != null && this.previousRun.getProbabilityBoundaries() != null) {
			caseControlSampling.setProbabilityBoundaries(this.previousRun.getProbabilityBoundaries());
		}
		caseControlSampling.setSampleSize(sampleSize);
		return caseControlSampling;
	}

}
