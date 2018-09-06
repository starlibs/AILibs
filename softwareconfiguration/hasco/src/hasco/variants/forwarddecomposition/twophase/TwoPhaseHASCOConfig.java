package hasco.variants.forwarddecomposition.twophase;

public interface TwoPhaseHASCOConfig extends HASCOWithRandomCompletionsConfig {
	public static final String K_RANDOM_SEED = "hasco.seed";
	public static final String K_BLOWUP_SELECTION = "hasco.blowup.selection";
	public static final String K_BLOWUP_POSTPROCESS = "hasco.blowup.postprocess";
	public static final String K_SELECTION_EVALUATION_TIMEOUT_TOLERANCE = "hasco.selection.timeouttolerance";	
	public static final String K_SELECTION_NUM_CONSIDERED_SOLUTIONS = "hasco.selection.num_considered_solutions";

	/**
	 * @return The seed for the pseudo randomness generator.
	 */
	@Key(K_RANDOM_SEED)
	@DefaultValue("0")
	public int randomSeed();

	
	/**
	 * @return The number of solutions that are considered during selection phase.
	 */
	@Key(K_SELECTION_NUM_CONSIDERED_SOLUTIONS)
	@DefaultValue("100")
	public int selectionNumConsideredSolutions();
	
	/**
	 * @return Expected multiplication in time for each solution candidate that will be required for evaluation
	 */
	@Key(K_BLOWUP_SELECTION)
	@DefaultValue("1")
	public double expectedBlowupInSelection();
	
	/**
	 * @return Expected multiplication in time for each solution candidate that will be required for a postprocessing that should be considered when computing the timeout
	 */
	@Key(K_BLOWUP_POSTPROCESS)
	@DefaultValue("1")
	public double expectedBlowupInPostprocessing();

	/**
	 * @return The factor by which the evaluation in the selection phase may exceed the time expected on the basis of the estimate given by the blow-up
	 */
	@Key(K_SELECTION_EVALUATION_TIMEOUT_TOLERANCE)
	@DefaultValue("0.1")
	public double selectionPhaseTimeoutTolerance();
}
