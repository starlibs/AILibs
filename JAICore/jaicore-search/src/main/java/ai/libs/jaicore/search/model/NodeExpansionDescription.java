package ai.libs.jaicore.search.model;

import org.api4.java.datastructure.graph.implicit.INewNodeDescription;

public class NodeExpansionDescription<S, A> implements INewNodeDescription<S, A> {

	private final S to;
	private final A action;

	public NodeExpansionDescription(final S to) {
		this(to, null);
	}

	public NodeExpansionDescription(final S to, final A action) {
		super();
		this.to = to;
		this.action = action;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.action == null) ? 0 : this.action.hashCode());
		result = prime * result + ((this.to == null) ? 0 : this.to.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		NodeExpansionDescription other = (NodeExpansionDescription) obj;
		if (this.action == null) {
			if (other.action != null) {
				return false;
			}
		} else if (!this.action.equals(other.action)) {
			return false;
		}
		if (this.to == null) {
			if (other.to != null) {
				return false;
			}
		} else if (!this.to.equals(other.to)) {
			return false;
		}
		return true;
	}

	@Override
	public S getTo() {
		return this.to;
	}

	public A getAction() {
		return this.action;
	}

	@Override
	public S getFrom() {
		throw new UnsupportedOperationException();
	}

	@Override
	public A getArcLabel() {
		return this.getAction();
	}
}