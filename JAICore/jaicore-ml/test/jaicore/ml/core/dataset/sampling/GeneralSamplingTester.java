package jaicore.ml.core.dataset.sampling;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import jaicore.basic.algorithm.AlgorithmProblemTransformer;
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
	 * This test verifies that the produced samples have the desired size.
	 * 
	 * This test executes the test on a small problem/data set.
	 * 
	 * @param sampleFraction
	 * @throws Exception
	 */
	@Test
	public void testSampleSizeSmallProblem() throws Exception {
		IDataset dataset = this.getSimpleProblemInputForGeneralTestPurposes();
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
	// @Test
	public void testSampleSizeLargeProblem() throws Exception {
		IDataset dataset = this.getDifficultProblemInputForGeneralTestPurposes();
		testSampleSize(dataset, DEFAULT_SAMPLE_FRACTION);
	}

	private void testSampleSize(IDataset dataset, double sampleFraction) {
		ASamplingAlgorithm samplingAlgorithm = (ASamplingAlgorithm) this.getFactory().getAlgorithm();
		int sampleSize = (int) (dataset.size() * sampleFraction);
		samplingAlgorithm.setSampleSize(sampleSize);
		IDataset sample = getSample(dataset, samplingAlgorithm);

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
			e.printStackTrace();
			fail("Sampling algorithm was not able to compute a sample: ");
		}
		if (sample == null) {
			fail("Sample is null!");
		}
		return sample;
	}

	@Override
	public AlgorithmProblemTransformer<Object, IDataset> getProblemReducer() {
		throw new UnsupportedOperationException("Problem reducer not applicable for sampling algorithms!");
	}

	@Override
	public IDataset getSimpleProblemInputForGeneralTestPurposes() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IDataset getDifficultProblemInputForGeneralTestPurposes() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
