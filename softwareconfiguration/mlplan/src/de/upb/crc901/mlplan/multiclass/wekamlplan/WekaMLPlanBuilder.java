package de.upb.crc901.mlplan.multiclass.wekamlplan;

import java.io.File;

import de.upb.crc901.mlplan.multiclass.MultiClassPerformanceMeasure;

public class WekaMLPlanBuilder {
	private File searchSpaceConfigFile;
	private File alhorithmConfigFile;
	private MultiClassPerformanceMeasure performanceMeasure;

	public WekaMLPlanBuilder(File searchSpaceConfigFile, File alhorithmConfigFile, MultiClassPerformanceMeasure performanceMeasure) {
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

	public WekaMLPlanBuilder withSearchSpaceConfigFile(File searchSpaceConfig) {
		this.searchSpaceConfigFile = searchSpaceConfig;
		return this;
	}

	public WekaMLPlanBuilder withAlgorithmConfigFile(File algorithmConfigFile) {
		this.alhorithmConfigFile = algorithmConfigFile;
		return this;
	}

	public WekaMLPlanBuilder withPerformanceMeasure(MultiClassPerformanceMeasure performanceMeasure) {
		this.performanceMeasure = performanceMeasure;
		return this;
	}
}
