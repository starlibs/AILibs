package jaicore.ml.core.dataset.sampling.inmemory.factories;

import java.util.Random;

import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.IInstance;
import jaicore.ml.core.dataset.sampling.inmemory.casecontrol.CaseControlSampling;
import jaicore.ml.core.dataset.sampling.inmemory.factories.interfaces.IRerunnableSamplingAlgorithmFactory;

public class CaseControlSamplingFactory<I extends IInstance>
		implements IRerunnableSamplingAlgorithmFactory<I, CaseControlSampling<I>> {

	private CaseControlSampling<I> previousRun = null;

	@Override
	public void setPreviousRun(CaseControlSampling<I> previousRun) {
		this.previousRun = previousRun;
	}

	@Override
	public CaseControlSampling<I> getAlgorithm(int sampleSize, IDataset<I> inputDataset, Random random) {
		CaseControlSampling<I> caseControlSampling = new CaseControlSampling<>(random, inputDataset);
		if (this.previousRun != null && this.previousRun.getProbabilityBoundaries() != null) {
			caseControlSampling.setProbabilityBoundaries(this.previousRun.getProbabilityBoundaries());
		}
		caseControlSampling.setSampleSize(sampleSize);
		return caseControlSampling;
	}

}
