package ai.libs.jaicore.ml.core.dataset.sampling.inmemory;

import java.io.File;
import java.io.IOException;

import org.openml.apiconnector.io.OpenmlConnector;
import org.openml.apiconnector.xml.DataSetDescription;

import ai.libs.jaicore.basic.algorithm.AAlgorithmTestProblemSet;
import ai.libs.jaicore.basic.algorithm.AlgorithmTestProblemSetCreationException;
import ai.libs.jaicore.ml.core.dataset.IDataset;
import ai.libs.jaicore.ml.core.dataset.IInstance;
import ai.libs.jaicore.ml.core.dataset.standard.SimpleDataset;
import ai.libs.jaicore.ml.core.dataset.weka.WekaInstancesUtil;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class SamplingAlgorithmTestProblemSet<I extends IInstance> extends AAlgorithmTestProblemSet<IDataset<I>> {

	private static final String OPENML_API_KEY = "4350e421cdc16404033ef1812ea38c01";

	public SamplingAlgorithmTestProblemSet() {
		super("Sampling");
	}

	@Override
	public IDataset<I> getSimpleProblemInputForGeneralTestPurposes() throws AlgorithmTestProblemSetCreationException {
		// Load whine quality data set
		try {
			return loadDatasetFromOpenML(287);
		} catch (IOException e) {
			throw new AlgorithmTestProblemSetCreationException(e);
		}
	}

	@Override
	public IDataset<I> getDifficultProblemInputForGeneralTestPurposes() throws AlgorithmTestProblemSetCreationException {
		// Load higgs data set
		try {
			return loadDatasetFromOpenML(23512);
		} catch (IOException e) {
			throw new AlgorithmTestProblemSetCreationException(e);
		}
	}

	@SuppressWarnings("unchecked")
	private IDataset<I> loadDatasetFromOpenML(int id) throws IOException {
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

		SimpleDataset simpleDataset = (SimpleDataset) WekaInstancesUtil.wekaInstancesToDataset(dataset);
		IDataset<I> toReturn = null;
		try {
			toReturn = (IDataset<I>) simpleDataset;
		} catch (ClassCastException e) {
			throw new RuntimeException("Cannot cast the loaded simple data set to the desired data set!", e);
		}

		return toReturn;

	}

}
