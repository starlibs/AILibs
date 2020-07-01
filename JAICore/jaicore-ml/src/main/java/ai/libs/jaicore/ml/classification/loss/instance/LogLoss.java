package ai.libs.jaicore.ml.classification.loss.instance;

public class LogLoss extends AInstanceMeasure<Integer, double[]> {

	private final CrossEntropyLoss cel;

	public LogLoss() {
		this.cel = new CrossEntropyLoss();
	}

	public LogLoss(final double epsilon) {
		this.cel = new CrossEntropyLoss(epsilon);
	}

	@Override
	public double loss(final Integer expected, final double[] predicted) {
		double[] expectedArr = new double[predicted.length];
		expectedArr[expected] = 1.0;
		return this.cel.loss(expectedArr, predicted);
	}

}
