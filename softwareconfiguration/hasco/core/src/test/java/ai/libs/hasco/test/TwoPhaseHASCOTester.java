package ai.libs.hasco.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.aeonbits.owner.ConfigCache;
import org.api4.java.algorithm.Timeout;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.awaitility.Awaitility;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

import ai.libs.hasco.builder.HASCOBuilder;
import ai.libs.hasco.builder.forwarddecomposition.HASCOViaFD;
import ai.libs.hasco.core.HASCOSolutionCandidate;
import ai.libs.hasco.core.events.HASCOSolutionEvent;
import ai.libs.hasco.twophase.TwoPhaseCandidateEvaluator;
import ai.libs.hasco.twophase.TwoPhaseHASCO;
import ai.libs.hasco.twophase.TwoPhaseHASCOConfig;
import ai.libs.hasco.twophase.TwoPhaseSoftwareConfigurationProblem;
import ai.libs.jaicore.basic.algorithm.AlgorithmTestProblemSetCreationException;
import ai.libs.jaicore.components.model.ComponentInstance;
import ai.libs.jaicore.components.model.RefinementConfiguredSoftwareConfigurationProblem;
import ai.libs.jaicore.logging.LoggerUtil;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import ai.libs.jaicore.test.LongParameterizedTest;
import ai.libs.jaicore.test.MediumParameterizedTest;

public class TwoPhaseHASCOTester extends SoftwareConfigurationAlgorithmTester {

	private final Logger logger = LoggerFactory.getLogger(TwoPhaseHASCOTester.class);

	@Override
	public TwoPhaseHASCO<TFDNode, String> getAlgorithmForSoftwareConfigurationProblem(final RefinementConfiguredSoftwareConfigurationProblem<Double> problem) {

		/* produce an HASCO instance */
		HASCOViaFD<Double> hasco = HASCOBuilder.get(problem).withBestFirst().withRandomCompletions().withNumSamples(3).getAlgorithm();

		/* produce two-phase HASCO */
		TwoPhaseSoftwareConfigurationProblem prob = new TwoPhaseSoftwareConfigurationProblem(problem, problem.getParamRefinementConfig(), problem.getCompositionEvaluator());
		TwoPhaseHASCOConfig config = ConfigCache.getOrCreate(TwoPhaseHASCOConfig.class);
		TwoPhaseHASCO<TFDNode, String> twoPhaseHASCO = new TwoPhaseHASCO<>(prob, config);
		twoPhaseHASCO.setHasco(hasco);
		return twoPhaseHASCO;
	}

	@MediumParameterizedTest
	@MethodSource("getProblemSets")
	public void testThatEnsembleConsideredBySelectionProcedureIsOrderedByScores(final SoftwareConfigurationProblemSet problemSet)
			throws AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException, AlgorithmTestProblemSetCreationException {
		TwoPhaseHASCO<TFDNode, String> twoPhaseHASCO = this.getAlgorithmForSoftwareConfigurationProblem(problemSet.getSimpleRecursiveProblemInput());
		twoPhaseHASCO.setLoggerName(LoggerUtil.LOGGER_NAME_TESTEDALGORITHM);
		twoPhaseHASCO.call();
		List<HASCOSolutionCandidate<Double>> list = twoPhaseHASCO.getEnsembleToSelectFromInPhase2();
		int n = list.size();
		for (int i = 1; i < n; i++) {
			assertTrue("The " + i + "-th entry of the list is smaller than its precedessor! List: " + list.stream().map(c -> "\n\t" + c.getScore()).collect(Collectors.joining()), list.get(i).getScore() >= list.get(i - 1).getScore());
		}
	}

	@LongParameterizedTest
	@MethodSource("getProblemSets")
	public void testThatBestSolutionFoundByHASCOIsSuccessfullyEvaluatedInPhase2(final SoftwareConfigurationProblemSet problemSet)
			throws AlgorithmTestProblemSetCreationException, AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException {
		TwoPhaseHASCO<TFDNode, String> twoPhaseHASCO = this.getAlgorithmForSoftwareConfigurationProblem(problemSet.getSimpleProblemInputForGeneralTestPurposes());
		twoPhaseHASCO.setLoggerName(LoggerUtil.LOGGER_NAME_TESTEDALGORITHM);
		twoPhaseHASCO.call();
		HASCOSolutionCandidate<Double> bestCandidateOfPhase1 = twoPhaseHASCO.getBestSeenSolution();
		assertTrue("Best solution of phase 1 has not been successfully evaluated in phase 2!",
				twoPhaseHASCO.getSelectionPhaseEvaluationRunners().containsKey(bestCandidateOfPhase1) && twoPhaseHASCO.getSelectionPhaseEvaluationRunners().get(bestCandidateOfPhase1) != null);
	}

	@MediumParameterizedTest
	@MethodSource("getProblemSets")
	public void testThatHalfOfSelectionPoolIsSuccessfullyEvaluated(final SoftwareConfigurationProblemSet problemSet)
			throws AlgorithmTestProblemSetCreationException, AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException {
		TwoPhaseHASCO<TFDNode, String> twoPhaseHASCO = this.getAlgorithmForSoftwareConfigurationProblem(problemSet.getSimpleRecursiveProblemInput());
		twoPhaseHASCO.setLoggerName(LoggerUtil.LOGGER_NAME_TESTEDALGORITHM);
		twoPhaseHASCO.call();
		List<HASCOSolutionCandidate<Double>> selectionPoolForPhase2 = twoPhaseHASCO.getEnsembleToSelectFromInPhase2();
		int n = selectionPoolForPhase2.size();
		for (int i = 0; i < (int) Math.ceil(n * 0.5); i++) {
			assertTrue("Solution at position " + i + "/" + n + " in selection pool list has not been evaluated!",
					twoPhaseHASCO.getSelectionPhaseEvaluationRunners().containsKey(selectionPoolForPhase2.get(i)) && twoPhaseHASCO.getSelectionPhaseEvaluationRunners().get(selectionPoolForPhase2.get(i)) != null);
		}
		this.logger.info("The first {}/{} solutions have been evaluated successfully.", n, selectionPoolForPhase2.size());
	}

	@MediumParameterizedTest
	@MethodSource("getProblemSets")
	public void testThatTimeoutsUsedForComputationsInPhase2CorrespondToThoseObservedInPhase1(final SoftwareConfigurationProblemSet problemSet)
			throws AlgorithmTestProblemSetCreationException, AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException {

		/* define the problem with simulated runtimes */
		RefinementConfiguredSoftwareConfigurationProblem<Double> problem = problemSet.getSimpleRecursiveProblemInput();
		final Map<String, Integer> runtimes = new HashMap<>();
		problem.getComponents().forEach(c -> runtimes.put(c.getName(), Integer.valueOf(("" + Math.abs(c.hashCode())).substring(0, 1))));
		final Random random = new Random(0);
		RefinementConfiguredSoftwareConfigurationProblem<Double> timeSimulatingProblem = new RefinementConfiguredSoftwareConfigurationProblem<>(problem, ci -> {
			int sleepTime = runtimes.get(ci.getComponent().getName());
			long start = System.currentTimeMillis();
			Awaitility.await().until(() -> System.currentTimeMillis() >= start + sleepTime);
			double score = random.nextDouble();
			return score;
		});

		/* get two-phase HASCO with solution listener */
		TwoPhaseHASCO<TFDNode, String> twoPhaseHASCO = this.getAlgorithmForSoftwareConfigurationProblem(timeSimulatingProblem);
		twoPhaseHASCO.setTimeout(new Timeout(30, TimeUnit.SECONDS));
		twoPhaseHASCO.setLoggerName(LoggerUtil.LOGGER_NAME_TESTEDALGORITHM);
		Map<ComponentInstance, Integer> trueTimes = new HashMap<>();
		twoPhaseHASCO.registerListener(new Object() {
			@Subscribe
			public void receiveSolution(final HASCOSolutionEvent<Double> e) {
				trueTimes.put(e.getSolutionCandidate().getComponentInstance(), e.getSolutionCandidate().getTimeToEvaluateCandidate());
			}
		});
		twoPhaseHASCO.call();

		/* check that phase 2 was conducted properly */
		List<HASCOSolutionCandidate<Double>> selectionPoolForPhase2 = twoPhaseHASCO.getEnsembleToSelectFromInPhase2();
		for (HASCOSolutionCandidate<Double> c : selectionPoolForPhase2) {
			assertEquals(trueTimes.get(c.getComponentInstance()), c.getTimeToEvaluateCandidate(), 0.00001);
		}
	}

	@MediumParameterizedTest
	@MethodSource("getProblemSets")
	public void testCorrectTimeoutsAndAdherenceOfWorkers(final SoftwareConfigurationProblemSet problemSet)
			throws AlgorithmTestProblemSetCreationException, AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException {

		/* define the problem with simulated runtimes */
		RefinementConfiguredSoftwareConfigurationProblem<Double> problem = problemSet.getSimpleRecursiveProblemInput();
		final Map<String, Integer> runtimes = new HashMap<>();
		problem.getComponents().forEach(c -> runtimes.put(c.getName(), 100 * Integer.valueOf(("" + Math.abs(c.hashCode())).substring(0, 1))));
		final Random random = new Random(0);
		RefinementConfiguredSoftwareConfigurationProblem<Double> timeSimulatingProblem = new RefinementConfiguredSoftwareConfigurationProblem<>(problem, ci -> {
			int sleepTime = runtimes.get(ci.getComponent().getName());
			long start = System.currentTimeMillis();
			Awaitility.await().until(() -> System.currentTimeMillis() >= start + sleepTime);
			double score = random.nextInt(3) + 1.0;
			return score;
		});

		/* get two-phase HASCO with solution listener */
		TwoPhaseHASCO<TFDNode, String> twoPhaseHASCO = this.getAlgorithmForSoftwareConfigurationProblem(timeSimulatingProblem);
		TwoPhaseHASCOConfig config = twoPhaseHASCO.getConfig();
		twoPhaseHASCO.setTimeout(new Timeout(20, TimeUnit.SECONDS));
		twoPhaseHASCO.setLoggerName(LoggerUtil.LOGGER_NAME_TESTEDALGORITHM);
		twoPhaseHASCO.call();

		/* check that phase 2 was conducted properly */
		Map<HASCOSolutionCandidate<Double>, TwoPhaseCandidateEvaluator> selectionPhaseEvaluators = twoPhaseHASCO.getSelectionPhaseEvaluationRunners();
		double blowUpInSelection = config.expectedBlowupInSelection();
		double blowUpInPostProcessing = config.expectedBlowupInPostprocessing();
		double toleranceThreshold = config.selectionPhaseTimeoutTolerance();
		for (TwoPhaseCandidateEvaluator e : selectionPhaseEvaluators.values()) {
			int inSearchRuntime = e.getSolution().getTimeToEvaluateCandidate();
			long totalTimeoutForEvaluationInMS = Math.max(1000, Math.round(inSearchRuntime * blowUpInSelection * (1 + toleranceThreshold)));
			assertEquals(inSearchRuntime * blowUpInSelection, e.getEstimatedInSelectionSingleIterationEvaluationTime(), 0.01);
			assertEquals(inSearchRuntime * blowUpInSelection * blowUpInPostProcessing, e.getEstimatedPostProcessingTime(), 0.01);
			assertEquals(totalTimeoutForEvaluationInMS, e.getTimeoutForEvaluation(), 0.01);
			assertTrue(e.getTrueEvaluationTime() <= totalTimeoutForEvaluationInMS);
		}
	}
}
