package ai.libs.jaicore.ml.classification.multilabel.evaluation.loss.nonadditive.owa;

import org.apache.commons.math3.util.CombinatoricsUtils;

public class MoebiusTransformOWAValueFunction implements IOWAValueFunction {

	private final int k;

	public MoebiusTransformOWAValueFunction(final int k) {
		this.k = k;
	}

	@Override
	public double transform(final double nominator, final double denominator) {
		if ((int) nominator >= this.k) {
			return CombinatoricsUtils.binomialCoefficientDouble((int) nominator, this.k) / CombinatoricsUtils.binomialCoefficientDouble((int) denominator, this.k);
		} else {
			return 0;
		}
	}

}
