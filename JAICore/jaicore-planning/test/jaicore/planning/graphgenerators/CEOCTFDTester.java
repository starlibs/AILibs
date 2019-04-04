package jaicore.planning.graphgenerators;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import jaicore.graphvisualizer.gui.VisualizationWindow;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import jaicore.basic.MathExt;
import jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import jaicore.planning.graphgenerators.task.ceoctfd.CEOCTFDGraphGenerator;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.planning.graphgenerators.task.tfd.TFDTooltipGenerator;
import jaicore.planning.model.ceoc.CEOCAction;
import jaicore.planning.model.ceoc.CEOCOperation;
import jaicore.planning.model.task.ceocstn.CEOCSTNPlanningProblem;
import jaicore.planning.model.task.ceocstn.OCMethod;
import jaicore.planning.model.task.ceocstn.StandardProblemFactory;
import jaicore.search.algorithms.standard.astar.AStar;
import jaicore.search.model.probleminputs.NumberBasedAdditiveTraversalTree;
import jaicore.search.model.travesaltree.Node;
import jaicore.search.model.travesaltree.NodeTooltipGenerator;

public class CEOCTFDTester {

	private List<String> classes;

	@Before
	public void setClasses() {
		classes = Arrays.asList(new String[] { "A", "B", "C", "D", "E" });
	}

	@Test
	public void testNestedDichotomy() throws Exception {
		solveProblemUsingAStar(StandardProblemFactory.getNestedDichotomyCreationProblem("root", classes, true, 1, 1));
	}

	private void solveProblemUsingAStar(CEOCSTNPlanningProblem problem) throws InterruptedException, AlgorithmExecutionCanceledException {

		/* create AStar algorithm to solve the problem */
		System.out.print("Generate problem ...");
		CEOCTFDGraphGenerator<CEOCOperation, OCMethod, CEOCAction> generator = new CEOCTFDGraphGenerator<>(problem);
		System.out.println(" done");
		System.out.print("Starting Search Process");
		long start = System.currentTimeMillis();
		AStar<TFDNode, String> astar = new AStar<TFDNode, String>(new NumberBasedAdditiveTraversalTree<TFDNode, String>(generator, (n1, n2) -> -1 * (Math.random() * 1000), n -> 0.0));

		new VisualizationWindow<Node<TFDNode, Double>, String>(astar).setTooltipGenerator(new NodeTooltipGenerator<>(new TFDTooltipGenerator()));

		List<TFDNode> solution = null;
		Collection<List<TFDNode>> solutions = new HashSet<>();
		do {
			solution = astar.nextSolution().getNodes();
			solutions.add(solution);
		} while (solution != null);
		long end = System.currentTimeMillis();
		float time = (int) Math.round((end - start) / 10.0) / 100f;
		System.out.println(" done");
		int expectedNumber = (int) MathExt.doubleFactorial((short) (2 * classes.size() - 3));
		System.out.println("Found " + solutions.size() + " solutions in " + time + "s. Expected number is " + expectedNumber);
		Assert.assertTrue(solutions.size() == expectedNumber);

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
