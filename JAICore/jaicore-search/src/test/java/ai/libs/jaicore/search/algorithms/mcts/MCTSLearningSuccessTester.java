package ai.libs.jaicore.search.algorithms.mcts;

import static org.junit.Assert.assertTrue;

import java.util.Objects;
import java.util.Random;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IEvaluatedPath;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.api4.java.common.attributedobjects.ObjectEvaluationFailedException;
import org.junit.Rule;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.rules.Timeout;

import ai.libs.jaicore.basic.ATest;
import ai.libs.jaicore.logging.LoggerUtil;
import ai.libs.jaicore.search.algorithms.mdp.mcts.ActionPredictionFailedException;
import ai.libs.jaicore.search.algorithms.mdp.mcts.EBehaviorForNotFullyExploredStates;
import ai.libs.jaicore.search.algorithms.mdp.mcts.uct.UCBPolicy;
import ai.libs.jaicore.search.algorithms.mdp.mcts.uct.UCT;
import ai.libs.jaicore.search.probleminputs.IMDP;
import ai.libs.jaicore.search.probleminputs.MDPUtils;
import ai.libs.jaicore.test.MediumTest;

@Tag("mcts")
public abstract class MCTSLearningSuccessTester<N, A> extends ATest {

	private static final int DEFAULT_NUMITERATIONS = 10000;
	private static final double DEFAULT_GAMMA = 1.0;
	private static final double DEFAULT_EPSILON = 0.01;

	@Rule
	public Timeout globalTimeout = Timeout.seconds(20);

	public abstract IMDP<N, A, Double> getMDP();

	public void preMCTSHook() {
		/* do nothing */
	}

	public abstract boolean isSuccess(IEvaluatedPath<N, A, Double> path);

	public abstract double getRequiredSuccessRate();

	public int getAllowedTrainingIterations() {
		return DEFAULT_NUMITERATIONS;
	}

	public double getGamma() {
		return DEFAULT_GAMMA;
	}

	public double getEpsilon() {
		return DEFAULT_EPSILON;
	}

	@Test
	@Disabled // currently disabled, because we cannot guarantee convergence!
	@MediumTest
	public void testLearningSuccess() throws AlgorithmTimeoutedException, ObjectEvaluationFailedException, InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException, ActionPredictionFailedException {
		final int max = 10;
		for (int i = 1; i <= max; i++) {
			this.logger.info("Starting test {}/{} of successful learning.", i, max);
			this.testLearningSuccessOnce();
			this.logger.info("Finished test {}/{} of successful learning.", i, max);
		}
	}

	public void testLearningSuccessOnce() throws AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException, ObjectEvaluationFailedException, ActionPredictionFailedException {
		IMDP<N, A, Double> mdp = this.getMDP();
		Objects.requireNonNull(mdp);
		MDPUtils utils = new MDPUtils();

		UCT<N, A> mcts = new UCT<>(mdp, this.getAllowedTrainingIterations(), this.getGamma(), this.getEpsilon(), new Random(0), false);
		mcts.setLoggerName(LoggerUtil.LOGGER_NAME_TESTEDALGORITHM);
		UCBPolicy<N, A> policy = mcts.getTreePolicy();
		this.logger.info("Training policy via MCTS");
		mcts.call();
		this.logger.info("Policy completed. Now testing it.");

		/* generate "greedy" (non-explorative) policy from the UCB */
		policy.setExplorationConstant(0);
		policy.setBehaviorWhenActionForNotFullyExploredStateIsRequested(EBehaviorForNotFullyExploredStates.RANDOM); // do not mind states that have not been explored

		/* now let's use the policy to move around */
		int successes = 0;
		int trials = 10000;
		for (int i = 1; i <= trials; i++) {
			this.logger.debug("Conducting trial {}/{}", i, trials);
			IEvaluatedPath<N, A, Double> run = utils.getRun(mdp, .5, policy, new Random(i), p -> p.getNumberOfNodes() > 1000);
			if (this.isSuccess(run)) {
				successes ++;
			}
		}
		double successRate = successes * 1.0 / trials;
		this.logger.info("Success rate is {}", successRate);
		double successRateReq = this.getRequiredSuccessRate();
		assertTrue("The policy is not good enough and only has a success rate of " + successRate + " while " + successRateReq + " is required.", successRate >= successRateReq);
	}
}
