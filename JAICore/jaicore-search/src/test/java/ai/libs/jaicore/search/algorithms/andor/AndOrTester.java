package ai.libs.jaicore.search.algorithms.andor;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ai.libs.jaicore.basic.IObjectEvaluator;
import ai.libs.jaicore.graph.Graph;
import ai.libs.jaicore.search.algorithms.andor.AndORBottomUpFilter;
import ai.libs.jaicore.search.algorithms.andor.SyntheticAndGrid.NodeLabel;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.BestFirst;
import ai.libs.jaicore.search.core.interfaces.GraphGenerator;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;

public class AndOrTester {

	@Test
	public void testOptimumIdentification() throws Exception {
		int k = 10;
		int b = 10;
		int d = 4;
		int limit = 1;
		GraphGenerator<NodeLabel, String> gg = new SyntheticAndGrid(k, b, d);
		IObjectEvaluator<Graph<NodeLabel>, Double> evaluator = g -> {
			double sum = 0;
			for (NodeLabel leaf : g.getSinks()) {
				sum += leaf.task;
			}
			return sum;
		};
		AndORBottomUpFilter<NodeLabel, String, Double> algo = new AndORBottomUpFilter<>(gg, evaluator, limit);
		GraphSearchWithSubpathEvaluationsInput<NodeLabel, String, Double> prob = new GraphSearchWithSubpathEvaluationsInput<>(gg, n -> 0.0);
		BestFirst<GraphSearchWithSubpathEvaluationsInput<NodeLabel,String,Double>, NodeLabel, String, Double> bf = new BestFirst<>(prob);
		//		VisualizationWindow<?,?> window = new VisualizationWindow<>(algo);
		long start = System.currentTimeMillis();
		Graph<NodeLabel> solution = algo.call();
		assertEquals(0, evaluator.evaluate(solution).doubleValue(), 0.0);

		System.out.println("Found optimal out of " + (Math.pow(k, Math.pow(b, d))) + " = k^(" + Math.pow(b, d) + ") solutions within " + (System.currentTimeMillis() - start) + "ms.");
		//		while (true);
	}

}
