package jaicore.search.testproblems.enhancedttsp;

import it.unimi.dsi.fastutil.shorts.ShortList;
import jaicore.basic.algorithm.reduction.AlgorithmicProblemReduction;
import jaicore.search.model.other.SearchGraphPath;
import jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;
import jaicore.testproblems.enhancedttsp.EnhancedTTSP;
import jaicore.testproblems.enhancedttsp.EnhancedTTSPNode;

public class EnhancedTTSPToGraphSearchReducer
implements AlgorithmicProblemReduction<EnhancedTTSP, ShortList, GraphSearchWithSubpathEvaluationsInput<EnhancedTTSPNode, String, Double>, SearchGraphPath<EnhancedTTSPNode, String>> {

	@Override
	public GraphSearchWithSubpathEvaluationsInput<EnhancedTTSPNode, String, Double> encodeProblem(final EnhancedTTSP problem) {
		return new GraphSearchWithSubpathEvaluationsInput<>(new EnhancedTTSPGraphGenerator(problem), node -> problem.getSolutionEvaluator().evaluate(node.getPoint().getCurTour()));
	}

	@Override
	public ShortList decodeSolution(final SearchGraphPath<EnhancedTTSPNode, String> solution) {
		ShortList tour = solution.getNodes().get(solution.getNodes().size() - 1).getCurTour();
		return tour.subList(0, tour.size() - 1); // remove trailing 0
	}
}
