//package jaicore.planning.gui;
//
//import java.util.Arrays;
//import java.util.Collection;
//import java.util.List;
//
//import org.junit.Test;
//
//import jaicore.graphvisualizer.gui.FXController;
//import jaicore.graphvisualizer.gui.FXGui;
//import jaicore.graphvisualizer.gui.Recorder;
//import jaicore.planning.algorithms.forwarddecomposition.ForwardDecompositionHTNPlanner;
//import jaicore.planning.graphgenerators.task.tfd.TFDNode;
//import jaicore.planning.graphgenerators.task.tfd.TFDTooltipGenerator;
//import jaicore.planning.model.task.ceocstn.CEOCSTNPlanningProblem;
//import jaicore.planning.model.task.ceocstn.StandardProblemFactory;
//import jaicore.search.algorithms.standard.bestfirst.BestFirst;
//import jaicore.search.core.interfaces.GraphGenerator;
//import jaicore.search.gui.dataSupplier.BestFSupplier;
//import jaicore.search.gui.dataSupplier.TooltipSupplier;
//import jaicore.search.model.travesaltree.Node;
//import jaicore.search.testproblems.bestfirst.abstractVersioning.TestGraphGenerator;
//import jaicore.search.testproblems.bestfirst.abstractVersioning.TestNode;
//import jaicore.search.testproblems.nqueens.NQueenGenerator;
//import jaicore.search.testproblems.nqueens.QueenNode;
//import javafx.application.Application;
//import javafx.stage.Stage;
//
//public class FXGuiTester extends Application {
//
//	FXGui gui;
//
//	@Test
//	public void test() {
//		launch();
//	}
//
//	@Override
//	public void start(Stage stage) throws Exception {
//		gui = new FXGui();
//		// bestFirstTest();
//
//		// tooltipTest();
//		//
//		// dataSupplierTest();
//		//
//		bestFTest();
//
//	}
//
//	private void bestFirstTest() throws InterruptedException {
//		GraphGenerator generator = new TestGraphGenerator();
//		BestFirst<TestNode, String, Double> bf = new BestFirst<>(generator, n -> (double) Math.round(Math.random() * 100));
//		// open(bf,"BestFirst");
//
//		Recorder rec = new Recorder(bf);
//
//		gui.open(rec, "Recorder");
//
//		// TooltipSupplier supplier = new TooltipSupplier();
//		// supplier.setGenerator(n->{
//		// Node node =(Node) n;
//		// return String.valueOf(((Node) n).getInternalLabel());
//		// });
//
//		// rec.addDataSupplier(supplier);
//
//		// FXController controller = gui.getControllers().get(gui.getControllers().size()-1);
//		// if(controller != null)
//		// controller.registerSupplier(sup);
//
//		// bf.registerListener(supplier);
//		bf.nextSolution();
//
//	}
//
//	private void tooltipTest() {
//
//		Collection<String> init = Arrays.asList(new String[] { "A", "B", "C", "D" });
//		CEOCSTNPlanningProblem problem = StandardProblemFactory.getNestedDichotomyCreationProblem("root", init, true, 0, 0);
//		ForwardDecompositionHTNPlanner planner = new ForwardDecompositionHTNPlanner(problem, 1);
//		ForwardDecompositionHTNPlanner.SolutionIterator plannerRun = planner.iterator();
//		// new VisualizationWindow<Node<TFDNode,Double>>(plannerRun.getSearch()).setTooltipGenerator(new TFDTooltipGenerator<>());
//
//		Recorder<Node<TFDNode, Double>> recorder = new Recorder<>(plannerRun.getSearch());
//		// recorder.setTooltipGenerator(new TFDTooltipGenerator<>());
//		TooltipSupplier dataSupplier = new TooltipSupplier();
//		dataSupplier.setGenerator(new TFDTooltipGenerator());
//
//		plannerRun.getSearch().registerListener(dataSupplier);
//		/* solve problem */
//		System.out.println("Starting search. Waiting for solutions:");
//		while (plannerRun.hasNext()) {
//			List<TFDNode> solution = (List<TFDNode>) plannerRun.next();
//			System.out.println("\t" + solution);
//		}
//		System.out.println("Algorithm has finished.");
//
//		// recorder.addNodeDataSupplier(dataSupplier);
//
//		gui.open(recorder, "TooltipTest");
//		FXController controller = gui.getControllers().get(gui.getControllers().size() - 1);
//		if (controller != null)
//			controller.registerSupplier(dataSupplier);
//	}
//
//	private void dataSupplierTest() throws InterruptedException {
//
//		GraphGenerator<TestNode,String> generator = new TestGraphGenerator();
//		BestFirst<TestNode,String,Double> bf = new BestFirst<>(generator, n -> (double) Math.round(Math.random() * 100));
//		// open(bf,"BestFirst");
//
//		Recorder rec = new Recorder(bf);
//
//		gui.open(rec, "Recorder");
//
//		// rec.setTooltipGenerator(n->{
//		// Node node = (Node) n;
//		// return String.valueOf(node.getInternalLabel());
//		// });
//
//		TooltipSupplier dataSupplier = new TooltipSupplier();
//
//		dataSupplier.setGenerator((n -> {
//			Node node = (Node) n;
//			Comparable c = node.getInternalLabel();
//			String s = String.valueOf(c);
//			return String.valueOf(s);
//		}));
//
//		rec.addDataSupplier(dataSupplier);
//
//		bf.nextSolution();
//
//		gui.open();
//
//	}
//
//	private void bestFTest() throws InterruptedException {
//		NQueenGenerator gen = new NQueenGenerator(8);
//		BestFirst<QueenNode, String, Double> search = new BestFirst<>(gen, n -> (double) n.getPoint().getNumberOfNotAttackedCells());
//
//		Recorder rec = new Recorder(search);
//		gui.open(rec, "Queens");
//
//		BestFSupplier dataSupplier = new BestFSupplier();
//
//		rec.registerListener(dataSupplier);
//
//		// rec.addGraphDataSupplier(dataSupplier);
//
//		FXController controller = gui.getControllers().get(gui.getControllers().size() - 1);
//		if (controller != null)
//			controller.registerSupplier(dataSupplier);
//
//		search.nextSolution();
//
//	}
//
//}
