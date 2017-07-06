package jaicore.planning.graphgenerators.task.ceoctfd;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import jaicore.basic.MathExt;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.planning.model.task.ceocstn.CEOCSTNPlanningProblem;
import jaicore.planning.model.task.ceocstn.StandardProblemFactory;
import jaicore.search.algorithms.standard.astar.AStar;

public class CEOCTFDTester {

	@Test
	public void testNestedDichotomy() throws Exception {
		solveProblemUsingAStar(StandardProblemFactory.getNestedDichotomyCreationProblem("root", Arrays.asList(new String[]{"A", "B", "C", "D"})));
	}
	
	private void solveProblemUsingAStar(CEOCSTNPlanningProblem problem) {
		
		/* create AStar algorithm to solve the problem */
		System.out.print("Generate problem ...");
		CEOCTFDGraphGenerator generator = new CEOCTFDGraphGenerator(problem);
		System.out.println(" done");
		System.out.print("Starting Search Process");
		long start = System.currentTimeMillis();
		AStar<TFDNode,String> astar = new AStar<>(generator, (n1,n2) -> -1 * (Math.random() * 1000), n -> 0);
		
//		new SimpleGraphVisualizationWindow<>(astar.getEventBus());
	
		List<TFDNode> solution = null;
		Collection<List<TFDNode>> solutions = new HashSet<>();
		do {
			solution = astar.nextSolution();
			solutions.add(solution);
		}
		while (solution != null);
		
		long end = System.currentTimeMillis();
		float time = (int)Math.round((end - start) / 10.0) / 100f;
		System.out.println(" done");
		System.out.println("Found " + solutions.size() + " solutions. Expected number is " + (int)MathExt.doubleFactorial((short)(2 * 4 - 3)));
		Assert.assertTrue(solutions.size() == (int)MathExt.doubleFactorial((short)(2 * 4 - 3)));
		
		System.out.println();
		List<String> solutionAsStringList = solutions.iterator().next().stream().filter(n -> n.getAppliedAction() != null).map(n -> n.getAppliedAction().getEncoding()).collect(Collectors.toList());
		System.out.println("Found solution of length " + solutionAsStringList.size() + " after " + time + "s.");
		System.out.println("Start solution\n---------------------");
		for (String s : solutionAsStringList) {
			System.out.println(s);
		}
		System.out.println("End solution. \n---------------------");
	}
}
