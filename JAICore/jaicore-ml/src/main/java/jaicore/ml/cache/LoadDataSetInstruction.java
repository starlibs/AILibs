package jaicore.ml.cache;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Instruction for dataset loading, provider and id are used to identify the data set. See {@link ReproducibleInstances} for more information.
 *
 * @author jnowack
 *
 */
public class LoadDataSetInstruction extends Instruction {

	/** Constant String to Identify this Instruction */
	public static final String COMMAND_NAME = "loadDataset";

	/**
	 * Constructor to create an instruction for loading a dataset that can be converted to json.
	 *
	 * @param provider used to identify origin of the dataset
	 * @param id used to identify dataset
	 */
	public LoadDataSetInstruction(@JsonProperty("provider") final DataProvider provider, @JsonProperty("id") final String id) {
		this.command = COMMAND_NAME;
		if (provider == null) {
			throw new IllegalArgumentException("Provider must not be null!");
		}
		if (id == null) {
			throw new IllegalArgumentException("id must not be null!");
		}
		this.inputs.put("provider", provider.name());
		this.inputs.put("id", id);
	}

}
