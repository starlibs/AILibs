package de.upb.crc901.mlplan.multiclass.wekamlplan;

import java.io.File;

import de.upb.crc901.mlpipeline_evaluation.PerformanceDBAdapter;
import de.upb.crc901.mlplan.metamining.dyadranking.WEKADyadRankedNodeQueueConfig;
import jaicore.ml.core.evaluation.measure.singlelabel.MultiClassPerformanceMeasure;

public class MLPlanWekaBuilder {
	private File searchSpaceConfigFile = new File("conf/automl/searchmodels/weka/weka-all-autoweka.json");
	private File alhorithmConfigFile = new File("conf/mlplan.properties");
	private MultiClassPerformanceMeasure performanceMeasure = MultiClassPerformanceMeasure.ERRORRATE;
	private boolean useCache = false;
	private PerformanceDBAdapter dbAdapter = null;
	private WEKADyadRankedNodeQueueConfig dyadRankingConfig = null;
	
	public MLPlanWekaBuilder() { }
	
	public MLPlanWekaBuilder(File searchSpaceConfigFile, File alhorithmConfigFile, MultiClassPerformanceMeasure performanceMeasure) {
		super();
		this.searchSpaceConfigFile = searchSpaceConfigFile;
		this.alhorithmConfigFile = alhorithmConfigFile;
		this.performanceMeasure = performanceMeasure;
		this.useCache = false;
	}
	
	public MLPlanWekaBuilder(File searchSpaceConfigFile, File alhorithmConfigFile, MultiClassPerformanceMeasure performanceMeasure, PerformanceDBAdapter dbAdapter) {
		super();
		this.searchSpaceConfigFile = searchSpaceConfigFile;
		this.alhorithmConfigFile = alhorithmConfigFile;
		this.performanceMeasure = performanceMeasure;
		this.useCache = true;
		this.dbAdapter = dbAdapter;
	}
	

	public File getSearchSpaceConfigFile() {
		return searchSpaceConfigFile;
	}

	public File getAlhorithmConfigFile() {
		return alhorithmConfigFile;
	}

	public MultiClassPerformanceMeasure getPerformanceMeasure() {
		return performanceMeasure;
	}

	public WEKADyadRankedNodeQueueConfig getDyadRankingConfig() {
		return dyadRankingConfig;
	}

	public MLPlanWekaBuilder withSearchSpaceConfigFile(File searchSpaceConfig) {
		this.searchSpaceConfigFile = searchSpaceConfig;
		return this;
	}

	public MLPlanWekaBuilder withAlgorithmConfigFile(File algorithmConfigFile) {
		this.alhorithmConfigFile = algorithmConfigFile;
		return this;
	}

	public MLPlanWekaBuilder withPerformanceMeasure(MultiClassPerformanceMeasure performanceMeasure) {
		this.performanceMeasure = performanceMeasure;
		return this;
	}
	
	public MLPlanWekaBuilder usingBFwithDyadRankedNodeQueue(WEKADyadRankedNodeQueueConfig dyadRankingConfig) {
		this.dyadRankingConfig = dyadRankingConfig;
		return this;
	}
	
	public boolean getUseCache() {
		return useCache;
	}
	
	public PerformanceDBAdapter getDBAdapter() {
		return dbAdapter;
	}
	
}
