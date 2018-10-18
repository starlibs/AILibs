package de.upb.crc901.mlplan.multiclass;

import java.io.File;

import org.aeonbits.owner.Config.Sources;
import org.aeonbits.owner.Mutable;

@Sources({ "file:conf/MLPlanWEKAClassifierConfig.properties" })
public interface MLPlanWEKAClassifierConfig extends Mutable {

	public static final String NUMBER_OF_CPUS = "system.numberOfCPUs";
	public static final String MEMORY_LIMIT = "system.memoryLimit";
	public static final String RANDOMNESS_SEED = "system.seed";

	public static final String COMPONENT_FILE = "mlplan.componentFile";
	public static final String PREFERRED_COMPONENTS = "mlplan.preferredComponents";
	public static final String SEARCH_MCCV_ITERATIONS = "mlplan.search.mccvFolds";
	public static final String SEARCH_MCCV_FOLDSIZE = "mlplan.search.foldSize";
	public static final String SELECTION_MCCV_ITERATIONS = "mlplan.selection.mccvFolds";
	public static final String SELECTION_MCCV_FOLDSIZE = "mlplan.selection.foldSize";
	public static final String SELECTION_PORTION = "mlplan.selection.mccvPortion";
	public static final String SELECTION_NUMBER_OF_CONSIDERED_SOLUTIONS = "mlplan.selection.numberOfConsideredSolutions";

	public static final String TIMEOUT_IN_SECONDS = "mlplan.timeout";
	public static final String TIMEOUT_PER_EVAL_IN_SECONDS = "mlplan.timeoutPerEval";

	@Key(NUMBER_OF_CPUS)
	@DefaultValue("1")
	public int numberOfCPUS();

	@Key(MEMORY_LIMIT)
	@DefaultValue("2048")
	public int memoryLimit();

	@Key(RANDOMNESS_SEED)
	@DefaultValue("0")
	public long seed();

	@Key(SEARCH_MCCV_ITERATIONS)
	@DefaultValue("5")
	public int numberOfMCIterationsDuringSearch();

	@Key(SEARCH_MCCV_FOLDSIZE)
	@DefaultValue(".7")
	public float getMCCVTrainFoldSizeDuringSearch();

	@Key(SELECTION_MCCV_ITERATIONS)
	@DefaultValue("5")
	public int numberOfMCIterationsDuringSelection();

	@Key(SELECTION_MCCV_FOLDSIZE)
	@DefaultValue(".7")
	public float getMCCVTrainFoldSizeDuringSelection();

	@Key(SELECTION_PORTION)
	@DefaultValue("0.3")
	public float dataPortionForSelection();

	@Key(TIMEOUT_IN_SECONDS)
	@DefaultValue("30")
	public int timeoutInSeconds();

	@Key(TIMEOUT_PER_EVAL_IN_SECONDS)
	@DefaultValue("10")
	public int timeoutPerNodeFComputation();

	@Key(SELECTION_NUMBER_OF_CONSIDERED_SOLUTIONS)
	@DefaultValue("100")
	public int numberOfConsideredSolutionDuringSelection();

	@Key(COMPONENT_FILE)
	@DefaultValue("model/weka/weka-all-autoweka.json")
	public File componentFile();

	@Key(PREFERRED_COMPONENTS)
	@DefaultValue("model/weka/preferredComponents.txt")
	public String preferredComponents();

}
