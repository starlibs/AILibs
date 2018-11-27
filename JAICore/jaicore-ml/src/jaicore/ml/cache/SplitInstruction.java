package jaicore.ml.cache;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Instruction to track a split.
 * 
 * @author jnowack
 *
 */
public class SplitInstruction extends Instruction {

	/**
	 * @param ratios
	 *            ratios for the split
	 * @param seed
	 *            random seed
	 * @param outInex
	 *            index of the portion to use in the following
	 */
	public SplitInstruction(@JsonProperty("ratios") String ratios, @JsonProperty("seed") long seed,
			@JsonProperty("outIndex") int outIndex) {
		command = "split";
		inputs.put("ratios", "" + ratios);
		inputs.put("seed", "" + seed);
		inputs.put("outIndex", "" + outIndex);
	}

}
