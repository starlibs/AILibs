package ai.libs.jaicore.ml.core.filter.sampling.inmemory;

import java.io.File;
import java.io.IOException;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.openml.apiconnector.io.OpenmlConnector;

import ai.libs.jaicore.basic.algorithm.AAlgorithmTestProblemSet;
import ai.libs.jaicore.basic.algorithm.AlgorithmTestProblemSetCreationException;
import ai.libs.jaicore.ml.core.dataset.serialization.ArffDatasetAdapter;

public class SamplingAlgorithmTestProblemSet extends AAlgorithmTestProblemSet<ILabeledDataset<ILabeledInstance>> {

	public SamplingAlgorithmTestProblemSet() {
		super("Sampling");
	}

	@Override
	public ILabeledDataset<ILabeledInstance> getSimpleProblemInputForGeneralTestPurposes() throws AlgorithmTestProblemSetCreationException {
		// Load whine quality data set
		try {
			return this.loadDatasetFromOpenML(287);
		} catch (IOException | ClassNotFoundException e) {
			throw new AlgorithmTestProblemSetCreationException(e);
		}
	}

	@Override
	public ILabeledDataset<ILabeledInstance> getDifficultProblemInputForGeneralTestPurposes() throws AlgorithmTestProblemSetCreationException {
		// Load higgs data set
		try {
			return this.loadDatasetFromOpenML(23512);
		} catch (IOException | ClassNotFoundException e) {
			throw new AlgorithmTestProblemSetCreationException(e);
		}
	}

	private ILabeledDataset<ILabeledInstance> loadDatasetFromOpenML(final int id) throws IOException, ClassNotFoundException {
		ILabeledDataset<ILabeledInstance> dataset = null;
		OpenmlConnector client = new OpenmlConnector();
		try {
			File file = client.datasetGet(client.dataGet(id));
			dataset = ArffDatasetAdapter.readDataset(file);
		} catch (Exception e) {
			throw new IOException("Could not load data set from OpenML!", e);
		}
		return dataset;
	}

}
