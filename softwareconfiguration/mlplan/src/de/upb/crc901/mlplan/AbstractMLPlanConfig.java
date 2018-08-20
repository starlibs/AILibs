package de.upb.crc901.mlplan;

import de.upb.crc901.automl.hascoml.supervised.HASCOSupervisedMLConfig;

public interface AbstractMLPlanConfig extends HASCOSupervisedMLConfig {

	public static final String K_SELECTION_DATA_PORTION = "mlplan.selection.data_portion";
	public static final String K_SELECTION_NUM_CONSIDERED_SOLUTIONS = "mlplan.selection.num_considered_solutions";
	public static final String K_SELECTION_MC_ITERATIONS = "mlplan.selection.num_mc_iterations";
	public static final String K_SEARCH_MC_ITERATIONS = "mlplan.search.num_mc_iterations";
	public static final String K_SEARCH_DATA_PORTION = "mlplan.search.data_portion";
	public static final String K_COMPONENTS_PRECEDENCE_LIST = "mlplan.search.precedence_list";

	/**
	 * @return The portion that is hold back during search to finally select a classifier.
	 */
	@Key(K_SELECTION_DATA_PORTION)
	@DefaultValue("0.3")
	public double selectionDataPortion();

	/**
	 * @return The number of solutions that are considered during selection phase.
	 */
	@Key(K_SELECTION_NUM_CONSIDERED_SOLUTIONS)
	@DefaultValue("100")
	public int selectionNumConsideredSolutions();

	/**
	 * @return The number of MC iterations that are conducted in selection phase.
	 */
	@Key(K_SELECTION_MC_ITERATIONS)
	@DefaultValue("3")
	public int selectionMCIterations();

	@Key(K_SEARCH_DATA_PORTION)
	@DefaultValue("0.7")
	public double searchDataPortion();

	@Key(K_SEARCH_MC_ITERATIONS)
	@DefaultValue("5")
	public int searchMCIterations();

	@Key(K_COMPONENTS_PRECEDENCE_LIST)
	@DefaultValue("model/weka/precedenceList.txt")
	public String componentsPrecedenceListFile();

	@Override
	@Key(K_REQUESTED_INTERFACE)
	@DefaultValue("AbstractClassifier")
	public String requestedInterface();

}
