package jaicore.search.algorithms.standard.bestfirst.events;

import java.util.List;

import jaicore.basic.algorithm.events.AAlgorithmEvent;

public class RolloutEvent<N, V extends Comparable<V>> extends AAlgorithmEvent {

	private final List<N> path;
	private final V score;

	public RolloutEvent(String algorithmId, List<N> path, V score) {
		super(algorithmId);
		this.path = path;
		this.score = score;
	}

	public List<N> getPath() {
		return path;
	}

	public V getScore() {
		return score;
	}
}
