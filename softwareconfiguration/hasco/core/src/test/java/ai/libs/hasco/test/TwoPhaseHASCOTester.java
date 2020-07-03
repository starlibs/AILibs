package ai.libs.hasco.test;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.aeonbits.owner.ConfigCache;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.hasco.builder.HASCOBuilder;
import ai.libs.hasco.builder.forwarddecomposition.HASCOViaFD;
import ai.libs.hasco.builder.forwarddecomposition.twophase.TwoPhaseHASCO;
import ai.libs.hasco.builder.forwarddecomposition.twophase.TwoPhaseHASCOConfig;
import ai.libs.hasco.builder.forwarddecomposition.twophase.TwoPhaseSoftwareConfigurationProblem;
import ai.libs.hasco.core.HASCOSolutionCandidate;
import ai.libs.jaicore.basic.algorithm.AlgorithmTestProblemSetCreationException;
import ai.libs.jaicore.components.model.RefinementConfiguredSoftwareConfigurationProblem;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;

public class TwoPhaseHASCOTester extends SoftwareConfigurationAlgorithmTester {

	private final Logger logger = LoggerFactory.getLogger(TwoPhaseHASCOTester.class);

	@Override
	public TwoPhaseHASCO<TFDNode, String> getAlgorithmForSoftwareConfigurationProblem(final RefinementConfiguredSoftwareConfigurationProblem<Double> problem) {

		/* produce an HASCO instance */
		HASCOViaFD<Double> hasco = HASCOBuilder.get(problem).withBestFirst().viaRandomCompletions().withNumSamples(3).getAlgorithm();

		/* produce two-phase HASCO */
		TwoPhaseSoftwareConfigurationProblem prob = new TwoPhaseSoftwareConfigurationProblem(problem, problem.getParamRefinementConfig(), problem.getCompositionEvaluator());
		TwoPhaseHASCOConfig config = ConfigCache.getOrCreate(TwoPhaseHASCOConfig.class);
		TwoPhaseHASCO<TFDNode, String> twoPhaseHASCO = new TwoPhaseHASCO<>(prob, config);
		twoPhaseHASCO.setHasco(hasco);
		return twoPhaseHASCO;
	}

	@Test
	public void testThatBestSolutionFoundByHASCOIsSuccessfullyEvaluatedInPhase2() throws AlgorithmTestProblemSetCreationException, AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException {
		TwoPhaseHASCO<TFDNode, String> twoPhaseHASCO = this.getAlgorithmForSoftwareConfigurationProblem(((SoftwareConfigurationProblemSet) this.getProblemSet()).getSimpleProblemInputForGeneralTestPurposes());
		twoPhaseHASCO.setLoggerName(TESTEDALGORITHM_LOGGERNAME);
		twoPhaseHASCO.call();
		HASCOSolutionCandidate<Double> bestCandidateOfPhase1 = twoPhaseHASCO.getBestSeenSolution();
		assertTrue("Best solution of phase 1 has not been successfully evaluated in phase 2!",
				twoPhaseHASCO.getSelectionScoresOfCandidates().containsKey(bestCandidateOfPhase1) && twoPhaseHASCO.getSelectionScoresOfCandidates().get(bestCandidateOfPhase1) != null);
	}

	@Test
	public void testThatHalfOfSelectionPoolIsSuccessfullyEvaluated() throws AlgorithmTestProblemSetCreationException, AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException {
		TwoPhaseHASCO<TFDNode, String> twoPhaseHASCO = this.getAlgorithmForSoftwareConfigurationProblem(((SoftwareConfigurationProblemSet)this.getProblemSet()).getSimpleProblemInputForGeneralTestPurposes());
		twoPhaseHASCO.setLoggerName(TESTEDALGORITHM_LOGGERNAME);
		twoPhaseHASCO.call();
		List<HASCOSolutionCandidate<Double>> selectionPoolForPhase2 = twoPhaseHASCO.getSelectionForPhase2();
		int n =  selectionPoolForPhase2.size();
		for (int i = 0; i < (int)Math.ceil(n * 0.5); i++) {
			assertTrue("Solution at position " + i + "/" + n + " in selection pool list has not been evaluated!", twoPhaseHASCO.getSelectionScoresOfCandidates().containsKey(selectionPoolForPhase2.get(i)) && twoPhaseHASCO.getSelectionScoresOfCandidates().get(selectionPoolForPhase2.get(i)) != null);
		}
		this.logger.info("The first {}/{} solutions have been evaluated successfully.", n, selectionPoolForPhase2.size());
	}
}
