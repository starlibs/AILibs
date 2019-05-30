package de.upb.crc901.mlplan.core;

import java.io.File;
import java.io.IOException;

import de.upb.crc901.mlplan.multiclass.MLPlanClassifierConfig;
import de.upb.crc901.mlplan.multiclass.wekamlplan.IClassifierFactory;
import de.upb.crc901.mlplan.multiclass.wekamlplan.weka.WEKAPipelineFactory;
import jaicore.basic.FileUtil;
import jaicore.basic.MathExt;
import jaicore.basic.ResourceUtil;
import jaicore.ml.core.dataset.sampling.inmemory.ASamplingAlgorithm;
import jaicore.ml.core.dataset.sampling.inmemory.factories.interfaces.ISamplingAlgorithmFactory;
import jaicore.ml.core.dataset.weka.WekaInstances;
import jaicore.ml.core.evaluation.measure.singlelabel.ZeroOneLoss;
import jaicore.ml.evaluation.evaluators.weka.factory.LearningCurveExtrapolationEvaluatorFactory;
import jaicore.ml.evaluation.evaluators.weka.factory.MonteCarloCrossValidationEvaluatorFactory;
import jaicore.ml.evaluation.evaluators.weka.splitevaluation.SimpleSLCSplitBasedClassifierEvaluator;
import jaicore.ml.learningcurve.extrapolation.LearningCurveExtrapolationMethod;
import jaicore.ml.weka.dataset.splitter.IDatasetSplitter;
import jaicore.ml.weka.dataset.splitter.MulticlassClassStratifiedSplitter;

public class MLPlanWekaBuilder extends AbstractMLPlanSingleLabelBuilder {

	private static final String RES_SSC_TINY_WEKA = "resources/automl/searchmodels/weka/tinytest.json";
	private static final String RES_SSC_WEKA_COMPLETE = "resources/automl/searchmodels/weka/weka-all-autoweka.json";
	private static final String FS_SSC_WEKA = "conf/mlplan-weka.json";

	private static final String RES_PREFERRED_COMPONENTS = "mlplan/weka-preferenceList.txt";
	private static final String FS_PREFERRED_COMPONENTS = "conf/mlpan-weka-preferenceList.txt";

	/* Default configuration values. */
	private static final String DEF_REQUESTED_HASCO_INTERFACE = "AbstractClassifier";
	private static final IDatasetSplitter DEF_SELECTION_HOLDOUT_SPLITTER = new MulticlassClassStratifiedSplitter();
	private static final IClassifierFactory DEF_CLASSIFIER_FACTORY = new WEKAPipelineFactory();
	private static final File DEF_PREFERRED_COMPONENTS = FileUtil.getExistingFileWithHighestPriority(RES_PREFERRED_COMPONENTS, FS_PREFERRED_COMPONENTS);
	private static final File DEF_SEARCH_SPACE_CONFIG = FileUtil.getExistingFileWithHighestPriority(RES_SSC_WEKA_COMPLETE, FS_SSC_WEKA);
	private static final MonteCarloCrossValidationEvaluatorFactory DEF_SEARCH_PHASE_EVALUATOR = new MonteCarloCrossValidationEvaluatorFactory().withNumMCIterations(SEARCH_NUM_MC_ITERATIONS).withTrainFoldSize(SEARCH_TRAIN_FOLD_SIZE)
			.withSplitBasedEvaluator(new SimpleSLCSplitBasedClassifierEvaluator(LOSS_FUNCTION)).withDatasetSplitter(new MulticlassClassStratifiedSplitter());
	private static final MonteCarloCrossValidationEvaluatorFactory DEF_SELECTION_PHASE_EVALUATOR = new MonteCarloCrossValidationEvaluatorFactory().withNumMCIterations(SELECTION_NUM_MC_ITERATIONS).withTrainFoldSize(SELECTION_TRAIN_FOLD_SIZE)
			.withSplitBasedEvaluator(new SimpleSLCSplitBasedClassifierEvaluator(LOSS_FUNCTION)).withDatasetSplitter(new MulticlassClassStratifiedSplitter());

	public MLPlanWekaBuilder() throws IOException {
		super();
		this.withSearchSpaceConfigFile(DEF_SEARCH_SPACE_CONFIG);
		this.withPreferredComponentsFile(DEF_PREFERRED_COMPONENTS);
		this.withRequestedInterface(DEF_REQUESTED_HASCO_INTERFACE);
		this.withClassifierFactory(DEF_CLASSIFIER_FACTORY);
		this.withDatasetSplitterForSearchSelectionSplit(DEF_SELECTION_HOLDOUT_SPLITTER);
		this.withSearchPhaseEvaluatorFactory(DEF_SEARCH_PHASE_EVALUATOR);
		this.withSelectionPhaseEvaluatorFactory(DEF_SELECTION_PHASE_EVALUATOR);
		this.setPerformanceMeasureName(LOSS_FUNCTION.getClass().getSimpleName());

		// /* configure blow-ups for MCCV */
		double blowUpInSelectionPhase = MathExt.round(1f / SEARCH_TRAIN_FOLD_SIZE * SELECTION_NUM_MC_ITERATIONS / SEARCH_NUM_MC_ITERATIONS, 2);
		this.getAlgorithmConfig().setProperty(MLPlanClassifierConfig.K_BLOWUP_SELECTION, String.valueOf(blowUpInSelectionPhase));
		double blowUpInPostprocessing = MathExt.round((1 / (1 - this.getAlgorithmConfig().dataPortionForSelection())) / SELECTION_NUM_MC_ITERATIONS, 2);
		this.getAlgorithmConfig().setProperty(MLPlanClassifierConfig.K_BLOWUP_POSTPROCESS, String.valueOf(blowUpInPostprocessing));
	}

	/**
	 * Sets the search space to a tiny weka search space configuration.
	 * @throws IOException Thrown if the resource file cannot be read.
	 */
	public MLPlanWekaBuilder withTinyWekaSearchSpace() throws IOException {
		this.withSearchSpaceConfigFile(ResourceUtil.getResourceAsFile(RES_SSC_TINY_WEKA));
		return this;
	}

	/**
	 * Allows to use learning curve extrapolation for predicting the quality of candidate solutions.
	 * @param anchorpoints The anchor points for which samples are actually evaluated on the respective data.
	 * @param subsamplingAlgorithmFactory The factory for the sampling algorithm that is to be used to randomly draw training instances.
	 * @param trainSplitForAnchorpointsMeasurement The training fold size for measuring the acnhorpoints.
	 * @param extrapolationMethod The method to be used in order to extrapolate the learning curve from the anchorpoints.
	 */
	public void withLearningCurveExtrapolationEvaluation(final int[] anchorpoints, final ISamplingAlgorithmFactory<WekaInstances<Object>, ? extends ASamplingAlgorithm<WekaInstances<Object>>> subsamplingAlgorithmFactory,
			final double trainSplitForAnchorpointsMeasurement, final LearningCurveExtrapolationMethod extrapolationMethod) {
		this.withSearchPhaseEvaluatorFactory(new LearningCurveExtrapolationEvaluatorFactory(anchorpoints, subsamplingAlgorithmFactory, trainSplitForAnchorpointsMeasurement, extrapolationMethod));
		this.withSelectionPhaseEvaluatorFactory(new MonteCarloCrossValidationEvaluatorFactory().withNumMCIterations(3).withTrainFoldSize(.7).withSplitBasedEvaluator(new SimpleSLCSplitBasedClassifierEvaluator(new ZeroOneLoss())));
		this.getAlgorithmConfig().setProperty(MLPlanClassifierConfig.K_BLOWUP_SELECTION, "" + 10);
	}

}
