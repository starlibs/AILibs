package jaicore.search.testproblems.enhancedttsp;

import jaicore.basic.algorithm.AlgorithmProblemTransformer;
import jaicore.graph.LabeledGraph;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import jaicore.search.model.probleminputs.NumberBasedAdditiveTraversalTree;
import jaicore.search.model.probleminputs.NumberBasedAdditiveTraversalTree.EdgeCostComputer;

public class EnhancedTTSPAdditiveGraphReducer implements AlgorithmProblemTransformer<EnhancedTTSP, NumberBasedAdditiveTraversalTree<EnhancedTTSPNode, String>> {

	@Override
	public NumberBasedAdditiveTraversalTree<EnhancedTTSPNode, String> transform(EnhancedTTSP problem) {

		/* define g */
		EdgeCostComputer<EnhancedTTSPNode> g = (from, to) -> to.getPoint().getTime() - from.getPoint().getTime();

		/* define h */
		LabeledGraph<Short, Double> travelGraph = problem.getMinTravelTimesGraph();
		INodeEvaluator<EnhancedTTSPNode, Double> h = node -> {
			double hVal = 0;
			// List<Double> edgesOfUncoveredPlaces = travelGraph.getEdges().stream()
			// .filter(e -> node.getPoint().getUnvisitedLocations().contains(e.getX()) && node.getPoint().getUnvisitedLocations().contains(e.getY())).map(e -> travelGraph.getEdgeLabel(e))
			// .sorted().collect(Collectors.toList());
			// int m = node.getPoint().getUnvisitedLocations().size();
			// if (m > 1) {
			// for (int i = 0; i < m; i++)
			// hVal += edgesOfUncoveredPlaces.get(i);
			// } else if (m == 1)
			// hVal = travelGraph.getEdgeLabel(node.getPoint().getCurLocation(), (short) 0);
			return hVal;
		};

		return new NumberBasedAdditiveTraversalTree<>(problem.getGraphGenerator(), g, h);
	}
}
