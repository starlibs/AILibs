package ai.libs.jaicore.ml.classification.singlelabel.loss;

import org.api4.java.ai.ml.core.evaluation.loss.IInstanceWiseLossFunction;

/**
 *
 * @author mwever
 */
public class ZeroOneLoss implements IInstanceWiseLossFunction {

	@Override
	public double loss(final Object expected, final Object actual) {
		return expected.equals(actual) ? 0.0 : 1.0;
	}

}
