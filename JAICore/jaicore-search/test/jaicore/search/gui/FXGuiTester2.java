package jaicore.search.gui;

import jaicore.graphvisualizer.gui.FXGui;
import jaicore.search.algorithms.standard.bestfirst.BestFirst;
import jaicore.search.graphgenerators.bestfirst.abstractVersioning.TestGraphGenerator;
import jaicore.search.gui.dataSupplier.TooltipSupplier;
import jaicore.search.structure.core.GraphGenerator;
import jaicore.search.structure.core.Node;

public class FXGuiTester2 {

	static void startGui() throws InterruptedException {

		FXGui gui = new FXGui();

		GraphGenerator generator = new TestGraphGenerator();
		BestFirst bf = new BestFirst<>(generator, n -> (double) Math.round(Math.random() * 100));

		TooltipSupplier dataSupplier = new TooltipSupplier();
		dataSupplier.setGenerator((n -> {
			Node node = (Node) n;
			Comparable c = node.getInternalLabel();
			String s = String.valueOf(c);
			return String.valueOf(s);
		}));

		gui.open(bf, "Test");

		bf.nextSolution();

	}

	public static void main(String[] args) throws InterruptedException {
		startGui();
	}
}
