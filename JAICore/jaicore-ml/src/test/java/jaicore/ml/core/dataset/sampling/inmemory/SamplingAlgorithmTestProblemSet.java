package jaicore.ml.core.dataset.sampling.inmemory;

import java.io.File;
import java.io.IOException;

import org.openml.apiconnector.io.OpenmlConnector;
import org.openml.apiconnector.xml.DataSetDescription;

import jaicore.basic.algorithm.AAlgorithmTestProblemSet;
import jaicore.basic.algorithm.AlgorithmTestProblemSetCreationException;
import jaicore.ml.core.dataset.IOrderedLabeledAttributeArrayDataset;
import jaicore.ml.core.dataset.weka.WekaInstances;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class SamplingAlgorithmTestProblemSet<L> extends AAlgorithmTestProblemSet<IOrderedLabeledAttributeArrayDataset<?, L>> {

	private static final String OPENML_API_KEY = "4350e421cdc16404033ef1812ea38c01";

	public SamplingAlgorithmTestProblemSet() {
		super("Sampling");
	}

	@Override
	public IOrderedLabeledAttributeArrayDataset<?, L> getSimpleProblemInputForGeneralTestPurposes() throws AlgorithmTestProblemSetCreationException {
		// Load whine quality data set
		try {
			return loadDatasetFromOpenML(287);
		} catch (IOException | ClassNotFoundException e) {
			throw new AlgorithmTestProblemSetCreationException(e);
		}
	}

	@Override
	public IOrderedLabeledAttributeArrayDataset<?, L> getDifficultProblemInputForGeneralTestPurposes() throws AlgorithmTestProblemSetCreationException {
		// Load higgs data set
		try {
			return loadDatasetFromOpenML(23512);
		} catch (IOException | ClassNotFoundException e) {
			throw new AlgorithmTestProblemSetCreationException(e);
		}
	}

	private IOrderedLabeledAttributeArrayDataset<?, L> loadDatasetFromOpenML(int id) throws IOException, ClassNotFoundException {
		Instances dataset = null;
		OpenmlConnector client = new OpenmlConnector();
		try {
			DataSetDescription description = client.dataGet(id);
			File file = description.getDataset(OPENML_API_KEY);
			DataSource source = new DataSource(file.getCanonicalPath());
			dataset = source.getDataSet();
			dataset.setClassIndex(dataset.numAttributes() - 1);
			Attribute targetAttribute = dataset.attribute(description.getDefault_target_attribute());
			dataset.setClassIndex(targetAttribute.index());
		} catch (Exception e) {
			throw new IOException("Could not load data set from OpenML!", e);
		}

		return new WekaInstances<>(dataset);
	}

}
