package ai.libs.jaicore.ml.core.evaluation.evaluator.splitevaluation;

import org.api4.java.ai.ml.classification.IClassifier;
import org.api4.java.ai.ml.classification.singlelabel.dataset.ISingleLabelClassificationDataset;
import org.api4.java.ai.ml.classification.singlelabel.dataset.ISingleLabelClassificationInstance;
import org.api4.java.ai.ml.core.evaluation.loss.ILossFunction;
import org.api4.java.ai.ml.core.learner.algorithm.IPredictionBatch;
import org.api4.java.common.attributedobjects.ObjectEvaluationFailedException;

import ai.libs.jaicore.ml.core.timeseries.util.WekaUtil;

/**
 * Basic implementation of the {@link AbstractSplitBasedClassifierEvaluator}. Uses the given loss function to compute loss on the given data. No extra steps are performed.
 *
 * @author jnowack
 *
 */
public class SimpleSLCSplitBasedClassifierEvaluator extends AbstractSplitBasedClassifierEvaluator<double[], ISingleLabelClassificationInstance, ISingleLabelClassificationDataset> {

	public SimpleSLCSplitBasedClassifierEvaluator(final ILossFunction<double[]> basicEvaluator) {
		super(basicEvaluator);
	}

	@Override
	public Double evaluateSplit(final IClassifier<ISingleLabelClassificationInstance, ISingleLabelClassificationDataset> classifier, final ISingleLabelClassificationDataset trainingData,
			final ISingleLabelClassificationDataset validationData) throws ObjectEvaluationFailedException, InterruptedException {

		try {
			classifier.fit(trainingData);
		} catch (InterruptedException e) {
			throw e;
		} catch (Exception e) {
			throw new ObjectEvaluationFailedException("Could not build model.", e);
		}

		try {
			IPredictionBatch prediction = classifier.predict(validationData);
			return this.getBasicEvaluator().calculateAvgMeasure(WekaUtil.getClassesAsList(validationData), predicted);
		} catch (InterruptedException e) {
			throw e;
		} catch (Exception e) {
			throw new ObjectEvaluationFailedException("Could not validate classifier.", e);
		}
	}

}
