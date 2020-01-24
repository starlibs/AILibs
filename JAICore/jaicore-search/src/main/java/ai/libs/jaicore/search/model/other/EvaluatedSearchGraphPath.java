package ai.libs.jaicore.search.model.other;

import java.util.List;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IEvaluatedPath;
import org.api4.java.datastructure.graph.ILabeledPath;

public class EvaluatedSearchGraphPath<N, A, V extends Comparable<V>> extends SearchGraphPath<N, A> implements IEvaluatedPath<N, A, V> {
	private final V score;

	public EvaluatedSearchGraphPath(final ILabeledPath<N, A> path, final V score) {
		super(path);
		this.score = score;
	}

	public EvaluatedSearchGraphPath(final List<N> nodes, final List<A> edges, final V score) {
		super(nodes, edges);
		this.score = score;
	}

	@Override
	public V getScore() {
		return this.score;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((this.score == null) ? 0 : this.score.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		EvaluatedSearchGraphPath other = (EvaluatedSearchGraphPath) obj;
		if (this.score == null) {
			if (other.score != null) {
				return false;
			}
		} else if (!this.score.equals(other.score)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "EvaluatedSearchGraphPath [score=" + this.score + "]";
	}
}
