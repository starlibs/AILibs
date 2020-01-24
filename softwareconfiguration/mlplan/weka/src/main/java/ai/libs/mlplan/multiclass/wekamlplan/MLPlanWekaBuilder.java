package ai.libs.mlplan.multiclass.wekamlplan;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.api4.java.ai.ml.core.dataset.schema.attribute.ICategoricalAttribute;
import org.api4.java.ai.ml.core.dataset.splitter.IFoldSizeConfigurableRandomDatasetSplitter;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.basic.FileUtil;
import ai.libs.jaicore.basic.MathExt;
import ai.libs.jaicore.basic.ResourceFile;
import ai.libs.jaicore.basic.ResourceUtil;
import ai.libs.jaicore.ml.core.evaluation.evaluator.factory.LearningCurveExtrapolationEvaluatorFactory;
import ai.libs.jaicore.ml.core.evaluation.evaluator.factory.MonteCarloCrossValidationEvaluatorFactory;
import ai.libs.jaicore.ml.core.filter.FilterBasedDatasetSplitter;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.ASamplingAlgorithm;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.factories.LabelBasedStratifiedSamplingFactory;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.factories.interfaces.ISamplingAlgorithmFactory;
import ai.libs.jaicore.ml.functionprediction.learner.learningcurveextrapolation.LearningCurveExtrapolationMethod;
import ai.libs.jaicore.ml.weka.classification.learner.IWekaClassifier;
import ai.libs.jaicore.ml.weka.dataset.WekaInstances;
import ai.libs.mlplan.core.AbstractMLPlanBuilder;
import ai.libs.mlplan.core.PreferenceBasedNodeEvaluator;
import ai.libs.mlplan.multiclass.MLPlanClassifierConfig;
import ai.libs.mlplan.multiclass.wekamlplan.weka.WekaPipelineFactory;
import ai.libs.mlplan.multiclass.wekamlplan.weka.WekaPipelineValidityCheckingNodeEvaluator;

public class MLPlanWekaBuilder extends AbstractMLPlanBuilder<IWekaClassifier, MLPlanWekaBuilder> {

	private static final String RES_SSC_TINY_WEKA = "automl/searchmodels/weka/tinytest.json";
	private static final String RES_SSC_WEKA_COMPLETE = "automl/searchmodels/weka/weka-all-autoweka.json";
	private static final String FS_SSC_WEKA = "conf/mlplan-weka.json";

	private static final String RES_PREFERRED_COMPONENTS = "mlplan/weka-preferenceList.txt";
	private static final String FS_PREFERRED_COMPONENTS = "conf/mlpan-weka-preferenceList.txt";

	/* Default configuration values. */
	private static final String DEF_REQUESTED_HASCO_INTERFACE = "AbstractClassifier";
	private static final String DEF_PREFERRED_COMPONENT_NAME_PREFIX = "resolveAbstractClassifierWith";

	private static final WekaPipelineFactory DEF_CLASSIFIER_FACTORY = new WekaPipelineFactory();
	private static final File DEF_PREFERRED_COMPONENTS = FileUtil.getExistingFileWithHighestPriority(RES_PREFERRED_COMPONENTS, FS_PREFERRED_COMPONENTS);
	private static final File DEF_SEARCH_SPACE_CONFIG = FileUtil.getExistingFileWithHighestPriority(RES_SSC_WEKA_COMPLETE, FS_SSC_WEKA);
	private static final IFoldSizeConfigurableRandomDatasetSplitter<ILabeledDataset<?>> DEF_SEARCH_SELECT_SPLITTER = new FilterBasedDatasetSplitter<>(new LabelBasedStratifiedSamplingFactory<>(), DEFAULT_SEARCH_TRAIN_FOLD_SIZE,
			new Random(0));
	// private static final IDatasetSplitter<WekaInstances> DEF_SEARCH_DATASET_SPLITTER = new FilterBasedDatasetSplitter<>(new LabelBasedStratifiedSamplingFactory<>(), DEFAULT_SEARCH_TRAIN_FOLD_SIZE, new Random(0));
	// private static final IDatasetSplitter<WekaInstances> DEF_SELECTION_DATASET_SPLITTER = new FilterBasedDatasetSplitter<>(new LabelBasedStratifiedSamplingFactory<>(), DEFAULT_SELECTION_TRAIN_FOLD_SIZE, new Random(0));
	private static final MonteCarloCrossValidationEvaluatorFactory DEF_SEARCH_PHASE_EVALUATOR = new MonteCarloCrossValidationEvaluatorFactory().withNumMCIterations(DEFAULT_SEARCH_NUM_MC_ITERATIONS)
			.withTrainFoldSize(DEFAULT_SEARCH_TRAIN_FOLD_SIZE).withMeasure(DEFAULT_PERFORMANCE_MEASURE);
	private static final MonteCarloCrossValidationEvaluatorFactory DEF_SELECTION_PHASE_EVALUATOR = new MonteCarloCrossValidationEvaluatorFactory().withNumMCIterations(DEFAULT_SELECTION_NUM_MC_ITERATIONS)
			.withTrainFoldSize(DEFAULT_SELECTION_TRAIN_FOLD_SIZE).withMeasure(DEFAULT_PERFORMANCE_MEASURE);

	private Logger logger = LoggerFactory.getLogger(MLPlanWekaBuilder.class);

	public MLPlanWekaBuilder() throws IOException {
		super();
		this.withSearchSpaceConfigFile(DEF_SEARCH_SPACE_CONFIG);
		this.withPreferredComponentsFile(DEF_PREFERRED_COMPONENTS, DEF_PREFERRED_COMPONENT_NAME_PREFIX);
		this.withRequestedInterface(DEF_REQUESTED_HASCO_INTERFACE);
		this.withClassifierFactory(DEF_CLASSIFIER_FACTORY);
		this.withSearchPhaseEvaluatorFactory(DEF_SEARCH_PHASE_EVALUATOR);
		this.withSelectionPhaseEvaluatorFactory(DEF_SELECTION_PHASE_EVALUATOR);
		this.withDatasetSplitterForSearchSelectionSplit(DEF_SEARCH_SELECT_SPLITTER);
		this.withPipelineValidityCheckingNodeEvaluator(new WekaPipelineValidityCheckingNodeEvaluator());

		// /* configure blow-ups for MCCV */
		double blowUpInSelectionPhase = MathExt.round(1f / DEFAULT_SEARCH_TRAIN_FOLD_SIZE * DEFAULT_SELECTION_NUM_MC_ITERATIONS / DEFAULT_SEARCH_NUM_MC_ITERATIONS, 2);
		this.getAlgorithmConfig().setProperty(MLPlanClassifierConfig.K_BLOWUP_SELECTION, String.valueOf(blowUpInSelectionPhase));
		double blowUpInPostprocessing = MathExt.round((1 / (1 - this.getAlgorithmConfig().dataPortionForSelection())) / DEFAULT_SELECTION_NUM_MC_ITERATIONS, 2);
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
	public void withLearningCurveExtrapolationEvaluation(final int[] anchorpoints, final ISamplingAlgorithmFactory<ILabeledDataset<?>, ? extends ASamplingAlgorithm<ILabeledDataset<?>>> subsamplingAlgorithmFactory,
			final double trainSplitForAnchorpointsMeasurement, final LearningCurveExtrapolationMethod extrapolationMethod) {
		this.withSearchPhaseEvaluatorFactory(new LearningCurveExtrapolationEvaluatorFactory(anchorpoints, subsamplingAlgorithmFactory, trainSplitForAnchorpointsMeasurement, extrapolationMethod));
		this.withSelectionPhaseEvaluatorFactory(new MonteCarloCrossValidationEvaluatorFactory().withNumMCIterations(3).withTrainFoldSize(.7));
		this.getAlgorithmConfig().setProperty(MLPlanClassifierConfig.K_BLOWUP_SELECTION, "" + 10);
	}

	/**
	 * Creates a preferred node evaluator that can be used to prefer components over other components.
	 *
	 * @param preferredComponentsFile The file containing a priority list of component names.
	 * @param preferableCompnentMethodPrefix The prefix of a method's name for refining a complex task to preferable components.
	 * @return The builder object.
	 * @throws IOException Thrown if a problem occurs while trying to read the file containing the priority list.
	 */
	public MLPlanWekaBuilder withPreferredComponentsFile(final File preferredComponentsFile, final String preferableCompnentMethodPrefix) throws IOException {
		this.getAlgorithmConfig().setProperty(MLPlanClassifierConfig.PREFERRED_COMPONENTS, preferredComponentsFile.getAbsolutePath());
		List<String> ordering;
		if (preferredComponentsFile instanceof ResourceFile) {
			ordering = ResourceUtil.readResourceFileToStringList((ResourceFile) preferredComponentsFile);
		} else if (!preferredComponentsFile.exists()) {
			this.logger.warn("The configured file for preferred components \"{}\" does not exist. Not using any particular ordering.", preferredComponentsFile.getAbsolutePath());
			ordering = new ArrayList<>();
		} else {
			ordering = FileUtil.readFileAsList(preferredComponentsFile);
		}
		return this.withPreferredNodeEvaluator(new PreferenceBasedNodeEvaluator(this.getComponents(), ordering, preferableCompnentMethodPrefix));
	}

	@Override
	public MLPlanWekaBuilder withDataset(final ILabeledDataset<?> dataset) {
		if (!(dataset.getLabelAttribute() instanceof ICategoricalAttribute)) {
			throw new IllegalArgumentException("MLPlanWeka currently only support categorically labeled data!");
		}
		WekaInstances instances = dataset instanceof WekaInstances ? (WekaInstances) dataset : new WekaInstances(dataset);
		super.withDataset(instances);
		this.logger.info("Setting dataset as WekaInstances object.");
		return this.getSelf();
	}

	@Override
	public MLPlanWekaBuilder getSelf() {
		return this;
	}

	@Override
	public MLPlan4Weka build() {
		this.checkPreconditionsForInitialization();
		this.prepareNodeEvaluatorInFactoryWithData(); // inform node evaluator about data and create the MLPlan object
		return new MLPlan4Weka(this, this.getDataset());
	}
}
