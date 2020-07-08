package ai.libs.mlplan.multilabel.mekamlplan;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.api4.java.ai.ml.classification.multilabel.evaluation.loss.IMultiLabelClassificationPredictionPerformanceMeasure;
import org.api4.java.ai.ml.core.dataset.splitter.IFoldSizeConfigurableRandomDatasetSplitter;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;

import ai.libs.jaicore.ml.classification.multilabel.evaluation.loss.AutoMEKAGGPFitnessMeasureLoss;
import ai.libs.jaicore.ml.classification.multilabel.evaluation.loss.InstanceWiseF1;
import ai.libs.jaicore.ml.classification.multilabel.learner.IMekaClassifier;
import ai.libs.jaicore.ml.core.dataset.splitter.RandomHoldoutSplitter;
import ai.libs.jaicore.ml.core.evaluation.evaluator.factory.ISupervisedLearnerEvaluatorFactory;
import ai.libs.jaicore.ml.core.evaluation.evaluator.factory.MonteCarloCrossValidationEvaluatorFactory;
import ai.libs.mlplan.core.AbstractMLPlanBuilder;
import ai.libs.mlplan.core.ILearnerFactory;
import ai.libs.mlplan.multiclass.wekamlplan.EMLPlanWekaProblemType;

public class ML2PlanMekaBuilder extends AbstractMLPlanBuilder<IMekaClassifier, ML2PlanMekaBuilder> {

	private static final ILearnerFactory<IMekaClassifier> DEF_CLASSIFIER_FACTORY = new MekaPipelineFactory();
	private static final IFoldSizeConfigurableRandomDatasetSplitter<ILabeledDataset<?>> DEF_SEARCH_SELECT_SPLITTER = new RandomHoldoutSplitter<>(new Random(0), DEFAULT_SEARCH_TRAIN_FOLD_SIZE);

	private static final IMultiLabelClassificationPredictionPerformanceMeasure DEFAULT_PERFORMANCE_MEASURE = new InstanceWiseF1();
	private static final MonteCarloCrossValidationEvaluatorFactory DEF_SEARCH_PHASE_EVALUATOR = new MonteCarloCrossValidationEvaluatorFactory().withNumMCIterations(DEFAULT_SEARCH_NUM_MC_ITERATIONS)
			.withTrainFoldSize(DEFAULT_SEARCH_TRAIN_FOLD_SIZE).withMeasure(DEFAULT_PERFORMANCE_MEASURE);
	private static final MonteCarloCrossValidationEvaluatorFactory DEF_SELECTION_PHASE_EVALUATOR = new MonteCarloCrossValidationEvaluatorFactory().withNumMCIterations(DEFAULT_SELECTION_NUM_MC_ITERATIONS)
			.withTrainFoldSize(DEFAULT_SELECTION_TRAIN_FOLD_SIZE).withMeasure(DEFAULT_PERFORMANCE_MEASURE);

	public ML2PlanMekaBuilder() throws IOException {
		super(EMLPlanWekaProblemType.CLASSIFICATION_MULTILABEL);
		this.withClassifierFactory(DEF_CLASSIFIER_FACTORY);
		this.withSearchPhaseEvaluatorFactory(DEF_SEARCH_PHASE_EVALUATOR);
		this.withSelectionPhaseEvaluatorFactory(DEF_SELECTION_PHASE_EVALUATOR);
		this.withPerformanceMeasure(DEFAULT_PERFORMANCE_MEASURE);
		this.withDatasetSplitterForSearchSelectionSplit(DEF_SEARCH_SELECT_SPLITTER);
	}

	/**
	 * Configures ML-Plan with the configuration as compared to AutoMEKA_GGP and GA-Auto-MLC.
	 * @return The builder object.
	 */
	public ML2PlanMekaBuilder withAutoMEKADefaultConfiguration() {
		this.withPerformanceMeasure(new AutoMEKAGGPFitnessMeasureLoss());
		return this;
	}

	/**
	 * Sets the performance measure to evaluate a candidate solution's generalization performance. Caution: This resets the evaluators to MCCV for both search and selection phase if these are not already MCCVs.
	 * @param lossFunction The loss function to be used.
	 * @return The builder object.
	 */
	public ML2PlanMekaBuilder withPerformanceMeasure(final IMultiLabelClassificationPredictionPerformanceMeasure measure) {
		List<ISupervisedLearnerEvaluatorFactory> phaseList = Arrays.asList(this.getSearchEvaluatorFactory(), this.getSelectionEvaluatorFactory());
		for (ISupervisedLearnerEvaluatorFactory factory : phaseList) {
			if (factory instanceof MonteCarloCrossValidationEvaluatorFactory) {
				((MonteCarloCrossValidationEvaluatorFactory) factory).withMeasure(measure);
			}
		}
		return this;
	}

	@Override
	public ML2PlanMekaBuilder getSelf() {
		return this;
	}

}
