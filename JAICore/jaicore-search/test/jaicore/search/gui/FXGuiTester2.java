//package jaicore.search.gui;
//
//import jaicore.graphvisualizer.gui.FXGui;
//import jaicore.search.algorithms.standard.bestfirst.BestFirst;
//import jaicore.search.algorithms.standard.bestfirst.model.GraphGenerator;
//import jaicore.search.algorithms.standard.bestfirst.model.Node;
//import jaicore.search.gui.dataSupplier.TooltipSupplier;
//import jaicore.search.testproblems.bestfirst.abstractVersioning.TestGraphGenerator;
//import jaicore.search.testproblems.bestfirst.abstractVersioning.TestNode;
//
//public class FXGuiTester2 {
//
//	static void startGui() throws InterruptedException {
//
//		FXGui gui = new FXGui();
//
//		GraphGenerator<TestNode,String> generator = new TestGraphGenerator();
//		BestFirst<TestNode,String,Double> bf = new BestFirst<>(generator, n -> (double) Math.round(Math.random() * 100));
//
//		TooltipSupplier dataSupplier = new TooltipSupplier();
//		dataSupplier.setGenerator((n -> {
//			Node node = (Node) n;
//			Comparable c = node.getInternalLabel();
//			String s = String.valueOf(c);
//			return String.valueOf(s);
//		}));
//
//		gui.open(bf, "Test");
//
//		bf.nextSolution();
//
//	}
//
//	public static void main(String[] args) throws InterruptedException {
//		startGui();
//	}
//}
