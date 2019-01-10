package jaicore.basic.sets;

import java.util.List;

import jaicore.basic.algorithm.events.AlgorithmEvent;

public class TupleOfCartesianProductFoundEvent<T> implements AlgorithmEvent {
	private final List<T> tuple;

	public TupleOfCartesianProductFoundEvent(List<T> tuple) {
		super();
		this.tuple = tuple;
	}
	
	public List<T> getTuple() {
		return tuple;
	}
}
