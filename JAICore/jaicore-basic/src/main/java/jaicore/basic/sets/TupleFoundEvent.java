package jaicore.basic.sets;

import java.util.List;

import jaicore.basic.algorithm.events.AAlgorithmEvent;

public class TupleFoundEvent<T> extends AAlgorithmEvent {
	private final List<T> tuple;

	public TupleFoundEvent(String algorithmId, List<T> tuple) {
		super(algorithmId);
		this.tuple = tuple;
	}

	public List<T> getTuple() {
		return tuple;
	}
}
