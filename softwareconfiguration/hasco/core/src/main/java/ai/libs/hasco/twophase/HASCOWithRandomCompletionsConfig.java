package ai.libs.hasco.twophase;

import java.util.concurrent.TimeUnit;

import org.api4.java.algorithm.Timeout;

import ai.libs.hasco.core.HASCOConfig;
import ai.libs.jaicore.basic.IOwnerBasedRandomizedAlgorithmConfig;

public interface HASCOWithRandomCompletionsConfig extends HASCOConfig, IOwnerBasedRandomizedAlgorithmConfig {
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
	@DefaultValue("15000")
	public int timeoutForNodeEvaluation();

	/**
	 * @return A timeout object representing the timeout for the evaluation of a single node.
	 */
	default Timeout getTimeoutForNodeEvaluation() {
		return new Timeout(this.timeoutForNodeEvaluation(), TimeUnit.MILLISECONDS);
	}

	/**
	 * @return Timeout in ms for a single evaluation of a solution candidate
	 */
	@Key(K_RANDOM_COMPLETIONS_TIMEOUT_PATH)
	@DefaultValue("15000")
	public int timeoutForCandidateEvaluation();

	/**
	 * @return A timeout object representing the timeout for the evaluation of a single candidate / random completion.
	 */
	default Timeout getTimeoutForCandidateEvaluation() {
		return new Timeout(this.timeoutForCandidateEvaluation(), TimeUnit.MILLISECONDS);
	}

}
