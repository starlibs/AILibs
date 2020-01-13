package ai.libs.jaicore.basic.sets;

import java.util.List;

import org.api4.java.algorithm.IAlgorithm;

import ai.libs.jaicore.basic.algorithm.AAlgorithmEvent;

public class TupleOfCartesianProductFoundEvent<T> extends AAlgorithmEvent {
	private final List<T> tuple;

	public TupleOfCartesianProductFoundEvent(final IAlgorithm<?, ?> algorithm, final List<T> tuple) {
		super(algorithm);
		this.tuple = tuple;
	}

	public List<T> getTuple() {
		return this.tuple;
	}
}
