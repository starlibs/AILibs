package ai.libs.jaicore.search.algorithms.standard.bestfirst.events;

import java.util.List;

import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.NodeExpansionDescription;

import ai.libs.jaicore.search.model.travesaltree.BackPointerPath;

public class SuccessorComputationCompletedEvent<T, A, V extends Comparable<V>> extends BestFirstEvent {
	private BackPointerPath<T, A, V> path;
	private List<NodeExpansionDescription<T, A>> successorDescriptions;

	public SuccessorComputationCompletedEvent(final String algorithmId, final BackPointerPath<T, A, V> path,
			final List<NodeExpansionDescription<T, A>> successorDescriptions) {
		super(algorithmId);
		this.path = path;
		this.successorDescriptions = successorDescriptions;
	}

	public BackPointerPath<T, A, V> getNode() {
		return this.path;
	}

	public void setNode(final BackPointerPath<T, A, V> node) {
		this.path = node;
	}

	public List<NodeExpansionDescription<T, A>> getSuccessorDescriptions() {
		return this.successorDescriptions;
	}

	public void setSuccessorDescriptions(final List<NodeExpansionDescription<T, A>> successorDescriptions) {
		this.successorDescriptions = successorDescriptions;
	}
}
