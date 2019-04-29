package jaicore.ml.cache;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import jaicore.ml.WekaUtil;
import jaicore.ml.openml.OpenMLHelper;
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
	private List<Instruction> history = new LinkedList<>();
	private boolean cacheStorage = true;
	private boolean cacheLookup = true;

	private ReproducibleInstances(final Instances dataset) {
		super(dataset);
	}

	/**
	 * Creates a {@link ReproducibleInstances} Object based on the given History. Instructions that no not modify the Instances will be ignored (No evaluation will be done).
	 *
	 * @param history List of Instructions used to create the original Instances
	 * @param apiKey apiKey in case openml.org is used
	 * @return new {@link ReproducibleInstances} object
	 * @throws IOException if something goes wrong while loading Instances from openml or when reading arff file
	 */
	public static ReproducibleInstances fromHistory(final List<Instruction> history, final String apiKey) throws IOException {
		ReproducibleInstances instances = null;
		for (int i = 0; i < history.size(); i++) {
			Instruction inst = history.get(i);
			switch (inst.command) {
			case LoadDataSetInstruction.COMMAND_NAME:
				// load openml or local dataset
				if(inst.inputs.get("provider").equals("openml.org")) {
					instances = fromOpenML(inst.inputs.get("id"), apiKey);
				}
				else if(inst.inputs.get("provider").startsWith("local")) {
					instances = fromARFF(inst.inputs.get("id"));
				}
				break;
			case FoldBasedSubsetInstruction.COMMAND_NAME:
				// create split
				String[] ratiosAsStrings = inst.getInputs().get("ratios").split(",");
				double[] ratios = new double[ratiosAsStrings.length];
				for (int j = 0; j < ratiosAsStrings.length; j++) {
					ratios[j] = Double.parseDouble(ratiosAsStrings[j].trim().substring(1, ratiosAsStrings[j].length()-1));
				}
				instances = WekaUtil.getStratifiedSplit(instances, Long.parseLong(inst.inputs.get("seed")), ratios).get( Integer.parseInt(inst.getInputs().get("outIndex")));
				break;
			default:
				break;
			}
		}
		return instances;
	}



	public ReproducibleInstances(final ReproducibleInstances dataset) {
		super(dataset);
		Iterator<Instruction> iterator = dataset.history.iterator();
		while(iterator.hasNext()) {
			Instruction i = iterator.next();
			history.add(i);
		}
		cacheLookup = dataset.cacheLookup;
		cacheStorage = dataset.cacheStorage;
	}

	/**
	 * Creates a new {@link ReproducibleInstances} object. Data is loaded from
	 * openml.org.
	 *
	 * @param id The id of the openml dataset
	 * @param apiKey apikey to use
	 * @return new {@link ReproducibleInstances} object
	 * @throws IOException if something goes wrong while loading Instances from openml
	 */
	public static ReproducibleInstances fromOpenML(final String id, final String apiKey) throws IOException {
		OpenMLHelper.setApiKey(apiKey);
		ReproducibleInstances result = new ReproducibleInstances(OpenMLHelper.getInstancesById(Integer.parseInt(id)));
		result.history.add(new LoadDataSetInstruction(DataProvider.OPENML, id));
		result.cacheLookup = true;
		result.cacheStorage = true;
		return result;
	}

	/**
	 * Creates a new {@link ReproducibleInstances} object. Data is loaded from a local arff file.
	 *
	 * @param path path to the dataset
	 * @return new {@link ReproducibleInstances} object
	 * @throws IOException if the ARFF file is not read successfully
	 */
	public static ReproducibleInstances fromARFF(final String path) throws IOException {
		Instances data = new Instances(new FileReader(path));
		data.setClassIndex(data.numAttributes() - 1);
		ReproducibleInstances result = new ReproducibleInstances(data);
		InetAddress addr = InetAddress.getLocalHost();
		result.history.add(new LoadDataSetInstruction(DataProvider.ARFFFILE, "file://" + addr.getHostName() + File.separator + new File(path).getAbsolutePath()));
		result.cacheLookup = true;
		result.cacheStorage = true;
		return result;
	}

	/**
	 *
	 * @return the ordered lists of instructions or null if cache is not used
	 */
	public List<Instruction> getInstructions() {
		if(cacheLookup || cacheStorage) {
			return history;
		}
		else {
			return null;
		}
	}

	/**
	 * Adds a new Instruction to the history of these Instances
	 *
	 * @param i - new Instruction
	 */
	public void addInstruction(final Instruction i) {
		history.add(i);
	}

	/** If true signifies that performance evaluation should be stored.
	 *
	 * @return true if performance should be saved
	 */
	public boolean isCacheStorage() {
		return cacheStorage;
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
		return cacheLookup;
	}

	/**
	 * If true signifies that performance on this data should be looked up in cache
	 * @param cacheLookup the cacheLookup to set
	 */
	public void setCacheLookup(final boolean cacheLookup) {
		this.cacheLookup = cacheLookup;
	}

}
