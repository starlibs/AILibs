package util.planning.graphgenerators.task.tfd;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import util.planning.model.task.stn.STNPlanningProblem;
import util.planning.model.task.stn.StandardProblemFactory;
import util.search.astar.AStar;

public class TFDTester {

	@Test
	public void testDockworker() throws Exception {
		solveProblemUsingAStar(StandardProblemFactory.getDockworkerProblem());
	}
	
	private void solveProblemUsingAStar(STNPlanningProblem problem) {
		
		/* create AStar algorithm to solve the problem */
		TFDGraphGenerator generator = new TFDGraphGenerator(problem);
		AStar<TFDNode,String> astar = new AStar<>(generator, (n1,n2) -> 1, n -> 0);
		List<TFDNode> solution = astar.nextSolution();
		Assert.assertNotNull(solution);
		
		
		System.out.println(solution.stream().map(n -> (n.getAppliedAction() != null) ? n.getAppliedAction().getEncoding() : null).collect(Collectors.toList()));
		System.out.println(solution.get(solution.size() - 1).getState());
	}
}
