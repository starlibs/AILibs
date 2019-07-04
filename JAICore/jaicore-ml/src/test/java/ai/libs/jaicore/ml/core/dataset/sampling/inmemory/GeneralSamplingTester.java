package ai.libs.jaicore.ml.core.dataset.sampling.inmemory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;

import ai.libs.jaicore.basic.algorithm.AlgorithmCreationException;
import ai.libs.jaicore.basic.algorithm.AlgorithmTestProblemSetCreationException;
import ai.libs.jaicore.basic.algorithm.GeneralAlgorithmTester;
import ai.libs.jaicore.basic.algorithm.IAlgorithm;
import ai.libs.jaicore.ml.core.dataset.ILabeledAttributeArrayInstance;
import ai.libs.jaicore.ml.core.dataset.INumericLabeledAttributeArrayInstance;
import ai.libs.jaicore.ml.core.dataset.IOrderedLabeledAttributeArrayDataset;

/**
 * This class provides some tests that verify basic properties of a sampling
 * algorithm. For a specific sampling algorithm, this class can be extended by a
 * class that is specific for that particular algorithm.
 *
 * @author Felix Weiland, fmohr
 *
 */
public abstract class GeneralSamplingTester<L> extends GeneralAlgorithmTester {

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

	@Override
	public SamplingAlgorithmTestProblemSet<L> getProblemSet() {
		return (SamplingAlgorithmTestProblemSet<L>) super.getProblemSet();
	}

	@Override
	public final IAlgorithm<?, ?> getAlgorithm(final Object problem) {
		@SuppressWarnings("unchecked")
		IOrderedLabeledAttributeArrayDataset<INumericLabeledAttributeArrayInstance<L>, L> dataset = (IOrderedLabeledAttributeArrayDataset<INumericLabeledAttributeArrayInstance<L>, L>) problem;
		return this.getAlgorithm(dataset);
	}

	public abstract IAlgorithm<?, ?> getAlgorithm(IOrderedLabeledAttributeArrayDataset<INumericLabeledAttributeArrayInstance<L>, L> dataset);

	/**
	 * This test verifies that the produced samples have the desired size.
	 *
	 * This test executes the test on a small problem/data set.
	 *
	 * @param sampleFraction
	 * @throws AlgorithmTestProblemSetCreationException
	 * @throws AlgorithmCreationException
	 * @throws Exception
	 */
	@Test
	public void testSampleSizeSmallProblem() throws AlgorithmTestProblemSetCreationException {
		SamplingAlgorithmTestProblemSet<L> problemSet = this.getProblemSet();
		IOrderedLabeledAttributeArrayDataset<?, L> dataset = problemSet.getSimpleProblemInputForGeneralTestPurposes();
		this.testSampleSize(dataset, DEFAULT_SAMPLE_FRACTION);
	}

	/**
	 * This test verifies that the produced samples have the desired size.
	 *
	 * This test executes the test on a large problem/data set.
	 *
	 * @param sampleFraction
	 * @throws AlgorithmTestProblemSetCreationException
	 * @throws AlgorithmCreationException
	 * @throws Exception
	 */
	@Test
	public void testSampleSizeLargeProblem() throws AlgorithmTestProblemSetCreationException {
		SamplingAlgorithmTestProblemSet<L> problemSet = this.getProblemSet();
		IOrderedLabeledAttributeArrayDataset<?, L> dataset = problemSet.getDifficultProblemInputForGeneralTestPurposes();
		this.testSampleSize(dataset, DEFAULT_SAMPLE_FRACTION);
	}

	private <I extends ILabeledAttributeArrayInstance<L>> void testSampleSize(final IOrderedLabeledAttributeArrayDataset<I, L> dataset, final double sampleFraction) {
		@SuppressWarnings("unchecked")
		ASamplingAlgorithm<I, IOrderedLabeledAttributeArrayDataset<I, L>> samplingAlgorithm = (ASamplingAlgorithm<I, IOrderedLabeledAttributeArrayDataset<I, L>>) this.getAlgorithm(dataset);
		int sampleSize = (int) (dataset.size() * sampleFraction);
		samplingAlgorithm.setSampleSize(sampleSize);
		IOrderedLabeledAttributeArrayDataset<?, ?> sample = this.getSample(samplingAlgorithm);
		assertNotNull(sample);
		if (sample != null) {
			assertEquals(sampleSize, sample.size());
		}
	}

	/**
	 * This test verifies that the produced samples do not contain duplicates.
	 *
	 * This test executes the test on a small problem/data set.
	 *
	 * @throws AlgorithmCreationException
	 *
	 * @throws Exception
	 *
	 */
	@Test
	public void testNoDuplicatesSmallProblem() throws AlgorithmTestProblemSetCreationException, AlgorithmCreationException {
		SamplingAlgorithmTestProblemSet<L> problemSet = this.getProblemSet();
		IOrderedLabeledAttributeArrayDataset<?, L> dataset = problemSet.getSimpleProblemInputForGeneralTestPurposes();
		this.testNoDuplicates(dataset);
	}

	/**
	 * This test verifies that the produced samples do not contain duplicates.
	 *
	 * This test executes the test on a large problem/data set.
	 *
	 * @throws AlgorithmTestProblemSetCreationException
	 *
	 * @throws Exception
	 *
	 */
	@Test
	public void testNoDuplicatesLargeProblem() throws AlgorithmTestProblemSetCreationException, AlgorithmCreationException {
		SamplingAlgorithmTestProblemSet<L> problemSet = this.getProblemSet();
		IOrderedLabeledAttributeArrayDataset<?, L> dataset = problemSet.getDifficultProblemInputForGeneralTestPurposes();
		this.testNoDuplicates(dataset);
	}

	private <I extends ILabeledAttributeArrayInstance<L>> void testNoDuplicates(final IOrderedLabeledAttributeArrayDataset<I, L> dataset) throws AlgorithmCreationException {
		@SuppressWarnings("unchecked")
		ASamplingAlgorithm<I, IOrderedLabeledAttributeArrayDataset<I, L>> samplingAlgorithm = (ASamplingAlgorithm<I, IOrderedLabeledAttributeArrayDataset<I, L>>) this.getAlgorithm(dataset);
		int sampleSize = (int) (dataset.size() * DEFAULT_SAMPLE_FRACTION);
		samplingAlgorithm.setSampleSize(sampleSize);
		IOrderedLabeledAttributeArrayDataset<I, L> sample = this.getSample(samplingAlgorithm);
		Set<ILabeledAttributeArrayInstance<L>> set = new HashSet<>();
		set.addAll(sample);
		if (sample == null) {
			fail("Sample must not be null");
		} else {
			assertEquals(sample.size(), set.size());
		}
	}

	/**
	 * This test verifies that the original data set, which is fed to the sampling
	 * algorithm, is not modified in the sampling process.
	 *
	 * @throws AlgorithmTestProblemSetCreationException
	 * @throws AlgorithmCreationException
	 *
	 * @throws Exception
	 */
	@Test
	public <I extends ILabeledAttributeArrayInstance<L>> void checkOriginalDataSetNotModified() throws AlgorithmTestProblemSetCreationException {
		SamplingAlgorithmTestProblemSet problemSet = this.getProblemSet();
		IOrderedLabeledAttributeArrayDataset<?, ?> dataset = problemSet.getSimpleProblemInputForGeneralTestPurposes();
		int hashCode = dataset.hashCode();
		@SuppressWarnings("unchecked")
		ASamplingAlgorithm<I, IOrderedLabeledAttributeArrayDataset<I, L>> samplingAlgorithm = (ASamplingAlgorithm<I, IOrderedLabeledAttributeArrayDataset<I, L>>) this.getAlgorithm(dataset);
		int sampleSize = (int) (dataset.size() * DEFAULT_SAMPLE_FRACTION);
		samplingAlgorithm.setSampleSize(sampleSize);
		this.getSample(samplingAlgorithm);
		assertEquals(hashCode, dataset.hashCode());
	}

	private <I extends ILabeledAttributeArrayInstance<L>> IOrderedLabeledAttributeArrayDataset<I, L> getSample(final ASamplingAlgorithm<I, IOrderedLabeledAttributeArrayDataset<I, L>> samplingAlgorithm) {
		IOrderedLabeledAttributeArrayDataset<I, L> sample = null;
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
