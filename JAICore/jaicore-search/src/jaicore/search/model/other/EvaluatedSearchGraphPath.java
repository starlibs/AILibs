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
}
