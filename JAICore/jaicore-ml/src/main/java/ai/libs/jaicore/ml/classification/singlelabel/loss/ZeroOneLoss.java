package ai.libs.jaicore.ml.classification.singlelabel.loss;

import org.api4.java.ai.ml.classification.singlelabel.evaluation.ISingleLabelClassification;
import org.api4.java.ai.ml.core.evaluation.loss.IInstanceMeasure;

/**
 *
 * @author mwever
 */
public class ZeroOneLoss implements IInstanceMeasure<ISingleLabelClassification> {

	@Override
	public double loss(final ISingleLabelClassification expected, final ISingleLabelClassification actual) {
		return expected.equals(actual) ? 0.0 : 1.0;
	}

	@Override
	public double score(final ISingleLabelClassification expected, final ISingleLabelClassification actual) {
		return 1 - this.loss(expected, actual);
	}

}
