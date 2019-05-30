package de.upb.crc901.mlplan.core;

import java.io.File;
import java.io.IOException;

import de.upb.crc901.mlplan.multiclass.wekamlplan.IClassifierFactory;
import de.upb.crc901.mlplan.multilabel.MekaPipelineFactory;
import jaicore.basic.FileUtil;
import jaicore.ml.core.evaluation.measure.IMeasure;
import jaicore.ml.core.evaluation.measure.multilabel.AutoMEKAGGPFitnessMeasureLoss;
import jaicore.ml.core.evaluation.measure.multilabel.InstanceWiseF1AsLoss;
import jaicore.ml.evaluation.evaluators.weka.factory.MonteCarloCrossValidationEvaluatorFactory;
import jaicore.ml.evaluation.evaluators.weka.splitevaluation.SimpleMLCSplitBasedClassifierEvaluator;
import jaicore.ml.weka.dataset.splitter.ArbitrarySplitter;
import jaicore.ml.weka.dataset.splitter.IDatasetSplitter;

public class MLPlanMekaBuilder extends AbstractMLPlanBuilder {

	private static final String RES_SSC_MEKA_COMPLETE = "automl/searchmodels/meka/meka-multilabel.json";
	private static final String FS_SSC_MEKA_COMPLETE = "conf/mlplan-meka.json";

	private static final String RES_PREFC_MEKA = "mlplan/meka-preferenceList.txt";
	private static final String FS_PREFC_MEKA = "conf/mlpan-meka-preferenceList.txt";

	/* Default configuration values. */
	private static final int SEARCH_NUM_MC_ITERATIONS = 5;
	private static final double SEARCH_TRAIN_FOLD_SIZE = 0.7;
	private static final int SELECTION_NUM_MC_ITERATIONS = 5;
	private static final double SELECTION_TRAIN_FOLD_SIZE = 0.7;
	private static final IMeasure<double[], Double> LOSS_FUNCTION = new InstanceWiseF1AsLoss();

	/* Default configurations */
	private static final String DEF_REQUESTED_HASCO_INTERFACE = "MLClassifier";
	private static final IDatasetSplitter DEF_SELECTION_HOLDOUT_SPLITTER = new ArbitrarySplitter();
	private static final File DEF_SEARCH_SPACE_CONFIG = FileUtil.getExistingFileWithHighestPriority(RES_SSC_MEKA_COMPLETE, FS_SSC_MEKA_COMPLETE);
	private static final File DEF_PREFERRED_COMPONENTS_CONFIG = FileUtil.getExistingFileWithHighestPriority(RES_PREFC_MEKA, FS_PREFC_MEKA);
	private static final IClassifierFactory CLASSIFIER_FACTORY = new MekaPipelineFactory();
	private static final MonteCarloCrossValidationEvaluatorFactory DEF_SEARCH_PHASE_EVALUATOR = new MonteCarloCrossValidationEvaluatorFactory().withNumMCIterations(SEARCH_NUM_MC_ITERATIONS).withTrainFoldSize(SEARCH_TRAIN_FOLD_SIZE)
			.withSplitBasedEvaluator(new SimpleMLCSplitBasedClassifierEvaluator(LOSS_FUNCTION)).withDatasetSplitter(new ArbitrarySplitter());
	private static final MonteCarloCrossValidationEvaluatorFactory DEF_SELECTION_PHASE_EVALUATOR = new MonteCarloCrossValidationEvaluatorFactory().withNumMCIterations(SELECTION_NUM_MC_ITERATIONS).withTrainFoldSize(SELECTION_TRAIN_FOLD_SIZE)
			.withSplitBasedEvaluator(new SimpleMLCSplitBasedClassifierEvaluator(LOSS_FUNCTION)).withDatasetSplitter(new ArbitrarySplitter());

	public MLPlanMekaBuilder() throws IOException {
		super();
		this.withSearchSpaceConfigFile(DEF_SEARCH_SPACE_CONFIG);
		this.withRequestedInterface(DEF_REQUESTED_HASCO_INTERFACE);
		this.withPreferredComponentsFile(DEF_PREFERRED_COMPONENTS_CONFIG);
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
	public MLPlanMekaBuilder withPerformanceMeasure(final IMeasure<double[], Double> lossFunction) {
		if (!(this.getSearchEvaluatorFactory() instanceof MonteCarloCrossValidationEvaluatorFactory)) {
			this.withSearchPhaseEvaluatorFactory(new MonteCarloCrossValidationEvaluatorFactory().withDatasetSplitter(this.getDefaultDatasetSplitter()).withNumMCIterations(SEARCH_NUM_MC_ITERATIONS).withTrainFoldSize(SEARCH_TRAIN_FOLD_SIZE));
		}
		if (!(this.getSearchEvaluatorFactory() instanceof MonteCarloCrossValidationEvaluatorFactory)) {
			this.withSearchPhaseEvaluatorFactory(
					new MonteCarloCrossValidationEvaluatorFactory().withDatasetSplitter(this.getDefaultDatasetSplitter()).withNumMCIterations(SELECTION_NUM_MC_ITERATIONS).withTrainFoldSize(SELECTION_TRAIN_FOLD_SIZE));
		}

		((MonteCarloCrossValidationEvaluatorFactory) this.getSelectionEvaluatorFactory()).withSplitBasedEvaluator(new SimpleMLCSplitBasedClassifierEvaluator(lossFunction));
		return this;
	}

	protected IDatasetSplitter getDefaultDatasetSplitter() {
		return new ArbitrarySplitter();
	}

}
