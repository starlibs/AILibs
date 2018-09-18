package de.upb.crc901.mlplan.multiclass.wekamlplan;

import java.io.File;

import de.upb.crc901.mlplan.multiclass.MultiClassPerformanceMeasure;

public class MLPlanWekaBuilder {
	private File searchSpaceConfigFile = new File("conf/automl/searchmodels/weka/weka-all-autoweka.json");
	private File alhorithmConfigFile = new File("conf/mlplan.properties");
	private MultiClassPerformanceMeasure performanceMeasure = MultiClassPerformanceMeasure.ERRORRATE;

	public MLPlanWekaBuilder() { }
	
	public MLPlanWekaBuilder(File searchSpaceConfigFile, File alhorithmConfigFile, MultiClassPerformanceMeasure performanceMeasure) {
		super();
		this.searchSpaceConfigFile = searchSpaceConfigFile;
		this.alhorithmConfigFile = alhorithmConfigFile;
		this.performanceMeasure = performanceMeasure;
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
}
