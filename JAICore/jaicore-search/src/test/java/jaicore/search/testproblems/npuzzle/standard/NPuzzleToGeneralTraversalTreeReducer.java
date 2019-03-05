package jaicore.search.testproblems.npuzzle.standard;

import jaicore.basic.algorithm.reduction.AlgorithmicProblemReduction;
import jaicore.search.core.interfaces.EdgeCountingSolutionEvaluator;
import jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;
import jaicore.testproblems.npuzzle.NPuzzleProblem;
import jaicore.testproblems.npuzzle.NPuzzleState;

public class NPuzzleToGeneralTraversalTreeReducer implements AlgorithmicProblemReduction<NPuzzleProblem, GraphSearchWithSubpathEvaluationsInput<NPuzzleState, String, Double>>{

	@Override
	public GraphSearchWithSubpathEvaluationsInput<NPuzzleState, String, Double> transform(NPuzzleProblem problem) {
		return new GraphSearchWithSubpathEvaluationsInput<>(new NPuzzleGenerator(problem.getBoard()), n -> new EdgeCountingSolutionEvaluator<NPuzzleState>().evaluateSolution(n.externalPath()));
	}

}
