package ai.libs.jaicore.ml.classification.singlelabel.loss;

import org.api4.java.ai.ml.core.evaluation.loss.IInstanceMeasure;

/**
 *
 * @author mwever
 */
public class ZeroOneLoss implements IInstanceMeasure<Object> {

	@Override
	public double loss(final Object expected, final Object actual) {
		return expected.equals(actual) ? 0.0 : 1.0;
	}

	@Override
	public double score(final Object expected, final Object actual) {
		return 1 - this.loss(expected, actual);
	}

}
