package ai.libs.jaicore.ml.classification.singlelabel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.api4.java.ai.ml.classification.singlelabel.evaluation.ISingleLabelClassification;
import org.api4.java.ai.ml.classification.singlelabel.evaluation.ISingleLabelClassificationPredictionBatch;

public class SingleLabelClassificationPredictionBatch extends ArrayList<ISingleLabelClassification> implements ISingleLabelClassificationPredictionBatch {

	/**
	 *
	 */
	private static final long serialVersionUID = 3575940001172802462L;

	public SingleLabelClassificationPredictionBatch(final Collection<ISingleLabelClassification> predictions) {
		this.addAll(predictions);
	}

	@Override
	public int getNumPredictions() {
		return this.size();
	}

	@Override
	public List<? extends ISingleLabelClassification> getPredictions() {
		return this;
	}
}
