package jaicore.ml.cache;

import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONWriter;

import jaicore.ml.WekaUtil;
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
	
	private ReproducibleInstances(Instances dataset) {
		super(dataset);
	}
	
	/**
	 * Creates a {@link ReproducibleInstances} Object based on the given History. Instructions that no not modify the Instances will be ignored (No evaluation will be done). 
	 * 
	 * @param history - List of INstructions used to create the original Instances
	 * @param apiKey - apiKey in case openml.org is used
	 * @return new {@link ReproducibleInstances} object
	 * @throws IOException 
	 * @throws  
	 */
	public static ReproducibleInstances FromHistory(List<Instruction> history, String apiKey) throws IOException {
		ReproducibleInstances instances = null;
		for (int i = 0; i < history.size(); i++) {
			Instruction inst = history.get(i);
			switch (inst.command) {
			case "LoadDataset":
				if(inst.inputs.get("provider").equals("openml.org")) {
					instances = fromOpenML(inst.inputs.get("id"), apiKey);
				}
				else if(inst.inputs.get("provider").startsWith("local")) {
					instances = fromARFF(inst.inputs.get("id"), inst.inputs.get("provider").split("_")[1]); // TODO user name extraction
				}
				break;
			case "split":
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
	
	
	public ReproducibleInstances(ReproducibleInstances dataset) {
		super(dataset);
		for (Iterator<Instruction> iterator = dataset.history.iterator(); iterator.hasNext();) {
			Instruction i = iterator.next();
			history.add(i);
		}
		nocache = dataset.nocache;
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
