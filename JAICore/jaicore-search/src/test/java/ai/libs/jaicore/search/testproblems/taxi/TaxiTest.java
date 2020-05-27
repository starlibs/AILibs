package ai.libs.jaicore.search.testproblems.taxi;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IEvaluatedPath;
import org.junit.Test;

import ai.libs.jaicore.basic.sets.IntCoordinates;
import ai.libs.jaicore.search.algorithms.mdp.mcts.IPolicy;
import ai.libs.jaicore.search.algorithms.mdp.mcts.MCTS;
import ai.libs.jaicore.search.algorithms.mdp.mcts.MCTSBuilder;
import ai.libs.jaicore.search.algorithms.mdp.mcts.uct.UCBPolicy;
import ai.libs.jaicore.search.exampleproblems.taxi.ETaxiAction;
import ai.libs.jaicore.search.exampleproblems.taxi.TaxiMDP;
import ai.libs.jaicore.search.exampleproblems.taxi.TaxiState;
import ai.libs.jaicore.search.probleminputs.MDPUtils;

public class TaxiTest {

	@Test
	public void test() throws Exception {

		int n = 5;
		boolean[][][] possibleTransitions = new boolean[n][n][4];
		for (int x = 0; x < n; x++) {
			for (int y = 0; y < n; y++) {
				possibleTransitions[x][y][0] = true;
				possibleTransitions[x][y][1] = true;
				possibleTransitions[x][y][2] = true;
				possibleTransitions[x][y][3] = true;
			}
		}
		List<IntCoordinates> pickupStations = new ArrayList<>();
		pickupStations.add(new IntCoordinates(0, 0));
		pickupStations.add(new IntCoordinates(0, n - 1));
		pickupStations.add(new IntCoordinates(n - 1, 0));
		pickupStations.add(new IntCoordinates(n - 1, n - 1));

		TaxiMDP mdp = new TaxiMDP(possibleTransitions, 0.5, pickupStations, new Random(0));
		System.out.println(mdp);
		System.out.println(mdp.getInitState());

		MCTS<TaxiState, ETaxiAction> mcts = new MCTSBuilder<TaxiState, ETaxiAction>().withMDP(mdp).withGamma(.99).withMaxIterations(10000).build();
		mcts.setLoggerName("testedalgorithm");
		IPolicy<TaxiState, ETaxiAction> policy = mcts.call();
		UCBPolicy<TaxiState, ETaxiAction> ucb = (UCBPolicy<TaxiState, ETaxiAction>) policy;

		/* testing the policy */
		MDPUtils utils = new MDPUtils();
		for (TaxiState s : utils.getStates(mdp)) {
			System.out.println(s);
			for (ETaxiAction a : mdp.getApplicableActions(s)) {
				System.out.println("\t" + a + ": " + ucb.getScore(s, a));
			}
		}

		ucb.setExplorationConstant(0); // now only exploit
		for (int r = 0; r < 100; r++) {
			IEvaluatedPath<TaxiState, ETaxiAction, Double> run = utils.getRun(mdp, 1.0, ucb, new Random(), p -> false);
			System.out.println(run.getNumberOfNodes() + ": " + run.getScore());
		}
	}
}
