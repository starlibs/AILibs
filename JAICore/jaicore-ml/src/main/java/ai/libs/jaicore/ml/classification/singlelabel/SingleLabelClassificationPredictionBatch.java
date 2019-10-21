package ai.libs.jaicore.ml.classification.singlelabel;

import java.util.ArrayList;
import java.util.List;

import org.api4.java.ai.ml.classification.singlelabel.learner.ISingleLabelClassificationPrediction;
import org.api4.java.ai.ml.classification.singlelabel.learner.ISingleLabelClassificationPredictionBatch;
import org.api4.java.ai.ml.core.learner.algorithm.IPrediction;
import org.api4.java.ai.ml.core.learner.algorithm.IPredictionBatch;

public class SingleLabelClassificationPredictionBatch extends ArrayList<ISingleLabelClassificationPrediction> implements ISingleLabelClassificationPredictionBatch {

	/**
	 *
	 */
	private static final long serialVersionUID = 3575940001172802462L;

	public SingleLabelClassificationPredictionBatch(final IPredictionBatch batch) {
		List<? extends IPrediction> predictions = batch.getPredictions();
		int n = predictions.size();
		for (int i = 0; i < n; i++) {
			super.add((ISingleLabelClassificationPrediction) predictions.get(i));
		}
	}

	@Override
	public int getNumPredictions() {
		return this.size();
	}

	@Override
	public List<? extends ISingleLabelClassificationPrediction> getPredictions() {
		return this;
	}
}
