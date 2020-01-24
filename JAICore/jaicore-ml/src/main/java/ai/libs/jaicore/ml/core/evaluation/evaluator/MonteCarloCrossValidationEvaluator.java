package ai.libs.jaicore.ml.core.evaluation.evaluator;

import java.util.Random;

import org.api4.java.ai.ml.core.dataset.splitter.IRandomDatasetSplitter;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.evaluation.execution.IAggregatedPredictionPerformanceMeasure;

import ai.libs.jaicore.ml.classification.loss.dataset.EAggregatedClassifierMetric;
import ai.libs.jaicore.ml.core.dataset.splitter.RandomHoldoutSplitter;
import ai.libs.jaicore.ml.core.evaluation.splitsetgenerator.FixedDataSplitSetGenerator;
import ai.libs.jaicore.ml.core.evaluation.splitsetgenerator.MonteCarloCrossValidationSplitSetGenerator;

public class MonteCarloCrossValidationEvaluator extends TrainPredictionBasedClassifierEvaluator {

	private final IRandomDatasetSplitter<ILabeledDataset<ILabeledInstance>> datasetSplitter;
	private final int repeats;
	private final Random random;
	private final IAggregatedPredictionPerformanceMeasure metric;

	public MonteCarloCrossValidationEvaluator(final ILabeledDataset<? extends ILabeledInstance> data, final int repeats, final double trainingPortion, final Random random) {
		this(data, new RandomHoldoutSplitter<>(trainingPortion), repeats, random, EAggregatedClassifierMetric.MEAN_ERRORRATE);
	}

	public MonteCarloCrossValidationEvaluator(final ILabeledDataset<? extends ILabeledInstance> data, final int repeats, final double trainingPortion, final Random random, final IAggregatedPredictionPerformanceMeasure metric) {
		this(data, new RandomHoldoutSplitter<>(trainingPortion), repeats, random, metric);
	}

	public MonteCarloCrossValidationEvaluator(final ILabeledDataset<? extends ILabeledInstance> data, final IRandomDatasetSplitter<ILabeledDataset<ILabeledInstance>> datasetSplitter, final int repeats, final Random random,
			final IAggregatedPredictionPerformanceMeasure metric) {
		super(new FixedDataSplitSetGenerator(data, new MonteCarloCrossValidationSplitSetGenerator<>(datasetSplitter, repeats, random)), metric);
		this.datasetSplitter = datasetSplitter;
		this.repeats = repeats;
		this.random = random;
		this.metric = metric;
	}

	@Override
	public String toString() {
		return "MonteCarloCrossValidationEvaluator [splitter = " + this.datasetSplitter + ", repeats = " + this.repeats + ", Random = " + this.random + ", metric = " + this.metric.getBaseMeasure() + "]";
	}
}
