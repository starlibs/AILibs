package ai.libs.mlplan.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import org.aeonbits.owner.ConfigFactory;
import org.api4.java.ai.graphsearch.problem.IOptimalPathInORGraphSearchFactory;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPathEvaluator;
import org.api4.java.ai.ml.core.dataset.splitter.IFoldSizeConfigurableRandomDatasetSplitter;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.evaluation.IPredictionPerformanceMetricConfigurable;
import org.api4.java.ai.ml.core.evaluation.supervised.loss.IDeterministicPredictionPerformanceMeasure;
import org.api4.java.ai.ml.core.learner.ISupervisedLearner;
import org.api4.java.algorithm.Timeout;
import org.api4.java.common.control.ILoggingCustomizable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.hasco.model.Component;
import ai.libs.hasco.model.Parameter;
import ai.libs.hasco.model.ParameterRefinementConfiguration;
import ai.libs.hasco.serialization.ComponentLoader;
import ai.libs.hasco.variants.forwarddecomposition.DefaultPathPriorizingPredicate;
import ai.libs.hasco.variants.forwarddecomposition.FDAndBestFirstWithRandomCompletionTransformer;
import ai.libs.hasco.variants.forwarddecomposition.HASCOViaFDFactory;
import ai.libs.hasco.variants.forwarddecomposition.twophase.HASCOWithRandomCompletionsConfig;
import ai.libs.jaicore.basic.FileUtil;
import ai.libs.jaicore.basic.IOwnerBasedAlgorithmConfig;
import ai.libs.jaicore.basic.IOwnerBasedRandomConfig;
import ai.libs.jaicore.basic.MathExt;
import ai.libs.jaicore.basic.ResourceFile;
import ai.libs.jaicore.basic.ResourceUtil;
import ai.libs.jaicore.basic.algorithm.reduction.AlgorithmicProblemReduction;
import ai.libs.jaicore.basic.reconstruction.ReconstructionUtil;
import ai.libs.jaicore.ml.classification.loss.dataset.EClassificationPerformanceMeasure;
import ai.libs.jaicore.ml.core.evaluation.evaluator.factory.ISupervisedLearnerEvaluatorFactory;
import ai.libs.jaicore.ml.core.evaluation.evaluator.factory.MonteCarloCrossValidationEvaluatorFactory;
import ai.libs.jaicore.ml.core.filter.FilterBasedDatasetSplitter;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.factories.LabelBasedStratifiedSamplingFactory;
import ai.libs.jaicore.ml.regression.loss.dataset.RootMeanSquaredError;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.BestFirstFactory;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.StandardBestFirstFactory;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.nodeevaluation.AlternativeNodeEvaluator;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithPathEvaluationsInput;
import ai.libs.mlplan.multiclass.MLPlanClassifierConfig;
import ai.libs.mlplan.safeguard.IEvaluationSafeGuardFactory;

/**
 * The MLPlanBuilder helps to easily configure and initialize ML-Plan with specific parameter settings.
 * For convenient use, the MLPlanBuilder also offers methods for initializing ML-Plan with default
 * configuration to use ML-Plan for single label classification in combination with WEKA or scikit-learn
 * or for multi-label classification in combination with MEKA and consequently with WEKA (for baselearners
 * of multi-label reduction strategies).
 *
 * @author mwever, fmohr
 */
public abstract class AbstractMLPlanBuilder<L extends ISupervisedLearner<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>>, B extends AbstractMLPlanBuilder<L, B>> implements IMLPlanBuilder<L, B>, ILoggingCustomizable {

	/* Logging */
	private Logger logger = LoggerFactory.getLogger(AbstractMLPlanBuilder.class);
	private String loggerName = AbstractMLPlanBuilder.class.getName();

	private static final String RES_ALGORITHM_CONFIG = "mlplan/mlplan.properties";
	private static final String FS_ALGORITHM_CONFIG = "conf/mlplan.properties";

	protected static final int DEFAULT_SEARCH_NUM_MC_ITERATIONS = 5;
	protected static final double DEFAULT_SEARCH_TRAIN_FOLD_SIZE = 0.7;
	protected static final int DEFAULT_SELECTION_NUM_MC_ITERATIONS = 5;
	protected static final double DEFAULT_SELECTION_TRAIN_FOLD_SIZE = 0.7;
	protected static final IDeterministicPredictionPerformanceMeasure<Object, Object> DEFAULT_PERFORMANCE_MEASURE = EClassificationPerformanceMeasure.ERRORRATE;

	private static final MonteCarloCrossValidationEvaluatorFactory DEF_SEARCH_PHASE_EVALUATOR = new MonteCarloCrossValidationEvaluatorFactory().withNumMCIterations(DEFAULT_SEARCH_NUM_MC_ITERATIONS)
			.withTrainFoldSize(DEFAULT_SEARCH_TRAIN_FOLD_SIZE).withMeasure(new RootMeanSquaredError());
	private static final MonteCarloCrossValidationEvaluatorFactory DEF_SELECTION_PHASE_EVALUATOR = new MonteCarloCrossValidationEvaluatorFactory().withNumMCIterations(DEFAULT_SELECTION_NUM_MC_ITERATIONS)
			.withTrainFoldSize(DEFAULT_SELECTION_TRAIN_FOLD_SIZE).withMeasure(new RootMeanSquaredError());

	private static final IFoldSizeConfigurableRandomDatasetSplitter<ILabeledDataset<?>> DEF_SEARCH_SELECT_SPLITTER = new FilterBasedDatasetSplitter<>(new LabelBasedStratifiedSamplingFactory<>(), DEFAULT_SEARCH_TRAIN_FOLD_SIZE,
			new Random(0));

	/* Default configuration values */
	private static final File DEF_ALGORITHM_CONFIG = FileUtil.getExistingFileWithHighestPriority(RES_ALGORITHM_CONFIG, FS_ALGORITHM_CONFIG);

	/* Builder (self) status variables */
	private boolean factoryPreparedWithData = false;

	/* Data for initializing ML-Plan */
	private MLPlanClassifierConfig algorithmConfig;

	@SuppressWarnings("rawtypes")
	private HASCOViaFDFactory hascoFactory = new HASCOViaFDFactory<GraphSearchWithPathEvaluationsInput<TFDNode, String, Double>, Double>();
	private Predicate<TFDNode> priorizingPredicate = new DefaultPathPriorizingPredicate<>(); // by default, we prefer paths that lead to default parametrizations
	private File searchSpaceFile;
	private String requestedHASCOInterface;
	private ILearnerFactory<L> learnerFactory;

	/* Node Evaluation */
	private IPathEvaluator<TFDNode, String, Double> preferredNodeEvaluator = null;
	private PipelineValidityCheckingNodeEvaluator pipelineValidityCheckingNodeEvaluator;

	/* Candidate Evaluation (if no other node evaluation is used) */
	private IFoldSizeConfigurableRandomDatasetSplitter<ILabeledDataset<?>> searchSelectionDatasetSplitter;
	protected ISupervisedLearnerEvaluatorFactory<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>> factoryForPipelineEvaluationInSearchPhase = null;
	protected ISupervisedLearnerEvaluatorFactory<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>> factoryForPipelineEvaluationInSelectionPhase = null;
	private IEvaluationSafeGuardFactory safeGuard = null;

	private Collection<Component> components = new LinkedList<>();

	/* The problem input for ML-Plan. */
	protected IProblemType problemType;
	private ILabeledDataset<?> dataset;

	protected AbstractMLPlanBuilder(final IProblemType problemType) throws IOException {
		super();
		this.withAlgorithmConfigFile(DEF_ALGORITHM_CONFIG);
		this.withRandomCompletionBasedBestFirstSearch();
		this.withProblemType(problemType);
		this.withSeed(0);

		// /* configure blow-ups for MCCV */
		double blowUpInSelectionPhase = MathExt.round(1f / DEFAULT_SEARCH_TRAIN_FOLD_SIZE * DEFAULT_SELECTION_NUM_MC_ITERATIONS / DEFAULT_SEARCH_NUM_MC_ITERATIONS, 2);
		this.getAlgorithmConfig().setProperty(MLPlanClassifierConfig.K_BLOWUP_SELECTION, String.valueOf(blowUpInSelectionPhase));
		double blowUpInPostprocessing = MathExt.round((1 / (1 - this.getAlgorithmConfig().dataPortionForSelection())) / DEFAULT_SELECTION_NUM_MC_ITERATIONS, 2);
		this.getAlgorithmConfig().setProperty(MLPlanClassifierConfig.K_BLOWUP_POSTPROCESS, String.valueOf(blowUpInPostprocessing));
	}

	public AbstractMLPlanBuilder<L, B> withProblemType(final IProblemType problemType) throws IOException {
		if (this.problemType != problemType) {
			this.problemType = problemType;
			if (this.logger.isInfoEnabled()) {
				this.logger.info("Setting problem type to {}.", this.problemType.getName());
			}
			if (this.getLearnerFactory() != null) {
				this.getLearnerFactory().setProblemType(this.problemType);
				if (this.logger.isInfoEnabled()) {
					this.logger.info("Setting factory for the problem type {}: {}", this.problemType.getName(), this.getLearnerFactory().getClass().getSimpleName());
				}
			}
			this.withSearchSpaceConfigFile(FileUtil.getExistingFileWithHighestPriority(this.problemType.getSearchSpaceConfigFileFromResource(), this.problemType.getSearchSpaceConfigFromFileSystem()));
			this.withRequestedInterface(problemType.getRequestedInterface());
			this.withSearchPhaseEvaluatorFactory(DEF_SEARCH_PHASE_EVALUATOR);
			this.withSelectionPhaseEvaluatorFactory(DEF_SELECTION_PHASE_EVALUATOR);
			this.withPortionOfDataReservedForSelection(this.problemType.getPortionOfDataReservedForSelectionPhase());
			this.withDatasetSplitterForSearchSelectionSplit(DEF_SEARCH_SELECT_SPLITTER);
		}
		return this.getSelf();
	}

	public IProblemType getProblemType() {
		return this.problemType;
	}

	/**
	 * Creates a preferred node evaluator that can be used to prefer components over other components.
	 *
	 * @param preferredComponentsFile The file containing a priority list of component names.
	 * @param preferableCompnentMethodPrefix The prefix of a method's name for refining a complex task to preferable components.
	 * @return The builder object.
	 * @throws IOException Thrown if a problem occurs while trying to read the file containing the priority list.
	 */
	public AbstractMLPlanBuilder<L, B> withPreferredComponentsFile(final File preferredComponentsFile, final String preferableCompnentMethodPrefix, final boolean replaceCurrentPreferences) throws IOException {
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
		if (replaceCurrentPreferences) {
			return this.withOnePreferredNodeEvaluator(new PreferenceBasedNodeEvaluator(this.problemType, this.getComponents(), ordering));
		} else {
			return this.withPreferredNodeEvaluator(new PreferenceBasedNodeEvaluator(this.problemType, this.getComponents(), ordering));
		}
	}

	public AbstractMLPlanBuilder<L, B> withPreferredComponentsFile(final File preferredComponentsFile, final String preferableCompnentMethodPrefix) throws IOException {
		return this.withPreferredComponentsFile(preferredComponentsFile, preferableCompnentMethodPrefix, false);
	}

	public String getPreferredComponentsFiles() {
		return this.getAlgorithmConfig().getProperty(MLPlanClassifierConfig.PREFERRED_COMPONENTS);
	}

	/**
	 * This ADDs a new preferred node evaluator; requires that the search will be a best-first search.
	 *
	 * It is possible to specify several preferred node evaluators, which will be ordered by the order in which they are specified. The latest given evaluator is the most preferred one.
	 *
	 * @param preferredNodeEvaluator
	 * @return
	 */
	public B withPreferredNodeEvaluator(final IPathEvaluator<TFDNode, String, Double> preferredNodeEvaluator) {
		if (this.factoryPreparedWithData) {
			throw new IllegalStateException("The method prepareNodeEvaluatorInFactoryWithData has already been called. No changes to the preferred node evaluator possible anymore");
		}

		/* first update the preferred node evaluator */
		if (this.preferredNodeEvaluator == null) {
			this.preferredNodeEvaluator = preferredNodeEvaluator;
		} else {
			this.preferredNodeEvaluator = new AlternativeNodeEvaluator<>(preferredNodeEvaluator, this.preferredNodeEvaluator);
		}
		this.update();
		return this.getSelf();
	}

	public B withOnePreferredNodeEvaluator(final IPathEvaluator<TFDNode, String, Double> preferredNodeEvaluator) {
		if (this.factoryPreparedWithData) {
			throw new IllegalStateException("The method prepareNodeEvaluatorInFactoryWithData has already been called. No changes to the preferred node evaluator possible anymore");
		}

		this.preferredNodeEvaluator = preferredNodeEvaluator;
		this.update();
		return this.getSelf();
	}

	@SuppressWarnings("unchecked")
	public B withSearchFactory(@SuppressWarnings("rawtypes") final IOptimalPathInORGraphSearchFactory searchFactory, @SuppressWarnings("rawtypes") final AlgorithmicProblemReduction transformer) {
		this.hascoFactory.setSearchFactory(searchFactory);
		this.hascoFactory.setSearchProblemTransformer(transformer);
		return this.getSelf();
	}

	public B withPerformanceMeasureForSearchPhase(final IDeterministicPredictionPerformanceMeasure<?, ?> performanceMeasure) {
		if (this.getLearnerEvaluationFactoryForSearchPhase() instanceof IPredictionPerformanceMetricConfigurable) {
			((IPredictionPerformanceMetricConfigurable) this.getLearnerEvaluationFactoryForSearchPhase()).setMeasure(performanceMeasure);
		} else {
			throw new UnsupportedOperationException("The evaluator (" + this.getLearnerEvaluationFactoryForSearchPhase() + ") for search phase does not support setting a metric.");
		}
		return this.getSelf();
	}

	public B withPerformanceMeasureForSelectionPhase(final IDeterministicPredictionPerformanceMeasure<?, ?> performanceMeasure) {
		if (this.getLearnerEvaluationFactoryForSelectionPhase() instanceof IPredictionPerformanceMetricConfigurable) {
			((IPredictionPerformanceMetricConfigurable) this.getLearnerEvaluationFactoryForSelectionPhase()).setMeasure(performanceMeasure);
		} else {
			throw new UnsupportedOperationException("The evaluator (" + this.getLearnerEvaluationFactoryForSelectionPhase() + ") for selection phase does not support setting a metric.");
		}
		return this.getSelf();
	}

	public B withPerformanceMeasure(final IDeterministicPredictionPerformanceMeasure<?, ?> performanceMeasure) {
		this.withPerformanceMeasureForSearchPhase(performanceMeasure);
		this.withPerformanceMeasureForSelectionPhase(performanceMeasure);
		return this.getSelf();
	}

	@SuppressWarnings("unchecked")
	public B withRandomCompletionBasedBestFirstSearch() {
		this.hascoFactory.setSearchFactory(new StandardBestFirstFactory<TFDNode, String, Double>());
		this.update();
		return this.getSelf();
	}

	public Collection<Component> getComponents() throws IOException {
		return new ComponentLoader(this.searchSpaceFile).getComponents();
	}

	public Map<Component, Map<Parameter, ParameterRefinementConfiguration>> getComponentParameterConfigurations() throws IOException {
		return new ComponentLoader(this.searchSpaceFile).getParamConfigs();
	}

	/***********************************************************************************************************************************************************************************************************************/
	/***********************************************************************************************************************************************************************************************************************/
	/***********************************************************************************************************************************************************************************************************************/
	/***********************************************************************************************************************************************************************************************************************/

	/**
	 * Loads the MLPlanClassifierConfig with default values and replaces all properties according to the properties defined in the given config file.
	 *
	 * @param algorithmConfigFile The file specifying the property values to replace the default configuration.
	 * @return The MLPlanBuilder object.
	 * @throws IOException An IOException is thrown if there are issues reading the config file.
	 */
	public B withAlgorithmConfigFile(final File algorithmConfigFile) {
		return this.withAlgorithmConfig((MLPlanClassifierConfig) ConfigFactory.create(MLPlanClassifierConfig.class).loadPropertiesFromFile(algorithmConfigFile));
	}

	/**
	 * Loads the MLPlanClassifierConfig with default values and replaces all properties according to the properties defined in the given config file.
	 *
	 * @param config The algorithm configuration.
	 * @return The MLPlanBuilder object.
	 * @throws IOException An IOException is thrown if there are issues reading the config file.
	 */
	public B withAlgorithmConfig(final MLPlanClassifierConfig config) {
		this.algorithmConfig = config;
		this.hascoFactory.withAlgorithmConfig(this.algorithmConfig);
		this.update();
		return this.getSelf();
	}

	/**
	 * Set the data for which ML-Plan is supposed to find the best pipeline.
	 *
	 * @param dataset The dataset for which ML-Plan is to be run.
	 * @return The builder object.
	 */
	public B withDataset(final ILabeledDataset<?> dataset) {
		if (!ReconstructionUtil.areInstructionsNonEmptyIfReconstructibilityClaimed(dataset)) {
			this.logger.warn("The dataset claims to be reconstructible, but it does not carry any instructions.");
		}
		this.dataset = dataset;
		return this.getSelf();
	}

	public ILabeledDataset<?> getDataset() {
		return this.dataset;
	}

	/**
	 * Specify the search space in which ML-Plan is required to work.
	 *
	 * @param searchSpaceConfig The file of the search space configuration.
	 * @return The builder object.
	 * @throws IOException Thrown if the given file does not exist.
	 */
	public B withSearchSpaceConfigFile(final File searchSpaceConfig) throws IOException {
		FileUtil.requireFileExists(searchSpaceConfig);
		this.searchSpaceFile = searchSpaceConfig;
		this.components.clear();
		this.components.addAll(new ComponentLoader(this.searchSpaceFile).getComponents());
		File preferredComponentsFile;
		if (this.getAlgorithmConfig().getProperty(MLPlanClassifierConfig.PREFERRED_COMPONENTS) != null) {
			preferredComponentsFile = new File(this.getAlgorithmConfig().getProperty(MLPlanClassifierConfig.PREFERRED_COMPONENTS));
		} else {
			preferredComponentsFile = FileUtil.getExistingFileWithHighestPriority(this.problemType.getPreferredComponentListFromResource(), this.problemType.getPreferredComponentListFromFileSystem());
		}
		this.withPreferredComponentsFile(preferredComponentsFile, this.problemType.getPreferredComponentName(), true);
		this.logger.info("The search space configuration file has been set to {}.", searchSpaceConfig.getCanonicalPath());
		return this.getSelf();
	}

	/**
	 * Set the classifier factory that translates <code>CompositionInstance</code> objects to classifiers that can be evaluated.
	 *
	 * @param classifierFactory The classifier factory to be used to translate CompositionInstance objects to classifiers.
	 * @return The builder object.
	 */
	public B withClassifierFactory(final ILearnerFactory<L> classifierFactory) {
		this.learnerFactory = classifierFactory;
		return this.getSelf();
	}

	/**
	 * Set the dataset splitter that is used for generating the holdout data portion that is put aside during search.
	 *
	 * @param datasetSplitter The dataset splitter to be used.
	 * @return The builder obect.
	 */
	public B withDatasetSplitterForSearchSelectionSplit(final IFoldSizeConfigurableRandomDatasetSplitter<ILabeledDataset<?>> datasetSplitter) {
		this.searchSelectionDatasetSplitter = datasetSplitter;
		return this.getSelf();
	}

	public B withRequestedInterface(final String requestedInterface) {
		this.requestedHASCOInterface = requestedInterface;
		return this.getSelf();
	}

	/**
	 * @param timeout The timeout for ML-Plan to search for the best classifier.
	 * @return The builder object.
	 */
	public B withTimeOut(final Timeout timeout) {
		this.algorithmConfig.setProperty(IOwnerBasedAlgorithmConfig.K_TIMEOUT, timeout.milliseconds() + "");
		this.update();
		return this.getSelf();
	}

	/**
	 * @return The timeout for ML-Plan to search for the best classifier.
	 */
	public Timeout getTimeOut() {
		return new Timeout(this.algorithmConfig.timeout(), TimeUnit.MILLISECONDS);
	}

	/**
	 * @param timeout The timeout for a single candidate evaluation.
	 * @return The builder object.
	 */
	public B withNodeEvaluationTimeOut(final Timeout timeout) {
		this.algorithmConfig.setProperty(HASCOWithRandomCompletionsConfig.K_RANDOM_COMPLETIONS_TIMEOUT_NODE, timeout.milliseconds() + "");
		this.update();
		return this.getSelf();
	}

	/**
	 * @return The timeout for ML-Plan to search for the best classifier.
	 */
	public Timeout getNodeEvaluationTimeOut() {
		return new Timeout(this.algorithmConfig.timeoutForNodeEvaluation(), TimeUnit.MILLISECONDS);
	}

	/**
	 * @param timeout The timeout for a single candidate evaluation.
	 * @return The builder object.
	 */
	public B withCandidateEvaluationTimeOut(final Timeout timeout) {
		this.algorithmConfig.setProperty(HASCOWithRandomCompletionsConfig.K_RANDOM_COMPLETIONS_TIMEOUT_PATH, timeout.milliseconds() + "");
		this.update();
		return this.getSelf();
	}

	/**
	 * @return The timeout for ML-Plan to search for the best classifier.
	 */
	public Timeout getCandidateEvaluationTimeOut() {
		return new Timeout(this.algorithmConfig.timeoutForCandidateEvaluation(), TimeUnit.MILLISECONDS);
	}

	public MonteCarloCrossValidationEvaluatorFactory withMCCVBasedCandidateEvaluationInSearchPhase() {
		this.factoryForPipelineEvaluationInSearchPhase = new MonteCarloCrossValidationEvaluatorFactory();
		return ((MonteCarloCrossValidationEvaluatorFactory) this.factoryForPipelineEvaluationInSearchPhase).withNumMCIterations(DEFAULT_SEARCH_NUM_MC_ITERATIONS).withTrainFoldSize(DEFAULT_SEARCH_TRAIN_FOLD_SIZE)
				.withMeasure(DEFAULT_PERFORMANCE_MEASURE);
	}

	public MonteCarloCrossValidationEvaluatorFactory withMCCVBasedCandidateEvaluationInSelectionPhase() {
		this.factoryForPipelineEvaluationInSelectionPhase = new MonteCarloCrossValidationEvaluatorFactory();
		return ((MonteCarloCrossValidationEvaluatorFactory) this.factoryForPipelineEvaluationInSelectionPhase).withNumMCIterations(DEFAULT_SELECTION_NUM_MC_ITERATIONS).withTrainFoldSize(DEFAULT_SELECTION_TRAIN_FOLD_SIZE)
				.withMeasure(DEFAULT_PERFORMANCE_MEASURE);
	}

	@Override
	public ISupervisedLearnerEvaluatorFactory<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>> getLearnerEvaluationFactoryForSearchPhase() {
		return this.factoryForPipelineEvaluationInSearchPhase;
	}

	@Override
	public ISupervisedLearnerEvaluatorFactory<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>> getLearnerEvaluationFactoryForSelectionPhase() {
		return this.factoryForPipelineEvaluationInSelectionPhase;
	}

	/**
	 * Sets the evaluator factory for the search phase.
	 *
	 * @param evaluatorFactory The evaluator factory for the search phase.
	 * @return The builder object.
	 */
	public B withSearchPhaseEvaluatorFactory(final ISupervisedLearnerEvaluatorFactory<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>> evaluatorFactory) {
		this.factoryForPipelineEvaluationInSearchPhase = evaluatorFactory;
		this.withPerformanceMeasureForSearchPhase(this.problemType.getPerformanceMetricForSearchPhase());
		return this.getSelf();
	}

	/**
	 * @return The factory for the classifier evaluator of the search phase.
	 */
	protected ISupervisedLearnerEvaluatorFactory<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>> getSearchEvaluatorFactory() {
		return this.factoryForPipelineEvaluationInSearchPhase;
	}

	/**
	 * Sets the evaluator factory for the selection phase.
	 *
	 * @param evaluatorFactory The evaluator factory for the selection phase.
	 * @return The builder object.
	 */
	public B withSelectionPhaseEvaluatorFactory(final ISupervisedLearnerEvaluatorFactory<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>> evaluatorFactory) {
		this.factoryForPipelineEvaluationInSelectionPhase = evaluatorFactory;
		this.withPerformanceMeasureForSelectionPhase(this.problemType.getPerformanceMetricForSelectionPhase());
		return this.getSelf();
	}

	/**
	 * Sets the number of cpus that may be used by ML-Plan.
	 *
	 * @param numCpus The number of cpus to use.
	 * @return The builder object.
	 */
	public B withNumCpus(final int numCpus) {
		this.algorithmConfig.setProperty(IOwnerBasedAlgorithmConfig.K_CPUS, numCpus + "");
		this.update();
		return this.getSelf();
	}

	public B withSeed(final long seed) {
		this.algorithmConfig.setProperty(IOwnerBasedRandomConfig.K_SEED, seed + "");
		this.update();
		this.logger.info("Seed has been set to {}", seed);
		return this.getSelf();
	}

	/**
	 * @return The factory for the classifier evaluator of the selection phase.
	 */
	protected ISupervisedLearnerEvaluatorFactory<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>> getSelectionEvaluatorFactory() {
		return this.factoryForPipelineEvaluationInSelectionPhase;
	}

	@Override
	public HASCOViaFDFactory getHASCOFactory() {
		return this.hascoFactory;
	}

	@Override
	public ILearnerFactory<L> getLearnerFactory() {
		return this.learnerFactory;
	}

	@Override
	public String getLoggerName() {
		return this.loggerName;
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger = LoggerFactory.getLogger(name);
		this.loggerName = name;
	}

	@Override
	public String getRequestedInterface() {
		return this.requestedHASCOInterface;
	}

	@Override
	public IFoldSizeConfigurableRandomDatasetSplitter<ILabeledDataset<? extends ILabeledInstance>> getSearchSelectionDatasetSplitter() {
		return this.searchSelectionDatasetSplitter;
	}

	@Override
	public File getSearchSpaceConfigFile() {
		return this.searchSpaceFile;
	}

	@Override
	public MLPlanClassifierConfig getAlgorithmConfig() {
		return this.algorithmConfig;
	}

	public B withPipelineValidityCheckingNodeEvaluator(final PipelineValidityCheckingNodeEvaluator ne) {
		this.pipelineValidityCheckingNodeEvaluator = ne;
		return this.getSelf();
	}

	public void prepareNodeEvaluatorInFactoryWithData() {
		if (!(this.hascoFactory.getSearchFactory() instanceof BestFirstFactory)) {
			return;
		}
		if (this.factoryPreparedWithData) {
			throw new IllegalStateException("Factory has already been prepared with data. This can only be done once!");
		}
		this.factoryPreparedWithData = true;

		/* nothing to do if there are no preferred node evaluators */
		if (this.pipelineValidityCheckingNodeEvaluator == null && this.preferredNodeEvaluator == null) {
			return;
		}

		/* now determine the real node evaluator to be used. A semantic node evaluator has highest priority */
		IPathEvaluator<TFDNode, String, Double> actualNodeEvaluator;
		if (this.pipelineValidityCheckingNodeEvaluator != null) {
			this.pipelineValidityCheckingNodeEvaluator.setComponents(this.components);
			this.pipelineValidityCheckingNodeEvaluator.setData(this.dataset);
			if (this.preferredNodeEvaluator != null) {
				actualNodeEvaluator = new AlternativeNodeEvaluator<>(this.pipelineValidityCheckingNodeEvaluator, this.preferredNodeEvaluator);
			} else {
				actualNodeEvaluator = this.pipelineValidityCheckingNodeEvaluator;
			}
		} else {
			actualNodeEvaluator = this.preferredNodeEvaluator;
		}

		/* update the preferred node evaluator in the HascoFactory */
		this.preferredNodeEvaluator = actualNodeEvaluator;
		this.update();
	}

	@SuppressWarnings("unchecked")
	private void update() {
		this.hascoFactory.setSearchProblemTransformer(new FDAndBestFirstWithRandomCompletionTransformer<Double>(this.preferredNodeEvaluator, this.priorizingPredicate, this.algorithmConfig.randomSeed(),
				this.algorithmConfig.numberOfRandomCompletions(), this.algorithmConfig.timeoutForCandidateEvaluation(), this.algorithmConfig.timeoutForNodeEvaluation()));
		this.hascoFactory.withAlgorithmConfig(this.getAlgorithmConfig());
	}

	public B withPortionOfDataReservedForSelection(final double value) {
		this.algorithmConfig.setProperty(MLPlanClassifierConfig.SELECTION_PORTION, value + "");
		return this.getSelf();
	}

	@Override
	public double getPortionOfDataReservedForSelectionPhase() {
		return this.algorithmConfig.dataPortionForSelection();
	}

	public B withSafeGuardFactory(final IEvaluationSafeGuardFactory safeGuard) {
		this.safeGuard = safeGuard;
		return this.getSelf();
	}

	@Override
	public IEvaluationSafeGuardFactory getSafeGuardFactory() {
		return this.safeGuard;
	}

	/**
	 * Builds an ML-Plan object for the given dataset as input.
	 *
	 * @param dataset The dataset for which an ML-Plan object is to be built.
	 * @return The ML-Plan object configured with this builder.
	 */
	public MLPlan<L> build(final ILabeledDataset<?> dataset) {
		return this.withDataset(dataset).build();
	}

	public void checkPreconditionsForInitialization() {
		Objects.requireNonNull(this.dataset, "A dataset needs to be provided as input to ML-Plan");
		Objects.requireNonNull(this.searchSelectionDatasetSplitter, "Dataset splitter for search phase must be set!");
		Objects.requireNonNull(this.requestedHASCOInterface, "No requested HASCO interface defined!");
	}

	/**
	 * Builds an ML-Plan object with the dataset provided earlier to this builder.
	 *
	 * @return The ML-Plan object configured with this builder.
	 */
	public MLPlan<L> build() {
		this.checkPreconditionsForInitialization();
		this.prepareNodeEvaluatorInFactoryWithData(); // inform node evaluator about data and create the MLPlan object
		return new MLPlan<>(this, this.dataset);
	}
}
