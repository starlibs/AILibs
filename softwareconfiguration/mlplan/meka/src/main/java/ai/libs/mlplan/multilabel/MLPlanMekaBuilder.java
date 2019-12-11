package ai.libs.mlplan.multilabel;

import java.io.File;
import java.io.IOException;

import org.api4.java.ai.ml.classification.multilabel.evaluation.loss.IMultiLabelClassificationMeasure;

import ai.libs.jaicore.basic.FileUtil;
import ai.libs.jaicore.ml.classification.multilabel.loss.AutoMEKAGGPFitnessMeasureLoss;
import ai.libs.jaicore.ml.classification.multilabel.loss.InstanceWiseF1;
import ai.libs.jaicore.ml.core.evaluation.evaluator.factory.ProbabilisticMonteCarloCrossValidationEvaluatorFactory;
import ai.libs.jaicore.ml.core.evaluation.evaluator.splitevaluation.SimpleMLCSplitBasedClassifierEvaluator;
import ai.libs.jaicore.ml.weka.classification.learner.IWekaClassifier;
import ai.libs.jaicore.ml.weka.dataset.splitter.ArbitrarySplitter;
import ai.libs.mlplan.core.AbstractMLPlanBuilder;
import ai.libs.mlplan.core.ILearnerFactory;

public class MLPlanMekaBuilder extends AbstractMLPlanBuilder<IWekaClassifier, MLPlanMekaBuilder> {

	private static final File SSC = FileUtil.getExistingFileWithHighestPriority(ML2PlanMekaPathConfig.RES_SSC, ML2PlanMekaPathConfig.FS_SSC);
	private static final File PREFERRED_COMPONENTS = FileUtil.getExistingFileWithHighestPriority(ML2PlanMekaPathConfig.RES_PREFC, ML2PlanMekaPathConfig.FS_PREFC);

	/* Default configuration values. */
	private static final int SEARCH_NUM_MC_ITERATIONS = 5;
	private static final double SEARCH_TRAIN_FOLD_SIZE = 0.7;
	private static final int SELECTION_NUM_MC_ITERATIONS = 5;
	private static final double SELECTION_TRAIN_FOLD_SIZE = 0.7;
	private static final IMultiLabelClassificationMeasure LOSS_FUNCTION = new InstanceWiseF1();

	/* Default configurations */
	private static final String DEF_REQUESTED_HASCO_INTERFACE = "MLClassifier";
	private static final String DEF_PREFERRED_COMPONENT_NAME_PREFIX = "resolveMLClassifierWith";

	private static final ILearnerFactory<IWekaClassifier> CLASSIFIER_FACTORY = new MekaPipelineFactory();
	private static final ProbabilisticMonteCarloCrossValidationEvaluatorFactory DEF_SEARCH_PHASE_EVALUATOR = new ProbabilisticMonteCarloCrossValidationEvaluatorFactory().withNumMCIterations(SEARCH_NUM_MC_ITERATIONS)
			.withTrainFoldSize(SEARCH_TRAIN_FOLD_SIZE).withSplitBasedEvaluator(new SimpleMLCSplitBasedClassifierEvaluator(LOSS_FUNCTION)).withDatasetSplitter(new ArbitrarySplitter());
	private static final ProbabilisticMonteCarloCrossValidationEvaluatorFactory DEF_SELECTION_PHASE_EVALUATOR = new ProbabilisticMonteCarloCrossValidationEvaluatorFactory().withNumMCIterations(SELECTION_NUM_MC_ITERATIONS)
			.withTrainFoldSize(SELECTION_TRAIN_FOLD_SIZE).withSplitBasedEvaluator(new SimpleMLCSplitBasedClassifierEvaluator(LOSS_FUNCTION)).withDatasetSplitter(new ArbitrarySplitter());

	public MLPlanMekaBuilder() throws IOException {
		super();
		this.withSearchSpaceConfigFile(SSC);
		this.withRequestedInterface(DEF_REQUESTED_HASCO_INTERFACE);
		this.withPreferredComponentsFile(PREFERRED_COMPONENTS, DEF_PREFERRED_COMPONENT_NAME_PREFIX);
		this.withDatasetSplitterForSearchSelectionSplit(DEF_SELECTION_HOLDOUT_SPLITTER);
		this.withClassifierFactory(CLASSIFIER_FACTORY);
		this.withSearchPhaseEvaluatorFactory(DEF_SEARCH_PHASE_EVALUATOR);
		this.withSelectionPhaseEvaluatorFactory(DEF_SELECTION_PHASE_EVALUATOR);
	}

	/**
	 * Configures ML-Plan with the configuration as compared to AutoMEKA_GGP and GA-Auto-MLC.
	 * @return The builder object.
	 */
	public MLPlanMekaBuilder withAutoMEKADefaultConfiguration() {
		this.withPerformanceMeasure(new AutoMEKAGGPFitnessMeasureLoss());
		return this;
	}

	/**
	 * Sets the performance measure to evaluate a candidate solution's generalization performance. Caution: This resets the evaluators to MCCV for both search and selection phase if these are not already MCCVs.
	 * @param lossFunction The loss function to be used.
	 * @return The builder object.
	 */
	public MLPlanMekaBuilder withPerformanceMeasure(final IMultiLabelClassificationMeasure measure) {
		throw new UnsupportedOperationException("This method is not yet implemented and needs to be fixed.");
//		if (!(this.getSearchEvaluatorFactory() instanceof MonteCarloCrossValidationEvaluatorFactory)) {
//			this.withSearchPhaseEvaluatorFactory(new MonteCarloCrossValidationEvaluatorFactory().withDatasetSplitter(this.getDefaultDatasetSplitter()).withNumMCIterations(SEARCH_NUM_MC_ITERATIONS).withTrainFoldSize(SEARCH_TRAIN_FOLD_SIZE));
//		}
//		if (!(this.getSearchEvaluatorFactory() instanceof MonteCarloCrossValidationEvaluatorFactory)) {
//			this.withSearchPhaseEvaluatorFactory(
//					new MonteCarloCrossValidationEvaluatorFactory().withDatasetSplitter(this.getDefaultDatasetSplitter()).withNumMCIterations(SELECTION_NUM_MC_ITERATIONS).withTrainFoldSize(SELECTION_TRAIN_FOLD_SIZE));
//		}
//
//		((MonteCarloCrossValidationEvaluatorFactory) this.getSelectionEvaluatorFactory()).withSplitBasedEvaluator(new SimpleMLCSplitBasedClassifierEvaluator(lossFunction));
//		return this;
	}

}
