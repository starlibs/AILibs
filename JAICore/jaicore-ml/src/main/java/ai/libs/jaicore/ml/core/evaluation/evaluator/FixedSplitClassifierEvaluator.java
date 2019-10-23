package ai.libs.jaicore.ml.core.evaluation.evaluator;

import java.util.Arrays;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.evaluation.loss.IMeasure;

import ai.libs.jaicore.ml.core.dataset.DatasetSplitSet;
import ai.libs.jaicore.ml.core.evaluation.SingleSplitEvaluationMetric;
import ai.libs.jaicore.ml.core.evaluation.splitsetgenerator.ConstantSplitSetGenerator;

public class FixedSplitClassifierEvaluator extends ExecutionBasedClassifierEvaluator {

	public <I extends ILabeledInstance, D extends ILabeledDataset<I>> FixedSplitClassifierEvaluator(final D train, final D validate, final IMeasure lossFunction) {
		super(new ConstantSplitSetGenerator<>(new DatasetSplitSet<>(Arrays.asList(Arrays.asList(train, validate)))), new SingleSplitEvaluationMetric(lossFunction));
	}
}
