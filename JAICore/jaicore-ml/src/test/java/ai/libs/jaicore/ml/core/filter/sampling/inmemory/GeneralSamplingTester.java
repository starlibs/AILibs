package ai.libs.jaicore.ml.core.filter.sampling.inmemory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.api4.java.ai.ml.core.dataset.IDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.algorithm.IAlgorithm;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.basic.algorithm.AlgorithmCreationException;
import ai.libs.jaicore.basic.algorithm.AlgorithmTestProblemSetCreationException;
import ai.libs.jaicore.basic.algorithm.GeneralAlgorithmTester;
import ai.libs.jaicore.ml.core.dataset.clusterable.ClusterableDataset;
import ai.libs.jaicore.ml.core.filter.sampling.IClusterableInstance;

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
	private Logger logger = LoggerFactory.getLogger(GeneralSamplingTester.class);
	private static final String SAMPLER_LOGGER_NAME = "testedalgorithm";

	@Parameters(name = "problemset = {0}")
	public static Collection<Object[]> data() {
		List<Object> problemSets = new ArrayList<>();

		/* add N-Queens (as a graph search problem set) */
		problemSets.add(new SamplingAlgorithmTestProblemSet());
		List<Collection<Object>> input = new ArrayList<>();
		input.add(problemSets);

		Object[][] data = new Object[problemSets.size()][1];
		for (int i = 0; i < data.length; i++) {
			data[i][0] = problemSets.get(i);
		}
		return Arrays.asList(data);
	}

	@Override
	public SamplingAlgorithmTestProblemSet getProblemSet() {
		return (SamplingAlgorithmTestProblemSet) super.getProblemSet();
	}

	@Override
	public final IAlgorithm<?, ?> getAlgorithm(final Object problem) {
		@SuppressWarnings("unchecked")
		ILabeledDataset<IClusterableInstance> dataset = new ClusterableDataset((ILabeledDataset<ILabeledInstance>) problem);
		return this.getAlgorithm(dataset);
	}

	public abstract IAlgorithm<?, ?> getAlgorithm(ILabeledDataset<?> dataset);

	@Test
	public void testSampleSizeTinyProblem() throws AlgorithmTestProblemSetCreationException, InterruptedException {
		SamplingAlgorithmTestProblemSet problemSet = this.getProblemSet();
		ILabeledDataset<?> dataset = problemSet.getTinyProblemInputForGeneralTestPurposes();
		this.logger.info("Testing sample on dataset with {} data points.", dataset.size());
		this.testSampleSize(dataset, DEFAULT_SAMPLE_FRACTION);
	}

	/**
	 * This test verifies that the produced samples have the desired size.
	 *
	 * This test executes the test on a small problem/data set.
	 *
	 * @param sampleFraction
	 * @throws AlgorithmTestProblemSetCreationException
	 * @throws InterruptedException
	 * @throws AlgorithmCreationException
	 * @throws Exception
	 */
	@Test
	public void testSampleSizeSmallProblem() throws AlgorithmTestProblemSetCreationException, InterruptedException {
		SamplingAlgorithmTestProblemSet problemSet = this.getProblemSet();
		ILabeledDataset<?> dataset = problemSet.getSimpleProblemInputForGeneralTestPurposes();
		this.testSampleSize(dataset, DEFAULT_SAMPLE_FRACTION);
	}

	/**
	 * This test verifies that the produced samples have the desired size.
	 *
	 * This test executes the test on a large problem/data set.
	 *
	 * @param sampleFraction
	 * @throws AlgorithmTestProblemSetCreationException
	 * @throws InterruptedException
	 * @throws AlgorithmCreationException
	 * @throws Exception
	 */
	@Test
	public void testSampleSizeLargeProblem() throws AlgorithmTestProblemSetCreationException, InterruptedException {
		SamplingAlgorithmTestProblemSet problemSet = this.getProblemSet();
		ILabeledDataset<?> dataset = problemSet.getDifficultProblemInputForGeneralTestPurposes();
		this.testSampleSize(dataset, DEFAULT_SAMPLE_FRACTION);
	}

	private <I extends ILabeledInstance> void testSampleSize(final ILabeledDataset<I> dataset, final double sampleFraction) {
		@SuppressWarnings("unchecked")
		ASamplingAlgorithm<ILabeledDataset<I>> samplingAlgorithm = (ASamplingAlgorithm<ILabeledDataset<I>>) this.getAlgorithm(dataset);
		int sampleSize = (int) (dataset.size() * sampleFraction);
		samplingAlgorithm.setSampleSize(sampleSize);
		IDataset<?> sample = this.getSample(samplingAlgorithm);
		assertNotNull(sample);
		if (sample != null) {
			assertEquals(sampleSize, sample.size());
		}
	}


	/**
	 * This test verifies that the produced samples do not contain duplicates.
	 *
	 * This test executes the test on a tiny problem/data set. This is useful to test algorithms with quadratic runtime.
	 *
	 * @throws AlgorithmCreationException
	 * @throws InterruptedException
	 *
	 * @throws Exception
	 *
	 */
	@Test
	public void testNoDuplicatesTinyProblem() throws AlgorithmTestProblemSetCreationException, AlgorithmCreationException, InterruptedException {
		SamplingAlgorithmTestProblemSet problemSet = this.getProblemSet();
		ILabeledDataset<?> dataset = problemSet.getTinyProblemInputForGeneralTestPurposes();
		this.testNoDuplicates(dataset);
	}

	/**
	 * This test verifies that the produced samples do not contain duplicates.
	 *
	 * This test executes the test on a small problem/data set.
	 *
	 * @throws AlgorithmCreationException
	 * @throws InterruptedException
	 *
	 * @throws Exception
	 *
	 */
	@Test
	public void testNoDuplicatesSmallProblem() throws AlgorithmTestProblemSetCreationException, AlgorithmCreationException, InterruptedException {
		SamplingAlgorithmTestProblemSet problemSet = this.getProblemSet();
		ILabeledDataset<?> dataset = problemSet.getSimpleProblemInputForGeneralTestPurposes();
		this.testNoDuplicates(dataset);
	}

	/**
	 * This test verifies that the produced samples do not contain duplicates.
	 *
	 * This test executes the test on a large problem/data set.
	 *
	 * @throws AlgorithmTestProblemSetCreationException
	 * @throws InterruptedException
	 *
	 * @throws Exception
	 *
	 */
	@Test
	public void testNoDuplicatesLargeProblem() throws AlgorithmTestProblemSetCreationException, AlgorithmCreationException, InterruptedException {
		SamplingAlgorithmTestProblemSet problemSet = this.getProblemSet();
		ILabeledDataset<?> dataset = problemSet.getDifficultProblemInputForGeneralTestPurposes();
		assertNotNull(dataset);
		this.logger.info("Loaded dataset with {} instances and {} attributes.", dataset.size(), dataset.getNumAttributes());
		this.testNoDuplicates(dataset);
	}

	private <I extends ILabeledInstance> void testNoDuplicates(final ILabeledDataset<I> dataset) throws AlgorithmCreationException {
		Set<I> dsAsSet = new HashSet<>(dataset);
		assertEquals("Cannot check sampling, because the original dataset already has duplicates.", dataset.size(), dsAsSet.size());
		@SuppressWarnings("unchecked")
		ASamplingAlgorithm<ILabeledDataset<I>> samplingAlgorithm = (ASamplingAlgorithm<ILabeledDataset<I>>) this.getAlgorithm(dataset);
		samplingAlgorithm.setLoggerName(SAMPLER_LOGGER_NAME);
		int sampleSize = (int) (dataset.size() * DEFAULT_SAMPLE_FRACTION);
		samplingAlgorithm.setSampleSize(sampleSize);
		ILabeledDataset<I> sample = this.getSample(samplingAlgorithm);
		Set<ILabeledInstance> set = new HashSet<>();
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
	 * @throws InterruptedException
	 * @throws AlgorithmCreationException
	 *
	 * @throws Exception
	 */
	@Test
	public <I extends ILabeledInstance> void checkOriginalDataSetNotModified() throws AlgorithmTestProblemSetCreationException, InterruptedException {
		SamplingAlgorithmTestProblemSet problemSet = this.getProblemSet();
		ILabeledDataset<? extends ILabeledInstance> dataset = problemSet.getTinyProblemInputForGeneralTestPurposes();
		int hashCode = dataset.hashCode();
		@SuppressWarnings("unchecked")
		ASamplingAlgorithm<ILabeledDataset<I>> samplingAlgorithm = (ASamplingAlgorithm<ILabeledDataset<I>>) this.getAlgorithm(dataset);
		int sampleSize = (int) (dataset.size() * DEFAULT_SAMPLE_FRACTION);
		samplingAlgorithm.setSampleSize(sampleSize);
		this.logger.debug("Drawing sample of size {} for dataset of size {}", sampleSize, dataset.size());
		this.getSample(samplingAlgorithm);
		assertEquals(hashCode, dataset.hashCode());
	}

	private <I extends ILabeledInstance> ILabeledDataset<I> getSample(final ASamplingAlgorithm<ILabeledDataset<I>> samplingAlgorithm) {
		ILabeledDataset<I> sample = null;
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

	@Override
	public String getLoggerName() {
		return this.logger.getName();
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger.info("Switching logger name from {} to {}.", this.logger.getName(), name);
		this.logger = LoggerFactory.getLogger(name);
		this.logger.info("Switched logger name to {}.", this.logger.getName());
	}
}
