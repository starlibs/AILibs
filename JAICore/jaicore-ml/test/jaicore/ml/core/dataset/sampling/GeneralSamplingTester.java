package jaicore.ml.core.dataset.sampling;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import jaicore.basic.algorithm.GeneralAlgorithmTester;
import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.IInstance;

/**
 * This class provides some tests that verify basic properties of a sampling
 * algorithm. For a specific sampling algorithm, this class can be extended by a
 * class that is specific for that particular algorithm.
 * 
 * @author Felix Weiland
 *
 */
public abstract class GeneralSamplingTester extends GeneralAlgorithmTester<Object, IDataset, IDataset> {

	private static final double DEFAULT_SAMPLE_FRACTION = 0.1;

	/**
	 * This test verifies that the produced samples have the desired size. The test
	 * is executed for different sample sizes, which are dependent on the size of
	 * the original data set.
	 * 
	 * This test executes the test on a small problem/data set.
	 * 
	 * @param sampleFraction
	 * @throws Exception
	 */
	@ParameterizedTest
	@ValueSource(doubles = { 0.1, 0.5, 1 })
	public void testSampleSizeSmallProblem(double sampleFraction) throws Exception {
		IDataset dataset = this.getSimpleProblemInputForGeneralTestPurposes();
		testSampleSize(dataset, sampleFraction);
	}

	/**
	 * This test verifies that the produced samples have the desired size. The test
	 * is executed for different sample sizes, which are dependent on the size of
	 * the original data set.
	 * 
	 * This test executes the test on a large problem/data set.
	 * 
	 * @param sampleFraction
	 * @throws Exception
	 */
	@ParameterizedTest
	@ValueSource(doubles = { 0.1, 0.5, 1 })
	public void testSampleSizeLargeProblem(double sampleFraction) throws Exception {
		IDataset dataset = this.getDifficultProblemInputForGeneralTestPurposes();
		testSampleSize(dataset, sampleFraction);
	}

	private void testSampleSize(IDataset dataset, double sampleFraction) {
		ASamplingAlgorithm samplingAlgorithm = (ASamplingAlgorithm) this.getFactory().getAlgorithm();
		IDataset sample = getSample(dataset, samplingAlgorithm);

		int sampleSize = (int) (dataset.size() * sampleFraction);
		samplingAlgorithm.setSampleSize(sampleSize);

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
		IDataset dataset = this.getSimpleProblemInputForGeneralTestPurposes();
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
		IDataset dataset = this.getDifficultProblemInputForGeneralTestPurposes();
		testNoDuplicates(dataset);
	}

	private void testNoDuplicates(IDataset dataset) {
		ASamplingAlgorithm samplingAlgorithm = (ASamplingAlgorithm) this.getFactory().getAlgorithm();
		samplingAlgorithm.setSampleSize((int) DEFAULT_SAMPLE_FRACTION * dataset.size());
		IDataset sample = getSample(dataset, samplingAlgorithm);
		int sampleSize = sample.size();
		Set<IInstance> set = new HashSet<>();
		set.addAll(sample);
		assertEquals(sampleSize, set.size());
	}

	/**
	 * This test verifies that the original data set, which is fed to the sampling
	 * algorithm, is not modified in the sampling process.
	 * 
	 * @throws Exception
	 */
	@Test
	public void checkOriginalDataSetNotModified() throws Exception {
		IDataset dataset = this.getSimpleProblemInputForGeneralTestPurposes();
		int hashCode = dataset.hashCode();
		ASamplingAlgorithm samplingAlgorithm = (ASamplingAlgorithm) this.getFactory().getAlgorithm();
		samplingAlgorithm.setSampleSize((int) DEFAULT_SAMPLE_FRACTION * dataset.size());
		getSample(dataset, samplingAlgorithm);
		assertEquals(hashCode, dataset.hashCode());
	}

	private IDataset getSample(IDataset dataset, ASamplingAlgorithm samplingAlgorithm) {
		samplingAlgorithm.setInput(dataset);
		IDataset sample = null;
		try {
			sample = samplingAlgorithm.call();
		} catch (Exception e) {
			fail("Sampling algorithm was not able to compute a sample!");
		}
		if (sample == null) {
			fail("Sample is null!");
		}
		return sample;
	}

}
