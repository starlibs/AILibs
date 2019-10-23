package ai.libs.jaicore.ml.classification.singlelabel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.api4.java.ai.ml.classification.singlelabel.learner.ISingleLabelClassificationPrediction;
import org.api4.java.ai.ml.classification.singlelabel.learner.ISingleLabelClassificationPredictionBatch;

public class SingleLabelClassificationPredictionBatch extends ArrayList<ISingleLabelClassificationPrediction> implements ISingleLabelClassificationPredictionBatch {

	/**
	 *
	 */
	private static final long serialVersionUID = 3575940001172802462L;

	public SingleLabelClassificationPredictionBatch(final Collection<ISingleLabelClassificationPrediction> predictions) {
		this.addAll(predictions);
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
