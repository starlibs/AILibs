package ai.libs.jaicore.ml.core.evaluation.evaluator;

import java.util.Arrays;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.evaluation.supervised.loss.IDeterministicPredictionPerformanceMeasure;

import ai.libs.jaicore.ml.core.dataset.splitter.DatasetSplitSet;
import ai.libs.jaicore.ml.core.evaluation.SingleEvaluationAggregatedMeasure;
import ai.libs.jaicore.ml.core.evaluation.splitsetgenerator.ConstantSplitSetGenerator;

public class FixedSplitClassifierEvaluator extends TrainPredictionBasedClassifierEvaluator {

	public FixedSplitClassifierEvaluator(final ILabeledDataset<? extends ILabeledInstance> train, final ILabeledDataset<? extends ILabeledInstance> validate, final IDeterministicPredictionPerformanceMeasure<?, ?> lossFunction) {
		super(new ConstantSplitSetGenerator<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>>(new DatasetSplitSet<ILabeledDataset<? extends ILabeledInstance>>(Arrays.asList(Arrays.asList(train, validate)))),
				new SingleEvaluationAggregatedMeasure(lossFunction));
	}
}
