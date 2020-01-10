package ai.libs.jaicore.ml.classification.multilabel.evaluation.loss.nonadditive.owa;

public interface IOWAValueFunction {

	public double transform(double nominator, double denominator);

}
