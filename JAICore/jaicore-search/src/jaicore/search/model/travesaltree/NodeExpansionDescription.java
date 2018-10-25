package jaicore.search.model.travesaltree;

public class NodeExpansionDescription<S, A> {

	private final S from, to;
	private final A action;
	private final NodeType typeOfToNode;

	public NodeExpansionDescription(S from, S to, A action, NodeType typeOfToNode) {
		super();
		this.from = from;
		this.to = to;
		this.action = action;
		this.typeOfToNode = typeOfToNode;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((action == null) ? 0 : action.hashCode());
		result = prime * result + ((from == null) ? 0 : from.hashCode());
		result = prime * result + ((to == null) ? 0 : to.hashCode());
		result = prime * result + ((typeOfToNode == null) ? 0 : typeOfToNode.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NodeExpansionDescription<?, ?> other = (NodeExpansionDescription<?, ?>) obj;
		if (action == null) {
			if (other.action != null)
				return false;
		} else if (!action.equals(other.action))
			return false;
		if (from == null) {
			if (other.from != null)
				return false;
		} else if (!from.equals(other.from))
			return false;
		if (to == null) {
			if (other.to != null)
				return false;
		} else if (!to.equals(other.to))
			return false;
		if (typeOfToNode != other.typeOfToNode)
			return false;
		return true;
	}

	public S getFrom() {
		return from;
	}

	public S getTo() {
		return to;
	}

	public A getAction() {
		return action;
	}

	public NodeType getTypeOfToNode() {
		return typeOfToNode;
	}

}
