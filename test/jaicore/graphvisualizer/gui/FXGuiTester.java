package jaicore.graphvisualizer.gui;

import jaicore.graphvisualizer.BestFGraphDataSupplier;
import jaicore.planning.algorithms.forwarddecomposition.ForwardDecompositionHTNPlanner;
import jaicore.planning.graphgenerators.task.tfd.TFDNode;
import jaicore.planning.graphgenerators.task.tfd.TFDTooltipGenerator;
import jaicore.planning.model.task.ceocstn.CEOCSTNPlanningProblem;
import jaicore.planning.model.task.ceocstn.StandardProblemFactory;
import jaicore.search.algorithms.standard.bestfirst.BestFirst;
import jaicore.search.algorithms.standard.core.ORGraphSearch;
import jaicore.search.graphgenerators.bestfirst.abstractVersioning.TestGraphGenerator;
import jaicore.search.graphgenerators.bestfirst.abstractVersioning.TestNode;
import jaicore.search.graphgenerators.nqueens.NQueenGenerator;
import jaicore.search.graphgenerators.nqueens.QueenNode;
import jaicore.search.structure.core.GraphGenerator;
import jaicore.search.structure.core.Node;
import javafx.stage.Stage;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class FXGuiTester extends FXGui{


	@Test
	public void test() {
		launch();
	}

	@Override
	public void start(Stage stage) throws Exception {
//		bestFirstTest();

//		tooltipTest();

//		dataSupplierTest();

		bestFTest();
	}

	private void bestFirstTest(){
		GraphGenerator generator = new TestGraphGenerator();
		BestFirst<TestNode, String> bf = new BestFirst<>(generator, n->(double)Math.round(Math.random()*100));
//		open(bf,"BestFirst");

		Recorder rec = new Recorder(bf);

		open(rec, "Recorder");

//		rec.setTooltipGenerator(n->{
//			Node node = (Node) n;
//			return String.valueOf(node.getInternalLabel());
//		});
		bf.nextSolution();

	}

	private void tooltipTest(){

		Collection<String> init = Arrays.asList(new String[] {"A", "B", "C", "D"});
		CEOCSTNPlanningProblem problem = StandardProblemFactory.getNestedDichotomyCreationProblem("root", init, true, 0, 0);
		ForwardDecompositionHTNPlanner planner = new ForwardDecompositionHTNPlanner(problem, 1);
		ForwardDecompositionHTNPlanner.SolutionIterator plannerRun = planner.iterator();
//		new SimpleGraphVisualizationWindow<Node<TFDNode,Double>>(plannerRun.getSearch()).getPanel().setTooltipGenerator(new TFDTooltipGenerator<>());


		Recorder<Node<TFDNode,Double>> recorder = new Recorder<>(plannerRun.getSearch());
//		recorder.setTooltipGenerator(new TFDTooltipGenerator<>());

		/* solve problem */
		System.out.println("Starting search. Waiting for solutions:");
		while (plannerRun.hasNext()) {
			List<TFDNode> solution = (List<TFDNode>) plannerRun.next();
			System.out.println("\t" + solution);
		}
		System.out.println("Algorithm has finished.");


		TooltipGraphDataSupplier dataSupplier = new TooltipGraphDataSupplier();
		dataSupplier.setTooltipGenerator(new TFDTooltipGenerator());

		recorder.addNodeDataSupplier(dataSupplier);


		open(recorder, "TooltipTest");
	}

	private void dataSupplierTest(){

		GraphGenerator generator = new TestGraphGenerator();
		BestFirst bf = new BestFirst<>(generator, n->(double)Math.round(Math.random()*100));
//		open(bf,"BestFirst");

		Recorder rec = new Recorder(bf);

		open(rec, "Recorder");

//		rec.setTooltipGenerator(n->{
//			Node node = (Node) n;
//			return String.valueOf(node.getInternalLabel());
//		});

		TooltipGraphDataSupplier dataSupplier = new TooltipGraphDataSupplier();

		dataSupplier.setTooltipGenerator((n -> {
			Node node = (Node) n;
			Comparable c = node.getInternalLabel();
			String s = String.valueOf(c);
			return String.valueOf(s);
		}));

		rec.addNodeDataSupplier(dataSupplier);

		bf.nextSolution();

		open();

	}


	private void bestFTest(){
		NQueenGenerator gen = new NQueenGenerator(8);
		ORGraphSearch<QueenNode, String, Double> search = new ORGraphSearch<>(gen, n->(double)n.getPoint().getNumberOfNotAttackedCells());

		Recorder rec = new Recorder(search);
		open(rec,"Queens");

		BestFGraphDataSupplier dataSupplier = new BestFGraphDataSupplier();

		rec.addGraphDataSupplier(dataSupplier);

		search.nextSolution();

	}

}
