package ai.libs.jaicore.search.testproblems.sailing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IEvaluatedPath;
import org.junit.Test;

import ai.libs.jaicore.search.algorithms.mdp.mcts.MCTSPolicySearch;
import ai.libs.jaicore.search.algorithms.standard.mcts.IPathUpdatablePolicy;
import ai.libs.jaicore.search.algorithms.standard.mcts.IPolicy;
import ai.libs.jaicore.search.algorithms.standard.mcts.UCBPolicy;
import ai.libs.jaicore.search.algorithms.standard.mcts.UniformRandomPolicy;
import ai.libs.jaicore.search.exampleproblems.sailing.SailingMDP;
import ai.libs.jaicore.search.exampleproblems.sailing.SailingMove;
import ai.libs.jaicore.search.exampleproblems.sailing.SailingState;
import ai.libs.jaicore.search.probleminputs.MDPUtils;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;

public class SailingTest {

	private static List<Integer> checkpoints = new ArrayList<>();

	static {
		checkpoints.addAll(Arrays.asList(10));
		for (int i = 100; i <= 1000; i+=100) {
			checkpoints.add(i);
		}
	}

	@Test
	public void test() throws Exception {
		IPathUpdatablePolicy<SailingState, SailingMove, Double> ucb = new UCBPolicy<>(false);

		for (int size = 15; size <= 15; size += 5) {
			SailingMDP mdp = new SailingMDP(size, size, 0, 0, size-1, size-1, SailingMove.NW);
			//			for (double alpha : new double[] {0.9999999}) {
			//				for (double b : new double[] {1.0, 2.0, 3.0, 4.0, 8.0, 16.0}) {

			//			IPathUpdatablePolicy<SailingState, SailingMove, Double> uucb = new UUCBPolicy<>(new VaR(alpha, b));
			//		IPathUpdatablePolicy<SailingState, SailingMove, Double> ucb = new DNG<>(false);
			MDPUtils utils = new MDPUtils();

			for (int seed = 0; seed < 20; seed ++) {
				MCTSPolicySearch<SailingState, SailingMove> mcts = new MCTSPolicySearch<>(mdp, ucb, new UniformRandomPolicy<>(new Random(seed)), 100000000);
				int finishedIterations = 0;
				mcts.nextWithException(); // activates MCTS
				long start = System.currentTimeMillis();
				DoubleList scores = new DoubleArrayList();
				for (int iterationsToNextCheckpoint : checkpoints) {
					int missingIterations = iterationsToNextCheckpoint - finishedIterations;
					System.out.println("Learning policy with " + missingIterations + " epochs");
					for (int epochs = 0; epochs < missingIterations; epochs ++) {
						mcts.nextWithException();
						finishedIterations ++;
						//					System.out.println("Iterations: " + mcts.getIterations());
					}
					IPolicy<SailingState, SailingMove> policy = mcts.getTreePolicy();
					//		utils.setLoggerName("testedalgorithm");
					//				System.out.println("Policy ready, now evaluating.");
					DescriptiveStatistics stats = new DescriptiveStatistics();
					for (int evalRun = 0; evalRun < 20; evalRun ++) {
						IEvaluatedPath<SailingState, SailingMove, Double> run = utils.getRun(mdp, policy, new Random((1 + evalRun) * (1 + seed)), a -> a.getHead().getRow() == 9 && a.getHead().getCol() == 9);
						stats.addValue(run.getScore());
					}
					scores.add(stats.getMean());
				}
				System.out.println(seed + ": " + scores);
				System.out.println(System.currentTimeMillis() - start);
			}
		}
	}
}
