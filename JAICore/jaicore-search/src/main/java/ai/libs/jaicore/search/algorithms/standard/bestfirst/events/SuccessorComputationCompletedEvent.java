package ai.libs.jaicore.search.algorithms.standard.bestfirst.events;

import java.util.List;

import org.api4.java.algorithm.IAlgorithm;
import org.api4.java.datastructure.graph.implicit.INewNodeDescription;

import ai.libs.jaicore.search.model.travesaltree.BackPointerPath;

public class SuccessorComputationCompletedEvent<T, A, V extends Comparable<V>> extends BestFirstEvent {
	private BackPointerPath<T, A, V> path;
	private List<INewNodeDescription<T, A>> successorDescriptions;

	public SuccessorComputationCompletedEvent(final IAlgorithm<?, ?> algorithm, final BackPointerPath<T, A, V> path,
			final List<INewNodeDescription<T, A>> successorDescriptions) {
		super(algorithm);
		this.path = path;
		this.successorDescriptions = successorDescriptions;
	}

	public BackPointerPath<T, A, V> getNode() {
		return this.path;
	}

	public void setNode(final BackPointerPath<T, A, V> node) {
		this.path = node;
	}

	public List<INewNodeDescription<T, A>> getSuccessorDescriptions() {
		return this.successorDescriptions;
	}

	public void setSuccessorDescriptions(final List<INewNodeDescription<T, A>> successorDescriptions) {
		this.successorDescriptions = successorDescriptions;
	}
}
