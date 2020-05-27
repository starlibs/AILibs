package ai.libs.jaicore.search.testproblems.lake;

import java.util.Random;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IEvaluatedPath;
import org.junit.Test;

import ai.libs.jaicore.search.algorithms.mdp.mcts.IPolicy;
import ai.libs.jaicore.search.algorithms.mdp.mcts.MCTSPolicySearch;
import ai.libs.jaicore.search.algorithms.mdp.mcts.old.UniformRandomPolicy;
import ai.libs.jaicore.search.algorithms.mdp.mcts.uct.UCBPolicy;
import ai.libs.jaicore.search.exampleproblems.lake.ELakeActions;
import ai.libs.jaicore.search.exampleproblems.lake.JasminLakeMDP;
import ai.libs.jaicore.search.exampleproblems.lake.LakeMDP;
import ai.libs.jaicore.search.exampleproblems.lake.TimedLakeState;
import ai.libs.jaicore.search.probleminputs.MDPUtils;

public class LakeTest {

	@Test
	public void test() throws Exception {
		LakeMDP mdp = new JasminLakeMDP(false);//new FelixLakeMDP(8, false);
		MDPUtils utils = new MDPUtils();
		System.out.println(mdp.getInitState().getStringVisualization());
		//		LakeState succ = mdp.getInitState();
		//		System.out.println(succ.getStringVisualization());
		//		for (int i = 0; i < 10; i++) {
		//			succ = mdp.drawSuccessorState(succ, ELakeActions.DOWN);
		//			System.out.println(succ.getStringVisualization());
		//		}

		UCBPolicy<TimedLakeState, ELakeActions> ucb = new UCBPolicy<>(true);
		MCTSPolicySearch<TimedLakeState, ELakeActions> mcts = new MCTSPolicySearch<>(mdp, ucb, new UniformRandomPolicy<>(), 10000, .99, 0.001);
		mcts.setLoggerName("testedalgorithm");
		IPolicy<TimedLakeState, ELakeActions> policy = mcts.call();

		/* generate "greedy" (non-explorative) policy from the UCB */
		ucb.setExplorationConstant(0);
		for (TimedLakeState s : utils.getStates(mdp)) {
			System.out.println(s);
			for (ELakeActions a : mdp.getApplicableActions(s)) {
				System.out.println("\t" + a + ": " + ucb.getEmpiricalMean(s, a));
			}
			if (!mdp.getApplicableActions(s).isEmpty()) {
				System.out.println("\tCHOICE: " + ucb.getAction(s, mdp.getApplicableActions(s)));
			}
		}

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
		int successes = 0;
		int trials = 100000;
		for (int i = 0; i < trials; i++) {
			System.out.println("RUN " + i);
			IEvaluatedPath<TimedLakeState, ELakeActions, Double> run = utils.getRun(mdp, .5, ucb, new Random(i), p -> false);

			if (mdp.isGoalState(run.getHead())) {
				successes ++;
			}
			else {
				boolean visitedCriticalState = false;
				for (TimedLakeState n : run.getPathToParentOfHead().getNodes()) {
					ELakeActions a = run.getOutArc(n);
					if (n.toString().equals("2/2")) {
						visitedCriticalState = true;
						break;
					}
					//					System.out.println(n.getStringVisualization());
					//					System.out.println("EXPL: " + ucb.getExplorationTerm(n, a));
					//					System.out.println(a);
				}
				if (!visitedCriticalState) {
					for (TimedLakeState n : run.getPathToParentOfHead().getNodes()) {
						ELakeActions a = run.getOutArc(n);
						System.out.println(n.getStringVisualization());
						System.out.println("EXPL: " + ucb.getExplorationTerm(n, a));
						System.out.println(a);
					}
					System.exit(1);
				}
			}
			//			System.out.println(run.getHead().getStringVisualization());
		}
		System.out.println(successes * 1.0 / trials);
	}
}
