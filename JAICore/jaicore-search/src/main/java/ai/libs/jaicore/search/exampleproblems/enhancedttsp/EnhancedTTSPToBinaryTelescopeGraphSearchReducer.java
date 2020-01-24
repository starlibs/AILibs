package ai.libs.jaicore.search.exampleproblems.enhancedttsp;

import java.util.function.ToDoubleFunction;

import org.api4.java.datastructure.graph.ILabeledPath;

import ai.libs.jaicore.basic.algorithm.reduction.AlgorithmicProblemReduction;
import ai.libs.jaicore.problems.enhancedttsp.EnhancedTTSP;
import ai.libs.jaicore.problems.enhancedttsp.EnhancedTTSPBinaryTelescopeNode;
import ai.libs.jaicore.search.exampleproblems.enhancedttsp.binarytelescope.EnhancedTTSPBinaryTelescopeSolutionPredicate;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;
import it.unimi.dsi.fastutil.shorts.ShortList;

public class EnhancedTTSPToBinaryTelescopeGraphSearchReducer
implements AlgorithmicProblemReduction<EnhancedTTSP, ShortList, GraphSearchWithSubpathEvaluationsInput<EnhancedTTSPBinaryTelescopeNode, String, Double>, ILabeledPath<EnhancedTTSPBinaryTelescopeNode, String>> {

	private final ToDoubleFunction<Number> linkFunction;

	public EnhancedTTSPToBinaryTelescopeGraphSearchReducer() {
		this(x -> (double)x);
	}

	public EnhancedTTSPToBinaryTelescopeGraphSearchReducer(final ToDoubleFunction<Number> linkFunction) {
		this.linkFunction = linkFunction;
	}

	@Override
	public GraphSearchWithSubpathEvaluationsInput<EnhancedTTSPBinaryTelescopeNode, String, Double> encodeProblem(final EnhancedTTSP problem) {
		return new GraphSearchWithSubpathEvaluationsInput<>(new EnhancedTTSPTelescopeGraphGenerator(problem), new EnhancedTTSPBinaryTelescopeSolutionPredicate(problem), path -> {
			ShortList tour = this.decodeSolution(path);
			return this.linkFunction.applyAsDouble(problem.getSolutionEvaluator().evaluate(tour));
		});
	}

	@Override
	public ShortList decodeSolution(final ILabeledPath<EnhancedTTSPBinaryTelescopeNode, String> solution) {
		return solution.getHead().getCurTour();
	}
}
