package jaicore.ml.cache;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Instruction for dataset loading, provider and id are used to identify the data set. See {@link ReproducibleInstances} for more information.
 * 
 * @author jnowack
 *
 */
public class LoadDataSetInstruction extends Instruction{
	
	/**
	 * @param provider used to identify origin of the dataset
	 * @param id used to identify dataset
	 */
	public LoadDataSetInstruction(@JsonProperty("provider") String provider, @JsonProperty("id") String id) {
		command = "loadDataset";
		inputs.put("provider", provider);
		inputs.put("id", id);
	}
	
}
