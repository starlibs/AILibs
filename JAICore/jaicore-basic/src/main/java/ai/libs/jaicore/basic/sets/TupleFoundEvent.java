package ai.libs.jaicore.basic.sets;

import java.util.List;

import org.api4.java.algorithm.events.AAlgorithmEvent;

public class TupleFoundEvent<T> extends AAlgorithmEvent {
	private final List<T> tuple;

	public TupleFoundEvent(final String algorithmId, final List<T> tuple) {
		super(algorithmId);
		this.tuple = tuple;
	}

	public List<T> getTuple() {
		return this.tuple;
	}
}
