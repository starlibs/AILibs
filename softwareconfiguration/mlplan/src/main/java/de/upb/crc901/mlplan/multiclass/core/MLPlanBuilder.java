package de.upb.crc901.mlplan.multiclass.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Properties;

import org.aeonbits.owner.ConfigFactory;

import de.upb.crc901.mlpipeline_evaluation.PerformanceDBAdapter;
import de.upb.crc901.mlplan.multiclass.MLPlanClassifierConfig;
import de.upb.crc901.mlplan.multiclass.wekamlplan.ClassifierFactory;
import de.upb.crc901.mlplan.multiclass.wekamlplan.weka.WEKAPipelineFactory;
import hasco.core.HASCOFactory;
import hasco.model.Component;
import hasco.serialization.ComponentLoader;
import hasco.variants.forwarddecomposition.HASCOViaFDAndBestFirstFactory;
import hasco.variants.forwarddecomposition.HASCOViaFDAndBestFirstWithRandomCompletionsFactory;
import jaicore.ml.core.evaluation.measure.singlelabel.MultiClassPerformanceMeasure;
import jaicore.planning.hierarchical.algorithms.forwarddecomposition.graphgenerators.tfd.TFDNode;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import jaicore.search.probleminputs.GraphSearchInput;

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
	
	private File searchSpaceConfigFile;
	private File algorithmConfigFile = new File("conf/mlplan.properties");
	private Collection<Component> components;
	private ClassifierFactory classifierFactory;
	private MLPlanClassifierConfig algorithmConfig = ConfigFactory.create(MLPlanClassifierConfig.class);
	private MultiClassPerformanceMeasure performanceMeasure = MultiClassPerformanceMeasure.ERRORRATE;
	private boolean useCache = false;
	private PerformanceDBAdapter dbAdapter = null;
	
	private INodeEvaluator<TFDNode, Double> preferredNodeEvaluator;
	private HASCOFactory<? extends GraphSearchInput<TFDNode,String>, TFDNode, String, Double> hascoFactory;
	
	public MLPlanBuilder() {
		super();
		this.hascoFactory = new HASCOViaFDAndBestFirstWithRandomCompletionsFactory();
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
		 this.classifierFactory = new WEKAPipelineFactory();
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
	
	@SuppressWarnings("unchecked")
	public MLPlanBuilder withPreferredNodeEvaluator(INodeEvaluator<TFDNode, Double> preferredNodeEvaluator) {
		if (!(hascoFactory instanceof HASCOViaFDAndBestFirstFactory))
			throw new IllegalStateException("Cannot define a preferred node evaluator if the hasco factory is not a HASCOViaFDAndBestFirstFactory (or a subclass of it)");
		((HASCOViaFDAndBestFirstFactory<Double>)hascoFactory).getSearchFactory().setNodeEvaluator(preferredNodeEvaluator);
		return this;
	}
	
	public HASCOFactory<? extends GraphSearchInput<TFDNode, String>, TFDNode, String, Double> getHASCOFactory() {
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
