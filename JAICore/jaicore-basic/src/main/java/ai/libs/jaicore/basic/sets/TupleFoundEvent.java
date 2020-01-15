package ai.libs.jaicore.basic.sets;

import java.util.List;

import org.api4.java.algorithm.IAlgorithm;

import ai.libs.jaicore.basic.algorithm.AAlgorithmEvent;

public class TupleFoundEvent<T> extends AAlgorithmEvent {
	private final List<T> tuple;

	public TupleFoundEvent(final IAlgorithm<?, ?> algorithm, final List<T> tuple) {
		super(algorithm);
		this.tuple = tuple;
	}

	public List<T> getTuple() {
		return this.tuple;
	}
}
