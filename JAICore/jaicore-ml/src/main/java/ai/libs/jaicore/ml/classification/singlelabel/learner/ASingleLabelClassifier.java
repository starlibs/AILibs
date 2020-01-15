package ai.libs.jaicore.ml.classification.singlelabel.learner;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.api4.java.ai.ml.classification.singlelabel.evaluation.ISingleLabelClassification;
import org.api4.java.ai.ml.classification.singlelabel.evaluation.ISingleLabelClassificationPredictionBatch;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.exception.PredictionException;

import ai.libs.jaicore.ml.classification.singlelabel.SingleLabelClassificationPredictionBatch;
import ai.libs.jaicore.ml.core.learner.ASupervisedLearner;

public abstract class ASingleLabelClassifier extends ASupervisedLearner<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>, ISingleLabelClassification, ISingleLabelClassificationPredictionBatch> {

	protected ASingleLabelClassifier(final Map<String, Object> config) {
		super(config);
	}

	protected ASingleLabelClassifier() {
		super();
	}

	@Override
	public ISingleLabelClassificationPredictionBatch predict(final ILabeledInstance[] dTest) throws PredictionException, InterruptedException {
		List<ISingleLabelClassification> batchList = new LinkedList<>();
		for (ILabeledInstance i : dTest) {
			batchList.add(this.predict(i));
		}
		return new SingleLabelClassificationPredictionBatch(batchList);
	}
}
