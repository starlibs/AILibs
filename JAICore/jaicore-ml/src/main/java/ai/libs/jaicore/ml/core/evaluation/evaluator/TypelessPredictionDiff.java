package ai.libs.jaicore.ml.core.evaluation.evaluator;

import java.util.List;

/**
 * This is a helper class with which one can create a prediction diff object without caring about the types of ground truths and predictions.
 *
 * Using the getCastedView method, one can later (in a place where the concrete types are known) get a more specific variant.
 *
 * @author Felix Mohr
 *
 */
public class TypelessPredictionDiff extends PredictionDiff<Object, Object> {
	public TypelessPredictionDiff() {
		super();
	}

	public TypelessPredictionDiff(final List<?> groundTruths, final List<?> predictions) {
		super(groundTruths, predictions);
	}
}
