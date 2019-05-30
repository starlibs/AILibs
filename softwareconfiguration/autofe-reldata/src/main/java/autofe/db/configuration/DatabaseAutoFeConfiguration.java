package autofe.db.configuration;

import org.aeonbits.owner.Mutable;

public interface DatabaseAutoFeConfiguration extends Mutable {

	public static final String K_RANDOM_COMPLETION_PATH_LENGTH = "randomcompletion_pathlength";
	public static final String K_EVALUATION_FUNCTION = "evaluationfunction";
	public static final String K_SEED = "seed";
	public static final String K_TIMEOUT = "timeout";

	@Key(K_RANDOM_COMPLETION_PATH_LENGTH)
	public int getRandomCompletionPathLength();

	@Key(K_EVALUATION_FUNCTION)
	public String getEvaluationFunction();

	@Key(K_SEED)
	public long getSeed();

	@Key(K_TIMEOUT)
	public int getTimeoutInMs();

}
