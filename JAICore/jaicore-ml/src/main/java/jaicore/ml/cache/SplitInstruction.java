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
	public SplitInstruction(@JsonProperty("ratios") final String ratios, @JsonProperty("seed") final long seed, @JsonProperty("outIndex") final int outIndex) {
		this.command = COMMAND_NAME;
		this.inputs.put("ratios", "" + ratios);
		this.inputs.put("seed", "" + seed);
		this.inputs.put("outIndex", "" + outIndex);
	}

}