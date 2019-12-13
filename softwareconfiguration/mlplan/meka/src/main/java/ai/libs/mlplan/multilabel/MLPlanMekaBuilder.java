package ai.libs.mlplan.multilabel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.api4.java.ai.ml.core.dataset.splitter.IFoldSizeConfigurableRandomDatasetSplitter;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.basic.FileUtil;
import ai.libs.jaicore.basic.ResourceFile;
import ai.libs.jaicore.basic.ResourceUtil;
import ai.libs.jaicore.ml.classification.multilabel.evaluation.EMultiLabelClassifierMetric;
import ai.libs.jaicore.ml.core.evaluation.evaluator.factory.MonteCarloCrossValidationEvaluatorFactory;
import ai.libs.jaicore.ml.core.filter.FilterBasedDatasetSplitter;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.factories.LabelBasedStratifiedSamplingFactory;
import ai.libs.mlplan.core.AbstractMLPlanBuilder;
import ai.libs.mlplan.core.ILearnerFactory;
import ai.libs.mlplan.multiclass.MLPlanClassifierConfig;
import ai.libs.mlplan.multilabel.model.IMekaClassifier;

public class MLPlanMekaBuilder extends AbstractMLPlanBuilder<IMekaClassifier, MLPlanMekaBuilder> {

	private static final File DEF_SEARCH_SPACE_CONFIG = FileUtil.getExistingFileWithHighestPriority(ML2PlanMekaPathConfig.RES_SSC, ML2PlanMekaPathConfig.FS_SSC);
	private static final File DEF_PREFERRED_COMPONENTS = FileUtil.getExistingFileWithHighestPriority(ML2PlanMekaPathConfig.RES_PREFC, ML2PlanMekaPathConfig.FS_PREFC);

	/* Default configuration values */
	private static final String DEF_REQUESTED_HASCO_INTERFACE = "MLClassifier";
	private static final String DEF_PREFERRED_COMPONENT_NAME_PREFIX = "resolveMLClassifierWith";

	private static final EMultiLabelClassifierMetric DEFAULT_PERFORMANCE_MEASURE = EMultiLabelClassifierMetric.MEAN_INSTANCEF1;

	private static final ILearnerFactory<IMekaClassifier> DEF_CLASSIFIER_FACTORY = new MekaPipelineFactory();
	private static final IFoldSizeConfigurableRandomDatasetSplitter<ILabeledDataset<?>> DEF_SEARCH_SELECT_SPLITTER = new FilterBasedDatasetSplitter<>(new LabelBasedStratifiedSamplingFactory<>(), DEFAULT_SEARCH_TRAIN_FOLD_SIZE,
			new Random(0));
	private static final MonteCarloCrossValidationEvaluatorFactory DEF_SEARCH_PHASE_EVALUATOR = new MonteCarloCrossValidationEvaluatorFactory().withNumMCIterations(DEFAULT_SEARCH_NUM_MC_ITERATIONS)
			.withTrainFoldSize(DEFAULT_SEARCH_TRAIN_FOLD_SIZE);
	private static final MonteCarloCrossValidationEvaluatorFactory DEF_SELECTION_PHASE_EVALUATOR = new MonteCarloCrossValidationEvaluatorFactory().withNumMCIterations(DEFAULT_SELECTION_NUM_MC_ITERATIONS)
			.withTrainFoldSize(DEFAULT_SELECTION_TRAIN_FOLD_SIZE);

	private Logger logger = LoggerFactory.getLogger(MLPlanMekaBuilder.class);

	public MLPlanMekaBuilder() throws IOException {
		super();
		this.withSearchSpaceConfigFile(DEF_SEARCH_SPACE_CONFIG);
		this.withPreferredComponentsFile(DEF_PREFERRED_COMPONENTS, DEF_PREFERRED_COMPONENT_NAME_PREFIX);
		this.withRequestedInterface(DEF_REQUESTED_HASCO_INTERFACE);
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
	public MLPlanMekaBuilder withAutoMEKADefaultConfiguration() {
		this.withPerformanceMeasure(EMultiLabelClassifierMetric.MEAN_AUTOMEKA_FITNESS);
		return this;
	}

	/**
	 * Creates a preferred node evaluator that can be used to prefer components over other components.
	 *
	 * @param preferredComponentsFile The file containing a priority list of component names.
	 * @param preferableCompnentMethodPrefix The prefix of a method's name for refining a complex task to preferable components.
	 * @return The builder object.
	 * @throws IOException Thrown if a problem occurs while trying to read the file containing the priority list.
	 */
	public MLPlanMekaBuilder withPreferredComponentsFile(final File preferredComponentsFile, final String preferableCompnentMethodPrefix) throws IOException {
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

	/**
	 * Sets the performance measure to evaluate a candidate solution's generalization performance. Caution: This resets the evaluators to MCCV for both search and selection phase if these are not already MCCVs.
	 * @param lossFunction The loss function to be used.
	 * @return The builder object.
	 */
	public MLPlanMekaBuilder withPerformanceMeasure(final EMultiLabelClassifierMetric measure) {
		super.withPerformanceMeasure(measure);
		return this;
	}

	@Override
	public EMultiLabelClassifierMetric getPerformanceMeasure() {
		return (EMultiLabelClassifierMetric) super.getPerformanceMeasure();
	}

	@Override
	public MLPlanMekaBuilder getSelf() {
		return this;
	}

}
