package jaicore.search.model.probleminputs.builders;

import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import jaicore.search.model.probleminputs.GeneralEvaluatedTraversalTree;

public class GeneralEvaluatedTraversalTreeBuilder<N, A, V extends Comparable<V>> extends SearchProblemInputBuilder<N, A, GeneralEvaluatedTraversalTree<N, A, V>> {

	private INodeEvaluator<N, V> nodeEvaluator;

	public GeneralEvaluatedTraversalTreeBuilder() {
		
	}
	
	public GeneralEvaluatedTraversalTreeBuilder(INodeEvaluator<N, V> nodeEvaluator) {
		super();
		this.nodeEvaluator = nodeEvaluator;
	}

	public INodeEvaluator<N, V> getNodeEvaluator() {
		return nodeEvaluator;
	}

	public void setNodeEvaluator(INodeEvaluator<N, V> nodeEvaluator) {
		this.nodeEvaluator = nodeEvaluator;
	}

	@Override
	public GeneralEvaluatedTraversalTree<N, A, V> build() {
		return new GeneralEvaluatedTraversalTree<>(getGraphGenerator(), nodeEvaluator);
	}

}
