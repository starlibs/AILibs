package jaicore.ml.core.dataset.sampling.inmemory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.openml.apiconnector.io.OpenmlConnector;
import org.openml.apiconnector.xml.DataSetDescription;

import jaicore.basic.algorithm.GeneralAlgorithmTester;
import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.IInstance;
import jaicore.ml.core.dataset.standard.SimpleDataset;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

/**
 * This class provides some tests that verify basic properties of a sampling
 * algorithm. For a specific sampling algorithm, this class can be extended by a
 * class that is specific for that particular algorithm.
 * 
 * @author Felix Weiland
 *
 */
public abstract class GeneralSamplingTester<I extends IInstance> extends GeneralAlgorithmTester {

	private static final double DEFAULT_SAMPLE_FRACTION = 0.1;

	private static final String OPENML_API_KEY = "4350e421cdc16404033ef1812ea38c01";

	/**
	 * This test verifies that the produced samples have the desired size.
	 * 
	 * This test executes the test on a small problem/data set.
	 * 
	 * @param sampleFraction
	 * @throws Exception
	 */
	@Test
	public void testSampleSizeSmallProblem() throws Exception {
		IDataset<I> dataset = this.getSimpleProblemInputForGeneralTestPurposes();
		testSampleSize(dataset, DEFAULT_SAMPLE_FRACTION);
	}

	/**
	 * This test verifies that the produced samples have the desired size.
	 * 
	 * This test executes the test on a large problem/data set.
	 * 
	 * @param sampleFraction
	 * @throws Exception
	 */
	@Test
	public void testSampleSizeLargeProblem() throws Exception {
		IDataset<I> dataset = this.getDifficultProblemInputForGeneralTestPurposes();
		testSampleSize(dataset, DEFAULT_SAMPLE_FRACTION);
	}

	private void testSampleSize(IDataset<I> dataset, double sampleFraction) {
		@SuppressWarnings("unchecked")
		ASamplingAlgorithm<I> samplingAlgorithm = (ASamplingAlgorithm<I>) this.getAlgorithm(dataset);
		int sampleSize = (int) (dataset.size() * sampleFraction);
		samplingAlgorithm.setSampleSize(sampleSize);
		IDataset<I> sample = getSample(samplingAlgorithm);

		assertEquals(sampleSize, sample.size());
	}

	/**
	 * This test verifies that the produced samples do not contain duplicates.
	 * 
	 * This test executes the test on a small problem/data set.
	 * 
	 * @throws Exception
	 * 
	 */
	@Test
	public void testNoDuplicatesSmallProblem() throws Exception {
		IDataset<I> dataset = this.getSimpleProblemInputForGeneralTestPurposes();
		testNoDuplicates(dataset);
	}

	/**
	 * This test verifies that the produced samples do not contain duplicates.
	 * 
	 * This test executes the test on a large problem/data set.
	 * 
	 * @throws Exception
	 * 
	 */
	@Test
	public void testNoDuplicatesLargeProblem() throws Exception {
		IDataset<I> dataset = this.getDifficultProblemInputForGeneralTestPurposes();
		testNoDuplicates(dataset);
	}

	private void testNoDuplicates(IDataset<I> dataset) {
		@SuppressWarnings("unchecked")
		ASamplingAlgorithm<I> samplingAlgorithm = (ASamplingAlgorithm<I>) this.getAlgorithm(dataset);
		int sampleSize = (int) (dataset.size() * DEFAULT_SAMPLE_FRACTION);
		samplingAlgorithm.setSampleSize(sampleSize);
		IDataset<I> sample = getSample(samplingAlgorithm);
		int actualSampleSize = sample.size();
		Set<IInstance> set = new HashSet<>();
		set.addAll(sample);
		assertEquals(actualSampleSize, set.size());
	}

	/**
	 * This test verifies that the original data set, which is fed to the sampling
	 * algorithm, is not modified in the sampling process.
	 * 
	 * @throws Exception
	 */
	@Test
	public void checkOriginalDataSetNotModified() throws Exception {
		IDataset<I> dataset = this.getSimpleProblemInputForGeneralTestPurposes();
		int hashCode = dataset.hashCode();
		@SuppressWarnings("unchecked")
		ASamplingAlgorithm<I> samplingAlgorithm = (ASamplingAlgorithm<I>) this.getAlgorithm(dataset);
		int sampleSize = (int) (dataset.size() * DEFAULT_SAMPLE_FRACTION);
		samplingAlgorithm.setSampleSize(sampleSize);
		getSample(samplingAlgorithm);
		assertEquals(hashCode, dataset.hashCode());
	}

	private IDataset<I> getSample(ASamplingAlgorithm<I> samplingAlgorithm) {
		IDataset<I> sample = null;
		try {
			sample = samplingAlgorithm.call();
		} catch (Exception e) {
			throw new RuntimeException("Sampling algorithm was not able to compute a sample", e);
		}
		if (sample == null) {
			fail("Sample is null!");
		}
		return sample;
	}

	public IDataset<I> getSimpleProblemInputForGeneralTestPurposes() throws Exception {
		// Load whine quality data set
		return loadDatasetFromOpenML(287);
	}

	public IDataset<I> getDifficultProblemInputForGeneralTestPurposes() throws Exception {
		// Load higgs data set
		return loadDatasetFromOpenML(23512);
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
