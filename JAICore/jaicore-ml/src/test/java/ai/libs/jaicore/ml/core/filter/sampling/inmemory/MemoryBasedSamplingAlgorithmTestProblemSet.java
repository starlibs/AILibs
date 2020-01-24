package ai.libs.jaicore.ml.core.filter.sampling.inmemory;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.api4.java.ai.ml.core.dataset.serialization.DatasetDeserializationFailedException;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.exception.DatasetCreationException;

import ai.libs.jaicore.basic.algorithm.AAlgorithmTestProblemSet;
import ai.libs.jaicore.basic.algorithm.AlgorithmTestProblemSetCreationException;
import ai.libs.jaicore.ml.core.dataset.DatasetDeriver;
import ai.libs.jaicore.ml.core.dataset.serialization.OpenMLDatasetReader;

public class MemoryBasedSamplingAlgorithmTestProblemSet extends AAlgorithmTestProblemSet<ILabeledDataset<ILabeledInstance>> {

	public MemoryBasedSamplingAlgorithmTestProblemSet() {
		super("Sampling");
	}

	@Override
	public ILabeledDataset<ILabeledInstance> getSimpleProblemInputForGeneralTestPurposes() throws AlgorithmTestProblemSetCreationException, InterruptedException {
		// Load bodyfat data set
		try {
			return this.loadDatasetFromOpenML(560);
		} catch (DatasetDeserializationFailedException e) {
			throw new AlgorithmTestProblemSetCreationException(e);
		}
	}

	public ILabeledDataset<ILabeledInstance> getMediumProblemInputForGeneralTestPurposes() throws AlgorithmTestProblemSetCreationException, InterruptedException {
		// Load whine quality data set
		try {
			return this.loadDatasetFromOpenML(287);
		} catch (DatasetDeserializationFailedException e) {
			throw new AlgorithmTestProblemSetCreationException(e);
		}
	}

	@Override
	public ILabeledDataset<ILabeledInstance> getDifficultProblemInputForGeneralTestPurposes() throws AlgorithmTestProblemSetCreationException, InterruptedException {
		// Load vancouver employee data set (more than 1.5 million instances but only 13 features)
		try {
			int blowUpFactor = 20;
			ILabeledDataset<ILabeledInstance> ds = this.loadDatasetFromOpenML(1237); // load large dataset and blow it up by the given factor
			DatasetDeriver<ILabeledDataset<ILabeledInstance>> deriver = new DatasetDeriver<>(ds);
			deriver.addIndices(IntStream.range(0, ds.size()).boxed().collect(Collectors.toList()),  blowUpFactor);
			return deriver.build();
		} catch (DatasetDeserializationFailedException | DatasetCreationException e) {
			throw new AlgorithmTestProblemSetCreationException(e);
		}
	}

	private ILabeledDataset<ILabeledInstance> loadDatasetFromOpenML(final int id) throws DatasetDeserializationFailedException, InterruptedException {
		return OpenMLDatasetReader.deserializeDataset(id);
	}

}
