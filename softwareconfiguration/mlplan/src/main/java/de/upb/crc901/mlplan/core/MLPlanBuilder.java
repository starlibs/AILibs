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
import hasco.variants.forwarddecomposition.HASCOViaFDAndBestFirstWithRandomCompletionsFactory;
import hasco.variants.forwarddecomposition.HASCOViaFDFactory;
import jaicore.basic.FileUtil;
import jaicore.basic.TimeOut;
import jaicore.basic.algorithm.reduction.AlgorithmicProblemReduction;
import jaicore.logging.ToJSONStringUtil;
import jaicore.ml.core.evaluation.measure.singlelabel.MultiClassPerformanceMeasure;
import jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.AlternativeNodeEvaluator;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import jaicore.search.core.interfaces.IOptimalPathInORGraphSearchFactory;
import jaicore.search.probleminputs.GraphSearchInput;
import weka.core.Instances;

public class MLPlanBuilder {
	static MLPlanClassifierConfig loadOwnerConfig(final File configFile) throws IOException {
		Properties props = new Properties();
		if (configFile.exists()) {
			FileInputStream fis = new FileInputStream(configFile);
			props.load(fis);
		} else {
			System.out.println("Config file " + configFile.getAbsolutePath() + " not found, working with default parameters.");
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
	private HASCOViaFDFactory<? extends GraphSearchInput<TFDNode, String>, Double> hascoFactory = new HASCOViaFDFactory<>();

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

	public MLPlanBuilder(final File searchSpaceConfigFile, final File alhorithmConfigFile, final MultiClassPerformanceMeasure performanceMeasure, final PerformanceDBAdapter dbAdapter) {
		this();
		this.searchSpaceConfigFile = searchSpaceConfigFile;
		this.algorithmConfigFile = alhorithmConfigFile;
		this.performanceMeasure = performanceMeasure;
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
		if (this.searchSpaceConfigFile == null) {
			this.withSearchSpaceConfigFile(new File("conf/automl/searchmodels/sklearn/sklearn-mlplan.json"));
		}

		File fileOfPreferredComponents = this.getAlgorithmConfig().preferredComponents();
		List<String> ordering;
		if (!fileOfPreferredComponents.exists()) {
			this.logger.warn("The configured file for preferred components \"{}\" does not exist. Not using any particular ordering.", fileOfPreferredComponents.getAbsolutePath());
			ordering = new ArrayList<>();
		} else {
			ordering = FileUtil.readFileAsList(fileOfPreferredComponents);
		}
		this.withPreferredNodeEvaluator(new PreferenceBasedNodeEvaluator(this.components, ordering));
		this.classifierFactory = new SKLearnClassifierFactory();
		return this;
	}

	public MLPlanBuilder withAutoWEKAConfiguration() throws IOException {
		if (this.searchSpaceConfigFile == null) {
			this.withSearchSpaceConfigFile(new File("conf/automl/searchmodels/weka/weka-all-autoweka.json"));
		}
		File fileOfPreferredComponents = this.getAlgorithmConfig().preferredComponents();
		List<String> ordering;
		if (!fileOfPreferredComponents.exists()) {
			this.logger.warn("The configured file for preferred components \"{}\" does not exist. Not using any particular ordering.", fileOfPreferredComponents.getAbsolutePath());
			ordering = new ArrayList<>();
		} else {
			ordering = FileUtil.readFileAsList(fileOfPreferredComponents);
		}
		this.withPreferredNodeEvaluator(new PreferenceBasedNodeEvaluator(this.components, ordering));
		this.classifierFactory = new WEKAPipelineFactory();
		this.pipelineValidityCheckingNodeEvaluator = new WekaPipelineValidityCheckingNodeEvaluator();
		return this;
	}

	public MLPlanBuilder withAlgorithmConfigFile(final File algorithmConfigFile) throws IOException {
		return this.withAlgorithmConfig(loadOwnerConfig(algorithmConfigFile));
	}

	public MLPlanBuilder withAlgorithmConfig(final MLPlanClassifierConfig config) {
		this.algorithmConfig = config;
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

		/* first update the preferred node evaluator */
		if (this.preferredNodeEvaluator == null) {
			this.preferredNodeEvaluator = preferredNodeEvaluator;
		} else {
			this.preferredNodeEvaluator = new AlternativeNodeEvaluator<>(preferredNodeEvaluator, this.preferredNodeEvaluator);
		}
		return this;
	}

	@SuppressWarnings("unchecked")
	public void prepareNodeEvaluatorInFactoryWithData(final Instances data) {
		if (!(this.hascoFactory instanceof HASCOViaFDAndBestFirstFactory)) {
			return;
			// throw new IllegalStateException("Cannot define a preferred node evaluator if the hasco factory is not a HASCOViaFDAndBestFirstFactory (or a subclass of it)");
		}
		if (this.factoryPreparedWithData) {
			throw new IllegalStateException("Factory has already been prepared with data. This can only be done once!");
		}
		this.factoryPreparedWithData = true;

		/* nothing to do if there are no preferred node evaluators */
		if (this.pipelineValidityCheckingNodeEvaluator == null && this.preferredNodeEvaluator == null) {
			return;
		}

		HASCOViaFDAndBestFirstFactory<Double> factory = (HASCOViaFDAndBestFirstFactory<Double>) this.hascoFactory;

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

		/* set the node evaluator as the preferred node evaluator in the search factory */
		factory.getSearchFactory().setPreferredNodeEvaluator(actualNodeEvaluator);
	}

	public HASCOFactory<? extends GraphSearchInput<TFDNode, String>, TFDNode, String, Double> getHASCOFactory() {
		//		if (!factoryPreparedWithData) {
		//			throw new IllegalStateException("Data have not been set on the factory yet.");
		//		}
		return this.hascoFactory;
	}

	@SuppressWarnings("unchecked")
	public MLPlanBuilder withSearchFactory(@SuppressWarnings("rawtypes") final IOptimalPathInORGraphSearchFactory searchFactory, final AlgorithmicProblemReduction transformer) {
		this.hascoFactory.setSearchFactory(searchFactory);
		this.hascoFactory.setSearchProblemTransformer(transformer);
		return this; 
	}

	public MLPlanBuilder withRandomCompletionBasedBestFirstSearch() {
		this.hascoFactory = new HASCOViaFDAndBestFirstWithRandomCompletionsFactory(this.algorithmConfig.randomSeed(),
				this.algorithmConfig.numberOfRandomCompletions(), this.algorithmConfig.timeoutForCandidateEvaluation(),
				this.algorithmConfig.timeoutForNodeEvaluation());
		return this;
	}

	public void withTimeoutForSingleSolutionEvaluation(final TimeOut timeout) {
		this.getAlgorithmConfig().setProperty(MLPlanClassifierConfig.K_RANDOM_COMPLETIONS_TIMEOUT_PATH, String.valueOf(timeout.milliseconds()));
	}

	public void withTimeoutForNodeEvaluation(final TimeOut timeout) {
		this.getAlgorithmConfig().setProperty(MLPlanClassifierConfig.K_RANDOM_COMPLETIONS_TIMEOUT_NODE, String.valueOf(timeout.milliseconds()));
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
}
