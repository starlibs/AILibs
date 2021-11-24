package ai.libs.jaicore.search.exampleproblems.enhancedttsp;

import java.util.function.ToDoubleFunction;

import org.api4.java.datastructure.graph.ILabeledPath;

import ai.libs.jaicore.basic.algorithm.reduction.AlgorithmicProblemReduction;
import ai.libs.jaicore.problems.enhancedttsp.EnhancedTTSP;
import ai.libs.jaicore.problems.enhancedttsp.AEnhancedTTSPBinaryTelescopeNode;
import ai.libs.jaicore.search.exampleproblems.enhancedttsp.binarytelescope.EnhancedTTSPBinaryTelescopeSolutionPredicate;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;
import it.unimi.dsi.fastutil.shorts.ShortList;

public class EnhancedTTSPToBinaryTelescopeGraphSearchReducer
implements AlgorithmicProblemReduction<EnhancedTTSP, ShortList, GraphSearchWithSubpathEvaluationsInput<AEnhancedTTSPBinaryTelescopeNode, String, Double>, ILabeledPath<AEnhancedTTSPBinaryTelescopeNode, String>> {

	private final ToDoubleFunction<Number> linkFunction;

	public EnhancedTTSPToBinaryTelescopeGraphSearchReducer() {
		this(x -> (double)x);
	}

	public EnhancedTTSPToBinaryTelescopeGraphSearchReducer(final ToDoubleFunction<Number> linkFunction) {
		this.linkFunction = linkFunction;
	}

	@Override
	public GraphSearchWithSubpathEvaluationsInput<AEnhancedTTSPBinaryTelescopeNode, String, Double> encodeProblem(final EnhancedTTSP problem) {
		return new GraphSearchWithSubpathEvaluationsInput<>(new EnhancedTTSPTelescopeGraphGenerator(problem), new EnhancedTTSPBinaryTelescopeSolutionPredicate(problem), path -> {
			ShortList tour = this.decodeSolution(path);
			double score = problem.getSolutionEvaluator().evaluate(tour);
			double linkedScore = this.linkFunction.applyAsDouble(score);
			return linkedScore;
		});
	}

	@Override
	public ShortList decodeSolution(final ILabeledPath<AEnhancedTTSPBinaryTelescopeNode, String> solution) {
		return solution.getHead().getCurTour();
	}
}
