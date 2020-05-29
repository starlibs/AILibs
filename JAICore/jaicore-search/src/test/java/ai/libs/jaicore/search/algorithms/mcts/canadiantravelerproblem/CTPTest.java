package ai.libs.jaicore.search.algorithms.mcts.canadiantravelerproblem;

import java.util.List;
import java.util.Random;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IEvaluatedPath;

import ai.libs.jaicore.graph.LabeledGraph;
import ai.libs.jaicore.problems.enhancedttsp.EnhancedTTSPGenerator;
import ai.libs.jaicore.problems.enhancedttsp.Location;
import ai.libs.jaicore.problems.enhancedttsp.locationgenerator.RandomLocationGenerator;
import ai.libs.jaicore.search.algorithms.mcts.MCTSLearningSuccessTester;
import ai.libs.jaicore.search.exampleproblems.canadiantravelerproblem.CTPMDP;
import ai.libs.jaicore.search.exampleproblems.canadiantravelerproblem.CTPState;
import ai.libs.jaicore.search.probleminputs.IMDP;
import it.unimi.dsi.fastutil.shorts.ShortList;

public class CTPTest extends MCTSLearningSuccessTester<CTPState, Short> {

	private static final int NUM_LOCATIONS = 5;

	@Override
	public IMDP<CTPState, Short, Double> getMDP() {

		/* create completely connected graph */
		List<Location> locations = new EnhancedTTSPGenerator(new RandomLocationGenerator(new Random(0))).generate(NUM_LOCATIONS, 100, 0).getLocations();
		int numLocations = locations.size();
		LabeledGraph<Short, Double> distances = new LabeledGraph<>();
		for (int x = 0; x < numLocations; x ++) {
			for (int y = 0; y < x; y ++) {
				double dist = Math.sqrt(Math.pow(locations.get(x).getX() - locations.get(y).getX(), 2) + Math.pow(locations.get(x).getY() - locations.get(y).getY(), 2));
				distances.addEdge((short)y, (short)x, dist);
			}
		}

		return new CTPMDP(distances);
	}

	@Override
	public boolean isSuccess(final IEvaluatedPath<CTPState, Short, Double> path) {
		ShortList tour = path.getHead().getCurrentTour();
		for (short p = 0; p < NUM_LOCATIONS; p ++) {
			if (!tour.contains(p)) {
				throw new IllegalArgumentException("The given tour cannot be a solution since it does not even visit all locations. The current solution does not visit location " + p + ": " + tour);
			}
		}
		return path.getNumberOfNodes() < NUM_LOCATIONS * 1.1;
	}

	@Override
	public double getRequiredSuccessRate() {
		return 1.0;
	}
}
