package jaicore.search.testproblems.npuzzle.standard;

import jaicore.basic.algorithm.AlgorithmProblemTransformer;
import jaicore.search.core.interfaces.EdgeCountingSolutionEvaluator;
import jaicore.search.model.probleminputs.GeneralEvaluatedTraversalTree;

public class NPuzzleToGeneralTraversalTreeReducer implements AlgorithmProblemTransformer<NPuzzleProblem, GeneralEvaluatedTraversalTree<NPuzzleNode, String, Double>>{

	@Override
	public GeneralEvaluatedTraversalTree<NPuzzleNode, String, Double> transform(NPuzzleProblem problem) {
		return new GeneralEvaluatedTraversalTree<>(new NPuzzleGenerator(problem.getBoard()), n -> new EdgeCountingSolutionEvaluator<NPuzzleNode>().evaluateSolution(n.externalPath()));
	}

}
