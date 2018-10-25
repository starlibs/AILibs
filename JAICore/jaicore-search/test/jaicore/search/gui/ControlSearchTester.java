
package jaicore.search.gui;

import jaicore.basic.algorithm.AlgorithmProblemTransformer;
import jaicore.search.algorithms.standard.bestfirst.BestFirstFactory;
import jaicore.search.core.interfaces.IGraphSearchFactory;
import jaicore.search.model.other.EvaluatedSearchGraphPath;
import jaicore.search.model.probleminputs.GeneralEvaluatedTraversalTree;
import jaicore.search.model.travesaltree.Node;
import jaicore.search.testproblems.nqueens.NQueenTester;
import jaicore.search.testproblems.nqueens.NQueensToGeneralTravesalTreeReducer;
import jaicore.search.testproblems.nqueens.QueenNode;

public class ControlSearchTester
		extends GuiTester<GeneralEvaluatedTraversalTree<QueenNode, String, Double>, EvaluatedSearchGraphPath<QueenNode, String, Double>, Node<QueenNode, Double>, String> {

	public ControlSearchTester() {
		super();
		this.setShowGraphs(true);
	}
	
	@Override
	public IGraphSearchFactory<GeneralEvaluatedTraversalTree<QueenNode, String, Double>, EvaluatedSearchGraphPath<QueenNode, String, Double>, QueenNode, String, Double, Node<QueenNode, Double>, String> getFactory() {
		BestFirstFactory<GeneralEvaluatedTraversalTree<QueenNode, String, Double>,QueenNode, String, Double> searchFactory = new BestFirstFactory<>();
		return searchFactory;
	}

	@Override
	public AlgorithmProblemTransformer<Integer, GeneralEvaluatedTraversalTree<QueenNode, String, Double>> getProblemReducer() {
		return new NQueensToGeneralTravesalTreeReducer();
	}
	
	
}
