package de.upb.crc901.mlplan.multilabel.mekamlplan;

import java.io.File;

import jaicore.ml.core.evaluation.measure.multilabel.MultiLabelPerformanceMeasure;

/**
 * Builder for ML2Plan that holds configuration for Ml2Plan itself, the used
 * search space and performance measure. By default uses a Meka configuration.
 * 
 * @author Helena Graf
 *
 */
public class ML2PlanMekaBuilder {
	private File searchSpaceConfigFile = new File("conf/automl/searchmodels/meka/mlplan-multilabel.json");
	private File alhorithmConfigFile = new File("conf/automl/ml2plan.properties");
	private MultiLabelPerformanceMeasure performanceMeasure = MultiLabelPerformanceMeasure.RANK;

	/**
	 * Constructor for a ML2PlanMekaBuilder that uses a default Meka configuration.
	 */
	public ML2PlanMekaBuilder() {
	}

	/**
	 * Constructor for a Ml2PlanMekaBuilder with the given configuration.
	 * 
	 * @param searchSpaceConfigFile
	 *            the components used in the search
	 * @param algorithmConfigFile
	 *            the configuration for ML2Plan
	 * @param measure
	 *            The multilabel performance measure used
	 */
	public ML2PlanMekaBuilder(File searchSpaceConfigFile, File algorithmConfigFile,
			MultiLabelPerformanceMeasure measure) {
		this.searchSpaceConfigFile = searchSpaceConfigFile;
		this.alhorithmConfigFile = algorithmConfigFile;
		this.performanceMeasure = measure;
	}

	/**
	 * Get the configuration file for the search that contains the components.
	 * 
	 * @return the search space configuration file
	 */
	public File getSearchSpaceConfigFile() {
		return searchSpaceConfigFile;
	}

	/**
	 * Get the algorithm configuration file that sets parameters for ML2Plan.
	 * 
	 * @return the algorithm configuration file
	 */
	public File getAlhorithmConfigFile() {
		return alhorithmConfigFile;
	}

	/**
	 * Get the multilabel performance measure used during the search
	 * 
	 * @return the performance measure
	 */
	public MultiLabelPerformanceMeasure getPerformanceMeasure() {
		return performanceMeasure;
	}

	/**
	 * Sets the search space configuration file of the builder and returns the
	 * builder.
	 * 
	 * @param searchSpaceConfigFile
	 *            the new search space configuration file
	 * @return this builder with an updated search space
	 */
	public ML2PlanMekaBuilder withSearchSpaceConfig(File searchSpaceConfigFile) {
		this.searchSpaceConfigFile = searchSpaceConfigFile;
		return this;
	}

	/**
	 * Sets the algorithm configuration file of the builder and returns the builder.
	 * 
	 * @param algorithmConfigFile
	 *            the new algorithm configuration file
	 * @return this builder with an updated algorithm configuration
	 */
	public ML2PlanMekaBuilder withAlgorithmConfigFile(File algorithmConfigFile) {
		this.alhorithmConfigFile = algorithmConfigFile;
		return this;
	}

	/**
	 * Sets the performance measure of this builder and returns the builder.
	 * 
	 * @param measure
	 *            the new performance measure
	 * @return this builder with an updated performance measures
	 */
	public ML2PlanMekaBuilder withPerformanceMeasure(MultiLabelPerformanceMeasure measure) {
		this.performanceMeasure = measure;
		return this;
	}
}
