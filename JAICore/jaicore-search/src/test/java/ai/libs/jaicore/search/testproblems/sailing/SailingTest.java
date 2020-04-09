package ai.libs.jaicore.search.testproblems.sailing;

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

	@Test
	public void test() throws Exception {
		IPathUpdatablePolicy<SailingState, SailingMove, Double> ucb = new UCBPolicy<>(false);
		for (int size = 10; size <= 50; size += 5) {
			SailingMDP mdp = new SailingMDP(size, size, 0, 0, size-1, size-1, SailingMove.NW);
			//			for (double alpha : new double[] {0.9999999}) {
			//				for (double b : new double[] {1.0, 2.0, 3.0, 4.0, 8.0, 16.0}) {

			//			IPathUpdatablePolicy<SailingState, SailingMove, Double> uucb = new UUCBPolicy<>(new VaR(alpha, b));
			//		IPathUpdatablePolicy<SailingState, SailingMove, Double> ucb = new DNG<>(false);
			DoubleList scores = new DoubleArrayList();
			long start = System.currentTimeMillis();
			for (int epochs = 4; epochs < 10000; epochs *= 2) {
				MDPUtils utils = new MDPUtils();
				DescriptiveStatistics stats = new DescriptiveStatistics();
				for (int seed = 0; seed < 10; seed++) {
					MCTSPolicySearch<SailingState, SailingMove> mcts = new MCTSPolicySearch<>(mdp, ucb, new UniformRandomPolicy<>(), epochs);
					System.out.println("Learning policy with " + epochs + " epochs");
					IPolicy<SailingState, SailingMove> policy = mcts.call();
					//		utils.setLoggerName("testedalgorithm");
					System.out.println("Policy ready, now evaluating.");
					for (int evalRun = 0; evalRun < 100; evalRun ++) {
						IEvaluatedPath<SailingState, SailingMove, Double> run = utils.getRun(mdp, policy, new Random(evalRun), a -> a.getHead().getRow() == 9 && a.getHead().getCol() == 9);
						stats.addValue(run.getScore());
					}
				}
				scores.add(stats.getMean());
			}
			System.out.println(size);// + "/ " + alpha + "/" + b + ": " + );
			System.out.println(scores);
			System.out.println(System.currentTimeMillis() - start);
		}
		//			}
		//		}
	}
}
