package jaicore.ml.cache;

/**
 * Instruction for dataset load
 * 
 * @author jnowack
 *
 */
public class LoadDataSetInstruction extends Instruction{
	
	/**
	 * @param provider used to identify origin of the dataset
	 * @param id used to identify dataset
	 */
	public LoadDataSetInstruction(String provider, String id) {
		command = "LoadDataset";
		inputs.put("provider", provider);
		inputs.put("id", id);
	}
	
}
