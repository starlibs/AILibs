package jaicore.search.algorithms.andor;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import jaicore.basic.IObjectEvaluator;
import jaicore.graph.Graph;
import jaicore.search.algorithms.andor.SyntheticAndGrid.NodeLabel;
import jaicore.search.algorithms.standard.bestfirst.BestFirst;
import jaicore.search.core.interfaces.GraphGenerator;
import jaicore.search.model.probleminputs.GeneralEvaluatedTraversalTree;

public class AndOrTester {

	@Test
	public void test() throws Exception {
		int k = 3;
		int b = 2;
		int d = 3;
		int limit = 10;
		GraphGenerator<NodeLabel, String> gg = new SyntheticAndGrid(k, b, d);
		IObjectEvaluator<Graph<NodeLabel>, Double> evaluator = g -> {
			double sum = 0;
			for (NodeLabel leaf : g.getSinks()) {
				sum += leaf.task;
			}
			return sum;
		};
		AndORBottomUpFilter<NodeLabel, String, Double> algo = new AndORBottomUpFilter<>(gg, evaluator, limit);
		GeneralEvaluatedTraversalTree<NodeLabel, String, Double> prob = new GeneralEvaluatedTraversalTree<>(gg, n -> 0.0);
		BestFirst<GeneralEvaluatedTraversalTree<NodeLabel,String,Double>, NodeLabel, String, Double> bf = new BestFirst<>(prob);
//		VisualizationWindow<?,?> window = new VisualizationWindow<>(algo);
		long start = System.currentTimeMillis();
		Graph<NodeLabel> solution = algo.call();
		assertEquals(0, evaluator.evaluate(solution).doubleValue(), 0.0);
		
		System.out.println("Found optimal out of " + (Math.pow(k, Math.pow(b, d))) + " = k^(" + Math.pow(b, d) + ") solutions within " + (System.currentTimeMillis() - start) + "ms.");
//		while (true);
	}

}
