package de.upb.crc901.mlplan.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import org.aeonbits.owner.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.upb.crc901.mlpipeline_evaluation.PerformanceDBAdapter;
import de.upb.crc901.mlplan.multiclass.MLPlanClassifierConfig;
import de.upb.crc901.mlplan.multiclass.wekamlplan.IClassifierFactory;
import de.upb.crc901.mlplan.multiclass.wekamlplan.weka.PreferenceBasedNodeEvaluator;
import hasco.core.HASCOFactory;
import hasco.model.Component;
import hasco.serialization.ComponentLoader;
import hasco.variants.forwarddecomposition.HASCOViaFDAndBestFirstFactory;
import hasco.variants.forwarddecomposition.HASCOViaFDFactory;
import jaicore.basic.FileUtil;
import jaicore.basic.ILoggingCustomizable;
import jaicore.basic.TimeOut;
import jaicore.basic.algorithm.reduction.AlgorithmicProblemReduction;
import jaicore.ml.evaluation.evaluators.weka.IClassifierEvaluator;
import jaicore.ml.evaluation.evaluators.weka.LearningCurveExtrapolationEvaluator;
import jaicore.ml.evaluation.evaluators.weka.factory.ClassifierEvaluatorConstructionFailedException;
import jaicore.ml.evaluation.evaluators.weka.factory.IClassifierEvaluatorFactory;
import jaicore.ml.evaluation.evaluators.weka.factory.MonteCarloCrossValidationEvaluatorFactory;
import jaicore.ml.weka.dataset.splitter.IDatasetSplitter;
import jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import jaicore.search.algorithms.standard.bestfirst.StandardBestFirstFactory;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.AlternativeNodeEvaluator;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import jaicore.search.core.interfaces.IOptimalPathInORGraphSearchFactory;
import jaicore.search.probleminputs.GraphSearchInput;
import jaicore.search.problemtransformers.GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformerViaRDFS;
import weka.core.Instances;

/**
 * The MLPlanBuilder helps to easily configure and initialize ML-Plan with specific parameter settings.
 * For convenient use, the MLPlanBuilder also offers methods for initializing ML-Plan with default
 * configuration to use ML-Plan for single label classification in combination with WEKA or scikit-learn
 * or for multi-label classification in combination with MEKA and consequently with WEKA (for baselearners
 * of multi-label reduction strategies).
 *
 * @author mwever, fmohr
 */
public abstract class AbstractMLPlanBuilder implements IMLPlanBuilder, ILoggingCustomizable {

	/* Logging */
	private Logger logger = LoggerFactory.getLogger(AbstractMLPlanBuilder.class);
	private String loggerName = AbstractMLPlanBuilder.class.getName();

	private static final String RES_ALGORITHM_CONFIG = "mlplan/mlplan.properties";
	private static final String FS_ALGORITHM_CONFIG = "conf/mlplan.properties";

	/* Default configuration values */
	private static final File DEF_ALGORITHM_CONFIG = FileUtil.getExistingFileWithHighestPriority(RES_ALGORITHM_CONFIG, FS_ALGORITHM_CONFIG);

	/* Builder (self) status variables */
	private boolean factoryPreparedWithData = false;

	/* Data for initializing ML-Plan */
	private MLPlanClassifierConfig algorithmConfig;

	@SuppressWarnings("rawtypes")
	private HASCOViaFDFactory hascoFactory = new HASCOViaFDFactory<GraphSearchInput<TFDNode, String>, Double>();
	private Predicate<TFDNode> priorizingPredicate = null;
	private File searchSpaceFile;
	private String requestedHASCOInterface;
	private IClassifierFactory classifierFactory;

	private INodeEvaluator<TFDNode, Double> preferredNodeEvaluator = null;
	private PipelineValidityCheckingNodeEvaluator pipelineValidityCheckingNodeEvaluator;
	/* The splitter is used to create the split for separating search and selection data */
	private IDatasetSplitter searchSelectionDatasetSplitter;
	private IClassifierEvaluatorFactory factoryForPipelineEvaluationInSearchPhase = null;
	private IClassifierEvaluatorFactory factoryForPipelineEvaluationInSelectionPhase = null;

	private Collection<Component> components = new LinkedList<>();
	private String performanceMeasureName;

	/* Use caching */
	private boolean useCache;
	private PerformanceDBAdapter dbAdapter = null;

	/* The problem input for ML-Plan. */
	private Instances dataset;

	protected AbstractMLPlanBuilder() {
		super();
		this.withAlgorithmConfigFile(DEF_ALGORITHM_CONFIG);
		this.withRandomCompletionBasedBestFirstSearch();
	}

	public static MLPlanSKLearnBuilder forSKLearn() throws IOException {
		return new MLPlanSKLearnBuilder();
	}

	public static MLPlanWekaBuilder forWeka() throws IOException {
		return new MLPlanWekaBuilder();
	}

	public static MLPlanMekaBuilder forMeka() throws IOException {
		return new MLPlanMekaBuilder();
	}

	/**
	 * This ADDs a new preferred node evaluator; requires that the search will be a best-first search.
	 *
	 * It is possible to specify several preferred node evaluators, which will be ordered by the order in which they are specified. The latest given evaluator is the most preferred one.
	 *
	 * @param preferredNodeEvaluator
	 * @return
	 */
	public AbstractMLPlanBuilder withPreferredNodeEvaluator(final INodeEvaluator<TFDNode, Double> preferredNodeEvaluator) {
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
		return this;
	}

	@SuppressWarnings("unchecked")
	public AbstractMLPlanBuilder withSearchFactory(@SuppressWarnings("rawtypes") final IOptimalPathInORGraphSearchFactory searchFactory, @SuppressWarnings("rawtypes") final AlgorithmicProblemReduction transformer) {
		this.hascoFactory.setSearchFactory(searchFactory);
		this.hascoFactory.setSearchProblemTransformer(transformer);
		return this;
	}

	@SuppressWarnings("unchecked")
	public AbstractMLPlanBuilder withRandomCompletionBasedBestFirstSearch() {
		this.hascoFactory.setSearchFactory(new StandardBestFirstFactory<TFDNode, String, Double>());
		this.update();
		return this;
	}

	public Collection<Component> getComponents() throws IOException {
		return new ComponentLoader(this.searchSpaceFile).getComponents();
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
	public AbstractMLPlanBuilder withAlgorithmConfigFile(final File algorithmConfigFile) {
		return this.withAlgorithmConfig((MLPlanClassifierConfig) ConfigFactory.create(MLPlanClassifierConfig.class).loadPropertiesFromFile(algorithmConfigFile));
	}

	/**
	 * Loads the MLPlanClassifierConfig with default values and replaces all properties according to the properties defined in the given config file.
	 *
	 * @param config The algorithm configuration.
	 * @return The MLPlanBuilder object.
	 * @throws IOException An IOException is thrown if there are issues reading the config file.
	 */
	public AbstractMLPlanBuilder withAlgorithmConfig(final MLPlanClassifierConfig config) {
		this.algorithmConfig = config;
		this.hascoFactory.withAlgorithmConfig(this.algorithmConfig);
		this.update();
		return this;
	}

	/**
	 * Creates a preferred node evaluator that can be used to prefer components over other components.
	 * @param preferredComponentsFile The file containing a priority list of component names.
	 * @return The builder object.
	 * @throws IOException Thrown if a problem occurs while trying to read the file containing the priority list.
	 */
	public AbstractMLPlanBuilder withPreferredComponentsFile(final File preferredComponentsFile) throws IOException {
		this.getAlgorithmConfig().setProperty(MLPlanClassifierConfig.PREFERRED_COMPONENTS, preferredComponentsFile.getAbsolutePath());
		List<String> ordering;
		if (!preferredComponentsFile.exists()) {
			this.logger.warn("The configured file for preferred components \"{}\" does not exist. Not using any particular ordering.", preferredComponentsFile.getAbsolutePath());
			ordering = new ArrayList<>();
		} else {
			ordering = FileUtil.readFileAsList(preferredComponentsFile);
		}
		return this.withPreferredNodeEvaluator(new PreferenceBasedNodeEvaluator(this.components, ordering));
	}

	/**
	 * Sets the name of the performance measure that is used.
	 * @param name The name of the performance measure.
	 */
	public void setPerformanceMeasureName(final String name) {
		this.performanceMeasureName = name;
	}

	/**
	 * Set the data for which ML-Plan is supposed to find the best pipeline.
	 * @param dataset The dataset for which ML-Plan is to be run.
	 * @return The builder object.
	 */
	public AbstractMLPlanBuilder withDataset(final Instances dataset) {
		this.dataset = dataset;
		return this;
	}

	/**
	 * Specify the search space in which ML-Plan is required to work.
	 *
	 * @param searchSpaceConfig The file of the search space configuration.
	 * @return The builder object.
	 * @throws IOException Thrown if the given file does not exist.
	 */
	public AbstractMLPlanBuilder withSearchSpaceConfigFile(final File searchSpaceConfig) throws IOException {
		FileUtil.requireFileExists(searchSpaceConfig);
		this.searchSpaceFile = searchSpaceConfig;
		this.components.clear();
		this.components.addAll(new ComponentLoader(this.searchSpaceFile).getComponents());
		return this;
	}

	/**
	 * Set the classifier factory that translates <code>CompositionInstance</code> objects to classifiers that can be evaluated.
	 *
	 * @param classifierFactory The classifier factory to be used to translate CompositionInstance objects to classifiers.
	 * @return The builder object.
	 */
	public AbstractMLPlanBuilder withClassifierFactory(final IClassifierFactory classifierFactory) {
		this.classifierFactory = classifierFactory;
		return this;
	}

	/**
	 * Set the dataset splitter that is used for generating the holdout data portion that is put aside during search.
	 * @param datasetSplitter The dataset splitter to be used.
	 * @return The builder obect.
	 */
	public AbstractMLPlanBuilder withDatasetSplitterForSearchSelectionSplit(final IDatasetSplitter datasetSplitter) {
		this.searchSelectionDatasetSplitter = datasetSplitter;
		return this;
	}

	public AbstractMLPlanBuilder withRequestedInterface(final String requestedInterface) {
		this.requestedHASCOInterface = requestedInterface;
		return this;
	}

	/**
	 * @param timeout The timeout for ML-Plan to search for the best classifier.
	 * @return The builder object.
	 */
	public AbstractMLPlanBuilder withTimeOut(final TimeOut timeout) {
		this.algorithmConfig.setProperty(MLPlanClassifierConfig.K_TIMEOUT, timeout.milliseconds() + "");
		this.update();
		return this;
	}

	/**
	 * @return The timeout for ML-Plan to search for the best classifier.
	 */
	public TimeOut getTimeOut() {
		return new TimeOut(this.algorithmConfig.timeout(), TimeUnit.MILLISECONDS);
	}

	/**
	 * @param timeout The timeout for a single candidate evaluation.
	 * @return The builder object.
	 */
	public AbstractMLPlanBuilder withNodeEvaluationTimeOut(final TimeOut timeout) {
		this.algorithmConfig.setProperty(MLPlanClassifierConfig.K_RANDOM_COMPLETIONS_TIMEOUT_NODE, timeout.milliseconds() + "");
		this.update();
		return this;
	}

	/**
	 * @return The timeout for ML-Plan to search for the best classifier.
	 */
	public TimeOut getNodeEvaluationTimeOut() {
		return new TimeOut(this.algorithmConfig.timeoutForNodeEvaluation(), TimeUnit.MILLISECONDS);
	}

	/**
	 * @param timeout The timeout for a single candidate evaluation.
	 * @return The builder object.
	 */
	public AbstractMLPlanBuilder withCandidateEvaluationTimeOut(final TimeOut timeout) {
		this.algorithmConfig.setProperty(MLPlanClassifierConfig.K_RANDOM_COMPLETIONS_TIMEOUT_PATH, timeout.milliseconds() + "");
		this.update();
		return this;
	}

	/**
	 * @return The timeout for ML-Plan to search for the best classifier.
	 */
	public TimeOut getCandidateEvaluationTimeOut() {
		return new TimeOut(this.algorithmConfig.timeoutForCandidateEvaluation(), TimeUnit.MILLISECONDS);
	}

	@Override
	public PipelineEvaluator getClassifierEvaluationInSearchPhase(final Instances data, final int seed, final int fullDatasetSize) throws ClassifierEvaluatorConstructionFailedException {
		Objects.requireNonNull(this.factoryForPipelineEvaluationInSearchPhase, "No factory for pipeline evaluation in search phase has been set!");

		IClassifierEvaluator evaluator = this.factoryForPipelineEvaluationInSearchPhase.getIClassifierEvaluator(data, seed);
		if (evaluator instanceof LearningCurveExtrapolationEvaluator) {
			((LearningCurveExtrapolationEvaluator) evaluator).setFullDatasetSize(fullDatasetSize);
		}

		return new PipelineEvaluator(this.getClassifierFactory(), evaluator, this.getAlgorithmConfig().timeoutForCandidateEvaluation());
	}

	@Override
	public PipelineEvaluator getClassifierEvaluationInSelectionPhase(final Instances data, final int seed) throws ClassifierEvaluatorConstructionFailedException {
		if (this.factoryForPipelineEvaluationInSelectionPhase == null) {
			throw new IllegalStateException("No factory for pipeline evaluation in selection phase has been set!");
		}
		return new PipelineEvaluator(this.getClassifierFactory(), this.factoryForPipelineEvaluationInSelectionPhase.getIClassifierEvaluator(data, seed), Integer.MAX_VALUE);
	}

	/**
	 * Sets the evaluator factory for the search phase.
	 * @param evaluatorFactory The evaluator factory for the search phase.
	 * @return The builder object.
	 */
	public void withSearchPhaseEvaluatorFactory(final IClassifierEvaluatorFactory evaluatorFactory) {
		this.factoryForPipelineEvaluationInSearchPhase = evaluatorFactory;
	}

	/**
	 * @return The factory for the classifier evaluator of the search phase.
	 */
	protected IClassifierEvaluatorFactory getSearchEvaluatorFactory() {
		return this.factoryForPipelineEvaluationInSearchPhase;
	}

	/**
	 * Sets the evaluator factory for the selection phase.
	 * @param evaluatorFactory The evaluator factory for the selection phase.
	 * @return The builder object.
	 */
	public AbstractMLPlanBuilder withSelectionPhaseEvaluatorFactory(final MonteCarloCrossValidationEvaluatorFactory evaluatorFactory) {
		this.factoryForPipelineEvaluationInSelectionPhase = evaluatorFactory;
		return this;
	}

	/**
	 * Sets the number of cpus that may be used by ML-Plan.
	 * @param numCpus The number of cpus to use.
	 * @return The builder object.
	 */
	public AbstractMLPlanBuilder withNumCpus(final int numCpus) {
		this.algorithmConfig.setProperty(MLPlanClassifierConfig.K_CPUS, numCpus + "");
		this.update();
		return this;
	}

	/**
	 * @return The factory for the classifier evaluator of the selection phase.
	 */
	protected IClassifierEvaluatorFactory getSelectionEvaluatorFactory() {
		return this.factoryForPipelineEvaluationInSelectionPhase;
	}

	@Override
	public String getPerformanceMeasureName() {
		return this.performanceMeasureName;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public HASCOFactory getHASCOFactory() {
		return this.hascoFactory;
	}

	@Override
	public IClassifierFactory getClassifierFactory() {
		return this.classifierFactory;
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
	public IDatasetSplitter getSearchSelectionDatasetSplitter() {
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

	@Override
	public boolean getUseCache() {
		return this.useCache;
	}

	@Override
	public PerformanceDBAdapter getDBAdapter() {
		return this.dbAdapter;
	}

	@Override
	public void prepareNodeEvaluatorInFactoryWithData(final Instances data) {
		if (!(this.hascoFactory instanceof HASCOViaFDAndBestFirstFactory)) {
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
		INodeEvaluator<TFDNode, Double> actualNodeEvaluator;
		if (this.pipelineValidityCheckingNodeEvaluator != null) {
			this.pipelineValidityCheckingNodeEvaluator.setComponents(this.components);
			this.pipelineValidityCheckingNodeEvaluator.setData(data);
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
		this.hascoFactory.setSearchProblemTransformer(new GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformerViaRDFS<TFDNode, String, Double>(this.preferredNodeEvaluator, this.priorizingPredicate,
				this.algorithmConfig.randomSeed(), this.algorithmConfig.numberOfRandomCompletions(), this.algorithmConfig.timeoutForCandidateEvaluation(), this.algorithmConfig.timeoutForNodeEvaluation()));
		this.hascoFactory.withAlgorithmConfig(this.getAlgorithmConfig());
	}

	/**
	 * Builds an ML-Plan object for the given dataset as input.
	 * @param dataset The dataset for which an ML-Plan object is to be built.
	 * @return The ML-Plan object configured with this builder.
	 */
	public MLPlan build(final Instances dataset) {
		this.dataset = dataset;
		return this.build();
	}

	/**
	 * Builds an ML-Plan object with the dataset provided earlier to this builder.
	 * @return The ML-Plan object configured with this builder.
	 */
	public MLPlan build() {
		Objects.requireNonNull(this.dataset, "A dataset needs to be provided as input to ML-Plan");
		MLPlan mlplan = new MLPlan(this, this.dataset);
		mlplan.setTimeout(this.getTimeOut());
		return mlplan;
	}

}
