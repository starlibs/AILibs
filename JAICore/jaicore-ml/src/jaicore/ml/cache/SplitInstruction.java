package jaicore.ml.cache;

/**
 * Instruction to track a split.
 * 
 * @author jnowack
 *
 */
public class SplitInstruction extends Instruction {
	
	/**
	 * @param ratios ratios for the split 
	 * @param seed random seed
	 * @param outInex index of the portion to use in the following
	 */
	public SplitInstruction(String ratios, long seed, int outIndex) {
		command = "split";
		inputs.put("ratio", ""+ratios);
		inputs.put("seed", ""+seed);
		inputs.put("outIndex", ""+outIndex);
	}
	
}
