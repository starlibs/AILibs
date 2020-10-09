package ai.libs.mlplan.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import org.aeonbits.owner.ConfigFactory;
import org.api4.java.ai.graphsearch.problem.IOptimalPathInORGraphSearchFactory;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPathEvaluator;
import org.api4.java.ai.ml.core.dataset.splitter.IFoldSizeConfigurableRandomDatasetSplitter;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.core.evaluation.supervised.loss.IDeterministicPredictionPerformanceMeasure;
import org.api4.java.ai.ml.core.learner.ISupervisedLearner;
import org.api4.java.algorithm.Timeout;
import org.api4.java.common.control.ILoggingCustomizable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.hasco.builder.forwarddecomposition.DefaultPathPriorizingPredicate;
import ai.libs.hasco.builder.forwarddecomposition.HASCOViaFDBuilder;
import ai.libs.hasco.twophase.HASCOWithRandomCompletionsConfig;
import ai.libs.jaicore.basic.FileUtil;
import ai.libs.jaicore.basic.IOwnerBasedAlgorithmConfig;
import ai.libs.jaicore.basic.IOwnerBasedRandomConfig;
import ai.libs.jaicore.basic.ResourceFile;
import ai.libs.jaicore.basic.ResourceUtil;
import ai.libs.jaicore.basic.algorithm.reduction.AlgorithmicProblemReduction;
import ai.libs.jaicore.basic.reconstruction.ReconstructionUtil;
import ai.libs.jaicore.basic.sets.SetUtil;
import ai.libs.jaicore.components.api.IComponentRepository;
import ai.libs.jaicore.components.api.INumericParameterRefinementConfigurationMap;
import ai.libs.jaicore.components.serialization.ComponentSerialization;
import ai.libs.jaicore.ml.core.evaluation.evaluator.factory.ISupervisedLearnerEvaluatorFactory;
import ai.libs.jaicore.ml.core.evaluation.evaluator.factory.MonteCarloCrossValidationEvaluatorFactory;
import ai.libs.jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import ai.libs.mlplan.multiclass.MLPlanClassifierConfig;
import ai.libs.mlplan.safeguard.IEvaluationSafeGuardFactory;

/**
 * The MLPlanBuilder helps to easily configure and initialize ML-Plan with specific parameter settings. For convenient use, the MLPlanBuilder also offers methods for initializing ML-Plan with default configuration to use ML-Plan for single
 * label classification in combination with WEKA or scikit-learn or for multi-label classification in combination with MEKA and consequently with WEKA (for baselearners of multi-label reduction strategies).
 *
 * @author Felix Mohr, Marcel Wever
 */
public abstract class AMLPlanBuilder<L extends ISupervisedLearner<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>>, B extends AMLPlanBuilder<L, B>> implements IMLPlanBuilder<L, B>, ILoggingCustomizable {

	/* Logging */
	private Logger logger = LoggerFactory.getLogger(AMLPlanBuilder.class);
	private String loggerName = AMLPlanBuilder.class.getName();

	private static final String RES_ALGORITHM_CONFIG = "mlplan/mlplan.properties";
	private static final String FS_ALGORITHM_CONFIG = "conf/mlplan.properties";

	/* Default configuration values */
	private static final File DEF_ALGORITHM_CONFIG = FileUtil.getExistingFileWithHighestPriority(RES_ALGORITHM_CONFIG, FS_ALGORITHM_CONFIG);

	/* problem description aspects */
	private final ComponentSerialization serializer = new ComponentSerialization();
	private File searchSpaceFile;
	private String requestedHASCOInterface;
	private String nameOfHASCOMethodToResolveBareLearner;
	private String nameOfHASCOMethodToResolverLearnerInPipeline;
	private ILearnerFactory<L> learnerFactory;
	private ILabeledDataset<?> dataset;

	/* other general properties of ML-Plan */
	private MLPlanClassifierConfig algorithmConfig;

	/* node evaluation and search guidance */
	private Predicate<TFDNode> priorizingPredicate = new DefaultPathPriorizingPredicate<>(); // by default, we prefer paths that lead to default parametrizations
	private List<IPathEvaluator<TFDNode, String, Double>> preferredNodeEvaluators = new ArrayList<>();
	private PipelineValidityCheckingNodeEvaluator pipelineValidityCheckingNodeEvaluator;

	/* Candidate Evaluation (if no other node evaluation is used) */
	private IFoldSizeConfigurableRandomDatasetSplitter<ILabeledDataset<?>> searchSelectionDatasetSplitter;
	private IDeterministicPredictionPerformanceMeasure<?, ?> metricForSearchPhase;
	private IDeterministicPredictionPerformanceMeasure<?, ?> metricForSelectionPhase;
	private ISupervisedLearnerEvaluatorFactory<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>> factoryForPipelineEvaluationInSearchPhase = this.getMCCVFactory(3, .7);
	private ISupervisedLearnerEvaluatorFactory<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>> factoryForPipelineEvaluationInSelectionPhase = this.getMCCVFactory(3, .7);
	private IEvaluationSafeGuardFactory safeGuard = null;

	protected AMLPlanBuilder() {
		super();
		this.withAlgorithmConfigFile(DEF_ALGORITHM_CONFIG);
		this.withSeed(0);
	}

	protected AMLPlanBuilder(final IProblemType<L> problemType) throws IOException {
		super();
		this.withAlgorithmConfigFile(DEF_ALGORITHM_CONFIG);
		this.withProblemType(problemType);
		this.withSeed(0);
	}

	public AMLPlanBuilder<L, B> withProblemType(final IProblemType<L> problemType) throws IOException {

		if (this.logger.isInfoEnabled()) {
			this.logger.info("Setting problem type to {}.", problemType.getName());
		}
		this.withSearchSpaceConfigFile(FileUtil.getExistingFileWithHighestPriority(problemType.getSearchSpaceConfigFileFromResource(), problemType.getSearchSpaceConfigFromFileSystem()));
		this.withRequestedInterface(problemType.getRequestedInterface());
		this.withLearnerFactory(problemType.getLearnerFactory());

		/* setup everything for preferred components */
		if (problemType.getPreferredComponentListFromResource() != null || problemType.getPreferredComponentListFromFileSystem() != null) {
			boolean relevantFileAvailable = true;
			if (problemType.getPreferredComponentListFromResource() == null) {
				relevantFileAvailable = new File(problemType.getPreferredComponentListFromFileSystem()).exists();
			}
			if (relevantFileAvailable) {
				this.withPreferredComponentsFile(FileUtil.getExistingFileWithHighestPriority(problemType.getPreferredComponentListFromResource(), problemType.getPreferredComponentListFromFileSystem()));
				this.nameOfHASCOMethodToResolveBareLearner = problemType.getLastHASCOMethodPriorToParameterRefinementOfBareLearner();
				this.nameOfHASCOMethodToResolverLearnerInPipeline = problemType.getLastHASCOMethodPriorToParameterRefinementOfPipeline();
			}
		}
		this.withPipelineValidityCheckingNodeEvaluator(problemType.getValidityCheckingNodeEvaluator());

		/* configure the metric defined in the problem type */
		this.withPerformanceMeasureForSearchPhase(problemType.getPerformanceMetricForSearchPhase());
		this.withPerformanceMeasureForSelectionPhase(problemType.getPerformanceMetricForSelectionPhase());
		this.searchSelectionDatasetSplitter = problemType.getSearchSelectionDatasetSplitter();
		return this.getSelf();
	}

	public B withPerformanceMeasureForSearchPhase(final IDeterministicPredictionPerformanceMeasure<?, ?> performanceMeasure) {
		this.metricForSearchPhase = performanceMeasure;
		return this.getSelf();
	}

	public B withPerformanceMeasureForSelectionPhase(final IDeterministicPredictionPerformanceMeasure<?, ?> performanceMeasure) {
		this.metricForSelectionPhase = performanceMeasure;
		return this.getSelf();
	}

	public B withPerformanceMeasure(final IDeterministicPredictionPerformanceMeasure<?, ?> performanceMeasure) {
		this.withPerformanceMeasureForSearchPhase(performanceMeasure);
		this.withPerformanceMeasureForSelectionPhase(performanceMeasure);
		return this.getSelf();
	}

	@Override
	public IDeterministicPredictionPerformanceMeasure<?, ?> getMetricForSearchPhase() {
		return this.metricForSearchPhase;
	}

	@Override
	public IDeterministicPredictionPerformanceMeasure<?, ?> getMetricForSelectionPhase() {
		return this.metricForSelectionPhase;
	}

	/**
	 * Creates a preferred node evaluator that can be used to prefer components over other components.
	 *
	 * @param preferredComponentsFile
	 *            The file containing a priority list of component names.
	 * @return The builder object.
	 * @throws IOException
	 *             Thrown if a problem occurs while trying to read the file containing the priority list.
	 */
	public B withPreferredComponentsFile(final File preferredComponentsFile) throws IOException {
		this.getAlgorithmConfig().setProperty(MLPlanClassifierConfig.PREFERRED_COMPONENTS, preferredComponentsFile.getAbsolutePath());
		List<String> namesOfPreferredComponents = null; // the order is important!
		if (preferredComponentsFile instanceof ResourceFile) {
			namesOfPreferredComponents = ResourceUtil.readResourceFileToStringList((ResourceFile) preferredComponentsFile);
		} else if (!preferredComponentsFile.exists()) {
			this.logger.warn("The configured file for preferred components \"{}\" does not exist. Not using any particular ordering.", preferredComponentsFile.getAbsolutePath());
		} else {
			namesOfPreferredComponents = FileUtil.readFileAsList(preferredComponentsFile);
		}
		if (namesOfPreferredComponents != null) {
			this.withPreferredComponents(namesOfPreferredComponents);
		}
		return this.getSelf();
	}

	public B withPreferredComponents(final List<String> preferredComponents) {
		this.getAlgorithmConfig().setProperty(MLPlanClassifierConfig.PREFERRED_COMPONENTS, "" + SetUtil.implode(preferredComponents, ", "));
		return this.getSelf();
	}

	public List<String> getPreferredComponents() {
		return this.getAlgorithmConfig().preferredComponents();
	}

	/**
	 * This adds a new preferred node evaluator
	 *
	 * It is possible to specify several preferred node evaluators, which will be ordered by the order in which they are specified. The latest given evaluator is the most preferred one.
	 *
	 * @param preferredNodeEvaluator
	 * @return
	 */
	public B withPreferredNodeEvaluator(final IPathEvaluator<TFDNode, String, Double> preferredNodeEvaluator) {
		this.preferredNodeEvaluators.add(preferredNodeEvaluator);
		return this.getSelf();
	}

	public List<IPathEvaluator<TFDNode, String, Double>> getPreferredNodeEvaluators() {
		return Collections.unmodifiableList(this.preferredNodeEvaluators);
	}

	public B withSearchFactory(@SuppressWarnings("rawtypes") final IOptimalPathInORGraphSearchFactory searchFactory, @SuppressWarnings("rawtypes") final AlgorithmicProblemReduction transformer) {
		throw new UnsupportedOperationException("Currently only support for BestFirst search. Will be extended in the upcoming release.");
	}

	public IComponentRepository getComponents() throws IOException {
		return this.serializer.deserializeRepository(this.searchSpaceFile);
	}

	public INumericParameterRefinementConfigurationMap getComponentParameterConfigurations() throws IOException {
		return this.serializer.deserializeParamMap(this.searchSpaceFile);
	}

	/**
	 * Loads the MLPlanClassifierConfig with default values and replaces all properties according to the properties defined in the given config file.
	 *
	 * @param algorithmConfigFile
	 *            The file specifying the property values to replace the default configuration.
	 * @return The MLPlanBuilder object.
	 * @throws IOException
	 *             An IOException is thrown if there are issues reading the config file.
	 */
	public B withAlgorithmConfigFile(final File algorithmConfigFile) {
		return this.withAlgorithmConfig((MLPlanClassifierConfig) ConfigFactory.create(MLPlanClassifierConfig.class).loadPropertiesFromFile(algorithmConfigFile));
	}

	/**
	 * Loads the MLPlanClassifierConfig with default values and replaces all properties according to the properties defined in the given config file.
	 *
	 * @param config
	 *            The algorithm configuration.
	 * @return The MLPlanBuilder object.
	 * @throws IOException
	 *             An IOException is thrown if there are issues reading the config file.
	 */
	public B withAlgorithmConfig(final MLPlanClassifierConfig config) {
		this.algorithmConfig = config;
		return this.getSelf();
	}

	/**
	 * Set the data for which ML-Plan is supposed to find the best pipeline.
	 *
	 * @param dataset
	 *            The dataset for which ML-Plan is to be run.
	 * @return The builder object.
	 */
	public B withDataset(final ILabeledDataset<?> dataset) {
		if (!ReconstructionUtil.areInstructionsNonEmptyIfReconstructibilityClaimed(dataset)) {
			this.logger.warn("The dataset claims to be reconstructible, but it does not carry any instructions.");
		}
		this.dataset = dataset;
		if (dataset.stream().anyMatch(i -> i.getLabel() == null)) {
			this.logger.warn("Dataset has instances without label. Dropping those lines!! Number of instances now: {}", this.dataset.size());
			this.dataset.removeIf(i -> i.getLabel() == null);
			this.logger.warn("Dataset is now reduced. Number of instances now: {}", this.dataset.size());
		}
		return this.getSelf();
	}

	public ILabeledDataset<?> getDataset() {
		return this.dataset;
	}

	/**
	 * Specify the search space in which ML-Plan is required to work.
	 *
	 * @param searchSpaceConfig
	 *            The file of the search space configuration.
	 * @return The builder object.
	 * @throws IOException
	 *             Thrown if the given file does not exist.
	 */
	public B withSearchSpaceConfigFile(final File searchSpaceConfig) throws IOException {
		FileUtil.requireFileExists(searchSpaceConfig);
		this.searchSpaceFile = searchSpaceConfig;
		this.logger.info("The search space configuration file has been set to {}.", searchSpaceConfig.getCanonicalPath());
		return this.getSelf();
	}

	/**
	 * Set the classifier factory that translates <code>CompositionInstance</code> objects to classifiers that can be evaluated.
	 *
	 * @param classifierFactory
	 *            The classifier factory to be used to translate CompositionInstance objects to classifiers.
	 * @return The builder object.
	 */
	public B withLearnerFactory(final ILearnerFactory<L> classifierFactory) {
		this.learnerFactory = classifierFactory;
		return this.getSelf();
	}

	/**
	 * Set the dataset splitter that is used for generating the holdout data portion that is put aside during search.
	 *
	 * @param datasetSplitter
	 *            The dataset splitter to be used.
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
	 * @param timeout
	 *            The timeout for ML-Plan to search for the best classifier.
	 * @return The builder object.
	 */
	public B withTimeOut(final Timeout timeout) {
		this.algorithmConfig.setProperty(IOwnerBasedAlgorithmConfig.K_TIMEOUT, timeout.milliseconds() + "");
		return this.getSelf();
	}

	/**
	 * @return The timeout for ML-Plan to search for the best classifier.
	 */
	public Timeout getTimeOut() {
		return new Timeout(this.algorithmConfig.timeout(), TimeUnit.MILLISECONDS);
	}

	public B withTimeoutPrecautionOffsetInSeconds(final int seconds) {
		this.algorithmConfig.setProperty(MLPlanClassifierConfig.PRECAUTION_OFFSET, "" + seconds);
		return this.getSelf();
	}

	public int getTimeoutPrecautionOffsetInSeconds() {
		return this.algorithmConfig.precautionOffset();
	}

	/**
	 * @param timeout
	 *            The timeout for a single candidate evaluation.
	 * @return The builder object.
	 */
	public B withNodeEvaluationTimeOut(final Timeout timeout) {
		this.algorithmConfig.setProperty(HASCOWithRandomCompletionsConfig.K_RANDOM_COMPLETIONS_TIMEOUT_NODE, timeout.milliseconds() + "");
		return this.getSelf();
	}

	/**
	 * @return The timeout for ML-Plan to search for the best classifier.
	 */
	public Timeout getNodeEvaluationTimeOut() {
		return new Timeout(this.algorithmConfig.timeoutForNodeEvaluation(), TimeUnit.MILLISECONDS);
	}

	/**
	 * @param timeout
	 *            The timeout for a single candidate evaluation.
	 * @return The builder object.
	 */
	public B withCandidateEvaluationTimeOut(final Timeout timeout) {
		this.algorithmConfig.setProperty(HASCOWithRandomCompletionsConfig.K_RANDOM_COMPLETIONS_TIMEOUT_PATH, timeout.milliseconds() + "");
		return this.getSelf();
	}

	/**
	 * @return The timeout for ML-Plan to search for the best classifier.
	 */
	public Timeout getCandidateEvaluationTimeOut() {
		return new Timeout(this.algorithmConfig.timeoutForCandidateEvaluation(), TimeUnit.MILLISECONDS);
	}

	public B withMCCVBasedCandidateEvaluationInSearchPhase(final int numIterations, final double trainPortion) {
		this.factoryForPipelineEvaluationInSearchPhase = this.getMCCVFactory(numIterations, trainPortion);
		return this.getSelf();
	}

	public B withMCCVBasedCandidateEvaluationInSelectionPhase(final int numIterations, final double trainPortion) {
		this.factoryForPipelineEvaluationInSelectionPhase = this.getMCCVFactory(numIterations, trainPortion);
		return this.getSelf();
	}

	private MonteCarloCrossValidationEvaluatorFactory getMCCVFactory(final int numIterations, final double trainPortion) {
		MonteCarloCrossValidationEvaluatorFactory factory = new MonteCarloCrossValidationEvaluatorFactory();
		factory.withNumMCIterations(numIterations).withTrainFoldSize(trainPortion);
		return factory;
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
	 * @param evaluatorFactory
	 *            The evaluator factory for the search phase.
	 * @return The builder object.
	 */
	public void withSearchPhaseEvaluatorFactory(final ISupervisedLearnerEvaluatorFactory<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>> evaluatorFactory) {
		this.factoryForPipelineEvaluationInSearchPhase = evaluatorFactory;
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
	 * @param evaluatorFactory
	 *            The evaluator factory for the selection phase.
	 * @return The builder object.
	 */
	public B withSelectionPhaseEvaluatorFactory(final ISupervisedLearnerEvaluatorFactory<ILabeledInstance, ILabeledDataset<? extends ILabeledInstance>> evaluatorFactory) {
		this.factoryForPipelineEvaluationInSelectionPhase = evaluatorFactory;
		return this.getSelf();
	}

	/**
	 * Sets the number of cpus that may be used by ML-Plan.
	 *
	 * @param numCpus
	 *            The number of cpus to use.
	 * @return The builder object.
	 */
	public B withNumCpus(final int numCpus) {
		this.algorithmConfig.setProperty(IOwnerBasedAlgorithmConfig.K_CPUS, numCpus + "");
		return this.getSelf();
	}

	public B withSeed(final long seed) {
		this.algorithmConfig.setProperty(IOwnerBasedRandomConfig.K_SEED, seed + "");
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
	public HASCOViaFDBuilder<Double, ?> getHASCOFactory() {
		return MLPlanUtil.getHASCOBuilder(this.algorithmConfig, this.dataset, this.searchSpaceFile, this.requestedHASCOInterface, this.priorizingPredicate, this.preferredNodeEvaluators, this.pipelineValidityCheckingNodeEvaluator,
				this.nameOfHASCOMethodToResolveBareLearner, this.nameOfHASCOMethodToResolverLearnerInPipeline);
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
		this.serializer.setLoggerName(name + ".serializer");
		this.loggerName = name;
	}

	@Override
	public String getRequestedInterface() {
		return this.requestedHASCOInterface;
	}

	@Override
	public IFoldSizeConfigurableRandomDatasetSplitter<ILabeledDataset<?>> getSearchSelectionDatasetSplitter() {
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

	public PipelineValidityCheckingNodeEvaluator getPipelineValidityCheckingNodeEvaluator() {
		return this.pipelineValidityCheckingNodeEvaluator;
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
	 * @param dataset
	 *            The dataset for which an ML-Plan object is to be built.
	 * @return The ML-Plan object configured with this builder.
	 */
	public MLPlan<L> build(final ILabeledDataset<?> dataset) {
		return this.withDataset(dataset).build();
	}

	public void checkPreconditionsForInitialization() {

		/* check proper problem definition */
		Objects.requireNonNull(this.searchSpaceFile, "No search space file defined.");
		Objects.requireNonNull(this.requestedHASCOInterface, "No requested HASCO interface defined!");

		/* check that data is available */
		Objects.requireNonNull(this.dataset, "A dataset needs to be provided as input to ML-Plan");

		/* check that the evaluation factories and the search/selection splitter are defined  */
		Objects.requireNonNull(this.learnerFactory, "The learner factory has not been set.");
		Objects.requireNonNull(this.factoryForPipelineEvaluationInSearchPhase, "Factory for pipeline evaluation in search phase is not set!");
		Objects.requireNonNull(this.factoryForPipelineEvaluationInSelectionPhase, "Factory for pipeline evaluation in selection phase is not set!");
		Objects.requireNonNull(this.searchSelectionDatasetSplitter, "Dataset splitter for search phase must be set!");
	}

	/**
	 * Builds an ML-Plan object with the dataset provided earlier to this builder.
	 *
	 * @return The ML-Plan object configured with this builder.
	 */
	public MLPlan<L> build() {
		this.checkPreconditionsForInitialization();
		return new MLPlan<>(this, this.dataset);
	}
}
