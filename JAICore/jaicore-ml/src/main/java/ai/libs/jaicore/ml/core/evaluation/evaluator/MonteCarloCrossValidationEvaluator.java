package ai.libs.jaicore.ml.core.evaluation.evaluator;

import java.util.Random;

import org.api4.java.ai.ml.classification.execution.IClassifierMetric;
import org.api4.java.ai.ml.core.dataset.splitter.IRandomDatasetSplitter;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;

import ai.libs.jaicore.ml.core.dataset.splitter.RandomHoldoutSplitter;
import ai.libs.jaicore.ml.core.evaluation.ClassifierMetric;
import ai.libs.jaicore.ml.core.evaluation.splitsetgenerator.FixedDataSplitSetGenerator;
import ai.libs.jaicore.ml.core.evaluation.splitsetgenerator.MonteCarloCrossValidationSplitSetGenerator;

public class MonteCarloCrossValidationEvaluator extends ExecutionBasedClassifierEvaluator {

	public <I extends ILabeledInstance, D extends ILabeledDataset<I>> MonteCarloCrossValidationEvaluator(final D data, final int repeats, final double trainingPortion, final Random random) {
		this(data, new RandomHoldoutSplitter<>(trainingPortion), repeats, random, ClassifierMetric.MEAN_ERRORRATE);
	}

	public <I extends ILabeledInstance, D extends ILabeledDataset<I>> MonteCarloCrossValidationEvaluator(final D data, final IRandomDatasetSplitter<I, D> datasetSplitter, final int repeats, final Random random, final IClassifierMetric metric) {
		super(new FixedDataSplitSetGenerator<>(data, new MonteCarloCrossValidationSplitSetGenerator<>(datasetSplitter, repeats, random)), metric);
	}
}
