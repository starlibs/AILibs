package jaicore.search.algorithms.andor;

import org.junit.Test;

import jaicore.graphvisualizer.gui.VisualizationWindow;
import jaicore.search.algorithms.andor.SyntheticAndGrid.NodeLabel;
import jaicore.search.core.interfaces.GraphGenerator;

public class AndOrTester {

	@Test
	public void test() throws Exception {
		GraphGenerator<NodeLabel, String> gg = new SyntheticAndGrid(100, 100, 100);
		AndORBottomUpFilter<NodeLabel, String, Double> algo = new AndORBottomUpFilter<>(gg);
//		GeneralEvaluatedTraversalTree<NodeLabel, String, Double> prob = new GeneralEvaluatedTraversalTree<>(gg, n -> 0.0);
//		BestFirst<GeneralEvaluatedTraversalTree<NodeLabel,String,Double>, NodeLabel, String, Double> bf = new BestFirst<>(prob);
		VisualizationWindow<?,?> window = new VisualizationWindow<>(algo);
		algo.call();
		while (true);
	}

}
