package ai.libs.jaicore.search.testproblems.enhancedttsp;

import java.util.function.Function;

import org.api4.java.datastructure.graph.IPath;

import ai.libs.jaicore.basic.algorithm.reduction.AlgorithmicProblemReduction;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;
import ai.libs.jaicore.testproblems.enhancedttsp.EnhancedTTSP;
import ai.libs.jaicore.testproblems.enhancedttsp.EnhancedTTSPState;
import it.unimi.dsi.fastutil.shorts.ShortList;

public class EnhancedTTSPToSimpleGraphSearchReducer
implements AlgorithmicProblemReduction<EnhancedTTSP, ShortList, GraphSearchWithSubpathEvaluationsInput<EnhancedTTSPState, String, Double>, IPath<EnhancedTTSPState, String>> {

	private final Function<Number, Double> linkFunction;

	public EnhancedTTSPToSimpleGraphSearchReducer() {
		this(x -> (double)x);
	}

	public EnhancedTTSPToSimpleGraphSearchReducer(final Function<Number, Double> linkFunction) {
		this.linkFunction = linkFunction;
	}

	@Override
	public GraphSearchWithSubpathEvaluationsInput<EnhancedTTSPState, String, Double> encodeProblem(final EnhancedTTSP problem) {
		return new GraphSearchWithSubpathEvaluationsInput<>(new EnhancedTTSPSimpleGraphGenerator(problem), new EnhancedTTSPSimpleSolutionPredicate(problem), node -> this.linkFunction.apply(problem.getSolutionEvaluator().evaluate(node.getHead().getCurTour())));
	}

	@Override
	public ShortList decodeSolution(final IPath<EnhancedTTSPState, String> solution) {
		ShortList tour = solution.getHead().getCurTour();
		return tour.subList(0, tour.size() - 1); // remove trailing 0
	}
}
