package ai.libs.jaicore.ml.classification.singlelabel.learner;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.api4.java.ai.ml.classification.IClassifier;
import org.api4.java.ai.ml.core.dataset.schema.attribute.ICategoricalAttribute;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.evaluation.IPrediction;
import org.api4.java.ai.ml.core.evaluation.IPredictionBatch;
import org.api4.java.ai.ml.core.exception.PredictionException;
import org.api4.java.ai.ml.core.exception.TrainingException;

import ai.libs.jaicore.ml.classification.singlelabel.SingleLabelClassification;
import ai.libs.jaicore.ml.core.evaluation.PredictionBatch;
import ai.libs.jaicore.ml.core.learner.ASupervisedLearner;

public class MajorityClassifier extends ASupervisedLearner<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>, IPrediction, IPredictionBatch> implements IClassifier {

	private Object majorityLabel;
	private double[] prediction;

	@Override
	public void fit(final ILabeledDataset<? extends ILabeledInstance> dTrain) throws TrainingException, InterruptedException {
		if (!(dTrain.getLabelAttribute() instanceof ICategoricalAttribute)) {
			throw new IllegalArgumentException("The label attribute of the given data is of type " + dTrain.getLabelAttribute().getClass() + ", but the " + MajorityClassifier.class.getName() + " can only work with categorical labels.");
		}
		Map<Object, Integer> labelCounter = new HashMap<>();
		Objects.requireNonNull(dTrain);
		if (dTrain.isEmpty()) {
			throw new IllegalArgumentException("Cannot train majority classifier with empty training set.");
		}
		for (ILabeledInstance i : dTrain) {
			labelCounter.put(i.getLabel(), labelCounter.computeIfAbsent(i.getLabel(), t -> 0) + 1);
		}
		this.majorityLabel = labelCounter.keySet().stream().max((l1, l2) -> Integer.compare(labelCounter.get(l1), labelCounter.get(l2))).get();
		ICategoricalAttribute labelAtt = ((ICategoricalAttribute) dTrain.getLabelAttribute());
		this.prediction = new double[labelAtt.getLabels().size()];
		this.prediction[labelAtt.getAsAttributeValue(this.majorityLabel).getValue()] = 1.0;
	}

	@Override
	public IPrediction predict(final ILabeledInstance xTest) throws PredictionException, InterruptedException {
		return new SingleLabelClassification(this.prediction);
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
