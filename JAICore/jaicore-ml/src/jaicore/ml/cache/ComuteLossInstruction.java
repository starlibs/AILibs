package jaicore.ml.cache;


/**
 * Instruction for loss coputation.
 * 
 * @author jnowack
 *
 */
public class ComuteLossInstruction extends Instruction{
	
	/**
	 * @param algorithm String representation of the pipeline used
	 * @param trainingPortion portion of the data that should be used for training
	 * @param seed random seed
	 */
	public ComuteLossInstruction(String algorithm, float trainingPortion, long seed) {
		command = "compueLoss";
		inputs.put("algorithm", algorithm);
		inputs.put("trainingPortion", "" + trainingPortion);
		inputs.put("seed", ""+seed);
	}
	
	
}
