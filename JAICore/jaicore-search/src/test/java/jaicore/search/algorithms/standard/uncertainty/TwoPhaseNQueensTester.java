package jaicore.search.algorithms.standard.uncertainty;

import jaicore.basic.algorithm.AlgorithmProblemTransformer;
import jaicore.search.algorithms.standard.uncertainty.OversearchAvoidanceConfig.OversearchAvoidanceMode;
import jaicore.search.core.interfaces.IGraphSearchFactory;
import jaicore.search.model.other.EvaluatedSearchGraphPath;
import jaicore.search.probleminputs.GraphSearchWithUncertaintyBasedSubpathEvaluationInput;
import jaicore.search.testproblems.nqueens.NQueenTester;
import jaicore.search.testproblems.nqueens.NQueensToUncertainlyEvaluatedTravesalTreeReducer;
import jaicore.search.testproblems.nqueens.QueenNode;

public class TwoPhaseNQueensTester extends NQueenTester<GraphSearchWithUncertaintyBasedSubpathEvaluationInput<QueenNode,String,Double>, EvaluatedSearchGraphPath<QueenNode, String, Double>> {

	@Override
	public AlgorithmProblemTransformer<Integer, GraphSearchWithUncertaintyBasedSubpathEvaluationInput<QueenNode, String, Double>> getProblemReducer() {
		return new NQueensToUncertainlyEvaluatedTravesalTreeReducer();
	}

	@Override
	public IGraphSearchFactory<GraphSearchWithUncertaintyBasedSubpathEvaluationInput<QueenNode, String, Double>, EvaluatedSearchGraphPath<QueenNode, String, Double>, QueenNode, String> getFactory() {
		OversearchAvoidanceConfig<QueenNode, Double> config = new OversearchAvoidanceConfig<>(OversearchAvoidanceMode.TWO_PHASE_SELECTION, 0);
		UncertaintyORGraphSearchFactory<QueenNode, String, Double> searchFactory = new UncertaintyORGraphSearchFactory<>();
		searchFactory.setConfig(config);
		return searchFactory;
	}

}
