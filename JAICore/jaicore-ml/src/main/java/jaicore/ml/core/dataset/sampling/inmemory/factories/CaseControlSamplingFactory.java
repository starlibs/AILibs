package jaicore.ml.core.dataset.sampling.inmemory.factories;

import java.util.Random;

import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.ILabeledInstance;
import jaicore.ml.core.dataset.sampling.inmemory.casecontrol.CaseControlSampling;
import jaicore.ml.core.dataset.sampling.inmemory.factories.interfaces.IRerunnableSamplingAlgorithmFactory;

public class CaseControlSamplingFactory<I extends ILabeledInstance<?>, D extends IDataset<I>> implements IRerunnableSamplingAlgorithmFactory<D, CaseControlSampling<I, D>> {

	private CaseControlSampling<I, D> previousRun = null;

	@Override
	public void setPreviousRun(CaseControlSampling<I, D> previousRun) {
		this.previousRun = previousRun;
	}

	@Override
	public CaseControlSampling<I, D> getAlgorithm(int sampleSize, D inputDataset, Random random) {
		CaseControlSampling<I, D> caseControlSampling = new CaseControlSampling<>(random, inputDataset);
		if (this.previousRun != null && this.previousRun.getProbabilityBoundaries() != null) {
			caseControlSampling.setProbabilityBoundaries(this.previousRun.getProbabilityBoundaries());
		}
		caseControlSampling.setSampleSize(sampleSize);
		return caseControlSampling;
	}

}
