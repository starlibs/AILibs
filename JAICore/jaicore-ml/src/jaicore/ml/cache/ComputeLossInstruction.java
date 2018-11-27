package jaicore.ml.cache;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Instruction for loss computation.
 * 
 * @author jnowack
 *
 */
public class ComputeLossInstruction extends Instruction {

	/**
	 * @param algorithm
	 *            String representation of the pipeline used
	 * @param trainingPortion
	 *            portion of the data that should be used for training
	 * @param seed
	 *            random seed
	 */
	public ComputeLossInstruction(@JsonProperty("algorithm") String algorithm,
			@JsonProperty("trainingPortion") double trainingPortion, @JsonProperty("seed") long seed) {
		command = "computeLoss";
		inputs.put("algorithm", algorithm);
		inputs.put("trainingPortion", "" + trainingPortion);
		inputs.put("seed", "" + seed);
	}

}
