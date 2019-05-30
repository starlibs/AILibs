package jaicore.ml.evaluation;

public class MeasureSingleComputationEvent<INPUT, OUTPUT> {
	private final INPUT actual;
	private final INPUT expected;
	private final OUTPUT out;

	public MeasureSingleComputationEvent(INPUT actual, INPUT expected, OUTPUT out) {
		super();
		this.actual = actual;
		this.expected = expected;
		this.out = out;
	}

	public INPUT getActual() {
		return actual;
	}

	public INPUT getExpected() {
		return expected;
	}

	public OUTPUT getOut() {
		return out;
	}
}
