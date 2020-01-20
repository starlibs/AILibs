package ai.libs.jaicore.search.probleminputs.builders;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPathEvaluator;

import ai.libs.jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;

public class GraphSearchWithSubpathEvaluationsInputBuilder<N, A, V extends Comparable<V>> extends SearchProblemInputBuilder<N, A, GraphSearchWithSubpathEvaluationsInput<N, A, V>, GraphSearchWithSubpathEvaluationsInputBuilder<N, A, V>> {

	private IPathEvaluator<N, A, V> nodeEvaluator;

	public GraphSearchWithSubpathEvaluationsInputBuilder() {

	}

	public GraphSearchWithSubpathEvaluationsInputBuilder(final IPathEvaluator<N, A, V> nodeEvaluator) {
		super();
		this.nodeEvaluator = nodeEvaluator;
	}

	public IPathEvaluator<N, A, V> getNodeEvaluator() {
		return this.nodeEvaluator;
	}

	public void setNodeEvaluator(final IPathEvaluator<N, A, V> nodeEvaluator) {
		this.nodeEvaluator = nodeEvaluator;
	}

	@Override
	public GraphSearchWithSubpathEvaluationsInput<N, A, V> build() {
		return new GraphSearchWithSubpathEvaluationsInput<>(this.getGraphGenerator(), this.getGoalTester(), this.nodeEvaluator);
	}

	@Override
	protected GraphSearchWithSubpathEvaluationsInputBuilder<N, A, V> self() {
		return this;
	}

}
