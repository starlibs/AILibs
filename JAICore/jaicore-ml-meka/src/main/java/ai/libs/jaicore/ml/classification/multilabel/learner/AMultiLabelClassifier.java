package ai.libs.jaicore.ml.classification.multilabel.learner;

import java.util.ArrayList;
import java.util.List;

import org.api4.java.ai.ml.classification.multilabel.evaluation.IMultiLabelClassification;
import org.api4.java.ai.ml.classification.multilabel.evaluation.IMultiLabelClassificationPredictionBatch;
import org.api4.java.ai.ml.classification.multilabel.learner.IMultiLabelClassifier;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.exception.PredictionException;

import ai.libs.jaicore.ml.classification.multilabel.MultiLabelClassificationPredictionBatch;
import ai.libs.jaicore.ml.core.learner.ASupervisedLearner;

public abstract class AMultiLabelClassifier extends ASupervisedLearner<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>, IMultiLabelClassification, IMultiLabelClassificationPredictionBatch> implements IMultiLabelClassifier {

	@Override
	public IMultiLabelClassificationPredictionBatch predict(final ILabeledInstance[] dTest) throws PredictionException, InterruptedException {
		List<IMultiLabelClassification> batch = new ArrayList<>();
		for (ILabeledInstance instance : dTest) {
			batch.add(this.predict(instance));
		}
		return new MultiLabelClassificationPredictionBatch(batch);
	}
}
