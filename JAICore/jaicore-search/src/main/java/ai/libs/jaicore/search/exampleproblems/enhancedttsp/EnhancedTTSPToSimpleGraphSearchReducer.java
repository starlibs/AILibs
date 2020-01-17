package ai.libs.jaicore.search.exampleproblems.enhancedttsp;

import java.util.function.ToDoubleFunction;

import org.api4.java.datastructure.graph.ILabeledPath;

import ai.libs.jaicore.basic.algorithm.reduction.AlgorithmicProblemReduction;
import ai.libs.jaicore.problems.enhancedttsp.EnhancedTTSP;
import ai.libs.jaicore.problems.enhancedttsp.EnhancedTTSPState;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;
import it.unimi.dsi.fastutil.shorts.ShortList;

public class EnhancedTTSPToSimpleGraphSearchReducer
implements AlgorithmicProblemReduction<EnhancedTTSP, ShortList, GraphSearchWithSubpathEvaluationsInput<EnhancedTTSPState, String, Double>, ILabeledPath<EnhancedTTSPState, String>> {

	private final ToDoubleFunction<Number> linkFunction;

	public EnhancedTTSPToSimpleGraphSearchReducer() {
		this(x -> (double)x);
	}

	public EnhancedTTSPToSimpleGraphSearchReducer(final ToDoubleFunction<Number> linkFunction) {
		this.linkFunction = linkFunction;
	}

	@Override
	public GraphSearchWithSubpathEvaluationsInput<EnhancedTTSPState, String, Double> encodeProblem(final EnhancedTTSP problem) {
		return new GraphSearchWithSubpathEvaluationsInput<>(new EnhancedTTSPSimpleGraphGenerator(problem), new EnhancedTTSPSimpleSolutionPredicate(problem), node -> this.linkFunction.applyAsDouble(problem.getSolutionEvaluator().evaluate(node.getHead().getCurTour())));
	}

	@Override
	public ShortList decodeSolution(final ILabeledPath<EnhancedTTSPState, String> solution) {
		ShortList tour = solution.getHead().getCurTour();
		return tour.subList(0, tour.size() - 1); // remove trailing 0
	}
}
