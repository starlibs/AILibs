package jaicore.search.testproblems.npuzzle.standard;

import jaicore.basic.algorithm.AlgorithmProblemTransformer;
import jaicore.search.core.interfaces.EdgeCountingSolutionEvaluator;
import jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;

public class NPuzzleToGeneralTraversalTreeReducer implements AlgorithmProblemTransformer<NPuzzleProblem, GraphSearchWithSubpathEvaluationsInput<NPuzzleNode, String, Double>>{

	@Override
	public GraphSearchWithSubpathEvaluationsInput<NPuzzleNode, String, Double> transform(NPuzzleProblem problem) {
		return new GraphSearchWithSubpathEvaluationsInput<>(new NPuzzleGenerator(problem.getBoard()), n -> new EdgeCountingSolutionEvaluator<NPuzzleNode>().evaluateSolution(n.externalPath()));
	}

}
