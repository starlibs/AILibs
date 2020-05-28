package ai.libs.jaicore.search.algorithms.mcts.taxi;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IEvaluatedPath;

import ai.libs.jaicore.basic.sets.IntCoordinates;
import ai.libs.jaicore.search.algorithms.mcts.MCTSLearningSuccessTester;
import ai.libs.jaicore.search.exampleproblems.taxi.ETaxiAction;
import ai.libs.jaicore.search.exampleproblems.taxi.TaxiMDP;
import ai.libs.jaicore.search.exampleproblems.taxi.TaxiState;
import ai.libs.jaicore.search.probleminputs.IMDP;

public class TaxiTest extends MCTSLearningSuccessTester<TaxiState, ETaxiAction> {

	@Override
	public IMDP<TaxiState, ETaxiAction, Double> getMDP() {

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

		return new TaxiMDP(possibleTransitions, 0.99, pickupStations, new Random(0));
	}

	@Override
	public boolean isSuccess(final IEvaluatedPath<TaxiState, ETaxiAction, Double> path) {
		return path.getNumberOfNodes() <= 10; // optimal tour has 9
	}

	@Override
	public double getRequiredSuccessRate() {
		return .9;
	}
}
