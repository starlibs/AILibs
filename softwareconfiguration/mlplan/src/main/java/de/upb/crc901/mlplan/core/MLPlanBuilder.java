package de.upb.crc901.mlplan.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.aeonbits.owner.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.upb.crc901.mlpipeline_evaluation.PerformanceDBAdapter;
import de.upb.crc901.mlplan.multiclass.MLPlanClassifierConfig;
import de.upb.crc901.mlplan.multiclass.wekamlplan.ClassifierFactory;
import de.upb.crc901.mlplan.multiclass.wekamlplan.weka.PreferenceBasedNodeEvaluator;
import de.upb.crc901.mlplan.multiclass.wekamlplan.weka.WEKAPipelineFactory;
import de.upb.crc901.mlplan.multiclass.wekamlplan.weka.WekaPipelineValidityCheckingNodeEvaluator;
import hasco.core.HASCOFactory;
import hasco.model.Component;
import hasco.serialization.ComponentLoader;
import hasco.variants.forwarddecomposition.HASCOViaFDAndBestFirstFactory;
import hasco.variants.forwarddecomposition.HASCOViaFDAndBestFirstWithRandomCompletionsFactory;
import jaicore.basic.FileUtil;
import jaicore.ml.core.evaluation.measure.singlelabel.MultiClassPerformanceMeasure;
import jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.AlternativeNodeEvaluator;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import jaicore.search.probleminputs.GraphSearchInput;
import weka.core.Instances;

public class MLPlanBuilder {
	static MLPlanClassifierConfig loadOwnerConfig(File configFile) throws IOException {
		Properties props = new Properties();
		if (configFile.exists()) {
			FileInputStream fis = new FileInputStream(configFile);
			props.load(fis);
		}
		else
			System.out.println("Config file " + configFile.getAbsolutePath() + " not found, working with default parameters.");
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
	private HASCOFactory<? extends GraphSearchInput<TFDNode,String>, TFDNode, String, Double> hascoFactory;
	
	public MLPlanBuilder() {
		super();
		this.hascoFactory = new HASCOViaFDAndBestFirstWithRandomCompletionsFactory(0, 3);
	}
	
	public MLPlanBuilder(File searchSpaceConfigFile, File alhorithmConfigFile, MultiClassPerformanceMeasure performanceMeasure) {
		this();
		this.searchSpaceConfigFile = searchSpaceConfigFile;
		this.algorithmConfigFile = alhorithmConfigFile;
		this.performanceMeasure = performanceMeasure;
		this.useCache = false;
	}
	
	public MLPlanBuilder(File searchSpaceConfigFile, File alhorithmConfigFile, MultiClassPerformanceMeasure performanceMeasure, PerformanceDBAdapter dbAdapter) {
		this();
		this.searchSpaceConfigFile = searchSpaceConfigFile;
		this.algorithmConfigFile = alhorithmConfigFile;
		this.performanceMeasure = performanceMeasure;
		this.useCache = true;
		this.dbAdapter = dbAdapter;
	}
	

	public File getSearchSpaceConfigFile() {
		return searchSpaceConfigFile;
	}

	public File getAlgorithmConfigFile() {
		return algorithmConfigFile;
	}

	public MLPlanClassifierConfig getAlgorithmConfig() {
		return algorithmConfig;
	}

	public MultiClassPerformanceMeasure getPerformanceMeasure() {
		return performanceMeasure;
	}

	public MLPlanBuilder withSearchSpaceConfigFile(File searchSpaceConfig) throws IOException {
		this.searchSpaceConfigFile = searchSpaceConfig;
		this.components = new ComponentLoader(searchSpaceConfig).getComponents();
		return this;
	}
	
	public MLPlanBuilder withAutoWEKAConfiguration() throws IOException {
		 withSearchSpaceConfigFile(new File("conf/automl/searchmodels/weka/weka-all-autoweka.json"));
		 File fileOfPreferredComponents = getAlgorithmConfig().preferredComponents();
		 List<String> ordering;
		 if (!fileOfPreferredComponents.exists()) {
			 logger.warn("The configured file for preferred components \"{}\" does not exist. Not using any particular ordering.", fileOfPreferredComponents.getAbsolutePath());
			 ordering = new ArrayList<>();
		 }
		 else
			 ordering = FileUtil.readFileAsList(fileOfPreferredComponents);
		 withPreferredNodeEvaluator(new PreferenceBasedNodeEvaluator(components, ordering));
		 this.classifierFactory = new WEKAPipelineFactory();
		 this.pipelineValidityCheckingNodeEvaluator = new WekaPipelineValidityCheckingNodeEvaluator();
		 return this;
	}

	public MLPlanBuilder withAlgorithmConfigFile(File algorithmConfigFile) throws IOException {
		return withAlgorithmConfig(loadOwnerConfig(algorithmConfigFile));
	}
	
	public MLPlanBuilder withAlgorithmConfig(MLPlanClassifierConfig config) {
		this.algorithmConfig = config;
		return this;
	}

	public MLPlanBuilder withPerformanceMeasure(MultiClassPerformanceMeasure performanceMeasure) {
		this.performanceMeasure = performanceMeasure;
		return this;
	}

	/**
	 * This ADDs a new preferred node evaluator; requires that the search will be a best-first search.
	 * 
	 * It is possible to specify several preferred node evaluators, which will be ordered by the order in which they are specified.
	 * The latest given evaluator is the most preferred one.
	 * 
	 * @param preferredNodeEvaluator
	 * @return
	 */
	public MLPlanBuilder withPreferredNodeEvaluator(INodeEvaluator<TFDNode, Double> preferredNodeEvaluator) {

		if (!(hascoFactory instanceof HASCOViaFDAndBestFirstFactory))
			throw new IllegalStateException("Cannot define a preferred node evaluator if the hasco factory is not a HASCOViaFDAndBestFirstFactory (or a subclass of it)");
		
		/* first update the preferred node evaluator */
		if (this.preferredNodeEvaluator == null) {
			this.preferredNodeEvaluator = preferredNodeEvaluator;
		}
		else {
			this.preferredNodeEvaluator = new AlternativeNodeEvaluator<>(preferredNodeEvaluator, this.preferredNodeEvaluator);
		}
		return this;
	}
	
	@SuppressWarnings("unchecked")
	public void prepareNodeEvaluatorInFactoryWithData(Instances data) {
		if (!(hascoFactory instanceof HASCOViaFDAndBestFirstFactory))
			throw new IllegalStateException("Cannot define a preferred node evaluator if the hasco factory is not a HASCOViaFDAndBestFirstFactory (or a subclass of it)");
		if (factoryPreparedWithData)
			throw new IllegalStateException("Factory has already been prepared with data. This can only be done once!");
		factoryPreparedWithData = true;
		
		/* nothing to do if there are no preferred node evaluators */
		if (pipelineValidityCheckingNodeEvaluator == null && preferredNodeEvaluator == null)
			return;
		
		HASCOViaFDAndBestFirstFactory<Double> factory = (HASCOViaFDAndBestFirstFactory<Double>)hascoFactory;
		
		/* now determine the real node evaluator to be used. A semantic node evaluator has highest priority */
		INodeEvaluator<TFDNode, Double> actualNodeEvaluator;
		if (pipelineValidityCheckingNodeEvaluator != null) {
			pipelineValidityCheckingNodeEvaluator.setComponents(components);
			pipelineValidityCheckingNodeEvaluator.setData(data);
			if (preferredNodeEvaluator != null)
				actualNodeEvaluator = new AlternativeNodeEvaluator<>(pipelineValidityCheckingNodeEvaluator, this.preferredNodeEvaluator);
			else
				actualNodeEvaluator = pipelineValidityCheckingNodeEvaluator;
		}
		else
			actualNodeEvaluator = this.preferredNodeEvaluator;
		
		/* set the node evaluator as the preferred node evaluator in the search factory */
		factory.getSearchFactory().setPreferredNodeEvaluator(actualNodeEvaluator);
	}
	
	public HASCOFactory<? extends GraphSearchInput<TFDNode, String>, TFDNode, String, Double> getHASCOFactory() {
		if (!factoryPreparedWithData)
			throw new IllegalStateException("Data have not been set on the factory yet.");
		return hascoFactory;
	}
	
	public boolean getUseCache() {
		return useCache;
	}
	
	public PerformanceDBAdapter getDBAdapter() {
		return dbAdapter;
	}

	public ClassifierFactory getClassifierFactory() {
		return classifierFactory;
	}

	public Collection<Component> getComponents() {
		return components;
	}
}
