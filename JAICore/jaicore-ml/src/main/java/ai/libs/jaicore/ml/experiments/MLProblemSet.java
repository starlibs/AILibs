package ai.libs.jaicore.ml.experiments;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.exception.DatasetCreationException;

import ai.libs.jaicore.basic.algorithm.AAlgorithmTestProblemSet;
import ai.libs.jaicore.basic.algorithm.AlgorithmTestProblemSetCreationException;

public abstract class MLProblemSet extends AAlgorithmTestProblemSet<ILabeledDataset<?>> {

	public MLProblemSet(final String name) {
		super("ML task " + name);
	}

	@Override
	public ILabeledDataset<?> getSimpleProblemInputForGeneralTestPurposes() throws AlgorithmTestProblemSetCreationException {
		try {
			return this.getDataset();
		} catch (DatasetCreationException e) {
			throw new AlgorithmTestProblemSetCreationException(e);
		}
	}

	@Override
	public ILabeledDataset<?> getDifficultProblemInputForGeneralTestPurposes() throws AlgorithmTestProblemSetCreationException {
		try {
			return this.getDataset();
		} catch (DatasetCreationException e) {
			throw new AlgorithmTestProblemSetCreationException(e);
		}
	}

	public abstract ILabeledDataset<?> getDataset() throws DatasetCreationException;
}