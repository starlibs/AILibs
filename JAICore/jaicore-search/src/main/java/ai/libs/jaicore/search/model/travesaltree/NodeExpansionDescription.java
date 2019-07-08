package ai.libs.jaicore.search.model.travesaltree;

public class NodeExpansionDescription<S, A> {

	private final S to;
	private final A action;
	private final NodeType typeOfToNode;

	public NodeExpansionDescription(final S to) {
		this(to, null, NodeType.OR);
	}

	public NodeExpansionDescription(final S to, final A action, final NodeType typeOfToNode) {
		super();
		this.to = to;
		this.action = action;
		this.typeOfToNode = typeOfToNode;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.action == null) ? 0 : this.action.hashCode());
		result = prime * result + ((this.to == null) ? 0 : this.to.hashCode());
		result = prime * result + ((this.typeOfToNode == null) ? 0 : this.typeOfToNode.hashCode());
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
		NodeExpansionDescription<?, ?> other = (NodeExpansionDescription<?, ?>) obj;
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
		if (this.typeOfToNode != other.typeOfToNode) {
			return false;
		}
		return true;
	}

	public S getTo() {
		return this.to;
	}

	public A getAction() {
		return this.action;
	}

	public NodeType getTypeOfToNode() {
		return this.typeOfToNode;
	}

}
