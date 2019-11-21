package ai.libs.jaicore.ml.experiments;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;

import ai.libs.jaicore.basic.algorithm.AAlgorithmTestProblemSet;

public abstract class MLProblemSet extends AAlgorithmTestProblemSet<ILabeledDataset<?>> {

	public MLProblemSet(final String name) {
		super("ML task " + name);
	}

	public abstract ILabeledDataset<?> getDataset();
}
