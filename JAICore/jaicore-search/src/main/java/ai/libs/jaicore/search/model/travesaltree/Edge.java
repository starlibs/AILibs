package ai.libs.jaicore.search.model.travesaltree;

public class Edge<T, A, V extends Comparable<V>> {

	private final BackPointerPath<T, A, V> from, to;

	public Edge(final BackPointerPath<T, A, V> from, final BackPointerPath<T, A, V> to) {
		super();
		this.from = from;
		this.to = to;
	}

	public BackPointerPath<T, A, V> getFrom() {
		return this.from;
	}

	public BackPointerPath<T, A, V> getTo() {
		return this.to;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.from == null) ? 0 : this.from.hashCode());
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
		@SuppressWarnings("unchecked")
		Edge<T, A, V> other = (Edge<T, A, V>) obj;
		if (this.from == null) {
			if (other.from != null) {
				return false;
			}
		} else if (!this.from.equals(other.from)) {
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
}
