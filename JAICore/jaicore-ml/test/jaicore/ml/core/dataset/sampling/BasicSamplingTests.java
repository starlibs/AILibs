package jaicore.ml.core.dataset.sampling;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.Set;

import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.IInstance;

/**
 * This class provides a collection of tests that verify basic properties of a
 * sampling algorithm. Note that not every test in this class is suitable for
 * each sampling algorithm. So for instance, in the sample produced by random
 * sampling it is not required that each class occurs at least once.
 * 
 * @author Felix Weiland
 *
 */
public class BasicSamplingTests {

	/**
	 * This test verifies that the produced sample is of the desired size.
	 * 
	 * @param dataset
	 *            The data set that has to be sampled
	 * @param samplingAlgorithm
	 *            The sampling algorithm that has to be tested
	 * @param sampleSize
	 *            The desired sample size
	 */
	public void testSampleSize(IDataset dataset, ASamplingAlgorithm samplingAlgorithm, int sampleSize) {
		IDataset sample = getSample(dataset, samplingAlgorithm);
		assertEquals(sample.size(), sampleSize);
	}

	/**
	 * This test verifies that the produced sample does not contain duplicates.
	 * 
	 * @param dataset
	 *            The data set that has to be sampled
	 * @param samplingAlgorithm
	 *            The sampling algorithm that has to be tested
	 */
	public void testNoDuplicates(IDataset dataset, ASamplingAlgorithm samplingAlgorithm) {
		IDataset sample = getSample(dataset, samplingAlgorithm);
		int sampleSize = sample.size();
		Set<IInstance> set = new HashSet<>();
		set.addAll(sample);
		assertEquals(sampleSize, set.size());
	}

	/**
	 * This test checks whether all classes (target attribute values) of the
	 * original data set also occur in the sample.
	 * 
	 * @param dataset
	 *            The data set that has to be sampled
	 * @param targetAttributeType
	 *            The target attribute type, depends on the data set / problem
	 * @param samplingAlgorithm
	 *            The sampling algorithm that has to be tested
	 */
	public <T> void testAllClassesOccur(IDataset dataset, Class<T> targetAttributeType,
			ASamplingAlgorithm samplingAlgorithm) {
		// Collect all classes
		Set<T> targetValues = new HashSet<>();
		for (IInstance instance : dataset) {
			targetValues.add(instance.getTargetValue(targetAttributeType).getValue());
		}

		// Do sampling
		IDataset sample = getSample(dataset, samplingAlgorithm);

		// Remove all seen classes
		for (IInstance instance : sample) {
			targetValues.remove(instance.getTargetValue(targetAttributeType).getValue());
		}

		// Check that there are no unseen classes
		assertTrue(targetValues.isEmpty());

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
