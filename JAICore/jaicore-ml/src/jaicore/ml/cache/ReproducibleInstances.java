package jaicore.ml.cache;

import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import jaicore.ml.openml.OpenMLHelper;
import weka.core.Instances;

/**
 * New Instances class to track splits and data origin.
 * 
 * @author jnowack
 * @author mirko
 * @author jonas
 *
 */
public class ReproducibleInstances extends Instances {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4318807226111536282L;
	private List<Instruction> history = new LinkedList<>();
	private boolean nocache = true;
	
	public ReproducibleInstances(Instances dataset) {
		super(dataset);
	}

	/**
	 * Creates a new {@link ReproducibleInstances} object. Data is loaded from
	 * openml.org.
	 * 
	 * @param id     The id of the openml dataset
	 * @param apiKey apikey to use
	 * @return new {@link ReproducibleInstances} object
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	public static ReproducibleInstances fromOpenML(String id, String apiKey) throws NumberFormatException, IOException {
		OpenMLHelper.setApiKey(apiKey);
		ReproducibleInstances result = new ReproducibleInstances(OpenMLHelper.getInstancesById(Integer.parseInt(id)));
		result.history.add(new LoadDataSetInstruction("openml.org", id));
		result.nocache = false;
		return result;
	}

	/**
	 * Creates a new {@link ReproducibleInstances} object. Data is loaded from a
	 * local arff file.
	 * 
	 * @param path path to the dataset
	 * @param user We assume that the combination of user and path is unique
	 * @return new {@link ReproducibleInstances} object
	 * @throws IOException
	 */
	public static ReproducibleInstances fromARFF(String path, String user) throws IOException {
		Instances data = new Instances(new FileReader(path)); // TODO mirko
		data.setClassIndex(data.numAttributes() - 1);
		ReproducibleInstances result = new ReproducibleInstances(data);
		result.history.add(new LoadDataSetInstruction("local_" + user, path));
		result.nocache = false;
		return result;
	}

	/**
	 * returns the ordered lists of instructions or null if cache is not used
	 * 
	 * @return
	 */
	public List<Instruction> getInstructions() {
		return nocache ? null : history;
	}
	
	/**
	 * Adds a new Instruction to the history of these Instances
	 * 
	 * @param i - new Instruction
	 */
	public void addInstruction(Instruction i) {
		history.add(i);
	}
	
}
