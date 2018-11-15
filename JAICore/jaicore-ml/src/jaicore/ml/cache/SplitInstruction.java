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
	public SplitInstruction(float ratio, long seed, int outIndex) {
		command = "split";
		inputs.put("ratio", ""+ratio);
		inputs.put("seed", ""+seed);
		inputs.put("outIndex", ""+outIndex);
	}
	
}
