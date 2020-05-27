package ai.libs.jaicore.search.testproblems.racetrack;

import java.io.File;
import java.util.Random;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IEvaluatedPath;
import org.junit.Test;

import ai.libs.jaicore.search.algorithms.mdp.mcts.IPolicy;
import ai.libs.jaicore.search.algorithms.mdp.mcts.MCTS;
import ai.libs.jaicore.search.algorithms.mdp.mcts.MCTSBuilder;
import ai.libs.jaicore.search.algorithms.mdp.mcts.uct.UCBPolicy;
import ai.libs.jaicore.search.exampleproblems.racetrack.RacetrackAction;
import ai.libs.jaicore.search.exampleproblems.racetrack.RacetrackMDP;
import ai.libs.jaicore.search.exampleproblems.racetrack.RacetrackReader;
import ai.libs.jaicore.search.exampleproblems.racetrack.RacetrackState;
import ai.libs.jaicore.search.probleminputs.MDPUtils;

public class RacetrackTest {

	@Test
	public void test() throws Exception {

		/* create completely connected graph */
		RacetrackMDP mdp = new RacetrackReader().read(new File("R-track.txt"), .9, new Random(0), true);

		MDPUtils utils = new MDPUtils();
		//		LakeState succ = mdp.getInitState();
		//		System.out.println(succ.getStringVisualization());
		//		for (int i = 0; i < 10; i++) {
		//			succ = mdp.drawSuccessorState(succ, ELakeActions.DOWN);
		//			System.out.println(succ.getStringVisualization());
		//		}

		UCBPolicy<RacetrackState, RacetrackAction> ucb = new UCBPolicy<>(true);
		MCTS<RacetrackState, RacetrackAction> mcts = new MCTSBuilder<RacetrackState, RacetrackAction>().withMDP(mdp).withGamma(.99).withMaxIterations(1000000).build();
		mcts.setLoggerName("testedalgorithm");
		IPolicy<RacetrackState, RacetrackAction> policy = mcts.call();

		/* generate "greedy" (non-explorative) policy from the UCB */
		ucb.setExplorationConstant(0);

		/* testing the policy */
		for (int r = 0; r < 100; r++) {
			IEvaluatedPath<RacetrackState, RacetrackAction, Double> run = utils.getRun(mdp, 1.0, policy, new Random(), p -> false);
			//			System.out.println(run.getNumberOfNodes());
			System.out.println(run.getHead());
		}
	}
}
