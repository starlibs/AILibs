package ai.libs.jaicore.ml.core.evaluation.evaluator;

import java.util.List;

import org.api4.java.ai.ml.classification.IClassifier;
import org.api4.java.ai.ml.classification.execution.ClassifierExecutionFailedException;
import org.api4.java.ai.ml.classification.execution.IClassifierExecutor;
import org.api4.java.ai.ml.classification.execution.IClassifierRunReport;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.exception.PredictionException;
import org.api4.java.ai.ml.core.exception.TrainingException;

public class ClassifierExecutor<I extends ILabeledInstance, D extends ILabeledDataset<I>> implements IClassifierExecutor<D> {

	@Override
	public IClassifierRunReport execute(final IClassifier<?, D> classifier, final D train, final D test) throws ClassifierExecutionFailedException {
		try {
			long startTrainTime = System.currentTimeMillis();
			classifier.fit(train);
			long endTrainTime = System.currentTimeMillis();
			List<?> predictions = classifier.predict(test);
			long endTestTime = System.currentTimeMillis();

			/* create difference table */
			int numTestInstances = test.size();
			PredictionDiff<Object> diff = new PredictionDiff<>();
			for (int j = 0; j < numTestInstances; j++) {
				diff.addPair(predictions.get(j), test.get(j).getLabel());
			}

			/* append report */
			return new RunReport((int) (endTrainTime - startTrainTime), (int) (endTestTime - endTrainTime), diff);
		} catch (PredictionException | InterruptedException | TrainingException e) {
			throw new ClassifierExecutionFailedException(e);
		}
	}
}
