package ai.libs.jaicore.ml.classification.singlelabel.learner;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.api4.java.ai.ml.classification.singlelabel.dataset.ISingleLabelClassificationDataset;
import org.api4.java.ai.ml.classification.singlelabel.dataset.ISingleLabelClassificationInstance;
import org.api4.java.ai.ml.classification.singlelabel.learner.ISingleLabelClassification;
import org.api4.java.ai.ml.classification.singlelabel.learner.ISingleLabelClassificationPredictionBatch;
import org.api4.java.ai.ml.core.exception.PredictionException;

import ai.libs.jaicore.ml.classification.singlelabel.SingleLabelClassificationPredictionBatch;
import ai.libs.jaicore.ml.core.learner.ASupervisedLearner;

public abstract class ASingleLabelClassifier extends ASupervisedLearner<ISingleLabelClassificationInstance, ISingleLabelClassificationDataset, ISingleLabelClassification, ISingleLabelClassificationPredictionBatch> {

	protected ASingleLabelClassifier(final Map<String, Object> config) {
		super(config);
	}

	protected ASingleLabelClassifier() {
		super();
	}

	@Override
	public ISingleLabelClassificationPredictionBatch predict(final ISingleLabelClassificationInstance[] dTest) throws PredictionException, InterruptedException {
		List<ISingleLabelClassification> batchList = new LinkedList<>();
		for (ISingleLabelClassificationInstance i : dTest) {
			batchList.add(this.predict(i));
		}
		return new SingleLabelClassificationPredictionBatch(batchList);
	}
}
