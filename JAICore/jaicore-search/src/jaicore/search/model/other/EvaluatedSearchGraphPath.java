package jaicore.search.model.other;

import java.util.List;

public class EvaluatedSearchGraphPath<N, A, V extends Comparable<V>> extends SearchGraphPath<N, A> {
	private final V score;

	public EvaluatedSearchGraphPath(List<N> nodes, List<A> edges, V score) {
		super(nodes, edges);
		this.score = score;
	}

	public V getScore() {
		return score;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((score == null) ? 0 : score.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		EvaluatedSearchGraphPath other = (EvaluatedSearchGraphPath) obj;
		if (score == null) {
			if (other.score != null)
				return false;
		} else if (!score.equals(other.score))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "EvaluatedSearchGraphPath [score=" + score + "]";
	}
}
