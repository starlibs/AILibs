package ai.libs.hasco.test;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.TimeUnit;

import org.api4.java.algorithm.Timeout;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import ai.libs.hasco.builder.HASCOBuilder;
import ai.libs.hasco.builder.forwarddecomposition.HASCOViaFD;
import ai.libs.hasco.builder.forwarddecomposition.HASCOViaFDAndDFSBuilder;
import ai.libs.hasco.builder.forwarddecomposition.HASCOViaFDBuilder;
import ai.libs.hasco.core.HASCO;
import ai.libs.jaicore.basic.algorithm.AlgorithmTestProblemSetCreationException;
import ai.libs.jaicore.components.model.RefinementConfiguredSoftwareConfigurationProblem;
import ai.libs.jaicore.search.algorithms.standard.auxilliary.iteratingoptimizer.IteratingGraphSearchOptimizer;
import ai.libs.jaicore.search.algorithms.standard.auxilliary.iteratingoptimizer.IteratingGraphSearchOptimizerFactory;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.BestFirst;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.BestFirstFactory;
import ai.libs.jaicore.search.algorithms.standard.dfs.DepthFirstSearch;
import ai.libs.jaicore.search.algorithms.standard.dfs.DepthFirstSearchFactory;

public class HASCOBuilderTest {

	private final SoftwareConfigurationProblemSet problemset = new SoftwareConfigurationProblemSet();
	private final RefinementConfiguredSoftwareConfigurationProblem<Double> simpleProblem;

	private static final int PARAM_TIMEOUT = 471132; // some strange number
	private static final int PARAM_CPUS = 105;

	public HASCOBuilderTest() throws AlgorithmTestProblemSetCreationException {
		this.simpleProblem = this.problemset.getSimpleProblemInputForGeneralTestPurposes();
	}

	@Tag("short-test")
	@SuppressWarnings("rawtypes")
	@Test
	public void testDFSFactorySetting() throws AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException {
		HASCOViaFDAndDFSBuilder<Double, ?> builder = HASCOBuilder.get(this.simpleProblem).withDFS();
		this.checkThatParamsAreSetAppropriatelyByBuilder(builder);
		HASCOViaFD<Double> hasco = builder.getAlgorithm();
		assertEquals(IteratingGraphSearchOptimizerFactory.class, hasco.getSearchFactory().getClass());
		assertEquals(DepthFirstSearchFactory.class, ((IteratingGraphSearchOptimizerFactory) hasco.getSearchFactory()).getBaseAlgorithmFactory().getClass());
		assertEquals(IteratingGraphSearchOptimizer.class, hasco.getSearch().getClass());
		assertEquals(DepthFirstSearch.class, ((IteratingGraphSearchOptimizer) hasco.getSearch()).getBaseAlgorithm().getClass());
	}

	@Tag("short-test")
	@Test
	public void testBestFirstFactorySetting() throws AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException {
		HASCOViaFDBuilder<Double, ?> builder = HASCOBuilder.get(this.simpleProblem).withBestFirst().withBlindSearch();
		this.checkThatParamsAreSetAppropriatelyByBuilder(builder);
		HASCOViaFD<Double> hasco = builder.getAlgorithm();
		assertEquals(BestFirstFactory.class, hasco.getSearchFactory().getClass());
		assertEquals(BestFirst.class, hasco.getSearch().getClass());
	}

	@Tag("short-test")
	@SuppressWarnings("rawtypes")
	@Test
	public void testThatSearchFactoryCanBeOverWritten() {

		/* First assign DFS and then BestFirst */
		HASCOViaFDBuilder<Double, ?> builder = HASCOBuilder.get(this.simpleProblem).withDFS().withBestFirst().withBlindSearch();
		this.checkThatParamsAreSetAppropriatelyByBuilder(builder);
		HASCOViaFD<Double> hasco = builder.getAlgorithm();
		assertEquals(BestFirstFactory.class, hasco.getSearchFactory().getClass());
		assertEquals(BestFirst.class, hasco.getSearch().getClass());

		/* First assign BestFirst and then DFS */
		builder = HASCOBuilder.get(this.simpleProblem).withBestFirst().withBlindSearch().withDFS();
		this.checkThatParamsAreSetAppropriatelyByBuilder(builder);
		hasco = builder.getAlgorithm();
		assertEquals(IteratingGraphSearchOptimizerFactory.class, hasco.getSearchFactory().getClass());
		assertEquals(DepthFirstSearchFactory.class, ((IteratingGraphSearchOptimizerFactory) hasco.getSearchFactory()).getBaseAlgorithmFactory().getClass());
		assertEquals(IteratingGraphSearchOptimizer.class, hasco.getSearch().getClass());
		assertEquals(DepthFirstSearch.class, ((IteratingGraphSearchOptimizer) hasco.getSearch()).getBaseAlgorithm().getClass());
	}

	@Tag("short-test")
	@Test
	public void testThatBestFirstFailsWithoutFurtherSpecification() {
		Assertions.assertThrows(IllegalStateException.class, () -> {
			HASCOBuilder.get(this.simpleProblem).withBestFirst().getAlgorithm();
		});
	}

	private void checkThatParamsAreSetAppropriatelyByBuilder(final HASCOBuilder<?, ?, ?, ?> builder) {
		builder.withTimeout(new Timeout(PARAM_TIMEOUT, TimeUnit.SECONDS));
		builder.withCPUs(PARAM_CPUS);
		HASCO<?, ?, ?> hasco = builder.getAlgorithm();
		assertEquals(PARAM_TIMEOUT, hasco.getTimeout().seconds());
		assertEquals(PARAM_CPUS, hasco.getNumCPUs());
	}
}
