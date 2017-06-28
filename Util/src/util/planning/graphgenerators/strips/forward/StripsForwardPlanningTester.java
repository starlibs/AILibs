package util.planning.graphgenerators.strips.forward;

import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import util.planning.model.strips.StandardProblemFactory;
import util.planning.model.strips.StripsPlanningProblem;
import util.search.astar.AStar;

public class StripsForwardPlanningTester {

	@Test
	public void testBlocksWorld() throws Exception {
		solveProblemUsingAStar(StandardProblemFactory.getBlocksWorldProblem());
	}
	
	@Test
	public void testDockworker() throws Exception {
		solveProblemUsingAStar(StandardProblemFactory.getDockworkerProblem());
	}
	
	private void solveProblemUsingAStar(StripsPlanningProblem problem) {
		
		/* create AStar algorithm to solve the problem */
		StripsForwardPlanningGraphGenerator generator = new StripsForwardPlanningGraphGenerator(problem);
		AStar<StripsForwardPlanningNode,String> astar = new AStar<>(generator, (n1,n2) -> 1, n -> 0);
		List<StripsForwardPlanningNode> solution = astar.nextSolution();
		assertNotNull(solution);
		System.out.println(solution.stream().map(n -> n.getActionToReachState() != null ? n.getActionToReachState().getEncoding() : "").collect(Collectors.toList()));
	}
}
