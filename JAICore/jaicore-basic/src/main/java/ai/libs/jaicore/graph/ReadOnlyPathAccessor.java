package ai.libs.jaicore.graph;

import java.util.Collections;
import java.util.List;

import org.api4.java.datastructure.graph.ILabeledPath;

public class ReadOnlyPathAccessor<N, A> implements ILabeledPath<N, A> {
	private final ILabeledPath<N, A> path;

	public ReadOnlyPathAccessor(final ILabeledPath<N, A> path) {
		super();
		this.path = path;
	}

	@Override
	public ILabeledPath<N, A> getUnmodifiableAccessor() {
		return this;
	}

	@Override
	public N getRoot() {
		return this.path.getRoot();
	}

	@Override
	public N getHead() {
		return this.path.getHead();
	}

	@Override
	public N getParentOfHead() {
		return this.path.getParentOfHead();
	}

	@Override
	public void extend(final N newHead, final A arcToNewHead) {
		throw new UnsupportedOperationException("This is a read-only path.");
	}

	@Override
	public void cutHead() {
		throw new UnsupportedOperationException("This is a read-only path.");
	}

	@Override
	public ILabeledPath<N, A> getPathToParentOfHead() {
		return this.path.getPathToParentOfHead();
	}

	@Override
	public ILabeledPath<N, A> getPathFromChildOfRoot() {
		return this.path.getPathFromChildOfRoot();
	}

	@Override
	public List<N> getNodes() {
		return Collections.unmodifiableList(this.path.getNodes());
	}

	@Override
	public boolean isPoint() {
		return this.path.isPoint();
	}

	@Override
	public int getNumberOfNodes() {
		return this.path.getNumberOfNodes();
	}

	@Override
	public List<A> getArcs() {
		return Collections.unmodifiableList(this.path.getArcs());
	}

	@Override
	public A getInArc(final N node) {
		return this.path.getInArc(node);
	}

	@Override
	public A getOutArc(final N node) {
		return this.path.getOutArc(node);
	}

	@Override
	public boolean containsNode(final N node) {
		return this.path.containsNode(node);
	}
}
