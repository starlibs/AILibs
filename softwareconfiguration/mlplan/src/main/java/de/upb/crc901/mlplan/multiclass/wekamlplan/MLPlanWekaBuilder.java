package de.upb.crc901.mlplan.multiclass.wekamlplan;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.aeonbits.owner.ConfigFactory;

import de.upb.crc901.mlpipeline_evaluation.PerformanceDBAdapter;
import de.upb.crc901.mlplan.multiclass.MLPlanClassifierConfig;
import jaicore.ml.core.evaluation.measure.singlelabel.MultiClassPerformanceMeasure;

public class MLPlanWekaBuilder {
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
	
	private File searchSpaceConfigFile = new File("conf/automl/searchmodels/weka/weka-all-autoweka.json");
	private File algorithmConfigFile = new File("conf/mlplan.properties");
	private MLPlanClassifierConfig algorithmConfig = ConfigFactory.create(MLPlanClassifierConfig.class);
	private MultiClassPerformanceMeasure performanceMeasure = MultiClassPerformanceMeasure.ERRORRATE;
	private boolean useCache = false;
	private PerformanceDBAdapter dbAdapter = null;
	
	public MLPlanWekaBuilder() { }
	
	public MLPlanWekaBuilder(File searchSpaceConfigFile, File alhorithmConfigFile, MultiClassPerformanceMeasure performanceMeasure) {
		super();
		this.searchSpaceConfigFile = searchSpaceConfigFile;
		this.algorithmConfigFile = alhorithmConfigFile;
		this.performanceMeasure = performanceMeasure;
		this.useCache = false;
	}
	
	public MLPlanWekaBuilder(File searchSpaceConfigFile, File alhorithmConfigFile, MultiClassPerformanceMeasure performanceMeasure, PerformanceDBAdapter dbAdapter) {
		super();
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

	public MLPlanWekaBuilder withSearchSpaceConfigFile(File searchSpaceConfig) {
		this.searchSpaceConfigFile = searchSpaceConfig;
		return this;
	}

	public MLPlanWekaBuilder withAlgorithmConfigFile(File algorithmConfigFile) throws IOException {
		return withAlgorithmConfig(loadOwnerConfig(algorithmConfigFile));
	}
	
	public MLPlanWekaBuilder withAlgorithmConfig(MLPlanClassifierConfig config) {
		this.algorithmConfig = config;
		return this;
	}

	public MLPlanWekaBuilder withPerformanceMeasure(MultiClassPerformanceMeasure performanceMeasure) {
		this.performanceMeasure = performanceMeasure;
		return this;
	}
	
	public boolean getUseCache() {
		return useCache;
	}
	
	public PerformanceDBAdapter getDBAdapter() {
		return dbAdapter;
	}
	
}
