package ai.libs.jaicore.search.probleminputs.builders;

import ai.libs.jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;

public class GraphSearchWithSubpathEvaluationsInputBuilder<N, A, V extends Comparable<V>> extends SearchProblemInputBuilder<N, A, GraphSearchWithSubpathEvaluationsInput<N, A, V>> {

	private INodeEvaluator<N, V> nodeEvaluator;

	public GraphSearchWithSubpathEvaluationsInputBuilder() {
		
	}
	
	public GraphSearchWithSubpathEvaluationsInputBuilder(INodeEvaluator<N, V> nodeEvaluator) {
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
	public GraphSearchWithSubpathEvaluationsInput<N, A, V> build() {
		return new GraphSearchWithSubpathEvaluationsInput<>(getGraphGenerator(), nodeEvaluator);
	}

}
