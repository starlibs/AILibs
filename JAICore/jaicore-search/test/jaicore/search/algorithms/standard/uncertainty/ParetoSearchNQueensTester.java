package jaicore.search.algorithms.standard.uncertainty;

import jaicore.basic.algorithm.AlgorithmProblemTransformer;
import jaicore.search.algorithms.standard.uncertainty.OversearchAvoidanceConfig.OversearchAvoidanceMode;
import jaicore.search.core.interfaces.IGraphSearchFactory;
import jaicore.search.model.other.EvaluatedSearchGraphPath;
import jaicore.search.model.probleminputs.UncertainlyEvaluatedTraversalTree;
import jaicore.search.model.travesaltree.Node;
import jaicore.search.testproblems.nqueens.NQueenTester;
import jaicore.search.testproblems.nqueens.NQueensToUncertainlyEvaluatedTravesalTreeReducer;
import jaicore.search.testproblems.nqueens.QueenNode;

public class ParetoSearchNQueensTester extends NQueenTester<UncertainlyEvaluatedTraversalTree<QueenNode,String,Double>, EvaluatedSearchGraphPath<QueenNode, String, Double>, Node<QueenNode, Double>, String> {

	@Override
	public AlgorithmProblemTransformer<Integer, UncertainlyEvaluatedTraversalTree<QueenNode, String, Double>> getProblemReducer() {
		return new NQueensToUncertainlyEvaluatedTravesalTreeReducer();
	}

	@Override
	public IGraphSearchFactory<UncertainlyEvaluatedTraversalTree<QueenNode, String, Double>, EvaluatedSearchGraphPath<QueenNode, String, Double>, QueenNode, String, Double, Node<QueenNode, Double>, String> getFactory() {
		OversearchAvoidanceConfig<QueenNode, Double> config = new OversearchAvoidanceConfig<>(OversearchAvoidanceMode.PARETO_FRONT_SELECTION, 0);
		UncertaintyORGraphSearchFactory<QueenNode, String, Double> searchFactory = new UncertaintyORGraphSearchFactory<>();
		searchFactory.setConfig(config);
		return searchFactory;
	}
}
