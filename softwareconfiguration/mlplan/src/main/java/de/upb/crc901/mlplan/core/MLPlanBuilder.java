package de.upb.crc901.mlplan.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Predicate;

import org.aeonbits.owner.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.upb.crc901.mlpipeline_evaluation.PerformanceDBAdapter;
import de.upb.crc901.mlplan.multiclass.MLPlanClassifierConfig;
import de.upb.crc901.mlplan.multiclass.wekamlplan.ClassifierFactory;
import de.upb.crc901.mlplan.multiclass.wekamlplan.sklearn.SKLearnClassifierFactory;
import de.upb.crc901.mlplan.multiclass.wekamlplan.weka.PreferenceBasedNodeEvaluator;
import de.upb.crc901.mlplan.multiclass.wekamlplan.weka.WEKAPipelineFactory;
import de.upb.crc901.mlplan.multiclass.wekamlplan.weka.WekaPipelineValidityCheckingNodeEvaluator;
import hasco.core.HASCOFactory;
import hasco.model.Component;
import hasco.serialization.ComponentLoader;
import hasco.variants.forwarddecomposition.HASCOViaFDAndBestFirstFactory;
import hasco.variants.forwarddecomposition.HASCOViaFDFactory;
import jaicore.basic.FileUtil;
import jaicore.basic.TimeOut;
import jaicore.basic.algorithm.reduction.AlgorithmicProblemReduction;
import jaicore.logging.ToJSONStringUtil;
import jaicore.ml.core.dataset.IInstance;
import jaicore.ml.core.dataset.sampling.inmemory.ASamplingAlgorithm;
import jaicore.ml.core.dataset.sampling.inmemory.factories.interfaces.ISamplingAlgorithmFactory;
import jaicore.ml.core.evaluation.measure.singlelabel.MultiClassPerformanceMeasure;
import jaicore.ml.evaluation.evaluators.weka.factory.ExtrapolatedSaturationPointEvaluatorFactory;
import jaicore.ml.evaluation.evaluators.weka.factory.IClassifierEvaluatorFactory;
import jaicore.ml.evaluation.evaluators.weka.factory.LearningCurveExtrapolationEvaluatorFactory;
import jaicore.ml.learningcurve.extrapolation.LearningCurveExtrapolationMethod;
import jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import jaicore.search.algorithms.standard.bestfirst.StandardBestFirstFactory;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.AlternativeNodeEvaluator;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import jaicore.search.core.interfaces.IOptimalPathInORGraphSearchFactory;
import jaicore.search.probleminputs.GraphSearchInput;
import jaicore.search.problemtransformers.GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformerViaRDFS;
import weka.core.Instances;

public class MLPlanBuilder {

	private static final Logger L = LoggerFactory.getLogger(MLPlanBuilder.class);

	private static final File SPC_TINYTEST = new File("resources/automl/searchmodels/weka/tinytest.json");
	private static final File SPC_AUTO_WEKA = new File("resources/automl/searchmodels/weka/weka-all-autoweka.json");
	private static final File SPC_SKLEARN = new File("resources/automl/searchmodels/sklearn/sklearn-mlplan.json");
	private static final File SPC_SKLEARN_UL = new File("resources/automl/searchmodels/sklearn/ml-plan-ul.json");

	private static final File PREFC_AUTO_WEKA = new File("resources/mlplan/weka-precedenceList.txt");
	private static final File PREFC_SKLEARN = new File("resources/mlplan/sklearn-precedenceList.txt");
	private static final File PREFC_SKLEARN_UL = new File("resources/mlplan/sklearn-ul-precedenceList.txt");

	private enum DefaultConfig {
		TINYTEST(SPC_TINYTEST, PREFC_AUTO_WEKA), AUTO_WEKA(SPC_AUTO_WEKA, PREFC_AUTO_WEKA), SKLEARN(SPC_SKLEARN, PREFC_SKLEARN), SKLEARN_UL(SPC_SKLEARN_UL, PREFC_SKLEARN_UL);

		private final File spcFile;
		private final File preferredComponentsFile;

		private DefaultConfig(final File spcFile, final File preferredComponentsFile) {
			this.spcFile = spcFile;
			this.preferredComponentsFile = preferredComponentsFile;
		}

		public File getSearchSpaceConfigFile() {
			return this.spcFile;
		}

		public File getPreferredComponentsFile() {
			return this.preferredComponentsFile;
		}
	}

	static MLPlanClassifierConfig loadOwnerConfig(final File configFile) throws IOException {
		Properties props = new Properties();
		if (configFile.exists()) {
			FileInputStream fis = new FileInputStream(configFile);
			props.load(fis);
		} else {
			L.warn("Config file {} not found, working with default parameters.", configFile.getAbsolutePath());
		}
		return ConfigFactory.create(MLPlanClassifierConfig.class, props);
	}

	private Logger logger = LoggerFactory.getLogger(MLPlanBuilder.class);
	private File searchSpaceConfigFile;
	private File algorithmConfigFile = new File("conf/mlplan.properties");
	private Collection<Component> components;
	private ClassifierFactory classifierFactory;
	private MLPlanClassifierConfig algorithmConfig = ConfigFactory.create(MLPlanClassifierConfig.class);
	private MultiClassPerformanceMeasure performanceMeasure = MultiClassPerformanceMeasure.ERRORRATE;
	private boolean useCache = false;
	private boolean factoryPreparedWithData = false;
	private PerformanceDBAdapter dbAdapter = null;

	private PipelineValidityCheckingNodeEvaluator pipelineValidityCheckingNodeEvaluator;
	private INodeEvaluator<TFDNode, Double> preferredNodeEvaluator = null;

	@SuppressWarnings("rawtypes")
	private HASCOViaFDFactory hascoFactory = new HASCOViaFDFactory<GraphSearchInput<TFDNode, String>, Double>();

	private Predicate<TFDNode> priorizingPredicate = null;

	private IClassifierEvaluatorFactory classifierEvaluatorFactory = null;

	public MLPlanBuilder() {
		super();
	}

	public MLPlanBuilder(final File searchSpaceConfigFile, final File alhorithmConfigFile, final MultiClassPerformanceMeasure performanceMeasure) {
		this();
		this.searchSpaceConfigFile = searchSpaceConfigFile;
		this.algorithmConfigFile = alhorithmConfigFile;
		this.performanceMeasure = performanceMeasure;
		this.useCache = false;
	}

	public MLPlanBuilder(final File searchSpaceConfigFile, final File algorithmConfigFile, final MultiClassPerformanceMeasure performanceMeasure, final PerformanceDBAdapter dbAdapter) {
		this(searchSpaceConfigFile, algorithmConfigFile, performanceMeasure);
		this.useCache = true;
		this.dbAdapter = dbAdapter;
	}

	/**
	 * Set the classifier factory that translates <code>CompositionInstance</code> objects to classifiers that can be evaluated.
	 *
	 * @param classifierFactory The classifier factory to be used to translate CompositionInstance objects to classifiers.
	 */
	public void withClassifierFactory(final ClassifierFactory classifierFactory) {
		this.classifierFactory = classifierFactory;
	}

	public File getSearchSpaceConfigFile() {
		return this.searchSpaceConfigFile;
	}

	public File getAlgorithmConfigFile() {
		return this.algorithmConfigFile;
	}

	public MLPlanClassifierConfig getAlgorithmConfig() {
		return this.algorithmConfig;
	}

	public MultiClassPerformanceMeasure getPerformanceMeasure() {
		return this.performanceMeasure;
	}

	public MLPlanBuilder withSearchSpaceConfigFile(final File searchSpaceConfig) throws IOException {
		this.searchSpaceConfigFile = searchSpaceConfig;
		this.components = new ComponentLoader(searchSpaceConfig).getComponents();
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
		return this.withDefaultConfig(DefaultConfig.SKLEARN);
	}

	public MLPlanBuilder withTpotConfig() throws IOException {
		this.classifierFactory = new SKLearnClassifierFactory();
		return this.withDefaultConfig(DefaultConfig.SKLEARN_UL);
	}

	public MLPlanBuilder withAutoWEKAConfiguration() throws IOException {
		this.classifierFactory = new WEKAPipelineFactory();
		this.pipelineValidityCheckingNodeEvaluator = new WekaPipelineValidityCheckingNodeEvaluator();
		return this.withDefaultConfig(DefaultConfig.AUTO_WEKA);
	}

	public MLPlanBuilder withTinyTestConfiguration() throws IOException {
		this.classifierFactory = new WEKAPipelineFactory();
		this.pipelineValidityCheckingNodeEvaluator = new WekaPipelineValidityCheckingNodeEvaluator();
		return this.withDefaultConfig(DefaultConfig.TINYTEST);
	}

	private MLPlanBuilder withDefaultConfig(final DefaultConfig defConfig) throws IOException {
		if (this.searchSpaceConfigFile == null) {
			this.withSearchSpaceConfigFile(defConfig.getSearchSpaceConfigFile());
		}
		File fileOfPreferredComponents = defConfig.getPreferredComponentsFile();
		List<String> ordering;
		if (!fileOfPreferredComponents.exists()) {
			this.logger.warn("The configured file for preferred components \"{}\" does not exist. Not using any particular ordering.", fileOfPreferredComponents.getAbsolutePath());
			ordering = new ArrayList<>();
		} else {
			ordering = FileUtil.readFileAsList(fileOfPreferredComponents);
		}
		this.withPreferredNodeEvaluator(new PreferenceBasedNodeEvaluator(this.components, ordering));
		this.withRandomCompletionBasedBestFirstSearch();

		return this;
	}

	public MLPlanBuilder withAlgorithmConfigFile(final File algorithmConfigFile) throws IOException {
		return this.withAlgorithmConfig(loadOwnerConfig(algorithmConfigFile));
	}

	public MLPlanBuilder withAlgorithmConfig(final MLPlanClassifierConfig config) {
		this.algorithmConfig = config;
		this.updateSearchProblemTransformer();
		return this;
	}

	public MLPlanBuilder withPerformanceMeasure(final MultiClassPerformanceMeasure performanceMeasure) {
		this.performanceMeasure = performanceMeasure;
		return this;
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
		this.updateSearchProblemTransformer();
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
		this.updateSearchProblemTransformer();

	}

	@SuppressWarnings("rawtypes")
	public HASCOFactory getHASCOFactory() {
		return this.hascoFactory;
	}

	@SuppressWarnings("unchecked")
	public MLPlanBuilder withSearchFactory(@SuppressWarnings("rawtypes") final IOptimalPathInORGraphSearchFactory searchFactory, @SuppressWarnings("rawtypes") final AlgorithmicProblemReduction transformer) {
		this.hascoFactory.setSearchFactory(searchFactory);
		this.hascoFactory.setSearchProblemTransformer(transformer);
		return this;
	}

	public MLPlanBuilder withRandomCompletionBasedBestFirstSearch() {
		this.hascoFactory.setSearchFactory(new StandardBestFirstFactory<TFDNode, String, Double>());
		this.updateSearchProblemTransformer();
		return this;
	}

	private void updateSearchProblemTransformer() {
		this.hascoFactory.setSearchProblemTransformer(new GraphSearchProblemInputToGraphSearchWithSubpathEvaluationInputTransformerViaRDFS<TFDNode, String, Double>(this.preferredNodeEvaluator, this.priorizingPredicate,
				this.algorithmConfig.randomSeed(), this.algorithmConfig.numberOfRandomCompletions(), this.algorithmConfig.timeoutForCandidateEvaluation(), this.algorithmConfig.timeoutForNodeEvaluation()));

	}

	public void withTimeoutForSingleSolutionEvaluation(final TimeOut timeout) {
		this.getAlgorithmConfig().setProperty(MLPlanClassifierConfig.K_RANDOM_COMPLETIONS_TIMEOUT_PATH, String.valueOf(timeout.milliseconds()));
	}

	public void withTimeoutForNodeEvaluation(final TimeOut timeout) {
		this.getAlgorithmConfig().setProperty(MLPlanClassifierConfig.K_RANDOM_COMPLETIONS_TIMEOUT_NODE, String.valueOf(timeout.milliseconds()));
	}

	public void withExtrapolatedSaturationPointEvaluation(int[] anchorpoints,
			ISamplingAlgorithmFactory<IInstance, ? extends ASamplingAlgorithm<IInstance>> subsamplingAlgorithmFactory,
			double trainSplitForAnchorpointsMeasurement, LearningCurveExtrapolationMethod extrapolationMethod) {
		this.classifierEvaluatorFactory = new ExtrapolatedSaturationPointEvaluatorFactory(anchorpoints,
				subsamplingAlgorithmFactory, trainSplitForAnchorpointsMeasurement, extrapolationMethod);

	}

	public void withLearningCurveExtrapolationEvaluation(int[] anchorpoints,
			ISamplingAlgorithmFactory<IInstance, ? extends ASamplingAlgorithm<IInstance>> subsamplingAlgorithmFactory,
			double trainSplitForAnchorpointsMeasurement, LearningCurveExtrapolationMethod extrapolationMethod) {
		this.classifierEvaluatorFactory = new LearningCurveExtrapolationEvaluatorFactory(anchorpoints,
				subsamplingAlgorithmFactory, trainSplitForAnchorpointsMeasurement, extrapolationMethod);
	}

	public boolean getUseCache() {
		return this.useCache;
	}

	public PerformanceDBAdapter getDBAdapter() {
		return this.dbAdapter;
	}

	public ClassifierFactory getClassifierFactory() {
		return this.classifierFactory;
	}

	public Collection<Component> getComponents() {
		return this.components;
	}

	@Override
	public String toString() {
		Map<String, Object> fields = new HashMap<>();
		fields.put("algorithmConfig", this.getAlgorithmConfig());
		fields.put("algorithmConfigFile", this.algorithmConfigFile);
		fields.put("classifierFactory", this.classifierFactory);
		return ToJSONStringUtil.toJSONString(fields);
	}

	public IClassifierEvaluatorFactory getClassifierEvaluatorFactory() {
		return classifierEvaluatorFactory;
	}

}
