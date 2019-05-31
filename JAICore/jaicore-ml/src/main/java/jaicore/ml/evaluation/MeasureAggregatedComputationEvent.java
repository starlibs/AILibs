package jaicore.ml.evaluation;

import java.util.List;

import jaicore.basic.aggregate.IAggregateFunction;

public class MeasureAggregatedComputationEvent<INPUT, OUTPUT> {
	private final List<INPUT> actual;
	private final List<INPUT> expected;
	private final IAggregateFunction<OUTPUT> aggregator;
	private final OUTPUT out;

	public MeasureAggregatedComputationEvent(List<INPUT> actual, List<INPUT> expected,
			IAggregateFunction<OUTPUT> aggregator, OUTPUT out) {
		super();
		this.actual = actual;
		this.expected = expected;
		this.aggregator = aggregator;
		this.out = out;
	}

	public List<INPUT> getActual() {
		return actual;
	}

	public List<INPUT> getExpected() {
		return expected;
	}

	public IAggregateFunction<OUTPUT> getAggregator() {
		return aggregator;
	}

	public OUTPUT getOut() {
		return out;
	}
}
