package ai.libs.jaicore.search.algorithms.standard.bestfirst.events;

import java.util.List;

import org.api4.java.algorithm.IAlgorithm;

import ai.libs.jaicore.basic.algorithm.AAlgorithmEvent;

public class RolloutEvent<N, V extends Comparable<V>> extends AAlgorithmEvent {

	private final List<N> path;
	private final V score;

	public RolloutEvent(final IAlgorithm<?, ?> algorithm, final List<N> path, final V score) {
		super(algorithm);
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
