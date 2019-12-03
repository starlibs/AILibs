package ai.libs.jaicore.ml.core.filter.sampling.inmemory;

import org.api4.java.ai.ml.core.dataset.serialization.DatasetDeserializationFailedException;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;

import ai.libs.jaicore.basic.algorithm.AAlgorithmTestProblemSet;
import ai.libs.jaicore.basic.algorithm.AlgorithmTestProblemSetCreationException;
import ai.libs.jaicore.ml.core.dataset.serialization.OpenMLDatasetReader;

public class SamplingAlgorithmTestProblemSet extends AAlgorithmTestProblemSet<ILabeledDataset<ILabeledInstance>> {

	public SamplingAlgorithmTestProblemSet() {
		super("Sampling");
	}

	public ILabeledDataset<ILabeledInstance> getTinyProblemInputForGeneralTestPurposes() throws AlgorithmTestProblemSetCreationException, InterruptedException {
		// Load bodyfat data set
		try {
			return this.loadDatasetFromOpenML(560);
		} catch (DatasetDeserializationFailedException e) {
			throw new AlgorithmTestProblemSetCreationException(e);
		}
	}

	@Override
	public ILabeledDataset<ILabeledInstance> getSimpleProblemInputForGeneralTestPurposes() throws AlgorithmTestProblemSetCreationException, InterruptedException {
		// Load whine quality data set
		try {
			return this.loadDatasetFromOpenML(287);
		} catch (DatasetDeserializationFailedException e) {
			throw new AlgorithmTestProblemSetCreationException(e);
		}
	}

	@Override
	public ILabeledDataset<ILabeledInstance> getDifficultProblemInputForGeneralTestPurposes() throws AlgorithmTestProblemSetCreationException, InterruptedException {
		// Load higgs data set
		try {
			return this.loadDatasetFromOpenML(23512);
		} catch (DatasetDeserializationFailedException e) {
			throw new AlgorithmTestProblemSetCreationException(e);
		}
	}

	private ILabeledDataset<ILabeledInstance> loadDatasetFromOpenML(final int id) throws DatasetDeserializationFailedException, InterruptedException {
		return OpenMLDatasetReader.deserializeDataset(id);
	}

}
