package jaicore.ml.cache;

import com.fasterxml.jackson.annotation.JsonProperty;

import jaicore.ml.WekaUtil;

/**
 * Instruction to track a split for a {@link ReproducibleInstances} object. Performns a stratified split from {@link WekaUtil} based on the given ratios and seed. The index gives the split to be used by the {@link ReproducibleInstances}.
 * 
 * @author jnowack
 *
 */
public class SplitInstruction extends Instruction {

	/** Constant string to identify this instruction. */
	public static final String COMMAND_NAME = "split";
	
	/**
	 * Constructor to create a split Instruction that can be converted into json.
	 * 
	 * @param ratios
	 *            ratios for the split
	 * @param seed
	 *            random seed
	 * @param outIndex
	 *            index of the portion to use in the following
	 */
	public SplitInstruction(@JsonProperty("ratios") String ratios, @JsonProperty("seed") long seed,
			@JsonProperty("outIndex") int outIndex) {
		command = COMMAND_NAME;
		inputs.put("ratios", "" + ratios);
		inputs.put("seed", "" + seed);
		inputs.put("outIndex", "" + outIndex);
	}

}
