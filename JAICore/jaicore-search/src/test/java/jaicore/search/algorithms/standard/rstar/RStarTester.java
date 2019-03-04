package jaicore.search.algorithms.standard.rstar;

import jaicore.basic.IMetric;
import jaicore.basic.algorithm.AlgorithmProblemTransformer;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import jaicore.search.core.interfaces.GraphGenerator;
import jaicore.search.core.interfaces.IGraphSearchFactory;
import jaicore.search.model.other.EvaluatedSearchGraphPath;
import jaicore.search.probleminputs.GraphSearchWithNumberBasedAdditivePathEvaluation;
import jaicore.search.probleminputs.GraphSearchWithNumberBasedAdditivePathEvaluation.EdgeCostComputer;
import jaicore.search.probleminputs.GraphSearchWithNumberBasedAdditivePathEvaluationAndSubPathHeuristic;
import jaicore.search.probleminputs.GraphSearchWithNumberBasedAdditivePathEvaluationAndSubPathHeuristic.DistantSuccessorGenerator;
import jaicore.search.probleminputs.GraphSearchWithNumberBasedAdditivePathEvaluationAndSubPathHeuristic.PathCostEstimator;
import jaicore.search.testproblems.enhancedttsp.EnhancedTTSP;
import jaicore.search.testproblems.enhancedttsp.EnhancedTTSPAdditiveGraphReducer;
import jaicore.search.testproblems.enhancedttsp.EnhancedTTSPNode;
import jaicore.search.testproblems.enhancedttsp.EnhancedTTSPTester;

public class RStarTester extends EnhancedTTSPTester<GraphSearchWithNumberBasedAdditivePathEvaluationAndSubPathHeuristic<EnhancedTTSPNode,String>,EvaluatedSearchGraphPath<EnhancedTTSPNode,String,Double>> {

	@Override
	public IGraphSearchFactory<GraphSearchWithNumberBasedAdditivePathEvaluationAndSubPathHeuristic<EnhancedTTSPNode,String>,EvaluatedSearchGraphPath<EnhancedTTSPNode,String,Double>,EnhancedTTSPNode, String> getFactory() {
		RStarFactory<EnhancedTTSPNode, String> factory = new RStarFactory<>();
		factory.setDelta(2);
		return factory;
	}

	@Override
	public AlgorithmProblemTransformer<EnhancedTTSP, GraphSearchWithNumberBasedAdditivePathEvaluationAndSubPathHeuristic<EnhancedTTSPNode,String>> getProblemReducer() {
		EnhancedTTSPAdditiveGraphReducer reducerForAStar = new EnhancedTTSPAdditiveGraphReducer();
		return new AlgorithmProblemTransformer<EnhancedTTSP, GraphSearchWithNumberBasedAdditivePathEvaluationAndSubPathHeuristic<EnhancedTTSPNode,String>>(){

			@Override
			public GraphSearchWithNumberBasedAdditivePathEvaluationAndSubPathHeuristic<EnhancedTTSPNode, String> transform(final EnhancedTTSP problem) {
				GraphSearchWithNumberBasedAdditivePathEvaluation<EnhancedTTSPNode, String> aStarInput = reducerForAStar.transform(problem);
				final GraphGenerator<EnhancedTTSPNode, String> graphGenerator = aStarInput.getGraphGenerator();
				final EdgeCostComputer<EnhancedTTSPNode> g = ((GraphSearchWithNumberBasedAdditivePathEvaluation.FComputer<EnhancedTTSPNode>) aStarInput.getNodeEvaluator()).getG();
				final INodeEvaluator<EnhancedTTSPNode, Double> h = ((GraphSearchWithNumberBasedAdditivePathEvaluation.FComputer<EnhancedTTSPNode>) aStarInput.getNodeEvaluator()).getH();
				final PathCostEstimator<EnhancedTTSPNode> hPath = (n1, n2) -> 0.0;
				final IMetric<EnhancedTTSPNode> metricOverStates = (n1, n2) -> {
					int firstIndexOfDeviation = 0;
					int n = Math.min(n1.getCurTour().size(), n2.getCurTour().size());
					int m = Math.max(n1.getCurTour().size(), n2.getCurTour().size());
					if (n == 0) {
						return m == 0 ? 0 : n + 1;
					}
					while (firstIndexOfDeviation < n && n1.getCurTour().getShort(firstIndexOfDeviation) == n2.getCurTour().getShort(firstIndexOfDeviation)) {
						firstIndexOfDeviation ++;
					}
					return 1 - firstIndexOfDeviation / m;
				};
				final DistantSuccessorGenerator<EnhancedTTSPNode, String> distantSuccessorGenerator = new GraphBasedDistantSuccessorGenerator<>(graphGenerator, 0);
				return new GraphSearchWithNumberBasedAdditivePathEvaluationAndSubPathHeuristic<>(graphGenerator, g, h, hPath, metricOverStates, distantSuccessorGenerator);
			}
		};
	}

	@Override
	public void testThatIteratorReturnsEachPossibleSolution() throws Throwable {

		/* we cannot do this for RStar, because RStar is not a complete algorithm */
	}
}
