package jaicore.basic.sets;

import jaicore.basic.algorithm.events.AlgorithmEvent;

public class TupleOfCartesianProductFoundEvent<T> implements AlgorithmEvent {
	private final Object[] tuple;

	public TupleOfCartesianProductFoundEvent(Object[] tuple) {
		super();
		this.tuple = tuple;
	}
	
	public Object[] getTuple() {
		return tuple;
	}
}
