package ai.libs.jaicore.search.testproblems.canadiantravelerproblem;

import java.util.List;
import java.util.Random;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IEvaluatedPath;
import org.junit.Test;

import ai.libs.jaicore.graph.LabeledGraph;
import ai.libs.jaicore.problems.enhancedttsp.EnhancedTTSPGenerator;
import ai.libs.jaicore.problems.enhancedttsp.Location;
import ai.libs.jaicore.problems.enhancedttsp.locationgenerator.RandomLocationGenerator;
import ai.libs.jaicore.search.algorithms.mdp.mcts.IPolicy;
import ai.libs.jaicore.search.algorithms.mdp.mcts.MCTS;
import ai.libs.jaicore.search.algorithms.mdp.mcts.MCTSBuilder;
import ai.libs.jaicore.search.algorithms.mdp.mcts.uct.UCBPolicy;
import ai.libs.jaicore.search.exampleproblems.canadiantravelerproblem.CTPMDP;
import ai.libs.jaicore.search.exampleproblems.canadiantravelerproblem.CTPState;
import ai.libs.jaicore.search.probleminputs.MDPUtils;

public class CTPTest {

	@Test
	public void test() throws Exception {

		/* create completely connected graph */
		List<Location> locations = new EnhancedTTSPGenerator(new RandomLocationGenerator(new Random(0))).generate(5, 100, 5).getLocations();
		int numLocations = locations.size();
		LabeledGraph<Short, Double> distances = new LabeledGraph<>();
		for (int x = 0; x < numLocations; x ++) {
			for (int y = 0; y < x; y ++) {
				double dist = Math.sqrt(Math.pow(locations.get(x).getX() - locations.get(y).getX(), 2) + Math.pow(locations.get(x).getY() - locations.get(y).getY(), 2));
				distances.addEdge((short)y, (short)x, dist);
			}
		}

		CTPMDP mdp = new CTPMDP(distances);//new FelixLakeMDP(8, false);
		MDPUtils utils = new MDPUtils();
		//		LakeState succ = mdp.getInitState();
		//		System.out.println(succ.getStringVisualization());
		//		for (int i = 0; i < 10; i++) {
		//			succ = mdp.drawSuccessorState(succ, ELakeActions.DOWN);
		//			System.out.println(succ.getStringVisualization());
		//		}

		UCBPolicy<CTPState, Short> ucb = new UCBPolicy<>(true);
		MCTS<CTPState, Short> mcts = new MCTSBuilder<CTPState, Short>().withMDP(mdp).withMaxIterations(10).build();
		mcts.setLoggerName("testedalgorithm");
		IPolicy<CTPState, Short> policy = mcts.call();

		/* generate "greedy" (non-explorative) policy from the UCB */
		ucb.setExplorationConstant(0);

		/* testing the policy */
		for (int r = 0; r < 100; r++) {
			IEvaluatedPath<CTPState, Short, Double> run = utils.getRun(mdp, 1.0, policy, new Random(), p -> false);
			//			System.out.println(run.getNumberOfNodes());
			System.out.println(run.getHead().getCurrentTour());
		}
	}
}
