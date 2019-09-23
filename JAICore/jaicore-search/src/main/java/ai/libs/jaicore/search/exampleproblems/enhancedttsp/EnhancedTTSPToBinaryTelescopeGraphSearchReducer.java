package ai.libs.jaicore.search.exampleproblems.enhancedttsp;

import java.util.function.Function;

import org.api4.java.datastructure.graph.IPath;

import ai.libs.jaicore.basic.algorithm.reduction.AlgorithmicProblemReduction;
import ai.libs.jaicore.problems.enhancedttsp.EnhancedTTSP;
import ai.libs.jaicore.problems.enhancedttsp.EnhancedTTSPBinaryTelescopeNode;
import ai.libs.jaicore.search.exampleproblems.enhancedttsp.binarytelescope.EnhancedTTSPBinaryTelescopeSolutionPredicate;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;
import it.unimi.dsi.fastutil.shorts.ShortList;

public class EnhancedTTSPToBinaryTelescopeGraphSearchReducer
implements AlgorithmicProblemReduction<EnhancedTTSP, ShortList, GraphSearchWithSubpathEvaluationsInput<EnhancedTTSPBinaryTelescopeNode, String, Double>, IPath<EnhancedTTSPBinaryTelescopeNode, String>> {

	private final Function<Number, Double> linkFunction;

	public EnhancedTTSPToBinaryTelescopeGraphSearchReducer() {
		this(x -> (double)x);
	}

	public EnhancedTTSPToBinaryTelescopeGraphSearchReducer(final Function<Number, Double> linkFunction) {
		this.linkFunction = linkFunction;
	}

	@Override
	public GraphSearchWithSubpathEvaluationsInput<EnhancedTTSPBinaryTelescopeNode, String, Double> encodeProblem(final EnhancedTTSP problem) {
		return new GraphSearchWithSubpathEvaluationsInput<>(new EnhancedTTSPTelescopeGraphGenerator(problem), new EnhancedTTSPBinaryTelescopeSolutionPredicate(problem), path -> {
			ShortList tour = this.decodeSolution(path);
			return this.linkFunction.apply(problem.getSolutionEvaluator().evaluate(tour));
		});
	}

	@Override
	public ShortList decodeSolution(final IPath<EnhancedTTSPBinaryTelescopeNode, String> solution) {
		ShortList tour = solution.getHead().getCurTour();
		return tour.subList(0, tour.size() - 1); // remove trailing 0
	}
}
