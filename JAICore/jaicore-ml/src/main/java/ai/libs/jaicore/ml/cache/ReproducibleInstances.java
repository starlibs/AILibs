package ai.libs.jaicore.ml.cache;

import java.io.File;
import java.io.IOException;

import ai.libs.jaicore.basic.sets.Pair;
import ai.libs.jaicore.ml.core.dataset.weka.WekaInstances;
import ai.libs.jaicore.ml.openml.OpenMLHelper;
import weka.core.Instances;

/**
 * New Instances class to track splits and data origin. Origin of the dataset is stored by a {@link LoadDataSetInstruction} and changed by {@link FoldBasedSubsetInstruction}s saved as a list of instructions.
 * This history of the instances can be converted to json and used to reproduce a specific set of instances.
 *
 *
 * @author jnowack
 * @author mirko
 * @author jonas
 *
 */
public class ReproducibleInstances extends Instances {

	private static final long serialVersionUID = 4318807226111536282L;
	private InstructionGraph history;
	private Pair<String, Integer> outputUnitOfHistory;
	private boolean cacheStorage = true;
	private boolean cacheLookup = true;

	private ReproducibleInstances(final Instances dataset) {
		super(dataset);
	}

	public ReproducibleInstances(final ReproducibleInstances dataset) {
		this((Instances)dataset);
		this.history = new InstructionGraph(dataset.history);
		this.outputUnitOfHistory = new Pair<>(dataset.outputUnitOfHistory.getX(), dataset.outputUnitOfHistory.getY());
		this.cacheLookup = dataset.cacheLookup;
		this.cacheStorage = dataset.cacheStorage;
	}

	/**
	 * Creates a new {@link ReproducibleInstances} object. Data is loaded from
	 * openml.org.
	 *
	 * @param id The id of the openml dataset
	 * @param apiKey apikey to use
	 * @return new {@link ReproducibleInstances} object
	 * @throws IOException if something goes wrong while loading Instances from openml
	 * @throws InterruptedException
	 * @throws InstructionFailedException
	 */
	public static ReproducibleInstances fromOpenML(final int id, final String apiKey) throws InstructionFailedException, InterruptedException {
		OpenMLHelper.setApiKey(apiKey);
		InstructionGraph graph = new InstructionGraph();
		graph.addNode("load", new LoadDatasetInstructionForOpenML(apiKey, id));
		Pair<String, Integer> outputUnit = new Pair<>("load", 0);
		ReproducibleInstances result = new ReproducibleInstances(((WekaInstances<Object>)graph.getDataForUnit(outputUnit)).getList());
		result.history = graph;
		result.outputUnitOfHistory = outputUnit;
		result.cacheLookup = true;
		result.cacheStorage = true;
		return result;
	}

	public static ReproducibleInstances fromARFF(final File arffFile) throws InstructionFailedException, InterruptedException {
		InstructionGraph graph = new InstructionGraph();
		graph.addNode("load", new LoadDataSetInstructionForARFFFile(arffFile));
		Pair<String, Integer> outputUnit = new Pair<>("load", 0);
		ReproducibleInstances result = new ReproducibleInstances(((WekaInstances<Object>)graph.getDataForUnit(outputUnit)).getList());
		result.history = graph;
		result.outputUnitOfHistory = outputUnit;
		result.cacheLookup = true;
		result.cacheStorage = true;
		return result;
	}

	/**
	 *
	 * @return the ordered lists of instructions or null if cache is not used
	 */
	public InstructionGraph getInstructions() {
		if(this.cacheLookup || this.cacheStorage) {
			return this.history;
		}
		else {
			return null;
		}
	}

	public Pair<String, Integer> getOutputUnit() {
		return this.outputUnitOfHistory;
	}

	public void setOutputUnitWithoutRecomputation(final Pair<String, Integer> outputUnit) {
		this.outputUnitOfHistory = outputUnit;
	}

	/** If true signifies that performance evaluation should be stored.
	 *
	 * @return true if performance should be saved
	 */
	public boolean isCacheStorage() {
		return this.cacheStorage;
	}

	/** If set to true, signifies that performance evaluation should be stored.
	 *
	 * @param cacheStorage the cacheStorage to set
	 */
	public void setCacheStorage(final boolean cacheStorage) {
		this.cacheStorage = cacheStorage;
	}

	/**
	 * If true signifies that performance on this data should be looked up in cache
	 *
	 * @return true if lookup should be performed
	 */
	public boolean isCacheLookup() {
		return this.cacheLookup;
	}

	/**
	 * If true signifies that performance on this data should be looked up in cache
	 * @param cacheLookup the cacheLookup to set
	 */
	public void setCacheLookup(final boolean cacheLookup) {
		this.cacheLookup = cacheLookup;
	}

}
