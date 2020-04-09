package ai.libs.jaicore.search.testproblems.lake;

import java.util.Random;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IEvaluatedPath;
import org.junit.Test;

import ai.libs.jaicore.search.algorithms.mdp.mcts.MCTSPolicySearch;
import ai.libs.jaicore.search.algorithms.standard.mcts.IPolicy;
import ai.libs.jaicore.search.algorithms.standard.mcts.UCBPolicy;
import ai.libs.jaicore.search.algorithms.standard.mcts.UniformRandomPolicy;
import ai.libs.jaicore.search.exampleproblems.lake.ELakeActions;
import ai.libs.jaicore.search.exampleproblems.lake.FelixLakeMDP;
import ai.libs.jaicore.search.exampleproblems.lake.LakeMDP;
import ai.libs.jaicore.search.exampleproblems.lake.LakeState;
import ai.libs.jaicore.search.probleminputs.MDPUtils;

public class LakeTest {

	@Test
	public void test() throws Exception {
		LakeMDP mdp = new FelixLakeMDP(4);
		System.out.println(mdp.getInitState().getStringVisualization());
		//		LakeState succ = mdp.getInitState();
		//		System.out.println(succ.getStringVisualization());
		//		for (int i = 0; i < 10; i++) {
		//			succ = mdp.drawSuccessorState(succ, ELakeActions.DOWN);
		//			System.out.println(succ.getStringVisualization());
		//		}

		MCTSPolicySearch<LakeState, ELakeActions> mcts = new MCTSPolicySearch<>(mdp, new UCBPolicy<>(false), new UniformRandomPolicy<>(), 10000);
		mcts.setLoggerName("testedalgorithm");
		IPolicy<LakeState, ELakeActions> policy = mcts.call();
		//
		//		for (int t = 0; t < 20; t++) {
		//			System.out.println("TIME " + t);
		//			for (int r = 0; r < layout.getRows(); r ++) {
		//				for (int c = 0; c < layout.getCols(); c++) {
		//					LakeState s = new LakeState(layout, r, c, t);
		//					System.out.println(s.getStringVisualization());
		//					if (mdp.getApplicableActions(s).isEmpty()) {
		//						System.out.println("NOTHING");
		//					}
		//					else {
		//						System.out.println(policy.getAction(s, mdp.getApplicableActions(s)));
		//					}
		//				}
		//			}
		//		}

		/* now let's use the policy to move around */
		for (int i = 0; i < 10; i++) {
			System.out.println("RUN " + i);
			IEvaluatedPath<LakeState, ELakeActions, Double> run = MDPUtils.getRun(mdp, policy, new Random(i), p -> false);
			for (LakeState n : run.getPathToParentOfHead().getNodes()) {
				ELakeActions a = run.getOutArc(n);
				System.out.println(n.getStringVisualization());
				System.out.println(a);
			}
			System.out.println(run.getHead().getStringVisualization());
		}
	}
}
