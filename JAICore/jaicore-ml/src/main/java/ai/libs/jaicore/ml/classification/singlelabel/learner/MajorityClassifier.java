package ai.libs.jaicore.ml.classification.singlelabel.learner;

import java.util.HashMap;
import java.util.Map;

import org.api4.java.ai.ml.classification.singlelabel.dataset.ISingleLabelClassificationDataset;
import org.api4.java.ai.ml.classification.singlelabel.dataset.ISingleLabelClassificationInstance;
import org.api4.java.ai.ml.classification.singlelabel.learner.ISingleLabelClassificationPrediction;
import org.api4.java.ai.ml.classification.singlelabel.learner.ISingleLabelClassifier;
import org.api4.java.ai.ml.core.exception.PredictionException;
import org.api4.java.ai.ml.core.exception.TrainingException;

import ai.libs.jaicore.ml.classification.singlelabel.SingleLabelClassificationPrediction;

public class MajorityClassifier extends ASingleLabelClassifier implements ISingleLabelClassifier {

	private String majorityLabel;

	@Override
	public void fit(final ISingleLabelClassificationDataset dTrain) throws TrainingException, InterruptedException {
		Map<String, Integer> labelCounter = new HashMap<>();
		for (ISingleLabelClassificationInstance i : dTrain) {
			labelCounter.put(i.getLabel(), labelCounter.computeIfAbsent(i.getLabel(), t -> 0) + 1);
		}

		String mostFrequentClass = null;
		for (String label : labelCounter.keySet()) {
			if (mostFrequentClass == null || labelCounter.get(label) > labelCounter.get(mostFrequentClass)) {
				mostFrequentClass = label;
			}
		}

		this.majorityLabel = mostFrequentClass;
	}

	@Override
	public ISingleLabelClassificationPrediction predict(final ISingleLabelClassificationInstance xTest) throws PredictionException, InterruptedException {
		return new SingleLabelClassificationPrediction(this.majorityLabel);
	}

}
