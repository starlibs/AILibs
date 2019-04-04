package jaicore.search.testproblems.nqueens;

import jaicore.basic.algorithm.AlgorithmProblemTransformer;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import jaicore.search.core.interfaces.GraphGenerator;
import jaicore.search.model.probleminputs.GeneralEvaluatedTraversalTree;

public class NQueensToGeneralTravesalTreeReducer implements AlgorithmProblemTransformer<Integer, GeneralEvaluatedTraversalTree<QueenNode, String, Double>> {

	@Override
	public GeneralEvaluatedTraversalTree<QueenNode, String, Double> transform(Integer problem) {
		GraphGenerator<QueenNode, String> graphGenerator = new NQueenGenerator(problem);
		INodeEvaluator<QueenNode, Double> nodeEvaluator = node -> (double) node.getPoint().getNumberOfAttackedCells();
		return new GeneralEvaluatedTraversalTree<>(graphGenerator, nodeEvaluator);

	}
}
