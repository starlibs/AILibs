package de.upb.crc901.mlplan.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.aeonbits.owner.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.upb.crc901.mlpipeline_evaluation.CacheEvaluatorMeasureBridge;
import de.upb.crc901.mlpipeline_evaluation.PerformanceDBAdapter;
import de.upb.crc901.mlplan.multiclass.MLPlanClassifierConfig;
import de.upb.crc901.mlplan.multiclass.wekamlplan.IClassifierFactory;
import de.upb.crc901.mlplan.multiclass.wekamlplan.sklearn.SKLearnClassifierFactory;
import de.upb.crc901.mlplan.multiclass.wekamlplan.weka.PreferenceBasedNodeEvaluator;
import de.upb.crc901.mlplan.multiclass.wekamlplan.weka.WEKAPipelineFactory;
import de.upb.crc901.mlplan.multiclass.wekamlplan.weka.WekaPipelineValidityCheckingNodeEvaluator;
import de.upb.crc901.mlplan.multilabel.MekaPipelineFactory;
import hasco.core.HASCOFactory;
import hasco.model.Component;
import hasco.serialization.ComponentLoader;
import hasco.variants.forwarddecomposition.HASCOViaFDAndBestFirstFactory;
import hasco.variants.forwarddecomposition.HASCOViaFDFactory;
import jaicore.basic.FileUtil;
import jaicore.basic.MathExt;
import jaicore.basic.TimeOut;
import jaicore.basic.algorithm.reduction.AlgorithmicProblemReduction;
import jaicore.logging.ToJSONStringUtil;
import jaicore.ml.core.dataset.sampling.inmemory.ASamplingAlgorithm;
import jaicore.ml.core.dataset.sampling.inmemory.factories.interfaces.ISamplingAlgorithmFactory;
import jaicore.ml.core.dataset.weka.WekaInstances;
import jaicore.ml.core.evaluation.measure.IMeasure;
import jaicore.ml.core.evaluation.measure.multilabel.AutoMEKAGGPFitnessMeasureLoss;
import jaicore.ml.core.evaluation.measure.multilabel.EMultilabelPerformanceMeasure;
import jaicore.ml.core.evaluation.measure.singlelabel.EMultiClassPerformanceMeasure;
import jaicore.ml.core.evaluation.measure.singlelabel.MultiClassMeasureBuilder;
import jaicore.ml.core.evaluation.measure.singlelabel.ZeroOneLoss;
import jaicore.ml.evaluation.evaluators.weka.IClassifierEvaluator;
import jaicore.ml.evaluation.evaluators.weka.LearningCurveExtrapolationEvaluator;
import jaicore.ml.evaluation.evaluators.weka.factory.ClassifierEvaluatorConstructionFailedException;
import jaicore.ml.evaluation.evaluators.weka.factory.IClassifierEvaluatorFactory;
import jaicore.ml.evaluation.evaluators.weka.factory.LearningCurveExtrapolationEvaluatorFactory;
import jaicore.ml.evaluation.evaluators.weka.factory.MonteCarloCrossValidationEvaluatorFactory;
import jaicore.ml.evaluation.evaluators.weka.splitevaluation.ISplitBasedClassifierEvaluator;
import jaicore.ml.evaluation.evaluators.weka.splitevaluation.SimpleMLCSplitBasedClassifierEvaluator;
import jaicore.ml.evaluation.evaluators.weka.splitevaluation.SimpleSLCSplitBasedClassifierEvaluator;
import jaicore.ml.learningcurve.extrapolation.LearningCurveExtrapolationMethod;
import jaicore.ml.weka.dataset.splitter.ArbitrarySplitter;
import jaicore.ml.weka.dataset.splitter.IDatasetSplitter;
import jaicore.ml.weka.dataset.splitter.MulticlassClassStratifiedSplitter;
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
public class MLPlanBuilder {

	/* Logging */
	private Logger logger = LoggerFactory.getLogger(MLPlanBuilder.class);

	private static final String SLC_REQUESTED_HASCO_INTERFACE = "AbstractClassifier";
	private static final String MLC_REQUESTED_HASCO_INTERFACE = "MLClassifier";

	/* Search space configuration files for default configurations */
	private static final File SPC_TINYTEST = new File("resources/automl/searchmodels/weka/tinytest.json");
	private static final File SPC_AUTO_WEKA = new File("resources/automl/searchmodels/weka/weka-all-autoweka.json");
	private static final File SPC_SKLEARN = new File("resources/automl/searchmodels/sklearn/sklearn-mlplan.json");
	private static final File SPC_SKLEARN_UL = new File("resources/automl/searchmodels/sklearn/ml-plan-ul.json");
	private static final File SPC_MEKA = new File("resources/automl/searchmodels/meka/meka-multilabel.json");

	/* Preferred classifier lists to define an order for the classifiers to be evaluated. */
	private static final File PREFC_AUTO_WEKA = new File("resources/mlplan/weka-precedenceList.txt");
	private static final File PREFC_SKLEARN = new File("resources/mlplan/sklearn-precedenceList.txt");
	private static final File PREFC_SKLEARN_UL = new File("resources/mlplan/sklearn-ul-precedenceList.txt");
	private static final File PREFC_MEKA = new File("resources/mlplan/meka-preferenceList.txt");

	/* Default values initially set when creating a builder. */
	private static final File DEFAULT_ALGORITHM_CONFIG_FILE = new File("conf/mlplan.properties");
	private static final boolean DEFAULT_USE_CACHE = false;
	private static final Predicate<TFDNode> DEFAULT_PRIORIZING_PREDICATE = null;
	private static final String DEFAULT_REQUESTED_HASCO_INTERFACE = SLC_REQUESTED_HASCO_INTERFACE;

	/**
	 * Default configurations including search space configuration files and lists of preferences with respect to the classifiers to be evaluated.
	 *
	 * @author mwever
	 */
	private enum EDefaultConfig {
		TINYTEST(SPC_TINYTEST, PREFC_AUTO_WEKA), AUTO_WEKA(SPC_AUTO_WEKA, PREFC_AUTO_WEKA), SKLEARN(SPC_SKLEARN, PREFC_SKLEARN), SKLEARN_UL(SPC_SKLEARN_UL, PREFC_SKLEARN_UL), MEKA(SPC_MEKA, PREFC_MEKA);

		/* Search space configuration file */
		private final File searchSpaceConfigurationFile;
		/* File containing a list of components defining an ordering of preference. */
		private final File preferredComponentsFile;

		private EDefaultConfig(final File spcFile, final File preferredComponentsFile) {
			this.searchSpaceConfigurationFile = spcFile;
			this.preferredComponentsFile = preferredComponentsFile;
		}

		public File getSearchSpaceConfigFile() {
			return this.searchSpaceConfigurationFile;
		}

		public File getPreferredComponentsFile() {
			return this.preferredComponentsFile;
		}
	}

	private boolean factoryPreparedWithData = false;

	private MLPlanClassifierConfig algorithmConfig;

	@SuppressWarnings("rawtypes")
	private HASCOViaFDFactory hascoFactory = new HASCOViaFDFactory<GraphSearchInput<TFDNode, String>, Double>();
	private File searchSpaceConfigFile;
	private Collection<Component> components;
	private IClassifierFactory classifierFactory;
	private String requestedHASCOInterface;

	private PipelineValidityCheckingNodeEvaluator pipelineValidityCheckingNodeEvaluator;
	private INodeEvaluator<TFDNode, Double> preferredNodeEvaluator = null;

	/* The splitter is used to create the split for separating search and selection data */
	private IDatasetSplitter searchSelectionDatasetSplitter = new MulticlassClassStratifiedSplitter();
	private IDatasetSplitter searchPhaseDatasetSplitter = new MulticlassClassStratifiedSplitter();
	private IDatasetSplitter selectionPhaseDatasetSplitter = new MulticlassClassStratifiedSplitter();

	private boolean useCache;
	private PerformanceDBAdapter dbAdapter = null;

	private EMultiClassPerformanceMeasure singleLabelPerformanceMeasure;
	private EMultilabelPerformanceMeasure multiLabelPerformanceMeasure;
	private ISplitBasedClassifierEvaluator<Double> splitBasedClassifierEvaluator;

	private Predicate<TFDNode> priorizingPredicate = null;

	private IClassifierEvaluatorFactory factoryForPipelineEvaluationInSearchPhase = null;
	private IClassifierEvaluatorFactory factoryForPipelineEvaluationInSelectionPhase = null;

	public MLPlanBuilder() {
		super();

		/* Setting up all generic default values. */
		try {
			this.withAlgorithmConfigFile(DEFAULT_ALGORITHM_CONFIG_FILE);
		} catch (IllegalArgumentException e) {
			this.logger.error("The default algorithm configuration file could not be loaded.", e);
		}
		this.useCache = DEFAULT_USE_CACHE;
		this.priorizingPredicate = DEFAULT_PRIORIZING_PREDICATE;
		this.requestedHASCOInterface = DEFAULT_REQUESTED_HASCO_INTERFACE;
	}

	public MLPlanBuilder(final File searchSpaceConfigFile, final File algorithmConfigFile, final EMultiClassPerformanceMeasure performanceMeasure) {
		this();
		this.withAlgorithmConfigFile(algorithmConfigFile);
		this.searchSpaceConfigFile = searchSpaceConfigFile;
		this.singleLabelPerformanceMeasure = performanceMeasure;
		this.useCache = false;
	}

	public MLPlanBuilder(final File searchSpaceConfigFile, final File algorithmConfigFile, final EMultiClassPerformanceMeasure performanceMeasure, final PerformanceDBAdapter dbAdapter) {
		this(searchSpaceConfigFile, algorithmConfigFile, performanceMeasure);
		this.useCache = true;
		this.dbAdapter = dbAdapter;
	}

	/**
	 * Set the classifier factory that translates <code>CompositionInstance</code> objects to classifiers that can be evaluated.
	 *
	 * @param classifierFactory The classifier factory to be used to translate CompositionInstance objects to classifiers.
	 */
	public void withClassifierFactory(final IClassifierFactory classifierFactory) {
		this.classifierFactory = classifierFactory;
	}

	public MLPlanBuilder withSearchSpaceConfigFile(final File searchSpaceConfig) throws IOException {
		FileUtil.requireFileExists(searchSpaceConfig);
		this.searchSpaceConfigFile = searchSpaceConfig;
		this.components = new ComponentLoader(searchSpaceConfig).getComponents();
		return this;
	}

	public MLPlanBuilder withDatasetSplitterForSearchSelectionSplit(final IDatasetSplitter datasetSplitter) {
		this.searchSelectionDatasetSplitter = datasetSplitter;
		return this;
	}

	public MLPlanBuilder withSearchPhaseDatasetSplitter(final IDatasetSplitter datasetSplitter) {
		this.searchPhaseDatasetSplitter = datasetSplitter;
		return this;
	}

	public MLPlanBuilder withSelectionPhaseDatasetSplitter(final IDatasetSplitter datasetSplitter) {
		this.selectionPhaseDatasetSplitter = datasetSplitter;
		return this;
	}

	public MLPlanBuilder withRequestedInterface(final String requestedInterface) {
		this.requestedHASCOInterface = requestedInterface;
		return this;
	}

	/**
	 * Configures the MLPlanBuilder to deal with the AutoSKLearn search space configuration.
	 *
	 * @return Returns the current MLPlanBuilder object with the AutoSKLearn search space configuration.
	 * @throws IOException Throws an IOException if the search space config file could not be loaded.
	 */
	public MLPlanBuilder withAutoSKLearnConfig() throws IOException {
		this.classifierFactory = new SKLearnClassifierFactory();
		return this.withDefaultConfiguration(EDefaultConfig.SKLEARN);
	}

	public MLPlanBuilder withTpotConfig() throws IOException {
		this.classifierFactory = new SKLearnClassifierFactory();
		return this.withDefaultConfiguration(EDefaultConfig.SKLEARN_UL);
	}

	public MLPlanBuilder withAutoWEKAConfiguration() throws IOException {
		this.classifierFactory = new WEKAPipelineFactory();
		this.pipelineValidityCheckingNodeEvaluator = new WekaPipelineValidityCheckingNodeEvaluator();
		return this.withDefaultConfiguration(EDefaultConfig.AUTO_WEKA);
	}

	public MLPlanBuilder withTinyTestConfiguration() throws IOException {
		this.classifierFactory = new WEKAPipelineFactory();
		this.pipelineValidityCheckingNodeEvaluator = new WekaPipelineValidityCheckingNodeEvaluator();
		return this.withDefaultConfiguration(EDefaultConfig.TINYTEST);
	}

	public MLPlanBuilder withMekaDefaultConfiguration() throws IOException {
		this.withDefaultConfiguration(EDefaultConfig.MEKA);
		this.singleLabelPerformanceMeasure = null;
		this.multiLabelPerformanceMeasure = EMultilabelPerformanceMeasure.AUTO_MEKA_GGP_FITNESS_LOSS;
		this.splitBasedClassifierEvaluator = new SimpleMLCSplitBasedClassifierEvaluator(new AutoMEKAGGPFitnessMeasureLoss());
		this.classifierFactory = new MekaPipelineFactory();
		this.requestedHASCOInterface = MLC_REQUESTED_HASCO_INTERFACE;
		this.withDatasetSplitterForSearchSelectionSplit(new ArbitrarySplitter());
		this.withSearchPhaseDatasetSplitter(new ArbitrarySplitter());
		this.withSelectionPhaseDatasetSplitter(new ArbitrarySplitter());
		return this;
	}

	private MLPlanBuilder withDefaultConfiguration(final EDefaultConfig defConfig) throws IOException {
		if (this.searchSpaceConfigFile == null) {
			this.withSearchSpaceConfigFile(defConfig.getSearchSpaceConfigFile());
		}
		this.withPreferredComponentsFile(defConfig.preferredComponentsFile);
		this.withRandomCompletionBasedBestFirstSearch();
		if (defConfig != EDefaultConfig.MEKA && this.singleLabelPerformanceMeasure == null) {
			this.singleLabelPerformanceMeasure = EMultiClassPerformanceMeasure.ERRORRATE;
			this.withSingleLabelClassificationMeasure(this.singleLabelPerformanceMeasure);
		}

		/* use MCCV for pipeline evaluation */
		int mccvIterationsDuringSearch = 5;
		int mccvIterationsDuringSelection = 5;
		double mccvPortion = 0.7;
		if (!(this.factoryForPipelineEvaluationInSearchPhase instanceof MonteCarloCrossValidationEvaluatorFactory)) {
			this.factoryForPipelineEvaluationInSearchPhase = new MonteCarloCrossValidationEvaluatorFactory().withNumMCIterations(mccvIterationsDuringSearch).withTrainFoldSize(mccvPortion).withSplitBasedEvaluator(new SimpleSLCSplitBasedClassifierEvaluator(new ZeroOneLoss()));
		}
		if (!(this.factoryForPipelineEvaluationInSelectionPhase instanceof MonteCarloCrossValidationEvaluatorFactory)) {
			this.factoryForPipelineEvaluationInSelectionPhase = new MonteCarloCrossValidationEvaluatorFactory().withNumMCIterations(mccvIterationsDuringSearch).withTrainFoldSize(mccvPortion).withSplitBasedEvaluator(new SimpleSLCSplitBasedClassifierEvaluator(new ZeroOneLoss()));
		}

		/* configure blow-ups for MCCV */
		double blowUpInSelectionPhase = MathExt.round(1f / mccvPortion * mccvIterationsDuringSelection / mccvIterationsDuringSearch, 2);
		this.algorithmConfig.setProperty(MLPlanClassifierConfig.K_BLOWUP_SELECTION, String.valueOf(blowUpInSelectionPhase));
		double blowUpInPostprocessing = MathExt.round((1 / (1 - this.algorithmConfig.dataPortionForSelection())) / mccvIterationsDuringSelection, 2);
		this.algorithmConfig.setProperty(MLPlanClassifierConfig.K_BLOWUP_POSTPROCESS, String.valueOf(blowUpInPostprocessing));
		return this;
	}

	public MLPlanBuilder withPreferredComponentsFile(final File preferredComponentsFile) throws IOException {
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
	 * Loads the MLPlanClassifierConfig with default values and replaces all properties according to the properties defined in the given config file.
	 *
	 * @param algorithmConfigFile The file specifying the property values to replace the default configuration.
	 * @return The MLPlanBuilder object.
	 * @throws IOException An IOException is thrown if there are issues reading the config file.
	 */
	public MLPlanBuilder withAlgorithmConfigFile(final File algorithmConfigFile) {
		return this.withAlgorithmConfig((MLPlanClassifierConfig) ConfigFactory.create(MLPlanClassifierConfig.class).loadPropertiesFromFile(algorithmConfigFile));
	}

	public MLPlanBuilder withAlgorithmConfig(final MLPlanClassifierConfig config) {
		this.algorithmConfig = config;
		this.hascoFactory.withAlgorithmConfig(this.algorithmConfig);
		this.updateEverything();
		return this;
	}

	public MLPlanBuilder withSingleLabelClassificationMeasure(final EMultiClassPerformanceMeasure measure) {
		this.singleLabelPerformanceMeasure = measure;
		return this.withSplitBasedClassifierEvaluator(this.getSingleLabelEvaluationMeasurementBridge(new MultiClassMeasureBuilder().getEvaluator(measure)));
	}

	public MLPlanBuilder withMultiLabelClassificationMeasure(final EMultilabelPerformanceMeasure measure) {
		this.multiLabelPerformanceMeasure = measure;
		return this.withSplitBasedClassifierEvaluator(this.getMultiLabelEvaluationMeasurementBridge(new MultiClassMeasureBuilder().getEvaluator(measure)));
	}

	/**
	 * This ADDs a new preferred node evaluator; requires that the search will be a best-first search.
	 *
	 * It is possible to specify several preferred node evaluators, which will be ordered by the order in which they are specified. The latest given evaluator is the most preferred one.
	 *
	 * @param preferredNodeEvaluator
	 * @return
	 */
	public MLPlanBuilder withPreferredNodeEvaluator(final INodeEvaluator<TFDNode, Double> preferredNodeEvaluator) {
		if (this.factoryPreparedWithData) {
			throw new IllegalStateException("The method prepareNodeEvaluatorInFactoryWithData has already been called. No changes to the preferred node evaluator possible anymore");
		}

		/* first update the preferred node evaluator */
		if (this.preferredNodeEvaluator == null) {
			this.preferredNodeEvaluator = preferredNodeEvaluator;
		} else {
			this.preferredNodeEvaluator = new AlternativeNodeEvaluator<>(preferredNodeEvaluator, this.preferredNodeEvaluator);
		}
		this.updateEverything();
		return this;
	}

	public MLPlanBuilder withSplitBasedClassifierEvaluator(final ISplitBasedClassifierEvaluator<Double> evaluator) {
		this.splitBasedClassifierEvaluator = evaluator;
		return this;
	}

	@SuppressWarnings("unchecked")
	public MLPlanBuilder withSearchFactory(@SuppressWarnings("rawtypes") final IOptimalPathInORGraphSearchFactory searchFactory, @SuppressWarnings("rawtypes") final AlgorithmicProblemReduction transformer) {
		this.hascoFactory.setSearchFactory(searchFactory);
		this.hascoFactory.setSearchProblemTransformer(transformer);
		return this;
	}

	@SuppressWarnings("unchecked")
	public MLPlanBuilder withRandomCompletionBasedBestFirstSearch() {
		this.hascoFactory.setSearchFactory(new StandardBestFirstFactory<TFDNode, String, Double>());
		this.updateEverything();
		return this;
	}

	public MLPlanBuilder withTimeoutForSingleSolutionEvaluation(final TimeOut timeout) {
		this.getAlgorithmConfig().setProperty(MLPlanClassifierConfig.K_RANDOM_COMPLETIONS_TIMEOUT_PATH, String.valueOf(timeout.milliseconds()));
		this.updateEverything();
		return this;
	}

	public MLPlanBuilder withTimeoutForNodeEvaluation(final TimeOut timeout) {
		this.getAlgorithmConfig().setProperty(MLPlanClassifierConfig.K_RANDOM_COMPLETIONS_TIMEOUT_NODE, String.valueOf(timeout.milliseconds()));
		this.updateEverything();
		return this;
	}

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
		this.updateEverything();
	}

	@SuppressWarnings("unchecked")
	private void updateSearchProblemTransformer() {
		this.hascoFactory.setSearchProblemTransformer(new GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformerViaRDFS<TFDNode, String, Double>(this.preferredNodeEvaluator, this.priorizingPredicate,
				this.algorithmConfig.randomSeed(), this.algorithmConfig.numberOfRandomCompletions(), this.algorithmConfig.timeoutForCandidateEvaluation(), this.algorithmConfig.timeoutForNodeEvaluation()));

	}

	private void updateAlgorithmConfigOfHASCO() {
		this.hascoFactory.withAlgorithmConfig(this.getAlgorithmConfig());
	}

	private void updateEverything() {
		this.updateSearchProblemTransformer();
		this.updateAlgorithmConfigOfHASCO();
	}

	/**
	 * @return The dataset splitter that is used for separating search and selection data.
	 */
	public IDatasetSplitter getSearchSelectionDatasetSplitter() {
		return this.searchSelectionDatasetSplitter;
	}

	/**
	 * @return The dataset splitter to be used in search phase for generating benchmark splits.
	 */
	public IDatasetSplitter getSearchPhaseDatasetSplitter() {
		return this.searchPhaseDatasetSplitter;
	}

	/**
	 * @return The dataset splitter to be used in selection phase for generating benchmark splits.
	 */
	public IDatasetSplitter getSelectionPhaseDatasetSplitter() {
		return this.selectionPhaseDatasetSplitter;
	}

	/**
	 * @return The interface that is requested to be provided by a solution candidate component instance.
	 */
	public String getRequestedInterface() {
		return this.requestedHASCOInterface;
	}

	//	public void withExtrapolatedSaturationPointEvaluation(final int[] anchorpoints, final ISamplingAlgorithmFactory<IInstance, ? extends ASamplingAlgorithm<IInstance>> subsamplingAlgorithmFactory,
	//			final double trainSplitForAnchorpointsMeasurement, final LearningCurveExtrapolationMethod extrapolationMethod) {
	//		this.builderForPipelineEvaluationInSearchPhase = new ExtrapolatedSaturationPointEvaluatorFactory(anchorpoints, subsamplingAlgorithmFactory, trainSplitForAnchorpointsMeasurement, extrapolationMethod);
	//
	//	}

	public void  withLearningCurveExtrapolationEvaluation(final int[] anchorpoints, final ISamplingAlgorithmFactory<WekaInstances<Object>, ? extends ASamplingAlgorithm<WekaInstances<Object>>> subsamplingAlgorithmFactory,
			final double trainSplitForAnchorpointsMeasurement, final LearningCurveExtrapolationMethod extrapolationMethod) {
		this.factoryForPipelineEvaluationInSearchPhase = new LearningCurveExtrapolationEvaluatorFactory(anchorpoints, subsamplingAlgorithmFactory, trainSplitForAnchorpointsMeasurement, extrapolationMethod);
		//		this.factoryForPipelineEvaluationInSelectionPhase = new LearningCurveExtrapolationEvaluatorFactory(anchorpoints, subsamplingAlgorithmFactory, trainSplitForAnchorpointsMeasurement, extrapolationMethod);
		this.factoryForPipelineEvaluationInSelectionPhase = new MonteCarloCrossValidationEvaluatorFactory().withNumMCIterations(3).withTrainFoldSize(.7).withSplitBasedEvaluator(new SimpleSLCSplitBasedClassifierEvaluator(new ZeroOneLoss()));
		this.algorithmConfig.setProperty(MLPlanClassifierConfig.K_BLOWUP_SELECTION, "" + 4); // evaluating on 1000 in selection MCCV is, assuming quadratic growth, roughly max 4 times costlier than search phase evaluations
	}

	public boolean getUseCache() {
		return this.useCache;
	}

	public PerformanceDBAdapter getDBAdapter() {
		return this.dbAdapter;
	}

	public IClassifierFactory getClassifierFactory() {
		return this.classifierFactory;
	}

	public Collection<Component> getComponents() {
		return this.components;
	}

	public File getSearchSpaceConfigFile() {
		return this.searchSpaceConfigFile;
	}

	public MLPlanClassifierConfig getAlgorithmConfig() {
		return this.algorithmConfig;
	}

	public EMultiClassPerformanceMeasure getSingleLabelPerformanceMeasure() {
		return this.singleLabelPerformanceMeasure;
	}

	public EMultilabelPerformanceMeasure getMultiLabelPerformanceMeasure() {
		return this.multiLabelPerformanceMeasure;
	}

	public ISplitBasedClassifierEvaluator<Double> getSingleLabelEvaluationMeasurementBridge(final IMeasure<Double, Double> measure) {
		if (this.splitBasedClassifierEvaluator == null) {
			if (this.getUseCache()) {
				return new CacheEvaluatorMeasureBridge(measure, this.getDBAdapter());
			} else {
				return new SimpleSLCSplitBasedClassifierEvaluator(measure);
			}
		} else {
			return this.splitBasedClassifierEvaluator;
		}
	}

	public ISplitBasedClassifierEvaluator<Double> getMultiLabelEvaluationMeasurementBridge(final IMeasure<double[], Double> measure) {
		if (this.splitBasedClassifierEvaluator == null) {
			return new SimpleMLCSplitBasedClassifierEvaluator(measure);
		} else {
			return this.splitBasedClassifierEvaluator;
		}
	}

	@SuppressWarnings("rawtypes")
	public HASCOFactory getHASCOFactory() {
		return this.hascoFactory;
	}

	//	public ISplitBasedClassifierEvaluator<Double> getEvaluationMeasurementBridge() {
	//		if (this.splitBasedClassifierEvaluator != null) {
	//			return this.splitBasedClassifierEvaluator;
	//		}
	//
	//		if (this.measure != null) {
	//			return this.getSingleLabelEvaluationMeasurementBridge(this.measure);
	//		} else {
	//			throw new IllegalStateException("Can not create evaluator measure bridge without a measure.");
	//		}
	//	}

	@Override
	public String toString() {
		Map<String, Object> fields = new HashMap<>();
		fields.put("algorithmConfig", this.getAlgorithmConfig());
		fields.put("classifierFactory", this.classifierFactory);
		return ToJSONStringUtil.toJSONString(fields);
	}

	public IClassifierEvaluatorFactory getFactoryForPipelineEvaluationInSearchPhase() {
		return this.factoryForPipelineEvaluationInSearchPhase;
	}

	public IClassifierEvaluatorFactory getFactoryForPipelineEvaluationInSelectionPhase() {
		return this.factoryForPipelineEvaluationInSelectionPhase;
	}

	public PipelineEvaluator getClassifierEvaluationInSearchPhase(final Instances data, final int seed, final int fullDatasetSize) throws ClassifierEvaluatorConstructionFailedException {
		if (this.factoryForPipelineEvaluationInSearchPhase == null) {
			throw new IllegalStateException("No factory for pipeline evaluation in search phase has been set!");
		}
		IClassifierEvaluator evaluator = this.factoryForPipelineEvaluationInSearchPhase.getIClassifierEvaluator(data, seed);
		if (evaluator instanceof LearningCurveExtrapolationEvaluator) {
			((LearningCurveExtrapolationEvaluator) evaluator).setFullDatasetSize(fullDatasetSize);
		}
		return new PipelineEvaluator(this.getClassifierFactory(), evaluator, this.getAlgorithmConfig().timeoutForCandidateEvaluation());
	}

	public PipelineEvaluator getClassifierEvaluationInSelectionPhase(final Instances data, final int seed) throws ClassifierEvaluatorConstructionFailedException {
		if (this.factoryForPipelineEvaluationInSelectionPhase == null) {
			throw new IllegalStateException("No factory for pipeline evaluation in selection phase has been set!");
		}
		return new PipelineEvaluator(this.getClassifierFactory(), this.factoryForPipelineEvaluationInSelectionPhase.getIClassifierEvaluator(data, seed), Integer.MAX_VALUE);
	}
}
