package jaicore.search.model.travesaltree;

public class Edge<T,V extends Comparable<V>> {

	private final Node<T,V> from, to;

	public Edge(Node<T,V> from, Node<T,V> to) {
		super();
		this.from = from;
		this.to = to;
	}

	public Node<T,V> getFrom() {
		return from;
	}

	public Node<T,V> getTo() {
		return to;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((from == null) ? 0 : from.hashCode());
		result = prime * result + ((to == null) ? 0 : to.hashCode());
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
		@SuppressWarnings("unchecked")
		Edge<T,V> other = (Edge<T,V>) obj;
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
		return true;
	}
}
