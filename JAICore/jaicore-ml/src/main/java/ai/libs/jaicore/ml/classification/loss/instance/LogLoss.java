package ai.libs.jaicore.ml.classification.loss.instance;

import org.api4.java.ai.ml.classification.singlelabel.evaluation.ISingleLabelClassification;

public class LogLoss extends AInstanceMeasure<Integer, ISingleLabelClassification> {

	private final CrossEntropyLoss cel;

	public LogLoss() {
		this.cel = new CrossEntropyLoss();
	}

	public LogLoss(final double epsilon) {
		this.cel = new CrossEntropyLoss(epsilon);
	}

	@Override
	public double loss(final Integer expected, final ISingleLabelClassification predicted) {
		double[] expectedArr = new double[predicted.getClassDistribution().size()];
		expectedArr[expected] = 1.0;
		return this.cel.loss(expectedArr, predicted);
	}

}
