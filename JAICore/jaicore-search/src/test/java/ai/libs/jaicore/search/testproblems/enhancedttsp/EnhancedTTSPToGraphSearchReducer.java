package ai.libs.jaicore.search.testproblems.enhancedttsp;

import org.api4.java.datastructure.graph.ILabeledPath;

import ai.libs.jaicore.basic.algorithm.reduction.AlgorithmicProblemReduction;
import ai.libs.jaicore.problems.enhancedttsp.EnhancedTTSP;
import ai.libs.jaicore.problems.enhancedttsp.EnhancedTTSPNode;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;
import it.unimi.dsi.fastutil.shorts.ShortList;

public class EnhancedTTSPToGraphSearchReducer
implements AlgorithmicProblemReduction<EnhancedTTSP, ShortList, GraphSearchWithSubpathEvaluationsInput<EnhancedTTSPNode, String, Double>, ILabeledPath<EnhancedTTSPNode, String>> {

	@Override
	public GraphSearchWithSubpathEvaluationsInput<EnhancedTTSPNode, String, Double> encodeProblem(final EnhancedTTSP problem) {
		return new GraphSearchWithSubpathEvaluationsInput<>(new EnhancedTTSPGraphGenerator(problem), new EnhancedTTSPSolutionPredicate(problem), node -> problem.getSolutionEvaluator().evaluate(node.getHead().getCurTour()));
	}

	@Override
	public ShortList decodeSolution(final ILabeledPath<EnhancedTTSPNode, String> solution) {
		System.out.println(solution.getClass());
		System.out.println(solution.getHead().getClass());
		ShortList tour = solution.getHead().getCurTour();
		return tour.subList(0, tour.size() - 1); // remove trailing 0
	}
}
