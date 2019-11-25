package ai.libs.jaicore.ml.core.evaluation.evaluator;

import java.util.Arrays;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.evaluation.loss.IMeasure;

import ai.libs.jaicore.ml.core.dataset.splitter.DatasetSplitSet;
import ai.libs.jaicore.ml.core.evaluation.SingleSplitEvaluationMetric;
import ai.libs.jaicore.ml.core.evaluation.splitsetgenerator.ConstantSplitSetGenerator;

public class FixedSplitClassifierEvaluator extends ExecutionBasedClassifierEvaluator {

	public FixedSplitClassifierEvaluator(final ILabeledDataset<ILabeledInstance> train, final ILabeledDataset<ILabeledInstance> validate, final IMeasure lossFunction) {
		super(new ConstantSplitSetGenerator<ILabeledInstance, ILabeledDataset<ILabeledInstance>>(new DatasetSplitSet<ILabeledDataset<ILabeledInstance>>(Arrays.asList(Arrays.asList(train, validate)))),
				new SingleSplitEvaluationMetric(lossFunction));
	}
}
