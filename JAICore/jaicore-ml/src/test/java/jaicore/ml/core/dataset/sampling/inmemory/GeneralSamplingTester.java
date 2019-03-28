package jaicore.ml.core.dataset.sampling.inmemory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;
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
	
	@Parameters(name = "problemset = {0}")
	public static Collection<Object[]> data() {
		List<Object> problemSets = new ArrayList<>();

		/* add N-Queens (as a graph search problem set) */
		problemSets.add(new SamplingAlgorithmTestProblemSet<>());
		List<Collection<Object>> input = new ArrayList<>();
		input.add(problemSets);

		Object[][] data = new Object[problemSets.size()][1];
		for (int i = 0; i < data.length; i++) {
			data[i][0] = problemSets.get(i);
		}
		return Arrays.asList(data);
	}

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
		@SuppressWarnings("unchecked")
		SamplingAlgorithmTestProblemSet<I> problemSet =  (SamplingAlgorithmTestProblemSet<I>) this.getProblemSet();
		IDataset<I> dataset = problemSet.getSimpleProblemInputForGeneralTestPurposes();
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
		@SuppressWarnings("unchecked")
		SamplingAlgorithmTestProblemSet<I> problemSet =  (SamplingAlgorithmTestProblemSet<I>) this.getProblemSet();
		IDataset<I> dataset = problemSet.getDifficultProblemInputForGeneralTestPurposes();
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
		@SuppressWarnings("unchecked")
		SamplingAlgorithmTestProblemSet<I> problemSet =  (SamplingAlgorithmTestProblemSet<I>) this.getProblemSet();
		IDataset<I> dataset = problemSet.getSimpleProblemInputForGeneralTestPurposes();
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
		@SuppressWarnings("unchecked")
		SamplingAlgorithmTestProblemSet<I> problemSet =  (SamplingAlgorithmTestProblemSet<I>) this.getProblemSet();
		IDataset<I> dataset = problemSet.getDifficultProblemInputForGeneralTestPurposes();
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
		@SuppressWarnings("unchecked")
		SamplingAlgorithmTestProblemSet<I> problemSet =  (SamplingAlgorithmTestProblemSet<I>) this.getProblemSet();
		IDataset<I> dataset = problemSet.getSimpleProblemInputForGeneralTestPurposes();
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

	

}
