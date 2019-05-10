package jaicore.ml.evaluation;

import java.util.List;

public class MeasureListComputationEvent<INPUT,OUTPUT> {
	private final List<INPUT> actual;
	private final List<INPUT> expected;
	private final List<OUTPUT> out;

	public MeasureListComputationEvent(List<INPUT> actual, List<INPUT> expected, List<OUTPUT> out) {
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

	public List<OUTPUT> getOut() {
		return out;
	}
}
