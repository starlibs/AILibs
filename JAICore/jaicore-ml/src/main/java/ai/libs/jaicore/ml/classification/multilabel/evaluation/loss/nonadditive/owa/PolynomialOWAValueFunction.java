package ai.libs.jaicore.ml.classification.multilabel.evaluation.loss.nonadditive.owa;

public class PolynomialOWAValueFunction implements IOWAValueFunction {

	private final double alpha;

	public PolynomialOWAValueFunction(final double alpha) {
		this.alpha = alpha;
	}

	@Override
	public double transform(final double nominator, final double denominator) {
		return Math.pow(nominator / denominator, this.alpha);
	}

}
