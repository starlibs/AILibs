package ai.libs.jaicore.search.testproblems.cannibals;

import static org.junit.Assert.assertEquals;

import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.junit.Test;

import ai.libs.jaicore.problems.cannibals.CannibalProblem;
import ai.libs.jaicore.search.algorithms.standard.astar.AStar;
import ai.libs.jaicore.search.exampleproblems.cannibals.CannibalGraphGenerator;
import ai.libs.jaicore.search.exampleproblems.cannibals.CannibalNodeGoalPredicate;
import ai.libs.jaicore.search.model.other.SearchGraphPath;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithNumberBasedAdditivePathEvaluation;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;

public class CannibalTester {

	@Test
	public void testCannibalsShortestPath() throws AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException {
		CannibalProblem p = new CannibalProblem(true, 3, 3, 0, 0);
		GraphSearchWithSubpathEvaluationsInput<CannibalProblem, String, Integer> prob = new GraphSearchWithSubpathEvaluationsInput<>(new CannibalGraphGenerator(p), new CannibalNodeGoalPredicate(), n -> n.getNodes().size());

		AStar<CannibalProblem, String> astar = new AStar<>(new GraphSearchWithNumberBasedAdditivePathEvaluation<>(prob, (n1,n2) -> 1, n -> 1.0 * n.getHead().getCannibalsOnLeft() + n.getHead().getMissionariesOnLeft()));

		SearchGraphPath<CannibalProblem, String> solution = astar.nextSolutionCandidate();
		int expectedMoves = 11;
		assertEquals(expectedMoves, solution.getArcs().size());
	}
}
