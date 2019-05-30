package jaicore.ml.evaluation;

import java.util.List;

public class MeasureAvgComputationEvent<INPUT, OUTPUT> {
	private final List<INPUT> actual;
	private final List<INPUT> expected;
	private final OUTPUT out;

	public MeasureAvgComputationEvent(List<INPUT> actual, List<INPUT> expected, OUTPUT out) {
		super();
		this.actual = actual;
		this.expected = expected;
		this.out = out;
	}

	public List<INPUT> getActual() {
		return actual;
	}

	public List<INPUT> getExpected() {
		return expected;
	}

	public OUTPUT getOut() {
		return out;
	}
}
