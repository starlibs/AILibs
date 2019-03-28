package hasco.variants.forwarddecomposition.twophase;

import hasco.core.HASCOConfig;

public interface HASCOWithRandomCompletionsConfig extends HASCOConfig {
	public static final String K_RANDOM_COMPLETIONS_NUM = "hasco.random_completions.num";
	public static final String K_RANDOM_COMPLETIONS_TIMEOUT_NODE = "hasco.random_completions.timeout_node";
	public static final String K_RANDOM_COMPLETIONS_TIMEOUT_PATH = "hasco.random_completions.timeout_path";

	/**
	 * @return Number of random completions drawn with RDFS.
	 */
	@Key(K_RANDOM_COMPLETIONS_NUM)
	@DefaultValue("3")
	public int numberOfRandomCompletions();

	/**
	 * @return Timeout in ms for a node (this is an upper bound for the sum of the evaluations of all randomly drawn candidates).
	 */
	@Key(K_RANDOM_COMPLETIONS_TIMEOUT_NODE)
	//FIXME: The timeout needs to be hardcoded since configuration does not work (SCALE-67)
	@DefaultValue("60000")
	public int timeoutForNodeEvaluation();

	/**
	 * @return Timeout in ms for a single evaluation of a solution candidate
	 */
	@Key(K_RANDOM_COMPLETIONS_TIMEOUT_PATH)
	//FIXME: The timeout needs to be hardcoded since configuration does not work (SCALE-67)
	@DefaultValue("30000")
	public int timeoutForCandidateEvaluation();

}
