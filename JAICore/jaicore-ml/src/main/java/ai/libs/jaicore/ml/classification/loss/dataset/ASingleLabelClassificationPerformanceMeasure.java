package ai.libs.jaicore.ml.classification.loss.dataset;

import org.api4.java.ai.ml.classification.singlelabel.evaluation.ISingleLabelClassification;
import org.api4.java.ai.ml.core.evaluation.supervised.loss.IDeterministicPredictionPerformanceMeasure;

public abstract class ASingleLabelClassificationPerformanceMeasure extends APredictionPerformanceMeasure<Integer, ISingleLabelClassification> implements IDeterministicPredictionPerformanceMeasure<Integer, ISingleLabelClassification> {

	public ASingleLabelClassificationPerformanceMeasure() {
		super();
	}

}
