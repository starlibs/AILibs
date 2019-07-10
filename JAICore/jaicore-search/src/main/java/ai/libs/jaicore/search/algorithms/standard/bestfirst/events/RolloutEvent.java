package ai.libs.jaicore.search.algorithms.standard.bestfirst.events;

import java.util.List;

import org.api4.java.algorithm.events.AAlgorithmEvent;

public class RolloutEvent<N, V extends Comparable<V>> extends AAlgorithmEvent {

	private final List<N> path;
	private final V score;

	public RolloutEvent(final String algorithmId, final List<N> path, final V score) {
		super(algorithmId);
		this.path = path;
		this.score = score;
	}

	public List<N> getPath() {
		return this.path;
	}

	public V getScore() {
		return this.score;
	}
}
