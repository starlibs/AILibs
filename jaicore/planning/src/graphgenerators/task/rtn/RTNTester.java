package jaicore.planning.graphgenerators.task.rtn;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import jaicore.graph.LabeledGraph;
import jaicore.graphvisualizer.SimpleGraphVisualizationWindow;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.planning.model.task.rtn.RTNPlanningProblem;
import jaicore.planning.model.task.rtn.StandardProblemFactory;
import jaicore.search.algorithms.standard.generalbestfirst.GeneralBestFirst;

public class RTNTester {

	@Test
	public void testNestedDichotomyGeneration() throws Exception {
		solveProblemUsingAStar(StandardProblemFactory.getNestedDichotomyCreationProblem("parent", Arrays.asList(new String[]{"A", "B", "C", "D"})));
	}
	
	private void solveProblemUsingAStar(RTNPlanningProblem problem) {
		
		/* create AStar algorithm to solve the problem */
		System.out.print("Generate problem ...");
		RTNGraphGenerator generator = new RTNGraphGenerator(problem);
		System.out.println(" done");
		System.out.print("Starting Search Process");
		GeneralBestFirst<RTNNode,RTNEdge> gbf = new GeneralBestFirst<>(generator, n -> new ArrayList<>(n.keySet()), n -> 0, n -> 0);
		
		new SimpleGraphVisualizationWindow<>(gbf);
	
		int i = 0;
		int j = 0;
		LabeledGraph<RTNNode,RTNEdge> solution = null;
		Collection<LabeledGraph<RTNNode,RTNEdge>> solutions = new HashSet<>();
		do {
			solution = gbf.getSolution();
			i = i +1;
			if (solution != null) {
				solutions.add(solution);
			}
		}
		while (solution != null);
		System.out.println("Found " + solutions.size() + " solutions.");
		Iterator<LabeledGraph<RTNNode,RTNEdge>> it = solutions.iterator();
		for (int k = 0; k < solutions.size(); k++) {
			System.out.println("Solution " + k + ": ");
			LabeledGraph<RTNNode,RTNEdge> g = it.next();
			List<TFDNode> list = RTNUtil.serializeGraph(g);
			for (TFDNode n : list) {
				if (!n.getRemainingTasks().isEmpty())
					System.out.println(n.getRemainingTasks().get(0));
				else
					System.out.println("FINISH");
			}
//			new SimpleGraphVisualizationWindow<>(g);
		}
		while (j >= 0) {
			i ++;
		}
		System.out.println(" done");
		assertNotNull(solution);
		
//		List<String> solutionAsStringList = solution.stream().filter(n -> n.getAppliedAction() != null).map(n -> n.getAppliedAction().getEncoding()).collect(Collectors.toList());
//		System.out.println("Found solution of length " + solutionAsStringList.size() + " after " + time + "s.");
//		System.out.println("Start solution\n---------------------");
//		for (String s : solutionAsStringList) {
//			System.out.println(s);
//		}
//		System.out.println("End solution. \n---------------------");
//		System.out.println(solution.get(solution.size() - 1).getState());
	}
}
