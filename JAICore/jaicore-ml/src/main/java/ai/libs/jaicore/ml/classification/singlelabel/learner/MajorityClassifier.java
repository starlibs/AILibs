package ai.libs.jaicore.ml.classification.singlelabel.learner;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.api4.java.ai.ml.classification.IClassifier;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.evaluation.IPrediction;
import org.api4.java.ai.ml.core.evaluation.IPredictionBatch;
import org.api4.java.ai.ml.core.exception.PredictionException;
import org.api4.java.ai.ml.core.exception.TrainingException;

import ai.libs.jaicore.ml.core.evaluation.Prediction;
import ai.libs.jaicore.ml.core.evaluation.PredictionBatch;
import ai.libs.jaicore.ml.core.learner.ASupervisedLearner;

public class MajorityClassifier extends ASupervisedLearner<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>, IPrediction, IPredictionBatch> implements IClassifier {

	private Object majorityLabel;

	@Override
	public void fit(final ILabeledDataset<? extends ILabeledInstance> dTrain) throws TrainingException, InterruptedException {
		Map<Object, Integer> labelCounter = new HashMap<>();
		Objects.requireNonNull(dTrain);
		if (dTrain.isEmpty()) {
			throw new IllegalArgumentException("Cannot train majority classifier with empty training set.");
		}
		for (ILabeledInstance i : dTrain) {
			labelCounter.put(i.getLabel(), labelCounter.computeIfAbsent(i.getLabel(), t -> 0) + 1);
		}
		this.majorityLabel = labelCounter.keySet().stream().max((l1, l2) -> Integer.compare(labelCounter.get(l1), labelCounter.get(l2))).get();
	}

	@Override
	public IPrediction predict(final ILabeledInstance xTest) throws PredictionException, InterruptedException {
		return new Prediction(this.majorityLabel);
	}

	@Override
	public IPredictionBatch predict(final ILabeledInstance[] dTest) throws PredictionException, InterruptedException {
		IPrediction[] predictions = new IPrediction[dTest.length];
		for (int i = 0; i < dTest.length; i++) {
			predictions[i] = this.predict(dTest[i]);
		}
		return new PredictionBatch(predictions);
	}
}
