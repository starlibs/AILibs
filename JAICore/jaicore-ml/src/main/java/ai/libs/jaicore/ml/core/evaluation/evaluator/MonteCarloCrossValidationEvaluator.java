package ai.libs.jaicore.ml.core.evaluation.evaluator;

import java.util.Random;

import org.api4.java.ai.ml.classification.execution.ISupervisedLearnerMetric;
import org.api4.java.ai.ml.core.dataset.splitter.IRandomDatasetSplitter;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;

import ai.libs.jaicore.ml.core.dataset.splitter.RandomHoldoutSplitter;
import ai.libs.jaicore.ml.core.evaluation.ClassifierMetric;
import ai.libs.jaicore.ml.core.evaluation.splitsetgenerator.FixedDataSplitSetGenerator;
import ai.libs.jaicore.ml.core.evaluation.splitsetgenerator.MonteCarloCrossValidationSplitSetGenerator;

public class MonteCarloCrossValidationEvaluator extends TrainPredictionBasedClassifierEvaluator {

	public MonteCarloCrossValidationEvaluator(final ILabeledDataset<? extends ILabeledInstance> data, final int repeats, final double trainingPortion, final Random random) {
		this(data, new RandomHoldoutSplitter<>(trainingPortion), repeats, random, ClassifierMetric.MEAN_ERRORRATE);
	}

	public MonteCarloCrossValidationEvaluator(final ILabeledDataset<? extends ILabeledInstance> data, final int repeats, final double trainingPortion, final Random random, final ISupervisedLearnerMetric metric) {
		this(data, new RandomHoldoutSplitter<>(trainingPortion), repeats, random, metric);
	}

	public MonteCarloCrossValidationEvaluator(final ILabeledDataset<? extends ILabeledInstance> data, final IRandomDatasetSplitter<ILabeledDataset<ILabeledInstance>> datasetSplitter, final int repeats, final Random random,
			final ISupervisedLearnerMetric metric) {
		super(new FixedDataSplitSetGenerator(data, new MonteCarloCrossValidationSplitSetGenerator<>(datasetSplitter, repeats, random)), metric);
	}
}
