package jaicore.ml.evaluation.evaluators.weka.batcheval;

import java.util.LinkedList;
import java.util.List;

public class BatchEvaluationResult {
	List<Double> expected;
	List<Double> actual;

	public BatchEvaluationResult() {
		this.expected = new LinkedList<>();
		this.actual = new LinkedList<>();
	}

	public BatchEvaluationResult(final List<Double> expected, final List<Double> actual) {
		this.expected = expected;
		this.actual = actual;
	}

	public void addDatapoint(final Double expected, final Double actual) {
		this.expected.add(expected);
		this.actual.add(actual);
	}

	public List<Double> getExpected() {
		return this.expected;
	}

	public List<Double> getActual() {
		return this.actual;
	}
}
